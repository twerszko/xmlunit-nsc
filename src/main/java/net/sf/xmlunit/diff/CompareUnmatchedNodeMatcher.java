package net.sf.xmlunit.diff;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Pair;
import net.sf.xmlunit.util.Predicate;

import org.w3c.dom.Node;

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