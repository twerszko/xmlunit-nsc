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
import net.sf.xmlunit.diff.comparators.DocTypeComparator;
import net.sf.xmlunit.diff.comparators.ChildrenNumberComparator;
import net.sf.xmlunit.diff.comparators.ProcessingInstructionComparator;
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
            compareNodes(Convert.toNode(control), new XPathContext(), Convert.toNode(test), new XPathContext());
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
    ComparisonResult compareNodes(Node control, XPathContext controlContext, Node test, XPathContext testContext) {
        NodeAndXpath<Node> controlNode = new NodeAndXpath<Node>(control, getXPath(controlContext));
        NodeAndXpath<Node> testNode = new NodeAndXpath<Node>(test, getXPath(testContext));

        ComparisonResult lastResult = compare(new Comparison(ComparisonType.NODE_TYPE,
                controlNode, control.getNodeType(),
                testNode, test.getNodeType()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compare(new Comparison(ComparisonType.NAMESPACE_URI,
                controlNode, control.getNamespaceURI(),
                testNode, test.getNamespaceURI()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compare(new Comparison(ComparisonType.NAMESPACE_PREFIX,
                controlNode, control.getPrefix(),
                testNode, test.getPrefix()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        Iterable<Node> controlChildren =
                Linqy.filter(new IterableNodeList(control.getChildNodes()), INTERESTING_NODES);
        Iterable<Node> testChildren =
                Linqy.filter(new IterableNodeList(test.getChildNodes()), INTERESTING_NODES);

        if (control.getNodeType() != Node.ATTRIBUTE_NODE) {
            lastResult = compareNodeList(control, controlContext, test, testContext);
            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }
        }

        lastResult = nodeTypeSpecificComparison(control, controlContext, test, testContext);
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        if (control.getNodeType() != Node.ATTRIBUTE_NODE) {
            controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
            testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

            lastResult = compareNodeLists(controlChildren, controlContext, testChildren, testContext);
            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }
        }
        return lastResult;
    }

    private ComparisonResult compareNodeList(
            Node controlNode, XPathContext controlContext,
            Node testNode, XPathContext testContext) {

        NodeAndXpathCtx<Node> control = new NodeAndXpathCtx<Node>(controlNode, controlContext);
        NodeAndXpathCtx<Node> test = new NodeAndXpathCtx<Node>(testNode, testContext);
        return new ChildrenNumberComparator(getComparisonPerformer()).compare(control, test);
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
                    return compareCharacterData(
                            (CharacterData) control, controlContext,
                            (CharacterData) test, testContext);
                }
                break;
            case Node.DOCUMENT_NODE:
                if (test instanceof Document) {
                    return compareDocuments(
                            (Document) control, controlContext,
                            (Document) test, testContext);
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
                    return compareProcessingInstructions((ProcessingInstruction) control,
                            controlContext,
                            (ProcessingInstruction) test,
                            testContext);
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                if (test instanceof DocumentType) {
                    return compareDocTypes(
                            (DocumentType) control, controlContext,
                            (DocumentType) test, testContext);
                }
                break;
            case Node.ATTRIBUTE_NODE:
                if (test instanceof Attr) {
                    return compareAttributes(
                            (Attr) control, controlContext,
                            (Attr) test, testContext);
                }
                break;
        }
        return ComparisonResult.EQUAL;
    }

    /**
     * Compares textual content.
     */
    @VisibleForTesting
    ComparisonResult compareCharacterData(
            CharacterData control, XPathContext controlContext,
            CharacterData test, XPathContext testContext) {

        NodeAndXpath controlNode = new NodeAndXpath(control, getXPath(controlContext));
        NodeAndXpath testNode = new NodeAndXpath(test, getXPath(testContext));

        ComparisonType comparisonType = ComparisonType.TEXT_VALUE;
        if (control.getNodeType() == test.getNodeType()) {
            switch (control.getNodeType()) {
                case Node.CDATA_SECTION_NODE:
                    comparisonType = ComparisonType.CDATA_VALUE;
                    break;
                case Node.COMMENT_NODE:
                    comparisonType = ComparisonType.COMMENT_VALUE;
                case Node.TEXT_NODE:
                default:
                    break;
            }
        }

        return compare(new Comparison(comparisonType,
                controlNode, control.getData(),
                testNode, test.getData()));
    }

    /**
     * Compares document node, doctype and XML declaration properties
     */
    private ComparisonResult compareDocuments(
            Document control, XPathContext controlContext,
            Document test, XPathContext testContext) {

        NodeAndXpath controlNode = new NodeAndXpath(control, getXPath(controlContext));
        NodeAndXpath testNode = new NodeAndXpath(test, getXPath(testContext));

        DocumentType controlDt = control.getDoctype();
        DocumentType testDt = test.getDoctype();

        ComparisonResult lastResult =
                compare(new Comparison(ComparisonType.HAS_DOCTYPE_DECLARATION,
                        controlNode, Boolean.valueOf(controlDt != null),
                        testNode, Boolean.valueOf(testDt != null)));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        if (controlDt != null && testDt != null) {
            lastResult = compareNodes(controlDt, controlContext, testDt, testContext);
            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }
        }

        lastResult = compare(new Comparison(ComparisonType.XML_VERSION,
                controlNode, control.getXmlVersion(),
                testNode, test.getXmlVersion()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compare(new Comparison(ComparisonType.XML_STANDALONE,
                controlNode, control.getXmlStandalone(),
                testNode, test.getXmlStandalone()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        return compare(new Comparison(ComparisonType.XML_ENCODING,
                controlNode, control.getXmlEncoding(),
                testNode, test.getXmlEncoding()));
    }

    /**
     * Compares properties of the doctype declaration.
     */
    private ComparisonResult compareDocTypes(
            DocumentType controlType, XPathContext controlContext,
            DocumentType testType, XPathContext testContext) {

        NodeAndXpathCtx<DocumentType> control = new NodeAndXpathCtx<DocumentType>(controlType, controlContext);
        NodeAndXpathCtx<DocumentType> test = new NodeAndXpathCtx<DocumentType>(testType, testContext);
        return new DocTypeComparator(comparisonPerformer).compare(control, test);
    }

    /**
     * Compares elements node properties, in particular the element's name and
     * its attributes.
     */

    @VisibleForTesting
    ComparisonResult compareElements(
            Element control, XPathContext controlContext,
            Element test, XPathContext testContext) {

        NodeAndXpath controlNode = new NodeAndXpath(control, getXPath(controlContext));
        NodeAndXpath testNode = new NodeAndXpath(test, getXPath(testContext));

        ComparisonResult lastResult = compare(new Comparison(ComparisonType.ELEMENT_TAG_NAME,
                controlNode, Nodes.getQName(control).getLocalPart(),
                testNode, Nodes.getQName(test).getLocalPart()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        NamedNodeMap controlAttrList = control.getAttributes();
        NamedNodeMap testAttrList = test.getAttributes();

        lastResult = compareElementAttributes(
                control, controlContext, controlAttrList,
                test, testContext, testAttrList);
        return lastResult;

    }

    @VisibleForTesting
    ComparisonResult compareElementAttributes(
            Element control, XPathContext controlContext, NamedNodeMap controlAttrList,
            Element test, XPathContext testContext, NamedNodeMap testAttrList) {

        NodeAndXpath controlNode = new NodeAndXpath(control, getXPath(controlContext));
        NodeAndXpath testNode = new NodeAndXpath(test, getXPath(testContext));

        Attributes controlAttributes = Attributes.createFrom(controlAttrList);
        Attributes testAttributes = Attributes.createFrom(testAttrList);
        controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(), QNAME_MAPPER));
        testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

        ComparisonResult lastResult = compare(new Comparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES,
                controlNode, controlAttributes.getRegularAttributes().size(),
                testNode, testAttributes.getRegularAttributes().size()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        Set<Attr> foundTestAttributes = new HashSet<Attr>();
        for (int i = 0; i < controlAttributes.getRegularAttributes().size(); i++) {
            Attr controlAttr = controlAttributes.getRegularAttributes().get(i);
            final Attr testAttr = findMatchingAttr(testAttributes.getRegularAttributes(), controlAttr);

            controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
            try {
                lastResult =
                        compare(new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                                control, getXPath(controlContext),
                                Boolean.TRUE,
                                test, getXPath(testContext),
                                Boolean.valueOf(testAttr != null)));
                if (lastResult == ComparisonResult.CRITICAL) {
                    return lastResult;
                }

                if (testAttr != null) {
                    // ===
                    // TODO extract
                    if (!properties.getIgnoreAttributeOrder()) {
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
                        lastResult = compareNodes(controlAttr, controlContext, testAttr, testContext);
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
                lastResult =
                        compare(new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                                control, getXPath(controlContext),
                                Boolean.valueOf(foundTestAttributes.contains(testAttr)),
                                test, getXPath(testContext),
                                Boolean.TRUE));
                if (lastResult == ComparisonResult.CRITICAL) {
                    return lastResult;
                }
            } finally {
                testContext.navigateToParent();
            }
        }

        lastResult =
                compare(new Comparison(ComparisonType.SCHEMA_LOCATION,
                        control, getXPath(controlContext),
                        controlAttributes.getSchemaLocation() != null
                                ? controlAttributes.getSchemaLocation().getValue()
                                : null,
                        test, getXPath(testContext),
                        testAttributes.getSchemaLocation() != null
                                ? testAttributes.getSchemaLocation().getValue()
                                : null));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        return compare(new Comparison(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION,
                control, getXPath(controlContext),
                controlAttributes.getNoNamespaceSchemaLocation() != null ?
                        controlAttributes.getNoNamespaceSchemaLocation().getValue()
                        : null,
                test, getXPath(testContext),
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
        return getUnNamespacedNodeName(aNode, isNamespaced(aNode));
    }

    private String getUnNamespacedNodeName(Node aNode, boolean isNamespacedNode) {
        if (isNamespacedNode) {
            return aNode.getLocalName();
        }
        return aNode.getNodeName();
    }

    /**
     * Compares properties of a processing instruction.
     */
    private ComparisonResult compareProcessingInstructions(
            ProcessingInstruction controlInstr, XPathContext controlContext,
            ProcessingInstruction testInstr, XPathContext testContext) {

        NodeAndXpathCtx<ProcessingInstruction> control =
                new NodeAndXpathCtx<ProcessingInstruction>(controlInstr, controlContext);
        NodeAndXpathCtx<ProcessingInstruction> test =
                new NodeAndXpathCtx<ProcessingInstruction>(testInstr, testContext);
        return new ProcessingInstructionComparator(getComparisonPerformer()).compare(control, test);
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

                lastResult = compareNodes(control, controlContext,
                        test, testContext);
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
     * Compares properties of an attribute.
     */
    private ComparisonResult compareAttributes(
            Attr controlAttr, XPathContext controlContext,
            Attr testAttr, XPathContext testContext) {

        NodeAndXpathCtx<Attr> control = new NodeAndXpathCtx<Attr>(controlAttr, controlContext);
        NodeAndXpathCtx<Attr> test = new NodeAndXpathCtx<Attr>(testAttr, testContext);
        return new AttributeComparator(getComparisonPerformer()).compare(control, test);
    }

    /**
     * Find the attribute with the same namespace and local name as a given
     * attribute in a list of attributes.
     */
    private static Attr findMatchingAttr(final List<Attr> attrs, final Attr attrToMatch) {
        final boolean hasNs = attrToMatch.getNamespaceURI() != null;
        final String nsToMatch = attrToMatch.getNamespaceURI();
        final String nameToMatch = hasNs ? attrToMatch.getLocalName() : attrToMatch.getName();
        for (Attr a : attrs) {
            if (((!hasNs && a.getNamespaceURI() == null)
                    ||
                    (hasNs && nsToMatch.equals(a.getNamespaceURI())))
                    &&
                    ((hasNs && nameToMatch.equals(a.getLocalName()))
                    ||
                    (!hasNs && nameToMatch.equals(a.getName())))) {
                return a;
            }
        }
        return null;
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
