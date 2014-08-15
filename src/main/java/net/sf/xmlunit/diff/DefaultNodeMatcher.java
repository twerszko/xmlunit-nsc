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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.util.Linqy;
import org.xmlunit.util.Pair;

/**
 * Strategy that matches control and tests nodes for comparison.
 */
public class DefaultNodeMatcher implements NodeMatcher {
    private final ElementSelector elementSelector;
    private final NodeTypeMatcher nodeTypeMatcher;

    public DefaultNodeMatcher() {
        this(ElementSelectors.Default);
    }

    public DefaultNodeMatcher(ElementSelector selector) {
        this(selector, new DefaultNodeTypeMatcher());
    }

    public DefaultNodeMatcher(ElementSelector selector, NodeTypeMatcher typeMatcher) {
        elementSelector = selector;
        nodeTypeMatcher = typeMatcher;
    }

    @Override
    public List<Pair<Node>> match(Iterable<Node> controlNodes,
            Iterable<Node> testNodes) {

        List<Pair<Node>> matches = new LinkedList<Pair<Node>>();
        List<Node> controlList = Linqy.asList(controlNodes);
        List<Node> testList = Linqy.asList(testNodes);

        final int testSize = testList.size();
        Set<Integer> unmatchedTestIndexes = new TreeSet<Integer>();
        for (int i = 0; i < testSize; i++) {
            unmatchedTestIndexes.add(Integer.valueOf(i));
        }
        final int controlSize = controlList.size();
        Match lastMatch = new Match(null, -1);
        for (int i = 0; i < controlSize; i++) {
            Node control = controlList.get(i);
            Match testMatch = findMatchingNode(control, testList,
                    lastMatch.index, unmatchedTestIndexes);
            if (testMatch != null) {
                unmatchedTestIndexes.remove(testMatch.index);
                matches.add(new Pair<Node>(control, testMatch.node));
            }
        }
        return matches;
    }

    private Match findMatchingNode(
            final Node searchFor,
            final List<Node> searchIn,
            final int indexOfLastMatch,
            final Set<Integer> availableIndexes) {

        final int searchSize = searchIn.size();
        for (int i = indexOfLastMatch + 1; i < searchSize; i++) {
            if (!availableIndexes.contains(Integer.valueOf(i))) {
                continue;
            }
            if (nodesMatch(searchFor, searchIn.get(i))) {
                return new Match(searchIn.get(i), i);
            }
        }
        for (int i = 0; i < indexOfLastMatch; i++) {
            if (!availableIndexes.contains(Integer.valueOf(i))) {
                continue;
            }
            if (nodesMatch(searchFor, searchIn.get(i))) {
                return new Match(searchIn.get(i), i);
            }
        }
        return null;
    }

    private boolean nodesMatch(final Node n1, final Node n2) {
        if (n1 instanceof Element && n2 instanceof Element) {
            return elementSelector.canBeCompared((Element) n1, (Element) n2);
        }
        return nodeTypeMatcher.canBeCompared(n1.getNodeType(),
                n2.getNodeType());
    }

    private class Match {
        private final Node node;
        private final int index;

        private Match(Node match, int index) {
            this.node = match;
            this.index = index;
        }
    }

    public interface NodeTypeMatcher {
        boolean canBeCompared(short controlType, short testType);
    }

    public static class DefaultNodeTypeMatcher implements NodeTypeMatcher {
        private static final short CDATA = Node.TEXT_NODE;
        private static final short TEXT = Node.CDATA_SECTION_NODE;

        @Override
        public boolean canBeCompared(short controlType, short testType) {
            return controlType == testType
                    || (controlType == CDATA && testType == TEXT)
                    || (controlType == TEXT && testType == CDATA);
        }
    }
}
