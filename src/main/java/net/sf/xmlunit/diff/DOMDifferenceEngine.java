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

package net.sf.xmlunit.diff;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import net.sf.xmlunit.diff.comparators.AttributeComparator;
import net.sf.xmlunit.diff.comparators.CharacterDataComparator;
import net.sf.xmlunit.diff.comparators.ChildrenNumberComparator;
import net.sf.xmlunit.diff.comparators.DoctypeComparator;
import net.sf.xmlunit.diff.comparators.NamespaceComparator;
import net.sf.xmlunit.diff.comparators.ProcessingInstructionComparator;
import net.sf.xmlunit.diff.comparators.XmlHeaderComparator;
import net.sf.xmlunit.diff.internal.Attributes;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Convert;
import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Nodes;
import net.sf.xmlunit.util.Predicate;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import com.google.common.annotations.VisibleForTesting;

/**
 * Difference engine based on DOM.
 */
public final class DOMDifferenceEngine extends AbstractDifferenceEngine {

    private final XmlUnitProperties properties;

    public DOMDifferenceEngine(XmlUnitProperties properties) {
        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }
    }

    @Override
    public void compare(Source control, Source test) {
        if (control == null) {
            throw new IllegalArgumentException("control must not be null");
        }
        if (test == null) {
            throw new IllegalArgumentException("test must not be null");
        }
        try {
            compareNodes(
                    NodeAndXpathCtx.from(Convert.toNode(control), new XPathContext()),
                    NodeAndXpathCtx.from(Convert.toNode(test), new XPathContext()));
        } catch (Exception ex) {
            // TODO remove pokemon exception handling
            throw new XMLUnitRuntimeException("Caught exception during comparison", ex);
        }
    }

    /**
     * Recursively compares two XML nodes.
     * 
     * <p>
     * Performs comparisons common to all node types, then performs the node
     * type specific comparisons and finally recurses into the node's child
     * lists.
     * </p>
     * 
     * <p>
     * Stops as soon as any comparison returns ComparisonResult.CRITICAL.
     * </p>
     * 
     * <p>
     * package private to support tests.
     * </p>
     */
    @VisibleForTesting
    ComparisonResult compareNodes(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
        Node controlNode = control.getNode();
        Node testNode = test.getNode();

        XPathContext controlContext = control.getXpathCtx();
        XPathContext testContext = test.getXpathCtx();

        ComparisonResult lastResult = compare(
                new Comparison(ComparisonType.NODE_TYPE,
                        control, controlNode.getNodeType(),
                        test, testNode.getNodeType()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = new NamespaceComparator(getComparisonPerformer()).compare(control, test);
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
            lastResult = new ChildrenNumberComparator(getComparisonPerformer()).compare(
                    control, test);

            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }
        }

        lastResult = nodeTypeSpecificComparison(
                controlNode, controlContext, testNode, testContext);
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
            Iterable<Node> controlChildren =
                    Linqy.filter(new IterableNodeList(controlNode.getChildNodes()), INTERESTING_NODES);
            Iterable<Node> testChildren =
                    Linqy.filter(new IterableNodeList(testNode.getChildNodes()), INTERESTING_NODES);

            controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
            testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

            lastResult = compareNodeLists(controlChildren, controlContext, testChildren, testContext);
            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }
        }
        return lastResult;
    }

    /**
     * Dispatches to the node type specific comparison if one is defined for the
     * given combination of nodes.
     */
    private ComparisonResult nodeTypeSpecificComparison(
            Node control, XPathContext controlContext,
            Node test, XPathContext testContext) {

        switch (control.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.TEXT_NODE:
                if (test instanceof CharacterData) {
                    return new CharacterDataComparator(getComparisonPerformer()).compare(
                            NodeAndXpathCtx.from((CharacterData) control, controlContext),
                            NodeAndXpathCtx.from((CharacterData) test, testContext));
                }
                break;
            case Node.DOCUMENT_NODE:
                if (test instanceof Document) {
                    return compareDocuments(
                            NodeAndXpathCtx.from((Document) control, controlContext),
                            NodeAndXpathCtx.from((Document) test, testContext));
                }
                break;
            case Node.ELEMENT_NODE:
                if (test instanceof Element) {
                    return compareElements(
                            (Element) control, controlContext,
                            (Element) test, testContext);
                }
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                if (test instanceof ProcessingInstruction) {
                    return new ProcessingInstructionComparator(getComparisonPerformer()).compare(
                            NodeAndXpathCtx.from((ProcessingInstruction) control, controlContext),
                            NodeAndXpathCtx.from((ProcessingInstruction) test, testContext));
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                if (test instanceof DocumentType) {
                    return new DoctypeComparator(comparisonPerformer).compare(
                            NodeAndXpathCtx.from((DocumentType) control, controlContext),
                            NodeAndXpathCtx.from((DocumentType) test, testContext));
                }
                break;
            case Node.ATTRIBUTE_NODE:
                if (test instanceof Attr) {
                    return new AttributeComparator(getComparisonPerformer()).compare(
                            NodeAndXpathCtx.from((Attr) control, controlContext),
                            NodeAndXpathCtx.from((Attr) test, testContext));
                }
                break;
        }
        return ComparisonResult.EQUAL;
    }

    /**
     * Compares document node, doctype and XML declaration properties
     */
    private ComparisonResult compareDocuments(
            NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {

        Document controlDoc = control.getNode();
        Document testDoc = test.getNode();

        DocumentType controlDt = controlDoc.getDoctype();
        DocumentType testDt = testDoc.getDoctype();

        ComparisonResult lastResult =
                compare(new Comparison(ComparisonType.HAS_DOCTYPE_DECLARATION,
                        control, Boolean.valueOf(controlDt != null),
                        test, Boolean.valueOf(testDt != null)));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        if (controlDt != null && testDt != null) {
            lastResult = compareNodes(
                    NodeAndXpathCtx.<Node> from(controlDt, control.getXpathCtx()),
                    NodeAndXpathCtx.<Node> from(testDt, test.getXpathCtx()));
            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }
        }

        return new XmlHeaderComparator(getComparisonPerformer()).compare(control, test);
    }

    /**
     * Compares elements node properties, in particular the element's name and
     * its attributes.
     */

    @VisibleForTesting
    ComparisonResult compareElements(
            Element control, XPathContext controlContext,
            Element test, XPathContext testContext) {

        NodeAndXpath<Element> controlNode = new NodeAndXpath<Element>(control, getXPath(controlContext));
        NodeAndXpath<Element> testNode = new NodeAndXpath<Element>(test, getXPath(testContext));

        ComparisonResult lastResult = compare(new Comparison(ComparisonType.ELEMENT_TAG_NAME,
                controlNode, Nodes.getQName(control).getLocalPart(),
                testNode, Nodes.getQName(test).getLocalPart()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compareElementAttributes(
                NodeAndXpathCtx.from(control, controlContext),
                NodeAndXpathCtx.from(test, testContext));
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

        boolean ignoreOrder = properties.getIgnoreAttributeOrder();

        Element controlElement = control.getNode();
        Element testElement = test.getNode();

        XPathContext controlContext = control.getXpathCtx();
        XPathContext testContext = test.getXpathCtx();

        Attributes controlAttributes = Attributes.from(controlAttrList);
        Attributes testAttributes = Attributes.from(testAttrList);
        controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(), QNAME_MAPPER));
        testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

        ComparisonResult lastResult = compare(new Comparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES,
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
                        compare(new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
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
                if (!ignoreOrder) {
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
                                compare(new Comparison(ComparisonType.ATTR_SEQUENCE,
                                        controlAttr, getXPath(controlContext),
                                        getUnNamespacedNodeName(controlAttr),
                                        orderedTestNode, getXPath(testContext), orderedTestNodeName));

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
                    lastResult = compareNodes(
                            NodeAndXpathCtx.<Node> from(controlAttr, controlContext),
                            NodeAndXpathCtx.<Node> from(testAttr, testContext));
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
                lastResult =
                        compare(new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
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

        lastResult =
                compare(new Comparison(ComparisonType.SCHEMA_LOCATION,
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

        return compare(new Comparison(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION,
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
     * Matches nodes of two node lists and invokes compareNode on each pair.
     * 
     * <p>
     * Also performs CHILD_LOOKUP comparisons for each node that couldn't be
     * matched to one of the "other" list.
     * </p>
     */
    private ComparisonResult compareNodeLists(Iterable<Node> controlSeq,
            XPathContext controlContext,
            Iterable<Node> testSeq,
            XPathContext testContext) {
        // if there are no children on either Node, the result is equal
        ComparisonResult lastResult = ComparisonResult.EQUAL;

        Iterable<Map.Entry<Node, Node>> matches = getNodeMatcher().match(controlSeq, testSeq);
        List<Node> controlList = Linqy.asList(controlSeq);
        List<Node> testList = Linqy.asList(testSeq);
        Set<Node> seen = new HashSet<Node>();
        for (Map.Entry<Node, Node> pair : matches) {
            Node control = pair.getKey();
            Node test = pair.getValue();
            seen.add(control);
            seen.add(test);
            int controlIndex = controlList.indexOf(control);
            int testIndex = testList.indexOf(test);

            controlContext.navigateToChild(controlIndex);
            testContext.navigateToChild(testIndex);
            try {
                lastResult =
                        compare(new Comparison(ComparisonType.CHILD_NODELIST_SEQUENCE,
                                control, getXPath(controlContext),
                                Integer.valueOf(controlIndex),
                                test, getXPath(testContext),
                                Integer.valueOf(testIndex)));
                if (lastResult == ComparisonResult.CRITICAL) {
                    return lastResult;
                }

                lastResult = compareNodes(
                        NodeAndXpathCtx.from(control, controlContext),
                        NodeAndXpathCtx.from(test, testContext));
                if (lastResult == ComparisonResult.CRITICAL) {
                    return lastResult;
                }
            } finally {
                testContext.navigateToParent();
                controlContext.navigateToParent();
            }
        }

        final int controlSize = controlList.size();
        for (int i = 0; i < controlSize; i++) {
            if (!seen.contains(controlList.get(i))) {
                controlContext.navigateToChild(i);
                try {
                    lastResult =
                            compare(new Comparison(ComparisonType.CHILD_LOOKUP,
                                    controlList.get(i),
                                    getXPath(controlContext),
                                    controlList.get(i).getNodeName(),
                                    null, null, null));
                    if (lastResult == ComparisonResult.CRITICAL) {
                        return lastResult;
                    }
                } finally {
                    controlContext.navigateToParent();
                }
            }
        }

        final int testSize = testList.size();
        for (int i = 0; i < testSize; i++) {
            if (!seen.contains(testList.get(i))) {
                testContext.navigateToChild(i);
                try {
                    lastResult =
                            compare(new Comparison(ComparisonType.CHILD_LOOKUP,
                                    null, null, null,
                                    testList.get(i),
                                    getXPath(testContext),
                                    testList.get(i).getNodeName()));
                    if (lastResult == ComparisonResult.CRITICAL) {
                        return lastResult;
                    }
                } finally {
                    testContext.navigateToParent();
                }
            }
        }
        return lastResult;
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

    /**
     * Maps Nodes to their NodeInfo equivalent.
     */
    private static final Linqy.Mapper<Node, XPathContext.NodeInfo> TO_NODE_INFO =
            new Linqy.Mapper<Node, XPathContext.NodeInfo>() {
                @Override
                public XPathContext.NodeInfo map(Node n) {
                    return new XPathContext.DOMNodeInfo(n);
                }
            };

    /**
     * Suppresses document-type nodes.
     */
    private static final Predicate<Node> INTERESTING_NODES =
            new Predicate<Node>() {
                @Override
                public boolean matches(Node n) {
                    return n.getNodeType() != Node.DOCUMENT_TYPE_NODE;
                }
            };
}
