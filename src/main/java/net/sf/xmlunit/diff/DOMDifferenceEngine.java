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

import net.sf.xmlunit.diff.comparators.NodeComparator;
import net.sf.xmlunit.diff.comparators.commands.CompareNodeCommand;
import net.sf.xmlunit.diff.comparators.commands.ComparisonCommand;
import net.sf.xmlunit.diff.comparators.commands.ComparisonCommandBase;
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
        new MasterComparator(getComparisonPerformer()).compare(control, test);
    }

    @VisibleForTesting
    class MasterComparator extends NodeComparator<Node> {

        /**
         * Matches nodes of two node lists and invokes compareNode on each pair.
         * 
         * <p>
         * Also performs CHILD_LOOKUP comparisons for each node that couldn't be
         * matched to one of the "other" list.
         * </p>
         */
        private class CompareNodeAndChildrenCommand extends ComparisonCommandBase<Node> {

            private final Iterable<Node> controlChildren;
            private final Iterable<Node> testChildren;

            public CompareNodeAndChildrenCommand(ComparisonPerformer compPerformer,
                    NodeAndXpathCtx<Node> control, Iterable<Node> controlChildren,
                    NodeAndXpathCtx<Node> test, Iterable<Node> testChildren) {
                super(compPerformer, control, test);
                this.controlChildren = controlChildren;
                this.testChildren = testChildren;
            }

            private void executeChildrenComparison() {
                final XPathContext controlContext = getControl().getXpathCtx();
                final XPathContext testContext = getTest().getXpathCtx();

                Queue<Comparison> comparisons = new LinkedList<Comparison>();
                Iterable<Map.Entry<Node, Node>> matches =
                        getNodeMatcher().match(controlChildren, testChildren);

                List<Node> controlList = Linqy.asList(controlChildren);
                List<Node> testList = Linqy.asList(testChildren);
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
                        executeComparison(new Comparison(
                                ComparisonType.CHILD_NODELIST_SEQUENCE,
                                NodeAndXpathCtx.from(control, controlContext), controlIndex,
                                NodeAndXpathCtx.from(test, testContext), testIndex));
                        if (isInterrupted()) {
                            return;
                        }

                        // TODO

                        executeNodeComparison(
                                NodeAndXpathCtx.from(control, controlContext),
                                NodeAndXpathCtx.from(test, testContext));
                        if (isInterrupted()) {
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

            private void executeNodeComparison(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
                final Node controlNode = control.getNode();
                final Node testNode = test.getNode();

                final XPathContext controlContext = control.getXpathCtx();
                final XPathContext testContext = test.getXpathCtx();

                // TODO
                Queue<Comparison> comparisons = new LinkedList<Comparison>();
                comparisons.addAll(new CompareNodeCommand(
                        compPerformer, properties.getIgnoreAttributeOrder(),
                        control, test)
                        .provideComparisons()
                        );
                executeComparisons(comparisons);
                if (isInterrupted()) {
                    return;
                }

                if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
                    final Iterable<Node> controlChildren =
                            Linqy.filter(new IterableNodeList(controlNode.getChildNodes()), INTERESTING_NODES);
                    final Iterable<Node> testChildren =
                            Linqy.filter(new IterableNodeList(testNode.getChildNodes()), INTERESTING_NODES);

                    controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
                    testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

                    ComparisonCommand command = new CompareNodeAndChildrenCommand(compPerformer,
                            control, controlChildren,
                            test, testChildren);
                    command.execute();
                    if (command.isInterrupted()) {
                        setInterrupted(true);
                    }
                }
            }

            @Override
            protected void executeInternal() {
                executeChildrenComparison();
            }

            @Override
            public Queue<Comparison> provideComparisons() {
                // TODO
                return null;
            }
        }

        public MasterComparator(ComparisonPerformer compPerformer) {
            super(compPerformer);
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
        @Override
        public ComparisonResult compare(final NodeAndXpathCtx<Node> control, final NodeAndXpathCtx<Node> test) {
            final Node controlNode = control.getNode();
            final Node testNode = test.getNode();

            final XPathContext controlContext = control.getXpathCtx();
            final XPathContext testContext = test.getXpathCtx();

            Queue<ComparisonCommand> commands = new LinkedList<ComparisonCommand>();
            commands.add(new CompareNodeCommand(compPerformer, properties.getIgnoreAttributeOrder(), control, test));
            ComparisonResult lastResult = executeCommands(commands);
            // TODO
            if (lastResult == ComparisonResult.CRITICAL) {
                return lastResult;
            }

            if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
                final Iterable<Node> controlChildren =
                        Linqy.filter(new IterableNodeList(controlNode.getChildNodes()), INTERESTING_NODES);
                final Iterable<Node> testChildren =
                        Linqy.filter(new IterableNodeList(testNode.getChildNodes()), INTERESTING_NODES);

                controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
                testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

                commands = new LinkedList<ComparisonCommand>();
                commands.add(new CompareNodeAndChildrenCommand(compPerformer,
                        control, controlChildren,
                        test, testChildren));

            }
            return executeCommands(commands);
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
