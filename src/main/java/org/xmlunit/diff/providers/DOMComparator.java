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
package org.xmlunit.diff.providers;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.NodeMatcher;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xmlunit.util.IterableNodeList;
import org.xmlunit.util.Linqy;
import org.xmlunit.util.Pair;
import org.xmlunit.util.Predicate;

/**
 * Recursively compares two XML nodes.
 * 
 * <p>
 * Performs comparisons common to all node types, then performs the node type
 * specific comparisons and finally recurses into the node's child lists.
 * </p>
 */
public class DOMComparator extends Comparator {

    private final ComparisonProviders providers;

    public DOMComparator(ComparisonProviders providers) {
        this.providers = providers;
    }

    public void compare(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
        setInterrupted(false);
        Comparisons comparisons = provideNodeComparisons(control, test);
        executeComparisons(comparisons);
        if (isInterrupted()) {
            return;
        }

        executeChildrenComparison(control, test);
    }

    private Comparisons provideNodeComparisons(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
        Node controlNode = control.getNode();
        Node testNode = test.getNode();

        Comparisons comparisons = new Comparisons();

        comparisons.add(Comparison.ofType(ComparisonType.NODE_TYPE)
                .between(control, controlNode.getNodeType())
                .and(test, testNode.getNodeType()));

        comparisons.addAll(providers.getNamespaceComparisonProvider().provideComparisons(control, test));

        if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
            comparisons.addAll(provideChildrenNumberComparisons(control, test));
        }

        comparisons.addAll(provideNodeTypeSpecificComparison(control, test));

        return comparisons;
    }

    /**
     * Matches nodes of two node lists and invokes compareNode on each pair.
     * 
     * <p>
     * Also performs CHILD_LOOKUP comparisons for each node that couldn't be
     * matched to one of the "other" list.
     * </p>
     */
    private void executeChildrenComparison(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
        if (control.getNode().getNodeType() == Node.ATTRIBUTE_NODE) {
            return;
        }

        final XPathContext controlContext = control.getXpathCtx();
        final XPathContext testContext = test.getXpathCtx();

        List<Node> controlChildren = getFilteredChildren(control.getNode());
        List<Node> testChildren = getFilteredChildren(test.getNode());

        controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
        testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

        Comparisons comparisons = new Comparisons();

        NodeMatcher nodeMatcher = providers.getNodeMatcher();
        List<Pair<Node>> matches = nodeMatcher.match(controlChildren, testChildren);
        Comparisons sequenceComparisons = new Comparisons();

        for (Pair<Node> pair : matches) {
            Node controlNode = pair.getFirst();
            Node testNode = pair.getSecond();

            int controlIndex = controlChildren.indexOf(controlNode);
            int testIndex = testChildren.indexOf(testNode);

            controlContext.navigateToChild(controlIndex);
            testContext.navigateToChild(testIndex);

            NodeAndXpath<Node> controlChild = NodeAndXpath.from(controlNode, controlContext);
            NodeAndXpath<Node> testChild = NodeAndXpath.from(testNode, testContext);

            sequenceComparisons.add(Comparison.ofType(ComparisonType.CHILD_NODELIST_SEQUENCE)
                    .between(controlChild, controlIndex)
                    .and(testChild, testIndex));

            testContext.navigateToParent();
            controlContext.navigateToParent();
        }

        executeComparisons(sequenceComparisons);

        for (Pair<Node> pair : matches) {
            Node controlNode = pair.getFirst();
            Node testNode = pair.getSecond();

            int controlIndex = controlChildren.indexOf(controlNode);
            int testIndex = testChildren.indexOf(testNode);

            controlContext.navigateToChild(controlIndex);
            testContext.navigateToChild(testIndex);

            NodeAndXpath<Node> controlChild = NodeAndXpath.from(controlNode, controlContext);
            NodeAndXpath<Node> testChild = NodeAndXpath.from(testNode, testContext);

            executeRecursion(controlChild, testChild);
            if (isInterrupted()) {
                return;
            }
            testContext.navigateToParent();
            controlContext.navigateToParent();
        }

        Set<Node> seenControlChildren = Linqy.asSet(Pair.getFirstElements(matches));
        List<NodeAndXpath<Node>> unseenControlChildren =
                findUnseenNodes(controlChildren, controlContext, seenControlChildren);
        for (NodeAndXpath<Node> controlChild : unseenControlChildren) {
            comparisons.add(Comparison.ofType(ComparisonType.CHILD_LOOKUP)
                    .between(controlChild, controlChild.getNode().getNodeName())
                    .and(null, null));
        }

        Set<Node> seenTestChildren = Linqy.asSet(Pair.getSecondElements(matches));
        List<NodeAndXpath<Node>> unseenTestChildren = findUnseenNodes(testChildren, testContext, seenTestChildren);
        for (NodeAndXpath<Node> testChild : unseenTestChildren) {
            comparisons.add(Comparison.ofType(ComparisonType.CHILD_LOOKUP)
                    .between(null, null)
                    .and(testChild, testChild.getNode().getNodeName()));
        }

        executeComparisons(comparisons);
    }

    private void executeRecursion(NodeAndXpath<Node> controlChild, NodeAndXpath<Node> testChild) {
        compare(controlChild, testChild);
    }

    private List<NodeAndXpath<Node>> findUnseenNodes(List<Node> nodeList, XPathContext context, Set<Node> seenNodes) {
        List<NodeAndXpath<Node>> unseen = new LinkedList<NodeAndXpath<Node>>();

        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if (seenNodes.contains(node)) {
                continue;
            }

            context.navigateToChild(i);
            unseen.add(NodeAndXpath.from(node, context));
            context.navigateToParent();
        }

        return unseen;
    }

    private Comparisons provideChildrenNumberComparisons(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
        Node controlNode = control.getNode();
        Node testNode = test.getNode();

        List<Node> controlChildren = getFilteredChildren(controlNode);
        List<Node> testChildren = getFilteredChildren(testNode);

        Comparisons comparisons = new Comparisons();

        int controlChildrenCount = controlChildren.size();
        int testChildrenCount = testChildren.size();
        if (controlChildrenCount > 0 && testChildrenCount > 0) {
            comparisons.add(Comparison.ofType(ComparisonType.CHILD_NODELIST_LENGTH)
                    .between(control, controlChildrenCount)
                    .and(test, testChildrenCount));
        } else {
            comparisons.add(Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
                    .between(control, controlChildrenCount > 0)
                    .and(test, testChildrenCount > 0));
        }
        return comparisons;
    }

    /**
     * Dispatches to the node type specific comparison if one is defined for the
     * given combination of nodes.
     */
    private Comparisons provideNodeTypeSpecificComparison(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {

        Node controlNode = control.getNode();
        Node testNode = test.getNode();

        XPathContext controlContext = control.getXpathCtx();
        XPathContext testContext = test.getXpathCtx();

        Comparisons comparisons = new Comparisons();

        switch (controlNode.getNodeType()) {
        case Node.CDATA_SECTION_NODE:
        case Node.COMMENT_NODE:
        case Node.TEXT_NODE:
            if (testNode instanceof CharacterData) {
                comparisons.addAll(providers.getCharDataComarisonProvider().provideComparisons(
                        NodeAndXpath.from((CharacterData) controlNode, controlContext),
                        NodeAndXpath.from((CharacterData) testNode, testContext)));
            }
            break;
        case Node.DOCUMENT_NODE:
            if (testNode instanceof Document) {
                comparisons.addAll(providers.getDocumentComparisonProvider().provideComparisons(
                        NodeAndXpath.from((Document) controlNode, controlContext),
                        NodeAndXpath.from((Document) testNode, testContext)));
            }
            break;
        case Node.ELEMENT_NODE:
            if (testNode instanceof Element) {
                comparisons.addAll(providers.getElementComparisonProvider().provideComparisons(
                        NodeAndXpath.from((Element) controlNode, controlContext),
                        NodeAndXpath.from((Element) testNode, testContext)));
            }
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            if (testNode instanceof ProcessingInstruction) {
                comparisons.addAll(providers.getProcInstrComparisonProvider().provideComparisons(
                        NodeAndXpath.from((ProcessingInstruction) controlNode, controlContext),
                        NodeAndXpath.from((ProcessingInstruction) testNode, testContext)));
            }
            break;
        case Node.DOCUMENT_TYPE_NODE:
            if (testNode instanceof DocumentType) {
                comparisons.addAll(providers.getDoctypeComparisonProvider().provideComparisons(
                        NodeAndXpath.from((DocumentType) controlNode, controlContext),
                        NodeAndXpath.from((DocumentType) testNode, testContext)));
            }
            break;
        case Node.ATTRIBUTE_NODE:
            if (testNode instanceof Attr) {
                comparisons.addAll(providers.getAttributeComparisonProvider().provideComparisons(
                        NodeAndXpath.from((Attr) controlNode, controlContext),
                        NodeAndXpath.from((Attr) testNode, testContext)));
            }
            break;
        }

        return comparisons;
    }

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

    private List<Node> getFilteredChildren(Node parentNode) {
        Iterable<Node> filteredChildren =
                Linqy.filter(new IterableNodeList(parentNode.getChildNodes()), INTERESTING_NODES);
        return Linqy.asList(filteredChildren);
    }

    private static final Predicate<Node> INTERESTING_NODES =
            new Predicate<Node>() {
                @Override
                public boolean matches(Node n) {
                    return n.getNodeType() != Node.DOCUMENT_TYPE_NODE;
                }
            };
}
