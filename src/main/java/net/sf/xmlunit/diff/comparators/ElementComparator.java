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
import java.util.Set;

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

        Element controlElement = control.getNode();
        Element testElement = test.getNode();

        XPathContext controlContext = control.getXpathCtx();
        XPathContext testContext = test.getXpathCtx();

        Attributes controlAttributes = Attributes.from(controlAttrList);
        Attributes testAttributes = Attributes.from(testAttrList);
        controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(), QNAME_MAPPER));
        testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES,
                        control, controlAttributes.getRegularAttributes().size(),
                        test, testAttributes.getRegularAttributes().size()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        Set<Attr> foundTestAttributes = new HashSet<Attr>();
        for (int i = 0; i < controlAttributes.getRegularAttributes().size(); i++) {
            final Attr controlAttr = controlAttributes.getRegularAttributes().get(i);
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

                if (testAttr == null) {
                    continue;
                }

                // ===
                // TODO extract
                if (!ignoreAttributeOrder) {
                    if (testAttributes.getRegularAttributes().indexOf(testAttr) != i) {
                        Node orderedTestNode = null;
                        String orderedTestNodeName = "[attribute absent]";
                        if (testAttributes.getRegularAttributes().size() > i) {
                            orderedTestNode = testAttributes.getRegularAttributes().get(i);
                            orderedTestNodeName = getUnNamespacedNodeName(orderedTestNode);
                        }
                        if (orderedTestNode != null) {
                            testContext.navigateToAttribute(Nodes.getQName(orderedTestNode));
                            try {
                                compPerformer.performComparison(
                                        new Comparison(ComparisonType.ATTR_SEQUENCE,
                                                NodeAndXpathCtx.from(controlAttr, controlContext),
                                                getUnNamespacedNodeName(controlAttr),
                                                NodeAndXpathCtx.from(orderedTestNode, testContext),
                                                orderedTestNodeName));
                                if (lastResult == ComparisonResult.CRITICAL) {
                                    return lastResult;
                                }
                            } finally {
                                testContext.navigateToParent();
                            }
                        }
                    }
                }

                // ===
                try {
                    testContext.navigateToAttribute(Nodes.getQName(testAttr));

                    lastResult = compareNamespaces(
                            NodeAndXpathCtx.<Node> from(controlAttr, controlContext),
                            NodeAndXpathCtx.<Node> from(testAttr, testContext));
                    if (lastResult == ComparisonResult.CRITICAL) {
                        return lastResult;
                    }

                    lastResult = compareAttributes(
                            NodeAndXpathCtx.from(controlAttr, controlContext),
                            NodeAndXpathCtx.from(testAttr, testContext));
                    if (lastResult == ComparisonResult.CRITICAL) {
                        return lastResult;
                    }

                    foundTestAttributes.add(testAttr);
                } finally {
                    testContext.navigateToParent();
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

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.SCHEMA_LOCATION,
                        NodeAndXpathCtx.from(controlElement, controlContext),
                        controlAttributes.getSchemaLocation() != null
                                ? controlAttributes.getSchemaLocation().getValue()
                                : null,
                        NodeAndXpathCtx.from(testElement, testContext),
                        testAttributes.getSchemaLocation() != null
                                ? testAttributes.getSchemaLocation().getValue()
                                : null));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        return compPerformer.performComparison(
                new Comparison(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION,
                        NodeAndXpathCtx.from(controlElement, controlContext),
                        controlAttributes.getNoNamespaceSchemaLocation() != null ?
                                controlAttributes.getNoNamespaceSchemaLocation().getValue()
                                : null,
                        NodeAndXpathCtx.from(testElement, testContext),
                        testAttributes.getNoNamespaceSchemaLocation() != null
                                ? testAttributes.getNoNamespaceSchemaLocation().getValue()
                                : null));
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
