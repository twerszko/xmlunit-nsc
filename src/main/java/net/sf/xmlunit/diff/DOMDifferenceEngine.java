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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.transform.Source;

import net.sf.xmlunit.diff.commands.CompareNodeCommand;
import net.sf.xmlunit.diff.commands.ComparisonCommand;
import net.sf.xmlunit.diff.commands.ComparisonCommandBase;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Convert;
import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.util.Linqy;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Node;

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

    @VisibleForTesting
    void compareNodes(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
        new CompareNodeAndChildrenCommand(getComparisonPerformer(), control, test).execute();
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
    private class CompareNodeAndChildrenCommand extends ComparisonCommandBase<Node> {

        public CompareNodeAndChildrenCommand(ComparisonPerformer compPerformer,
                NodeAndXpathCtx<Node> control,
                NodeAndXpathCtx<Node> test) {
            super(compPerformer, control, test);
        }

        /**
         * Matches nodes of two node lists and invokes compareNode on each pair.
         * 
         * <p>
         * Also performs CHILD_LOOKUP comparisons for each node that couldn't be
         * matched to one of the "other" list.
         * </p>
         */
        private void executeChildrenComparison() {
            if (getControl().getNode().getNodeType() == Node.ATTRIBUTE_NODE) {
                return;
            }

            final XPathContext controlContext = getControl().getXpathCtx();
            final XPathContext testContext = getTest().getXpathCtx();

            Iterable<Node> controlChildren = getFilteredChildNodes(getControl().getNode());
            Iterable<Node> testChildren = getFilteredChildNodes(getTest().getNode());

            controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
            testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

            Queue<Comparison> comparisons = new LinkedList<Comparison>();
            Iterable<Map.Entry<Node, Node>> matches =
                    getNodeMatcher().match(controlChildren, testChildren);

            List<Node> controlList = Linqy.asList(controlChildren);
            List<Node> testList = Linqy.asList(testChildren);
            Set<Node> seen = new HashSet<Node>();
            for (Map.Entry<Node, Node> pair : matches) {
                Node controlNode = pair.getKey();
                Node testNode = pair.getValue();
                seen.add(controlNode);
                seen.add(testNode);
                int controlIndex = controlList.indexOf(controlNode);
                int testIndex = testList.indexOf(testNode);

                controlContext.navigateToChild(controlIndex);
                testContext.navigateToChild(testIndex);
                try {
                    executeComparison(new Comparison(
                            ComparisonType.CHILD_NODELIST_SEQUENCE,
                            NodeAndXpathCtx.from(controlNode, controlContext), controlIndex,
                            NodeAndXpathCtx.from(testNode, testContext), testIndex));
                    if (isInterrupted()) {
                        return;
                    }

                    // TODO
                    ComparisonCommand command = new CompareNodeAndChildrenCommand(compPerformer,
                            NodeAndXpathCtx.from(controlNode, controlContext),
                            NodeAndXpathCtx.from(testNode, testContext));
                    command.execute();
                    if (command.isInterrupted()) {
                        setInterrupted(true);
                        return;
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
                        comparisons.add(new Comparison(
                                ComparisonType.CHILD_LOOKUP,
                                controlList.get(i), getXPath(controlContext), controlList.get(i).getNodeName(),
                                null, null, null));
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
                        comparisons.add(new Comparison(
                                ComparisonType.CHILD_LOOKUP,
                                null, null, null,
                                testList.get(i), getXPath(testContext), testList.get(i).getNodeName()));
                    } finally {
                        testContext.navigateToParent();
                    }
                }
            }
            executeComparisons(comparisons);
        }

        @Override
        protected void executeInternal() {
            // TODO
            Queue<Comparison> comparisons = new LinkedList<Comparison>();
            comparisons.addAll(new CompareNodeCommand(
                    compPerformer, properties.getIgnoreAttributeOrder(),
                    getControl(), getTest())
                    .provideComparisons()
                    );
            executeComparisons(comparisons);
            if (isInterrupted()) {
                return;
            }

            executeChildrenComparison();
        }

        private Iterable<Node> getFilteredChildNodes(Node parentNode) {
            return Linqy.filter(new IterableNodeList(parentNode.getChildNodes()), INTERESTING_NODES);
        }

        @Override
        public Queue<Comparison> provideComparisons() {
            throw new UnsupportedOperationException(
                    "This operation is unsupported for " + this.getClass().getSimpleName() + "!");
        }
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
}
