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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.xmlunit.util.Linqy;
import org.xmlunit.util.Pair;
import org.xmlunit.util.Predicate;

public class CompareUnmatchedNodeMatcher implements NodeMatcher {
    private final NodeMatcher matcher;

    public CompareUnmatchedNodeMatcher(NodeMatcher nested) {
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
