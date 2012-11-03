/*
 ******************************************************************
Copyright (c) 2001-2010, Jeff Martin, Tim Bacon
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package org.custommonkey.xmlunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.Comparison.Detail;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.util.IterableNodeList;

import org.custommonkey.xmlunit.comparators.StringComparator;
import org.custommonkey.xmlunit.diff.DifferenceType;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * Class that has responsibility for comparing Nodes and notifying a
 * DifferenceListener of any differences or dissimilarities that are found.
 * Knows how to compare namespaces and nested child nodes, but currently only
 * compares nodes of type ELEMENT_NODE, CDATA_SECTION_NODE, COMMENT_NODE,
 * DOCUMENT_TYPE_NODE, PROCESSING_INSTRUCTION_NODE and TEXT_NODE. Nodes of other
 * types (eg ENTITY_NODE) will be skipped. <br />
 * Examples and more at <a href="http://xmlunit.sourceforge.net"/>xmlunit.
 * sourceforge.net</a>
 * 
 * @see DifferenceListener#differenceFound(Difference)
 */
public class DifferenceEngine implements DifferenceEngineContract {

    private final XmlUnitProperties properties;
    private final StringComparator stringComparator;

    private static final String NULL_NODE = "null";
    private static final String NOT_NULL_NODE = "not null";
    private static final String ATTRIBUTE_ABSENT = "[attribute absent]";
    private final ComparisonController controller;
    private MatchTracker matchTracker;
    private final XpathNodeTracker controlTracker;
    private final XpathNodeTracker testTracker;

    /**
     * Simple constructor that uses no MatchTracker at all.
     * 
     * @param controller
     *            the instance used to determine whether a Difference detected
     *            by this class should halt further comparison or not
     * @see ComparisonController#haltComparison(Difference)
     */
    public DifferenceEngine(@Nullable XmlUnitProperties properties, ComparisonController controller) {
        this(properties, controller, null);
    }

    /**
     * Simple constructor
     * 
     * @param controller
     *            the instance used to determine whether a Difference detected
     *            by this class should halt further comparison or not
     * @param matchTracker
     *            the instance that is notified on each successful match. May be
     *            null.
     * @see ComparisonController#haltComparison(Difference)
     * @see MatchTracker#matchFound(Difference)
     */
    public DifferenceEngine(
            @Nullable XmlUnitProperties properties,
            ComparisonController controller,
            MatchTracker matchTracker) {

        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }
        this.stringComparator = new StringComparator(this.properties);

        this.controller = controller;
        this.matchTracker = matchTracker;
        this.controlTracker = new XpathNodeTracker();
        this.testTracker = new XpathNodeTracker();
    }

    /**
     * @param matchTracker
     *            the instance that is notified on each successful match. May be
     *            null.
     */
    public void setMatchTracker(MatchTracker matchTracker) {
        this.matchTracker = matchTracker;
    }

    /**
     * Entry point for Node comparison testing.
     * 
     * @param control
     *            Control XML to compare
     * @param test
     *            Test XML to compare
     * @param listener
     *            Notified of any {@link Difference differences} detected during
     *            node comparison testing
     * @param elementQualifier
     *            Used to determine which elements qualify for comparison e.g.
     *            when a node has repeated child elements that may occur in any
     *            sequence and that sequence is not considered important.
     */
    public void compare(
            Node control,
            Node test,
            DifferenceListener listener,
            ElementQualifier elementQualifier) {
        controlTracker.reset();
        testTracker.reset();
        try {
            Comparison comparison = new Comparison(ComparisonType.NODE_TYPE,
                    control, controlTracker.toXpathString(), getNullOrNotNull(control),
                    test, testTracker.toXpathString(), getNullOrNotNull(test));
            createValueComparator(listener)
                    .compare(comparison);

            if (control != null) {
                compareNode(control, test, listener, elementQualifier);
            }
        } catch (DifferenceFoundException e) {
            // thrown by the protected compare() method to terminate the
            // comparison and unwind the call stack back to here
        }
    }

    private String getNullOrNotNull(Node aNode) {
        return aNode == null ? NULL_NODE : NOT_NULL_NODE;
    }

    /**
     * First point of call: if nodes are comparable it compares node values then
     * recurses to compare node children.
     * 
     * @param control
     * @param test
     * @param listener
     * @param elementQualifier
     * @throws DifferenceFoundException
     */
    protected void compareNode(Node control, Node test,
            DifferenceListener listener, ElementQualifier elementQualifier)
            throws DifferenceFoundException {
        boolean comparable = compareNodeBasics(control, test, listener);
        boolean isDocumentNode = false;

        if (comparable) {
            switch (control.getNodeType()) {
                case Node.ELEMENT_NODE:
                    compareElement((Element) control, (Element) test, listener);
                    break;
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                    compareText((CharacterData) control,
                            (CharacterData) test, listener);
                    break;
                case Node.COMMENT_NODE:
                    compareComment((Comment) control, (Comment) test, listener);
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    compareDocumentType((DocumentType) control,
                            (DocumentType) test, listener);
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    compareProcessingInstruction((ProcessingInstruction) control,
                            (ProcessingInstruction) test, listener);
                    break;
                case Node.DOCUMENT_NODE:
                    isDocumentNode = true;
                    compareDocument((Document) control, (Document) test,
                            listener, elementQualifier);
                    break;
                default:
                    listener.skippedComparison(control, test);
            }
        }

        compareHasChildNodes(control, test, listener);
        if (isDocumentNode) {
            Element controlElement = ((Document) control).getDocumentElement();
            Element testElement = ((Document) test).getDocumentElement();
            if (controlElement != null && testElement != null) {
                compareNode(controlElement, testElement, listener, elementQualifier);
            }
        } else {
            controlTracker.indent();
            testTracker.indent();
            compareNodeChildren(control, test, listener, elementQualifier);
            controlTracker.outdent();
            testTracker.outdent();
        }
    }

    /**
     * Compare two Documents for doctype and then element differences
     * 
     * @param control
     * @param test
     * @param listener
     * @param elementQualifier
     * @throws DifferenceFoundException
     */
    protected void compareDocument(Document control, Document test,
            DifferenceListener listener, ElementQualifier elementQualifier)
            throws DifferenceFoundException {
        DocumentType controlDoctype = control.getDoctype();
        DocumentType testDoctype = test.getDoctype();

        Comparison comparison = new Comparison(ComparisonType.HAS_DOCTYPE_DECLARATION,
                control, controlTracker.toXpathString(), getNullOrNotNull(controlDoctype),
                test, controlTracker.toXpathString(), getNullOrNotNull(testDoctype));

        createValueComparator(listener)
                .compare(comparison);

        if (controlDoctype != null && testDoctype != null) {
            compareNode(controlDoctype, testDoctype, listener, elementQualifier);
        }
    }

    /**
     * Compares node type and node namespace characteristics: basically
     * determines if nodes are comparable further
     * 
     * @param control
     * @param test
     * @param listener
     * @return true if the nodes are comparable further, false otherwise
     * @throws DifferenceFoundException
     */
    protected boolean compareNodeBasics(Node control, Node test,
            DifferenceListener listener) throws DifferenceFoundException {
        controlTracker.visited(control);
        testTracker.visited(test);

        Short controlType = new Short(control.getNodeType());
        Short testType = new Short(test.getNodeType());

        boolean textAndCDATA = comparingTextAndCDATA(control.getNodeType(),
                test.getNodeType());
        if (!textAndCDATA) {
            Comparison comparison = new Comparison(ComparisonType.NODE_TYPE,
                    control, controlTracker.toXpathString(), controlType,
                    test, testTracker.toXpathString(), testType);

            createValueComparator(listener).compare(comparison);
        }

        Comparison comparison = new Comparison(ComparisonType.NAMESPACE_URI,
                control, controlTracker.toXpathString(), control.getNamespaceURI(),
                test, testTracker.toXpathString(), test.getNamespaceURI());
        createValueComparator(listener)
                .compare(comparison);

        comparison = new Comparison(ComparisonType.NAMESPACE_PREFIX,
                control, controlTracker.toXpathString(), control.getPrefix(),
                test, testTracker.toXpathString(), test.getPrefix());
        createValueComparator(listener)
                .compare(comparison);

        return textAndCDATA || controlType.equals(testType);
    }

    private boolean comparingTextAndCDATA(short controlType, short testType) {
        return properties.getIgnoreDiffBetweenTextAndCDATA() &&
                (controlType == Node.TEXT_NODE
                        && testType == Node.CDATA_SECTION_NODE
                        ||
                testType == Node.TEXT_NODE
                        && controlType == Node.CDATA_SECTION_NODE);
    }

    /**
     * Compare the number of children, and if the same, compare the actual
     * children via their NodeLists.
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareHasChildNodes(Node control, Node test, DifferenceListener listener)
            throws DifferenceFoundException {

        List<Node> controlChildren = nodeList2List(control.getChildNodes());
        List<Node> testChildren = nodeList2List(test.getChildNodes());

        Comparison comparison = new
                Comparison(ComparisonType.HAS_CHILD_NODES,
                        control, controlTracker.toXpathString(), controlChildren.size() > 0,
                        test, testTracker.toXpathString(), testChildren.size() > 0);
        createValueComparator(listener)
                .compare(comparison);

    }

    /**
     * Returns the NodeList's Nodes as List, taking ignoreComments into account.
     */
    List<Node> nodeList2List(NodeList nl) {
        IterableNodeList nodes = new IterableNodeList(nl);
        List<Node> result = new LinkedList<Node>();
        for (Node node : nodes) {
            if (node.getNodeType() == Node.COMMENT_NODE &&
                    properties.getIgnoreComments()) {
                continue;
            }
            result.add(node);
        }

        return result;
    }

    /**
     * Compare the number of children, and if the same, compare the actual
     * children via their NodeLists.
     * 
     * @param control
     * @param test
     * @param listener
     * @param elementQualifier
     * @throws DifferenceFoundException
     */
    protected void compareNodeChildren(Node control, Node test,
            DifferenceListener listener, ElementQualifier elementQualifier)
            throws DifferenceFoundException {
        if (control.hasChildNodes() && test.hasChildNodes()) {
            List<Node> controlChildren = nodeList2List(control.getChildNodes());
            List<Node> testChildren = nodeList2List(test.getChildNodes());

            Integer controlLength = new Integer(controlChildren.size());
            Integer testLength = new Integer(testChildren.size());

            Comparison comparison = new Comparison(ComparisonType.CHILD_NODELIST_LENGTH,
                    control, controlTracker.toXpathString(), controlLength,
                    test, testTracker.toXpathString(), testLength);
            createValueComparator(listener)
                    .compare(comparison);

            compareNodeList(controlChildren, testChildren,
                    controlLength.intValue(), listener, elementQualifier);
        }
    }

    /**
     * Compare the contents of two node list one by one, assuming that order of
     * children is NOT important: matching begins at same position in test list
     * as control list.
     * 
     * @param control
     * @param test
     * @param numNodes
     *            convenience parameter because the calling method should know
     *            the value already
     * @param listener
     * @param elementQualifier
     *            used to determine which of the child elements in the test
     *            NodeList should be compared to the current child element in
     *            the control NodeList.
     * @throws DifferenceFoundException
     * @deprecated Use the version with List arguments instead
     */
    @Deprecated
    protected void compareNodeList(final NodeList control, final NodeList test,
            final int numNodes,
            final DifferenceListener listener,
            final ElementQualifier elementQualifier)
            throws DifferenceFoundException {
        compareNodeList(nodeList2List(control), nodeList2List(test),
                numNodes, listener, elementQualifier);
    }

    /**
     * Compare the contents of two node list one by one, assuming that order of
     * children is NOT important: matching begins at same position in test list
     * as control list.
     * 
     * @param control
     * @param test
     * @param numNodes
     *            convenience parameter because the calling method should know
     *            the value already
     * @param listener
     * @param elementQualifier
     *            used to determine which of the child elements in the test
     *            NodeList should be compared to the current child element in
     *            the control NodeList.
     * @throws DifferenceFoundException
     */
    protected void compareNodeList(final List<Node> controlChildren,
            final List<Node> testChildren,
            final int numNodes,
            final DifferenceListener listener,
            final ElementQualifier elementQualifier)
            throws DifferenceFoundException {

        int j = 0;
        final int lastTestNode = testChildren.size() - 1;
        testTracker.preloadChildList(testChildren);

        Map<Node, Node> matchingNodes = new HashMap<Node, Node>();
        Map<Node, Integer> matchingNodeIndexes = new HashMap<Node, Integer>();

        List<Node> unmatchedTestNodes = new ArrayList<Node>(testChildren);

        // first pass to find the matching nodes in control and test docs
        for (int i = 0; i < numNodes; ++i) {
            Node nextControl = controlChildren.get(i);
            boolean matchOnElement = nextControl instanceof Element;
            short findNodeType = nextControl.getNodeType();
            int startAt = (i > lastTestNode ? lastTestNode : i);
            j = startAt;

            boolean matchFound = false;

            /*
             * XMLUnit 1.2 and earlier don't check whether the "matched" test
             * node has already been matched to a different control node and
             * will happily match the same test node to each and every control
             * node, if necessary.
             * 
             * I (Stefan) feel this is wrong but can't change it without
             * breaking backwards compatibility (testXpathLocation12 in
             * test_DifferenceEngine which predates XMLUnit 1.0 fails, so at one
             * point it has been the expected and intended behaviour).
             * 
             * As a side effect it may leave test nodes inside the unmatched
             * list, see
             * https://sourceforge.net/tracker/?func=detail&aid=2807167
             * &group_id=23187&atid=377768
             * 
             * To overcome the later problem the code will now prefer test nodes
             * that haven't already been matched to any other node and falls
             * back to the first (multiply-)matched node if none could be found.
             * Yes, this is strange.
             */
            int fallbackMatch = -1;

            while (!matchFound) {
                Node t = testChildren.get(j);
                if (findNodeType == t.getNodeType()
                        || comparingTextAndCDATA(findNodeType, t.getNodeType())) {
                    matchFound = !matchOnElement
                            || elementQualifier == null
                            || elementQualifier
                                    .qualifyForComparison((Element) nextControl,
                                            (Element) t);
                }
                if (matchFound && !unmatchedTestNodes.contains(t)) {
                    /*
                     * test node already matched to a different control node,
                     * try the other test nodes first but keep this as
                     * "fallback" (unless there already is a fallback)
                     */
                    if (fallbackMatch < 0) {
                        fallbackMatch = j;
                    }
                    matchFound = false;
                }
                if (!matchFound) {
                    ++j;
                    if (j > lastTestNode) {
                        j = 0;
                    }
                    if (j == startAt) {
                        // been through all children
                        break;
                    }
                }
            }
            if (!matchFound && properties.getCompareUnmatched()
                    && fallbackMatch >= 0) {
                matchFound = true;
                j = fallbackMatch;
            }
            if (matchFound) {
                matchingNodes.put(nextControl, testChildren.get(j));
                matchingNodeIndexes.put(nextControl, new Integer(j));
                unmatchedTestNodes.remove(testChildren.get(j));
            }
        }

        // next, do the actual comparision on those that matched - or
        // match them against the first test nodes that didn't match
        // any other control nodes
        for (int i = 0; i < numNodes; ++i) {
            Node nextControl = controlChildren.get(i);
            Node nextTest = matchingNodes.get(nextControl);
            Integer testIndex = matchingNodeIndexes.get(nextControl);
            if (nextTest == null && properties.getCompareUnmatched()
                    && !unmatchedTestNodes.isEmpty()) {
                nextTest = unmatchedTestNodes.get(0);
                testIndex = new Integer(testChildren.indexOf(nextTest));
                unmatchedTestNodes.remove(0);
            }
            if (nextTest != null) {
                compareNode(nextControl, nextTest, listener, elementQualifier);

                Comparison comparison = new Comparison(ComparisonType.CHILD_NODELIST_SEQUENCE,
                        nextControl, controlTracker.toXpathString(), i,
                        nextTest, testTracker.toXpathString(), testIndex);
                createValueComparator(listener)
                        .compare(comparison);
            } else {
                missingNode(nextControl, null, listener);
            }
        }

        // now handle remaining unmatched test nodes
        for (Iterator<Node> iter = unmatchedTestNodes.iterator(); iter.hasNext();) {
            missingNode(null, iter.next(), listener);
        }
    }

    private void missingNode(Node control, Node test,
            DifferenceListener listener)
            throws DifferenceFoundException {
        if (control != null) {
            controlTracker.visited(control);
            Comparison comparison = new Comparison(ComparisonType.CHILD_LOOKUP,
                    control, controlTracker.toXpathString(), control.getNodeName(),
                    null, null, null);
            createValueComparator(listener)
                    .compare(comparison);
        } else {
            testTracker.visited(test);
            Comparison comparison = new Comparison(ComparisonType.CHILD_LOOKUP,
                    null, null, null,
                    test, testTracker.toXpathString(), test.getNodeName());
            createValueComparator(listener)
                    .compare(comparison);
        }
    }

    /**
     * @param aNode
     * @return true if the node has a namespace
     */
    private boolean isNamespaced(Node aNode) {
        String namespace = aNode.getNamespaceURI();
        return namespace != null && namespace.length() > 0;
    }

    /**
     * Compare 2 elements and their attributes
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareElement(
            Element control, Element test, DifferenceListener listener) throws DifferenceFoundException {

        Comparison comparison = new Comparison(ComparisonType.ELEMENT_TAG_NAME,
                control, controlTracker.toXpathString(), getUnNamespacedNodeName(control),
                test, testTracker.toXpathString(), getUnNamespacedNodeName(test));
        createValueComparator(listener)
                .compare(comparison);

        NamedNodeMap controlAttr = control.getAttributes();
        Integer controlNonXmlnsAttrLength = getNonSpecialAttrLength(controlAttr);
        NamedNodeMap testAttr = test.getAttributes();
        Integer testNonXmlnsAttrLength = getNonSpecialAttrLength(testAttr);

        comparison = new Comparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES,
                control, controlTracker.toXpathString(), controlNonXmlnsAttrLength,
                test, testTracker.toXpathString(), testNonXmlnsAttrLength);
        createValueComparator(listener)
                .compare(comparison);

        compareElementAttributes(control, test, controlAttr, testAttr, listener);
    }

    /**
     * The number of attributes not related to namespace declarations and/or
     * Schema location.
     */
    private Integer getNonSpecialAttrLength(NamedNodeMap attributes) {
        int length = 0, maxLength = attributes.getLength();
        for (int i = 0; i < maxLength; ++i) {
            Attr a = (Attr) attributes.item(i);
            if (!isXMLNSAttribute(a)
                    && !isRecognizedXMLSchemaInstanceAttribute(a)) {
                ++length;
            }
        }
        return new Integer(length);
    }

    void compareElementAttributes(Element control, Element test,
            NamedNodeMap controlAttr,
            NamedNodeMap testAttr,
            DifferenceListener listener)
            throws DifferenceFoundException {
        ArrayList<Attr> unmatchedTestAttrs = new ArrayList<Attr>();
        for (int i = 0; i < testAttr.getLength(); ++i) {
            Attr nextAttr = (Attr) testAttr.item(i);
            if (!isXMLNSAttribute(nextAttr)) {
                unmatchedTestAttrs.add(nextAttr);
            }
        }

        for (int i = 0; i < controlAttr.getLength(); ++i) {
            Attr nextAttr = (Attr) controlAttr.item(i);
            if (isXMLNSAttribute(nextAttr)) {
                // xml namespacing is handled in compareNodeBasics
            } else {
                boolean isNamespacedAttr = isNamespaced(nextAttr);
                String attrName = getUnNamespacedNodeName(nextAttr, isNamespacedAttr);
                Attr compareTo = null;

                if (isNamespacedAttr) {
                    compareTo = (Attr) testAttr.getNamedItemNS(
                            nextAttr.getNamespaceURI(), attrName);
                } else {
                    compareTo = (Attr) testAttr.getNamedItem(attrName);
                }

                if (compareTo != null) {
                    unmatchedTestAttrs.remove(compareTo);
                }

                if (isRecognizedXMLSchemaInstanceAttribute(nextAttr)) {
                    compareRecognizedXMLSchemaInstanceAttribute(nextAttr,
                            compareTo,
                            listener);

                } else if (compareTo != null) {
                    compareAttribute(nextAttr, compareTo, listener);

                    if (!properties.getIgnoreAttributeOrder()) {
                        Attr attributeItem = (Attr) testAttr.item(i);
                        String testAttrName = ATTRIBUTE_ABSENT;
                        if (attributeItem != null) {
                            testAttrName =
                                    getUnNamespacedNodeName(attributeItem);
                        }

                        Comparison comparison = new Comparison(ComparisonType.ATTR_SEQUENCE,
                                nextAttr, controlTracker.toXpathString(), attrName,
                                compareTo, testTracker.toXpathString(), testAttrName);
                        createValueComparator(listener)
                                .compare(comparison);
                    }
                } else {
                    controlTracker.clearTrackedAttribute();
                    testTracker.clearTrackedAttribute();

                    Comparison comparison = new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                            control, controlTracker.toXpathString(), attrName,
                            test, testTracker.toXpathString(), null);
                    createValueComparator(listener)
                            .compare(comparison);
                }
            }
        }

        for (Iterator<Attr> iter = unmatchedTestAttrs.iterator(); iter.hasNext();) {
            Attr nextAttr = iter.next();
            if (isRecognizedXMLSchemaInstanceAttribute(nextAttr)) {
                compareRecognizedXMLSchemaInstanceAttribute(null, nextAttr,
                        listener);
            } else {
                controlTracker.clearTrackedAttribute();
                testTracker.clearTrackedAttribute();

                Comparison comparison = new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                        control, controlTracker.toXpathString(), null,
                        test, testTracker.toXpathString(), getUnNamespacedNodeName(nextAttr, isNamespaced(nextAttr)));
                createValueComparator(listener)
                        .compare(comparison);
            }
        }

        controlTracker.clearTrackedAttribute();
        testTracker.clearTrackedAttribute();
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
     * @param attribute
     * @return true if the attribute represents a namespace declaration
     */
    private boolean isXMLNSAttribute(Attr attribute) {
        return XMLConstants.XMLNS_PREFIX.equals(attribute.getPrefix()) ||
                XMLConstants.XMLNS_PREFIX.equals(attribute.getName());
    }

    /**
     * @param attr
     * @return true if the attribute is an XML Schema Instance namespace
     *         attribute XMLUnit treats in a special way.
     */
    private boolean isRecognizedXMLSchemaInstanceAttribute(Attr attr) {
        return XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(attr.getNamespaceURI())
                && (XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR
                        .equals(attr.getLocalName())
                || XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR
                        .equals(attr.getLocalName()));
    }

    /**
     * Compare two attributes
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareRecognizedXMLSchemaInstanceAttribute(Attr control,
            Attr test,
            DifferenceListener listener)
            throws DifferenceFoundException {
        Attr nonNullNode = control != null ? control : test;
        Difference d = new Difference(DifferenceType.NO_NAMESPACE_SCHEMA_LOCATION);
        if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR.equals(nonNullNode.getLocalName())) {
            d = new Difference(DifferenceType.SCHEMA_LOCATION);
        }

        if (control != null) {
            controlTracker.visited(control);
        }
        if (test != null) {
            testTracker.visited(test);
        }

        ComparisonType type = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
        if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR.equals(nonNullNode.getLocalName())) {
            type = ComparisonType.SCHEMA_LOCATION;
        }
        Comparison comparison = new Comparison(type,
                control, controlTracker.toXpathString(), control != null ? control.getValue() : ATTRIBUTE_ABSENT,
                test, testTracker.toXpathString(), test != null ? test.getValue() : ATTRIBUTE_ABSENT);
        createValueComparator(listener)
                .compare(comparison);
    }

    /**
     * Compare two attributes
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareAttribute(Attr control, Attr test,
            DifferenceListener listener) throws DifferenceFoundException {
        controlTracker.visited(control);
        testTracker.visited(test);

        Comparison comparison = new Comparison(ComparisonType.NAMESPACE_PREFIX,
                control, controlTracker.toXpathString(), control.getPrefix(),
                test, testTracker.toXpathString(), test.getPrefix());
        createValueComparator(listener)
                .compare(comparison);

        comparison = new Comparison(ComparisonType.ATTR_VALUE,
                control, controlTracker.toXpathString(), control.getValue(),
                test, testTracker.toXpathString(), test.getValue());
        createValueComparator(listener)
                .compare(comparison);

        comparison = new Comparison(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED,
                control, controlTracker.toXpathString(), control.getSpecified(),
                test, testTracker.toXpathString(), test.getSpecified());
        createValueComparator(listener)
                .compare(comparison);
    }

    /**
     * Compare two CDATA sections - unused, kept for backwards compatibility
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareCDataSection(CDATASection control, CDATASection test,
            DifferenceListener listener) throws DifferenceFoundException {
        compareText(control, test, listener);
    }

    /**
     * Compare two comments
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareComment(Comment control, Comment test,
            DifferenceListener listener) throws DifferenceFoundException {
        if (!properties.getIgnoreComments()) {
            compareCharacterData(control, test, listener, new Difference(DifferenceType.COMMENT_VALUE));
        }
    }

    /**
     * Compare two DocumentType nodes
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareDocumentType(DocumentType control, DocumentType test,
            DifferenceListener listener) throws DifferenceFoundException {

        Comparison comparison = new Comparison(ComparisonType.DOCTYPE_NAME,
                control, controlTracker.toXpathString(), control.getName(),
                test, testTracker.toXpathString(), test.getName());
        createValueComparator(listener)
                .compare(comparison);

        comparison = new Comparison(ComparisonType.DOCTYPE_PUBLIC_ID,
                control, controlTracker.toXpathString(), control.getPublicId(),
                test, testTracker.toXpathString(), test.getPublicId());
        createValueComparator(listener)
                .compare(comparison);

        comparison = new Comparison(ComparisonType.DOCTYPE_SYSTEM_ID,
                control, controlTracker.toXpathString(), control.getSystemId(),
                test, testTracker.toXpathString(), test.getSystemId());
        createValueComparator(listener)
                .compare(comparison);
    }

    /**
     * Compare two processing instructions
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareProcessingInstruction(ProcessingInstruction control,
            ProcessingInstruction test, DifferenceListener listener)
            throws DifferenceFoundException {

        Comparison comparison = new Comparison(ComparisonType.PROCESSING_INSTRUCTION_TARGET,
                control, controlTracker.toXpathString(), control.getTarget(),
                test, testTracker.toXpathString(), test.getTarget());
        createValueComparator(listener)
                .compare(comparison);

        comparison = new Comparison(ComparisonType.PROCESSING_INSTRUCTION_DATA,
                control, controlTracker.toXpathString(), control.getData(),
                test, testTracker.toXpathString(), test.getData());
        createValueComparator(listener)
                .compare(comparison);
    }

    /**
     * Compare text
     * 
     * @param control
     * @param test
     * @param listener
     * @throws DifferenceFoundException
     */
    protected void compareText(
            CharacterData control, CharacterData test, DifferenceListener listener)
            throws DifferenceFoundException {

        Difference diff;
        if (control instanceof CDATASection) {
            diff = new Difference(DifferenceType.CDATA_VALUE);
        } else {
            diff = new Difference(DifferenceType.TEXT_VALUE);
        }

        compareCharacterData(control, test, listener, diff);
    }

    /**
     * Character comparison method used by comments, text and CDATA sections
     * 
     * @param control
     * @param test
     * @param listener
     * @param differenceType
     * @throws DifferenceFoundException
     */
    private void compareCharacterData(CharacterData control, CharacterData test,
            DifferenceListener listener, Difference difference)
            throws DifferenceFoundException {

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
        Comparison comparison = new Comparison(comparisonType,
                control, controlTracker.toXpathString(), control.getData(),
                test, testTracker.toXpathString(), test.getData());
        createValueComparator(listener)
                .compare(comparison);
    }

    /**
     * Marker exception thrown by the protected compare() method and passed
     * upwards through the call stack to the public compare() method.
     */
    @SuppressWarnings("serial")
    protected static final class DifferenceFoundException extends Exception {
        private DifferenceFoundException() {
            super("This exception is used to control flow");
        }
    }

    protected ValueComparator createValueComparator(DifferenceListener listener) {
        return new ValueComparator(listener);
    }

    public class ValueComparator {
        private final DifferenceListener listener;

        private ValueComparator(DifferenceListener listener) {
            this.listener = listener;
        }

        public void compare(Comparison comparison) throws DifferenceFoundException {
            Detail controlDetails = comparison.getControlDetails();
            Detail testDetails = comparison.getTestDetails();

            Difference difference = NewDifferenceEngine.toDifference(comparison);

            Difference differenceInstance = new Difference(
                    difference,
                    difference.getControlNodeDetail(),
                    difference.getTestNodeDetail());
            if (!haveEqualValues(controlDetails.getValue(), testDetails.getValue())) {
                listener.differenceFound(differenceInstance);
                if (controller.haltComparison(differenceInstance)) {
                    throw new DifferenceFoundException();
                }
            } else if (matchTracker != null) {
                matchTracker.matchFound(differenceInstance);
            }
        }

        /**
         * Test two possibly null values for equality
         * 
         * @param expected
         * @param actual
         * @return TRUE if the values are both null or equals() equal
         */
        private <T> boolean haveEqualValues(T expected, T actual) {
            if (expected == null) {
                return actual == null;
            }

            if (expected instanceof String && actual instanceof String) {
                int result = stringComparator.compare((String) expected, (String) actual);
                return result == 0;
            }
            return expected.equals(actual);
        }
    }
}
