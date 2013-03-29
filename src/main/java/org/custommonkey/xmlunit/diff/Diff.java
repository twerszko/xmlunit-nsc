/*
 ******************************************************************
Copyright (c) 2001-2008,2010 Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit.diff;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.transform.Source;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonFilter;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DOMDifferenceEngine;
import net.sf.xmlunit.diff.DefaultNodeMatcher;
import net.sf.xmlunit.diff.DifferenceEngine;
import net.sf.xmlunit.diff.DifferenceEvaluator;
import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.NodeMatcher;
import net.sf.xmlunit.input.CommentLessSource;
import net.sf.xmlunit.input.WhitespaceNormalizedSource;
import net.sf.xmlunit.input.WhitespaceStrippedSource;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Pair;
import net.sf.xmlunit.util.Predicate;

import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.XsltUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Compares and describes any difference between XML documents. Two documents
 * are either: <br />
 * <ul>
 * <li><i>identical</i>: the content and sequence of the nodes in the documents
 * are exactly the same</li>
 * <li><i>similar</i>: the content of the nodes in the documents are the same,
 * but minor differences exist e.g. sequencing of sibling elements, values of
 * namespace prefixes, use of implied attribute values</li>
 * <li><i>different</i>: the contents of the documents are fundamentally
 * different</li>
 * </ul>
 * <br />
 * The difference between compared documents is contained in a message buffer
 * held in this class, accessible either through the <code>appendMessage</code>
 * or <code>toString</code> methods. NB: When comparing documents, the
 * comparison is halted as soon as the status (identical / similar / different)
 * is known with certainty. For a list of all differences between the documents
 * an instance of {@link DetailedDiff the DetailedDiff class} can be used
 * instead. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 */
public class Diff {
    private final XmlUnitProperties properties;

    private final Document controlDoc;
    private final Document testDoc;
    private boolean similar = true;
    private boolean identical = true;
    private boolean compared = false;
    protected final DifferenceEngine differenceEngine;
    private DifferenceEvaluator differenceEvaluator;
    private ElementSelector elementSelector;

    private ControllingListener controllingListener;

    /**
     * Construct a Diff that compares the XML in two Documents using a specific
     * DifferenceEngine and ElementQualifier
     */
    Diff(DiffBuilder builder) {
        this.properties = builder.properties.clone();
        this.controlDoc = getManipulatedDocument(builder.controlDocument);
        this.testDoc = getManipulatedDocument(builder.testDocument);
        this.elementSelector = builder.elementSelector;
        if (builder.differenceEngine == null) {
            this.differenceEngine = new DOMDifferenceEngine();
        } else {
            this.differenceEngine = builder.differenceEngine;
        }
        // TODO
        initEngine();
    }

    /**
     * Construct a Diff from a prototypical instance. Used by extension
     * subclasses
     * 
     * @param prototype
     *            a prototypical instance
     */
    protected Diff(Diff prototype) {
        // TODO clone?
        this.properties = prototype.properties.clone();
        this.controlDoc = getManipulatedDocument(prototype.controlDoc);
        this.testDoc = getManipulatedDocument(prototype.testDoc);
        this.elementSelector = prototype.elementSelector;
        this.differenceEngine = prototype.differenceEngine;
        this.differenceEvaluator = prototype.differenceEvaluator;
        initEngine();
    }

    // TODO bleeee
    private void initEngine() {
        controllingListener = new ControllingListener();
        differenceEngine.addComparisonListener(controllingListener);
    }

    protected final void removeControllingListener() {
        differenceEngine.removeComparisonListener(controllingListener);
    }

    /**
     * If {@link XmlUnit#getIgnoreWhitespace whitespace is ignored} in
     * differences then manipulate the content to strip the redundant whitespace
     * 
     * @param originalDoc
     *            a document making up one half of this difference
     * @return the original document with redundant whitespace removed if
     *         differences ignore whitespace
     */
    private Document getWhitespaceManipulatedDocument(Document originalDoc) {
        return properties.getIgnoreWhitespace()
                ? new XsltUtils(properties).getWhitespaceStrippedDocument(originalDoc)
                : originalDoc;
    }

    /**
     * Manipulates the given document according to the setting in the XMLUnit
     * class.
     * 
     * <p>
     * This may involve:
     * </p>
     * <ul>
     * <li>{@link XmlUnit.setIgnoreWhitespace stripping redundant whitespace}</li>
     * <li>{@link XmlUnit.setIgnoreComments stripping comments}</li>
     * <li>{@link XmlUnit.setNormalize normalizing Text nodes}</li>
     * </ul>
     * 
     * @param orig
     *            a document making up one half of this difference
     * @return manipulated doc
     */
    private Document getManipulatedDocument(Document orig) {
        return getNormalizedDocument(getCommentlessDocument(getWhitespaceManipulatedDocument(orig)));
    }

    /**
     * Removes all comment nodes if {@link XmlUnit.getIgnoreComments comments
     * are ignored}.
     * 
     * @param originalDoc
     *            a document making up one half of this difference
     * @return manipulated doc
     */
    private Document getCommentlessDocument(Document orig) {
        if (!properties.getIgnoreComments()) {
            return orig;
        }

        return new XsltUtils(properties).getStripCommentsTransform(orig).toDocument();
    }

    private Document getNormalizedDocument(Document orig) {
        if (!properties.getNormalize()) {
            return orig;
        }
        Document d = (Document) orig.cloneNode(true);
        d.normalize();
        return d;
    }

    /**
     * Top of the recursive comparison execution tree
     */
    protected final void compare() {
        if (compared) {
            return;
        }
        DefaultNodeMatcher nodeMatcher = new DefaultNodeMatcher(elementSelector);
        if (properties.getCompareUnmatched()) {
            differenceEngine.setNodeMatcher(new CompareUnmatchedNodeMatcher(nodeMatcher));
        } else {
            differenceEngine.setNodeMatcher(nodeMatcher);
        }

        // TODO this must be set higher
        differenceEngine.setIgnoreAttributeOrder(properties.getIgnoreAttributeOrder());

        // TODO
        DifferenceEvaluator controllingEvaluator = createControllingEvaluator();
        DifferenceEvaluator evaluator = new IgnorantDifferenceEvaluator(controllingEvaluator);
        differenceEngine.setDifferenceEvaluator(evaluator);
        Source ctrlSource = Input.fromNode(controlDoc).build();
        Source testSource = Input.fromNode(testDoc).build();
        if (properties.getIgnoreComments()) {
            ctrlSource = new CommentLessSource(ctrlSource);
            testSource = new CommentLessSource(testSource);
        }
        if (properties.getNormalizeWhitespace()) {
            ctrlSource = new WhitespaceNormalizedSource(ctrlSource);
            testSource = new WhitespaceNormalizedSource(testSource);
        }
        if (properties.getIgnoreWhitespace()) {
            ctrlSource = new WhitespaceStrippedSource(ctrlSource);
            testSource = new WhitespaceStrippedSource(testSource);
        }

        differenceEngine.setFilter(new ComparisonFilter() {
            @Override
            public boolean ignore(Comparison comparison) {
                switch (comparison.getType()) {
                    case XML_ENCODING:
                    case XML_STANDALONE:
                    case XML_VERSION:
                        return true;
                    default:
                        return false;
                }
            }
        });

        differenceEngine.compare(ctrlSource, testSource);
        compared = true;
    }

    /**
     * Return the result of a comparison. Two documents are considered to be
     * "similar" if they contain the same elements and attributes regardless of
     * order.
     */
    public boolean similar() {
        compare();
        return similar;
    }

    /**
     * Return the result of a comparison. Two documents are considered to be
     * "identical" if they contain the same elements and attributes in the same
     * order.
     */
    public boolean identical() {
        compare();
        return identical;
    }

    protected DifferenceEvaluator createControllingEvaluator() {
        return new ControllingEvaluator();
    }

    protected void stopComparison() {
        differenceEngine.stop();
    }

    // TODO
    protected class ControllingListener implements ComparisonListener {
        @Override
        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
            if (ignoreComparison(comparison, outcome)) {
                return;
            }
            setVardict(comparison, outcome);
            boolean critical = isCritical(comparison, outcome);
            if (critical) {
                stopComparison();
            }
        }
    }

    // TODO replace this with something more readable
    protected class ControllingEvaluator implements DifferenceEvaluator {
        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            if (ignoreComparison(comparison, outcome)) {
                return outcome;
            }

            ComparisonResult evaluatedOutcome = outcome;
            if (differenceEvaluator != null) {
                evaluatedOutcome = differenceEvaluator.evaluate(comparison, outcome);
            }

            // setVardict(comparison, evaluatedOutcome);

            // boolean critical = isCritical(comparison, evaluatedOutcome);
            // if (critical) {
            // stopComparison();
            // }
            return evaluatedOutcome;
        }
    }

    private boolean ignoreComparison(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) {
            return true;
        }

        Node controlTarget = comparison.getControlDetails().getTarget();
        if ((comparesChildrenListLength(comparison) && isDocument(controlTarget))
                ||
                (comparison.getType() == ComparisonType.CHILD_LOOKUP
                &&
                (isNonElementDocumentChild(comparison.getControlDetails())
                || isNonElementDocumentChild(comparison.getTestDetails()))
                )) {
            return true;
        }
        return false;
    }

    private boolean comparesChildrenListLength(Comparison comparison) {
        return comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH;
    }

    private boolean isDocument(Node controlTarget) {
        return controlTarget instanceof Document;
    }

    private static boolean isNonElementDocumentChild(Comparison.Detail detail) {
        return isNode(detail)
                && isNotElement(detail)
                && hasDocumentParent(detail);
    }

    private static boolean hasDocumentParent(Comparison.Detail detail) {
        return detail.getTarget().getParentNode() instanceof Document;
    }

    private static boolean isNotElement(Comparison.Detail detail) {
        return !(detail.getTarget() instanceof Element);
    }

    private static boolean isNode(Comparison.Detail detail) {
        return detail != null && detail.getTarget() instanceof Node;
    }

    private boolean isCritical(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.CRITICAL) {
            return true;
        }
        boolean isNotRecoverable = !comparison.getType().isRecoverable();
        if (outcome == ComparisonResult.DIFFERENT && isNotRecoverable) {
            return true;
        }
        return false;
    }

    private void setVardict(Comparison comparison, ComparisonResult outcome) {
        boolean isRecoverable = comparison.getType().isRecoverable();
        switch (outcome) {
            case SIMILAR:
                identical = false;
                break;
            case DIFFERENT:
                identical = false;
                if (!isRecoverable) {
                    similar = false;
                }
                break;
            case CRITICAL:
                identical = false;
                similar = false;
                break;
            default:
                break;
        }
    }

    /**
     * Override the <code>DifferenceListener</code> used to determine how to
     * handle differences that are found.
     * 
     * @param evaluator
     *            the DifferenceListener instance to delegate handling to.
     */
    public void overrideDifferenceEvaluator(DifferenceEvaluator evaluator) {
        this.differenceEvaluator = evaluator;
    }

    /**
     * Override the <code>ElementQualifier</code> used to determine which
     * control and test nodes are comparable for this difference comparison.
     * 
     * @param selector
     *            the ElementQualifier instance to delegate to.
     */
    public void overrideElementSelector(ElementSelector selector) {
        this.elementSelector = selector;
    }

    public DifferenceEngine getDifferenceEngine() {
        return differenceEngine;
    }

    public static DiffBuilder newDiff(@Nullable XmlUnitProperties properties) {
        return new DiffBuilder(properties);
    }

    private static class CompareUnmatchedNodeMatcher implements NodeMatcher {
        private final NodeMatcher matcher;

        private CompareUnmatchedNodeMatcher(NodeMatcher nested) {
            this.matcher = nested;
        }

        @Override
        public List<Pair<Node>> match(Iterable<Node> controlNodes, Iterable<Node> testNodes) {

            final Map<Node, Node> matches = findMatches(controlNodes, testNodes);
            Iterable<Node> unmatchedTestNodes = findUnmatchedTestNodes(testNodes, matches);
            final List<Pair<Node>> result = new LinkedList<Pair<Node>>();

            for (Node controlNode : controlNodes) {
                if (matches.containsKey(controlNode)) {
                    result.add(Pair.of(controlNode, matches.get(controlNode)));
                } else {
                    for (Node unmatchedTestNode : unmatchedTestNodes) {
                        matches.put(controlNode, unmatchedTestNode);
                        result.add(Pair.of(controlNode, unmatchedTestNode));
                    }
                }
            }
            return result;
        }

        private Iterable<Node> findUnmatchedTestNodes(Iterable<Node> testNodes, final Map<Node, Node> matches) {
            return Linqy.filter(testNodes, new Predicate<Node>() {
                @Override
                public boolean matches(Node testNode) {
                    return !matches.containsValue(testNode);
                }
            });
        }

        private Map<Node, Node> findMatches(Iterable<Node> controlNodes, Iterable<Node> testNodes) {
            Map<Node, Node> map = new LinkedHashMap<Node, Node>();
            List<Pair<Node>> matches = matcher.match(controlNodes, testNodes);
            for (Pair<Node> match : matches) {
                map.put(match.getFirst(), match.getSecond());
            }
            return map;
        }
    }

    // TODO replace it with something more suitable
    private class IgnorantDifferenceEvaluator implements DifferenceEvaluator {
        private final DifferenceEvaluator delegate;

        public IgnorantDifferenceEvaluator(DifferenceEvaluator delegate) {
            this.delegate = delegate;
        }

        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            if (swallowComparison(comparison, outcome)) {
                return outcome;
            }
            return delegate.evaluate(comparison, outcome);
        }

        private boolean swallowComparison(Comparison comparison, ComparisonResult outcome) {
            if (properties.getIgnoreDiffBetweenTextAndCDATA() && comparesNodeTypes(comparison)) {
                int controlValue = (Short) comparison.getControlDetails().getValue();
                int testValue = (Short) comparison.getTestDetails().getValue();
                return isTextOrCdataNode(controlValue) && isTextOrCdataNode(testValue);
            }
            return false;
        }

        private final Short TEXT_TYPE = Node.TEXT_NODE;
        private final Short CDATA_TYPE = Node.CDATA_SECTION_NODE;

        private boolean isTextOrCdataNode(int nodeType) {
            return TEXT_TYPE.equals(nodeType) || CDATA_TYPE.equals(nodeType);
        }

        private boolean comparesNodeTypes(Comparison comparison) {
            return comparison.getType() == ComparisonType.NODE_TYPE;
        }
    }
}
