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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

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
    public ComparisonResult compare(NodeAndXpathCtx<Element> control, NodeAndXpathCtx<Element> test) {
        Element controlElement = control.getNode();
        Element testElement = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.ELEMENT_TAG_NAME,
                        control, Nodes.getQName(controlElement).getLocalPart(),
                        test, Nodes.getQName(testElement).getLocalPart()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compareElementAttributes(control, test);
        return lastResult;
    }

    private ComparisonResult compareElementAttributes(
            NodeAndXpathCtx<Element> control, NodeAndXpathCtx<Element> test) {
        NamedNodeMap controlAttrList = control.getNode().getAttributes();
        NamedNodeMap testAttrList = test.getNode().getAttributes();
        return compareElementAttributes(control, controlAttrList, test, testAttrList);
    }

    @VisibleForTesting
    ComparisonResult compareElementAttributes(
            NodeAndXpathCtx<Element> control, NamedNodeMap controlAttrList,
            NodeAndXpathCtx<Element> test, NamedNodeMap testAttrList) {

        Attributes controlAttributes = Attributes.from(controlAttrList);
        Attributes testAttributes = Attributes.from(testAttrList);

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES,
                        control, controlAttributes.getRegularAttributes().size(),
                        test, testAttributes.getRegularAttributes().size()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compareAttributeList(control, controlAttributes, test, testAttributes);
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = new CompareSchemasLocationOperation(
                control, controlAttributes, test, testAttributes)
                .executeComparison();
        return lastResult;
    }

    private ComparisonResult compareAttributeList(
            NodeAndXpathCtx<Element> control, Attributes controlAttributes,
            NodeAndXpathCtx<Element> test, Attributes testAttributes) {

        Element controlElement = control.getNode();
        Element testElement = test.getNode();
        XPathContext controlContext = control.getXpathCtx();
        XPathContext testContext = test.getXpathCtx();

        controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(), QNAME_MAPPER));
        testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

        ComparisonResult lastResult = ComparisonResult.EQUAL;
        Set<Attr> foundTestAttributes = new HashSet<Attr>();
        for (Attr controlAttr : controlAttributes.getRegularAttributes()) {
            final Attr testAttr = testAttributes.findMatchingRegularAttr(controlAttr);

            controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
            try {
                boolean hasMatchingAttr = testAttr != null;
                lastResult =
                        compPerformer.performComparison(
                                new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                                        NodeAndXpathCtx.from(controlElement, controlContext), true,
                                        NodeAndXpathCtx.from(testElement, testContext), hasMatchingAttr));
                if (lastResult == ComparisonResult.CRITICAL) {
                    return lastResult;
                }

                if (testAttr != null) {

                    if (!ignoreAttributeOrder) {
                        lastResult = new CompareAttrSequenceNumberOperation(
                                NodeAndXpathCtx.from(controlAttr, controlContext), controlAttributes,
                                NodeAndXpathCtx.from(testAttr, testContext), testAttributes).executeComparison();
                        if (lastResult == ComparisonResult.CRITICAL) {
                            return lastResult;
                        }
                    }

                    try {
                        testContext.navigateToAttribute(Nodes.getQName(testAttr));

                        Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
                        operations.add(new CompareNamespaceOperation(
                                NodeAndXpathCtx.<Node> from(controlAttr, controlContext),
                                NodeAndXpathCtx.<Node> from(testAttr, testContext)));
                        operations.add(new CompareAttributeOperation(
                                NodeAndXpathCtx.from(controlAttr, controlContext),
                                NodeAndXpathCtx.from(testAttr, testContext)));
                        lastResult = execute(operations);
                        if (lastResult == ComparisonResult.CRITICAL) {
                            return lastResult;
                        }

                        foundTestAttributes.add(testAttr);
                    } finally {
                        testContext.navigateToParent();
                    }
                }
            } finally {
                controlContext.navigateToParent();
            }
        }

        for (Attr testAttr : testAttributes.getRegularAttributes()) {
            testContext.navigateToAttribute(Nodes.getQName(testAttr));
            try {
                lastResult = compPerformer.performComparison(
                        new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                                NodeAndXpathCtx.from(controlElement, controlContext),
                                foundTestAttributes.contains(testAttr),
                                NodeAndXpathCtx.from(testElement, testContext), true));
                if (lastResult == ComparisonResult.CRITICAL) {
                    return lastResult;
                }
            } finally {
                testContext.navigateToParent();
            }
        }

        return lastResult;
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
