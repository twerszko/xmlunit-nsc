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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DOMDifferenceEngine;
import net.sf.xmlunit.diff.DefaultNodeMatcher;
import net.sf.xmlunit.diff.DifferenceEvaluator;
import net.sf.xmlunit.diff.DifferenceEvaluators;
import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ElementSelectors;
import net.sf.xmlunit.diff.NodeMatcher;
import net.sf.xmlunit.input.CommentLessSource;
import net.sf.xmlunit.input.WhitespaceNormalizedSource;
import net.sf.xmlunit.input.WhitespaceStrippedSource;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Predicate;

import org.custommonkey.xmlunit.diff.DifferenceType;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
public class NewDifferenceEngine
        implements DifferenceEngineContract {

    private static final Integer ZERO = Integer.valueOf(0);
    private static final Map<Class<?>, ElementSelector> KNOWN_SELECTORS;
    static {
        Map<Class<?>, ElementSelector> m = new HashMap<Class<?>, ElementSelector>();
        m.put(ElementNameAndTextQualifier.class, ElementSelectors.byNameAndText);
        m.put(ElementQualifier.class, ElementSelectors.byName);
        m.put(RecursiveElementNameAndTextQualifier.class, ElementSelectors.byNameAndTextRec);
        KNOWN_SELECTORS = Collections.unmodifiableMap(m);
    }

    private final XmlUnitProperties properties;

    private final ComparisonController controller;
    private MatchTracker matchTracker;

    /**
     * Simple constructor that uses no MatchTracker at all.
     * 
     * @param controller
     *            the instance used to determine whether a Difference detected
     *            by this class should halt further comparison or not
     * @see ComparisonController#haltComparison(Difference)
     */
    public NewDifferenceEngine(@Nullable XmlUnitProperties properties, ComparisonController controller) {
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
    public NewDifferenceEngine(@Nullable XmlUnitProperties properties, ComparisonController controller,
            MatchTracker matchTracker) {

        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }

        this.controller = controller;
        this.matchTracker = matchTracker;
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
    public void compare(Node control, Node test, DifferenceListener listener,
            ElementQualifier elementQualifier) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine();

        final IsBetweenDocumentNodeAndRootElement checkPrelude =
                new IsBetweenDocumentNodeAndRootElement();
        engine.addComparisonListener(checkPrelude);

        if (matchTracker != null) {
            engine
                    .addMatchListener(new MatchTracker2ComparisonListener(matchTracker));
        }

        DifferenceEvaluator controllerAsEvaluator =
                new ComparisonController2DifferenceEvaluator(controller);
        DifferenceEvaluator ev = null;
        if (listener != null) {
            ev = DifferenceEvaluators
                    .first(new DifferenceListener2DifferenceEvaluator(listener),
                            controllerAsEvaluator);
        } else {
            ev = controllerAsEvaluator;
        }
        final net.sf.xmlunit.diff.DifferenceEvaluator evaluator = ev;
        engine
                .setDifferenceEvaluator(new DifferenceEvaluator() {
                    public ComparisonResult evaluate(Comparison comparison,
                            ComparisonResult outcome) {
                        if (!swallowComparison(comparison, outcome,
                                checkPrelude)) {
                            return evaluator.evaluate(comparison, outcome);
                        }
                        return outcome;
                    }
                });

        NodeMatcher m = new DefaultNodeMatcher();
        if (elementQualifier != null) {
            Class<?> c = elementQualifier.getClass();
            if (KNOWN_SELECTORS.containsKey(c)) {
                m = new DefaultNodeMatcher(KNOWN_SELECTORS.get(c));
            } else {
                m = new DefaultNodeMatcher(new ElementQualifier2ElementSelector(elementQualifier));
            }
        }
        if (!properties.getCompareUnmatched()) {
            engine.setNodeMatcher(m);
        } else {
            engine.setNodeMatcher(new CompareUnmatchedNodeMatcher(m));
        }

        Input.Builder ctrlBuilder = Input.fromNode(control);
        Input.Builder tstBuilder = Input.fromNode(test);

        Source ctrlSource = ctrlBuilder.build();
        Source tstSource = tstBuilder.build();
        if (properties.getIgnoreComments()) {
            ctrlSource = new CommentLessSource(ctrlSource);
            tstSource = new CommentLessSource(tstSource);
        }
        if (properties.getNormalizeWhitespace()) {
            ctrlSource = new WhitespaceNormalizedSource(ctrlSource);
            tstSource = new WhitespaceNormalizedSource(tstSource);
        } else if (properties.getIgnoreWhitespace()) {
            ctrlSource = new WhitespaceStrippedSource(ctrlSource);
            tstSource = new WhitespaceStrippedSource(tstSource);
        }

        engine.compare(ctrlSource, tstSource);
    }

    public static Difference toDifference(Comparison comp) {
        Difference proto = null;
        switch (comp.getType()) {
            case ATTR_VALUE_EXPLICITLY_SPECIFIED:
                proto = new Difference(DifferenceType.ATTR_VALUE_EXPLICITLY_SPECIFIED);
                break;
            case HAS_DOCTYPE_DECLARATION:
                proto = new Difference(DifferenceType.HAS_DOCTYPE_DECLARATION);
                break;
            case DOCTYPE_NAME:
                proto = new Difference(DifferenceType.DOCTYPE_NAME);
                break;
            case DOCTYPE_PUBLIC_ID:
                proto = new Difference(DifferenceType.DOCTYPE_PUBLIC_ID);
                break;
            case DOCTYPE_SYSTEM_ID:
                proto = new Difference(DifferenceType.DOCTYPE_SYSTEM_ID);
                break;
            case SCHEMA_LOCATION:
                proto = new Difference(DifferenceType.SCHEMA_LOCATION);
                break;
            case NO_NAMESPACE_SCHEMA_LOCATION:
                proto = new Difference(DifferenceType.NO_NAMESPACE_SCHEMA_LOCATION);
                break;
            case NODE_TYPE:
                proto = new Difference(DifferenceType.NODE_TYPE);
                break;
            case NAMESPACE_PREFIX:
                proto = new Difference(DifferenceType.NAMESPACE_PREFIX);
                break;
            case NAMESPACE_URI:
                proto = new Difference(DifferenceType.NAMESPACE_URI);
                break;
            case TEXT_VALUE:
                if (comp.getControlDetails().getTarget() instanceof CDATASection) {
                    proto = new Difference(DifferenceType.CDATA_VALUE);
                } else if (comp.getControlDetails().getTarget() instanceof Comment) {
                    proto = new Difference(DifferenceType.COMMENT_VALUE);
                } else {
                    proto = new Difference(DifferenceType.TEXT_VALUE);
                }
                break;
            case PROCESSING_INSTRUCTION_TARGET:
                proto = new Difference(DifferenceType.PROCESSING_INSTRUCTION_TARGET);
                break;
            case PROCESSING_INSTRUCTION_DATA:
                proto = new Difference(DifferenceType.PROCESSING_INSTRUCTION_DATA);
                break;
            case ELEMENT_TAG_NAME:
                proto = new Difference(DifferenceType.ELEMENT_TAG_NAME);
                break;
            case ELEMENT_NUM_ATTRIBUTES:
                proto = new Difference(DifferenceType.ELEMENT_NUM_ATTRIBUTES);
                break;
            case ATTR_VALUE:
                proto = new Difference(DifferenceType.ATTR_VALUE);
                break;
            case CHILD_NODELIST_LENGTH:
                Comparison.Detail cd = comp.getControlDetails();
                Comparison.Detail td = comp.getTestDetails();
                if (ZERO.equals(cd.getValue())
                        || ZERO.equals(td.getValue())) {
                    return new Difference(new Difference(DifferenceType.HAS_CHILD_NODES),
                            new Comparison.Detail(
                                    cd.getTarget(),
                                    cd.getXpath(),
                                    String.valueOf(!ZERO.equals(cd.getValue()))),
                            new Comparison.Detail(
                                    td.getTarget(),
                                    td.getXpath(),
                                    String.valueOf(!ZERO.equals(td.getValue()))));
                }
                proto = new Difference(DifferenceType.CHILD_NODELIST_LENGTH);
                break;
            case CHILD_NODELIST_SEQUENCE:
                proto = new Difference(DifferenceType.CHILD_NODELIST_SEQUENCE);
                break;
            case CHILD_LOOKUP:
                proto = new Difference(DifferenceType.CHILD_NODE_NOT_FOUND);
                break;
            case ATTR_NAME_LOOKUP:
                proto = new Difference(DifferenceType.ATTR_NAME_NOT_FOUND);
                break;
            default:
                /* comparison doesn't match one of legacy's built-in differences */
                break;
        }
        if (proto != null) {
            return new Difference(proto, adaptNodeDetail(comp.getControlDetails()),
                    adaptNodeDetail(comp.getTestDetails()));
        }
        return null;
    }

    public static Comparison.Detail adaptNodeDetail(Comparison.Detail detail) {
        String value = String.valueOf(detail.getValue());
        if (detail.getValue() instanceof QName) {
            value = ((QName) detail.getValue()).getLocalPart();
        } else if (detail.getValue() instanceof Node) {
            value = ((Node) detail.getValue()).getNodeName();
        }
        return new Comparison.Detail(detail.getTarget(), detail.getXpath(), value);
    }

    public static class MatchTracker2ComparisonListener
            implements ComparisonListener {
        private final MatchTracker mt;

        public MatchTracker2ComparisonListener(MatchTracker m) {
            mt = m;
        }

        public void comparisonPerformed(Comparison comparison,
                ComparisonResult outcome) {
            Difference diff = toDifference(comparison);
            if (diff != null) {
                mt.matchFound(diff);
            }
        }
    }

    public static class DifferenceListener2ComparisonListener
            implements ComparisonListener {
        private final DifferenceListener dl;

        public DifferenceListener2ComparisonListener(DifferenceListener dl) {
            this.dl = dl;
        }

        public void comparisonPerformed(Comparison comparison,
                ComparisonResult outcome) {
            Difference diff = toDifference(comparison);
            if (diff != null) {
                dl.differenceFound(diff);
            }
        }
    }

    private static final Short TEXT_TYPE = Short.valueOf(Node.TEXT_NODE);
    private static final Short CDATA_TYPE =
            Short.valueOf(Node.CDATA_SECTION_NODE);

    private boolean swallowComparison(Comparison comparison,
            ComparisonResult outcome,
            IsBetweenDocumentNodeAndRootElement
            checkPrelude) {
        if (outcome == ComparisonResult.EQUAL) {
            return true;
        }
        if ((comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
                && comparison.getControlDetails().getTarget() instanceof Document)
                ||
                (
                comparison.getType() == ComparisonType.CHILD_LOOKUP
                &&
                (isNonElementDocumentChild(comparison.getControlDetails())
                || isNonElementDocumentChild(comparison.getTestDetails()))
                )
                || checkPrelude.shouldSkip()) {
            return true;
        }
        if (properties.getIgnoreDiffBetweenTextAndCDATA()
                && comparison.getType() == ComparisonType.NODE_TYPE) {
            return (
                    TEXT_TYPE.equals(comparison.getControlDetails().getValue())
                    ||
                    CDATA_TYPE.equals(comparison.getControlDetails().getValue())
                    )
                    && (
                    TEXT_TYPE.equals(comparison.getTestDetails().getValue())
                    ||
                    CDATA_TYPE.equals(comparison.getTestDetails().getValue())
                    );
        }
        return false;
    }

    private static boolean isNonElementDocumentChild(Comparison.Detail detail) {
        return detail != null && detail.getTarget() instanceof Node
                && !(detail.getTarget() instanceof Element)
                && detail.getTarget().getParentNode() instanceof Document;
    }

    public static class ComparisonController2DifferenceEvaluator
            implements DifferenceEvaluator {
        private final ComparisonController cc;

        public ComparisonController2DifferenceEvaluator(ComparisonController c) {
            cc = c;
        }

        public ComparisonResult evaluate(Comparison comparison,
                ComparisonResult outcome) {
            Difference diff = toDifference(comparison);
            if (diff != null && cc.haltComparison(diff)) {
                return ComparisonResult.CRITICAL;
            }
            return outcome;
        }
    }

    public static class ElementQualifier2ElementSelector
            implements ElementSelector {
        private final ElementQualifier eq;

        public ElementQualifier2ElementSelector(ElementQualifier eq) {
            this.eq = eq;
        }

        public boolean canBeCompared(Element controlElement,
                Element testElement) {
            return eq.qualifyForComparison(controlElement, testElement);
        }

    }

    public static class DifferenceListener2DifferenceEvaluator
            implements DifferenceEvaluator {
        private final DifferenceListener dl;

        public DifferenceListener2DifferenceEvaluator(DifferenceListener dl) {
            this.dl = dl;
        }

        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            Difference diff = toDifference(comparison);
            if (diff != null) {
                switch (dl.differenceFound(diff)) {
                    case DIFFERENT_NODES_IDENTICAL:
                        return ComparisonResult.EQUAL;
                    case DIFFERENT_NODES_SIMILAR:
                        return ComparisonResult.SIMILAR;
                    case SIMILAR_NODES_DIFFERENT:
                        return ComparisonResult.DIFFERENT;
                    case ACCEPT_DIFFERENCE:
                        break;
                }
            }
            return outcome;
        }
    }

    /**
     * Tests whether the DifferenceEngine is currently processing comparisons of
     * "things" between the document node and the document's root element
     * (comments or PIs, mostly) since these must be ignored for backwards
     * compatibility reasons.
     * 
     * <p>
     * Relies on the following assumptions:
     * <ul>
     * 
     * <li>the last comparison DOMDifferenceEngine performs on the document node
     * is an XML_ENCODING comparison.</li>
     * <li>the first comparison DOMDifferenceEngine performs on matching root
     * elements is a NODE_TYPE comparison. The control Node is an Element Node.</li>
     * <li>the first comparison DOMDifferenceEngine performs if the root
     * elements don't match is a CHILD_LOOKUP comparison. The control Node is an
     * Element Node.</li>
     * </ul>
     * </p>
     */
    private static class IsBetweenDocumentNodeAndRootElement
            implements ComparisonListener {

        private boolean haveSeenXmlEncoding = false;
        private boolean haveSeenElementNodeComparison = false;

        public void comparisonPerformed(Comparison comparison,
                ComparisonResult outcome) {
            if (comparison.getType() == ComparisonType.XML_ENCODING) {
                haveSeenXmlEncoding = true;
            } else if (comparison.getControlDetails().getTarget()
                    instanceof Element
                    &&
                    (comparison.getType() == ComparisonType.NODE_TYPE
                    || comparison.getType() == ComparisonType.CHILD_LOOKUP)) {
                haveSeenElementNodeComparison = true;
            }
        }

        private boolean shouldSkip() {
            return haveSeenXmlEncoding && !haveSeenElementNodeComparison;
        }
    }

    private static class CompareUnmatchedNodeMatcher
            implements NodeMatcher {
        private final NodeMatcher nestedMatcher;

        private CompareUnmatchedNodeMatcher(NodeMatcher nested) {
            nestedMatcher = nested;
        }

        public Iterable<Map.Entry<Node, Node>>
                match(Iterable<Node> controlNodes,
                        Iterable<Node> testNodes) {
            final Map<Node, Node> map = new HashMap<Node, Node>();
            for (Map.Entry<Node, Node> e : nestedMatcher.match(controlNodes, testNodes)) {
                map.put(e.getKey(), e.getValue());
            }

            final LinkedList<Map.Entry<Node, Node>> result =
                    new LinkedList<Map.Entry<Node, Node>>();

            for (Node n : controlNodes) {
                if (map.containsKey(n)) {
                    result.add(new Entry(n, map.get(n)));
                } else {
                    Iterable<Node> unmatchedTestElements =
                            Linqy.filter(testNodes, new Predicate<Node>() {
                                public boolean matches(Node t) {
                                    return !map.containsValue(t);
                                }
                            });
                    Iterator<Node> it = unmatchedTestElements.iterator();
                    if (it.hasNext()) {
                        Node t = it.next();
                        map.put(n, t);
                        result.add(new Entry(n, t));
                    }
                }
            }
            return result;
        }

        private static class Entry implements Map.Entry<Node, Node> {
            private final Node key;
            private final Node value;

            private Entry(Node k, Node v) {
                key = k;
                value = v;
            }

            public Node getKey() {
                return key;
            }

            public Node getValue() {
                return value;
            }

            public Node setValue(Node v) {
                throw new UnsupportedOperationException();
            }
        }
    }

}
