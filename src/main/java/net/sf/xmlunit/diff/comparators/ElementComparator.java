/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package net.sf.xmlunit.diff.comparators;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.Attributes;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Nodes;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;

public class ElementComparator extends NodeComparator<Element> {

    private final boolean ignoreAttributeOrder;

    public ElementComparator(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder) {
        super(compPerformer);
        this.ignoreAttributeOrder = ignoreAttributeOrder;
    }

    /**
     * Compares elements node properties, in particular the element's name and
     * its attributes.
     */
    @Override
    public ComparisonResult compare(final NodeAndXpathCtx<Element> control, final NodeAndXpathCtx<Element> test) {
        final Element controlElement = control.getNode();
        final Element testElement = test.getNode();

        Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
        operations.add(new ComparisonOperation() {
            @Override
            public ComparisonResult executeComparison() {
                return compPerformer.performComparison(
                        new Comparison(ComparisonType.ELEMENT_TAG_NAME,
                                control, Nodes.getQName(controlElement).getLocalPart(),
                                test, Nodes.getQName(testElement).getLocalPart()));
            }
        });
        operations.add(new CompareElementAttributesOperation(control, test));
        return execute(operations);
    }

    @VisibleForTesting
    class CompareElementAttributesOperation extends AbstractComparisonOperation<Element> {

        private final NamedNodeMap controlAttributes;
        private final NamedNodeMap testAttributes;

        @VisibleForTesting
        CompareElementAttributesOperation(
                NodeAndXpathCtx<Element> control, NamedNodeMap controlAttributes,
                NodeAndXpathCtx<Element> test, NamedNodeMap testAttributes) {
            super(control, test);
            this.controlAttributes = controlAttributes;
            this.testAttributes = testAttributes;
        }

        public CompareElementAttributesOperation(NodeAndXpathCtx<Element> control, NodeAndXpathCtx<Element> test) {
            super(control, test);
            this.controlAttributes = control.getNode().getAttributes();
            this.testAttributes = test.getNode().getAttributes();
        }

        @Override
        public ComparisonResult executeComparison() {
            final Attributes controlAttributes = Attributes.from(this.controlAttributes);
            final Attributes testAttributes = Attributes.from(this.testAttributes);

            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES,
                                    getControl(), controlAttributes.getRegularAttributes().size(),
                                    getTest(), testAttributes.getRegularAttributes().size()));
                }
            });
            operations.add(
                    new CompareAttributeListOperation(getControl(), controlAttributes, getTest(), testAttributes));
            operations.add(
                    new CompareSchemasLocationOperation(getControl(), controlAttributes, getTest(), testAttributes));
            return execute(operations);
        }

    }

    private class CompareAttributeListOperation extends AbstractComparisonOperation<Element> {

        private final Attributes controlAttributes;
        private final Attributes testAttributes;

        public CompareAttributeListOperation(
                NodeAndXpathCtx<Element> control, Attributes controlAttributes,
                NodeAndXpathCtx<Element> test, Attributes testAttributes) {
            super(control, test);
            this.controlAttributes = controlAttributes;
            this.testAttributes = testAttributes;
        }

        @Override
        public ComparisonResult executeComparison() {
            final Element controlElement = getControl().getNode();
            final Element testElement = getTest().getNode();
            final XPathContext controlContext = getControl().getXpathCtx();
            final XPathContext testContext = getTest().getXpathCtx();

            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();

            controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(), QNAME_MAPPER));
            testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

            for (final Attr controlAttr : controlAttributes.getRegularAttributes()) {
                final Attr testAttr = testAttributes.findMatchingRegularAttr(controlAttr);
                final boolean hasMatchingAttr = testAttr != null;

                operations.add(new ComparisonOperation() {
                    @Override
                    public ComparisonResult executeComparison() {
                        controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
                        try {
                            return compPerformer.performComparison(
                                    new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                                            NodeAndXpathCtx.from(controlElement, controlContext), true,
                                            NodeAndXpathCtx.from(testElement, testContext), hasMatchingAttr));
                        } finally {
                            controlContext.navigateToParent();
                        }
                    }
                });
                if (testAttr != null) {
                    operations.add(new CompareMatchedAttrOperation(
                            NodeAndXpathCtx.from(controlAttr, controlContext), controlAttributes,
                            NodeAndXpathCtx.from(testAttr, testContext), testAttributes));
                }
            }

            if (testAttributes.getRegularAttributes().size() > 0) {
                operations.add(new FindUnamtchedTestAttrOperation(
                        getControl(), controlAttributes, getTest(), testAttributes));
            }
            return execute(operations);
        }

    }

    private class CompareMatchedAttrOperation extends AbstractComparisonOperation<Attr> {

        private final Attributes controlAttributes;
        private final Attributes testAttributes;

        public CompareMatchedAttrOperation(
                NodeAndXpathCtx<Attr> control, Attributes controlAttributes,
                NodeAndXpathCtx<Attr> test, Attributes testAttributes) {
            super(control, test);
            this.controlAttributes = controlAttributes;
            this.testAttributes = testAttributes;
        }

        @Override
        public ComparisonResult executeComparison() {
            Attr controlAttr = getControl().getNode();
            Attr testAttr = getTest().getNode();

            XPathContext controlContext = getControl().getXpathCtx();
            XPathContext testContext = getTest().getXpathCtx();

            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();

            controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
            try {
                if (!ignoreAttributeOrder) {
                    operations.add(new CompareAttrSequenceNumberOperation(
                            NodeAndXpathCtx.from(controlAttr, controlContext), controlAttributes,
                            NodeAndXpathCtx.from(testAttr, testContext), testAttributes));
                }

                try {
                    testContext.navigateToAttribute(Nodes.getQName(testAttr));

                    operations.add(new CompareNamespaceOperation(
                            NodeAndXpathCtx.<Node> from(controlAttr, controlContext),
                            NodeAndXpathCtx.<Node> from(testAttr, testContext)));
                    operations.add(new CompareAttributeOperation(
                            NodeAndXpathCtx.from(controlAttr, controlContext),
                            NodeAndXpathCtx.from(testAttr, testContext)));
                } finally {
                    testContext.navigateToParent();
                }
            } finally {
                controlContext.navigateToParent();
            }

            return execute(operations);
        }

    }

    private class FindUnamtchedTestAttrOperation extends AbstractComparisonOperation<Element> {

        private final Attributes controlAttributes;
        private final Attributes testAttributes;

        public FindUnamtchedTestAttrOperation(
                NodeAndXpathCtx<Element> control, Attributes controlAttributes,
                NodeAndXpathCtx<Element> test, Attributes testAttributes) {
            super(control, test);

            this.controlAttributes = controlAttributes;
            this.testAttributes = testAttributes;
        }

        @Nullable
        @Override
        public ComparisonResult executeComparison() {
            final Element controlElement = getControl().getNode();
            final Element testElement = getTest().getNode();

            final XPathContext controlContext = getControl().getXpathCtx();
            final XPathContext testContext = getTest().getXpathCtx();

            Queue<ComparisonOperation> operations = new LinkedList<NodeComparator.ComparisonOperation>();

            for (Attr testAttr : testAttributes.getRegularAttributes()) {
                testContext.navigateToAttribute(Nodes.getQName(testAttr));
                try {
                    Attr matchingControlAttr = controlAttributes.findMatchingRegularAttr(testAttr);
                    final boolean hasMatchingAttr = matchingControlAttr != null;

                    operations.add(new ComparisonOperation() {
                        @Override
                        public ComparisonResult executeComparison() {
                            return compPerformer.performComparison(
                                    new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                                            NodeAndXpathCtx.from(controlElement, controlContext),
                                            hasMatchingAttr,
                                            NodeAndXpathCtx.from(testElement, testContext), true));
                        }
                    });
                } finally {
                    testContext.navigateToParent();
                }
            }

            return execute(operations);
        }
    }

    private class CompareAttrSequenceNumberOperation extends AbstractComparisonOperation<Attr> {

        private final Attributes controlAttributes;
        private final Attributes testAttributes;

        public CompareAttrSequenceNumberOperation(
                NodeAndXpathCtx<Attr> control, Attributes controlAttributes,
                NodeAndXpathCtx<Attr> test, Attributes testAttributes) {
            super(control, test);
            this.controlAttributes = controlAttributes;
            this.testAttributes = testAttributes;
        }

        @Nullable
        @Override
        public ComparisonResult executeComparison() {
            Attr controlAttr = getControl().getNode();
            Attr testAttr = getTest().getNode();
            XPathContext controlContext = getControl().getXpathCtx();
            XPathContext testContext = getTest().getXpathCtx();

            ComparisonResult lastResult = null;

            int controlAttrIndex = controlAttributes.getRegularAttributes().indexOf(controlAttr);
            int testAttrIndex = testAttributes.getRegularAttributes().indexOf(testAttr);
            if (testAttrIndex == controlAttrIndex) {
                return lastResult;
            }

            Attr mirrorTestAttr = findMirrorTestAttr(controlAttrIndex);
            String orderedTestNodeName = findMirrorTestAttrName(mirrorTestAttr);

            if (mirrorTestAttr == null) {
                return lastResult;
            }
            testContext.navigateToAttribute(Nodes.getQName(mirrorTestAttr));
            try {
                return compPerformer.performComparison(
                        new Comparison(ComparisonType.ATTR_SEQUENCE,
                                NodeAndXpathCtx.from(controlAttr, controlContext),
                                getUnNamespacedNodeName(controlAttr),
                                NodeAndXpathCtx.from(mirrorTestAttr, testContext),
                                orderedTestNodeName));
            } finally {
                testContext.navigateToParent();
            }
        }

        @Nullable
        private Attr findMirrorTestAttr(int controlAttrIndex) {
            if (testAttributes.getRegularAttributes().size() > controlAttrIndex) {
                return testAttributes.getRegularAttributes().get(controlAttrIndex);
            }
            return null;
        }

        private String findMirrorTestAttrName(@Nullable Attr mirrorTestAttr) {
            if (mirrorTestAttr == null) {
                return "[attribute absent]";
            }
            return getUnNamespacedNodeName(mirrorTestAttr);
        }

        /**
         * @param aNode
         * @return true if the node has a namespace
         */
        private boolean isNamespaced(Node aNode) {
            String namespace = aNode.getNamespaceURI();
            return namespace != null && namespace.length() > 0;
        }

        private String getUnNamespacedNodeName(Node aNode) {
            if (isNamespaced(aNode)) {
                return aNode.getLocalName();
            }
            return aNode.getNodeName();
        }

    }

    private class CompareSchemasLocationOperation extends AbstractComparisonOperation<Element> {

        private final Attributes controlAttributes;
        private final Attributes testAttributes;

        public CompareSchemasLocationOperation(
                NodeAndXpathCtx<Element> control, Attributes controlAttributes,
                NodeAndXpathCtx<Element> test, Attributes testAttributes) {
            super(control, test);
            this.controlAttributes = controlAttributes;
            this.testAttributes = testAttributes;
        }

        @Override
        public ComparisonResult executeComparison() {
            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();

            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.SCHEMA_LOCATION,
                                    getControl(), controlAttributes.getSchemaLocationValue(),
                                    getTest(), testAttributes.getSchemaLocationValue()));
                }
            });
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION,
                                    getControl(),
                                    controlAttributes.getNoNamespaceSchemaLocationValue(),
                                    getTest(), testAttributes.getNoNamespaceSchemaLocationValue()));
                }

            });

            return execute(operations);

        }
    }

    /**
     * Maps Nodes to their QNames.
     */
    private static final Linqy.Mapper<Node, QName> QNAME_MAPPER =
            new Linqy.Mapper<Node, QName>() {
                @Override
                public QName map(Node n) {
                    return Nodes.getQName(n);
                }
            };

}
