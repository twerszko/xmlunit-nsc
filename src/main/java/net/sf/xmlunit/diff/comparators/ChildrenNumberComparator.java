package net.sf.xmlunit.diff.comparators;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Predicate;

import org.w3c.dom.Node;

public class ChildrenNumberComparator extends NodeComparator<Node> {

    public ChildrenNumberComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    @Override
    public ComparisonResult compare(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
        Node controlNode = control.getNode();
        Node testNode = test.getNode();

        Iterable<Node> controlChildren =
                Linqy.filter(new IterableNodeList(controlNode.getChildNodes()), INTERESTING_NODES);
        Iterable<Node> testChildren =
                Linqy.filter(new IterableNodeList(testNode.getChildNodes()), INTERESTING_NODES);

        return compare(control, controlChildren, test, testChildren);
    }

    private ComparisonResult compare(
            NodeAndXpathCtx<Node> control, Iterable<Node> controlChildren,
            NodeAndXpathCtx<Node> test, Iterable<Node> testChildren) {

        ComparisonResult lastResult;
        if (Linqy.count(controlChildren) > 0 && Linqy.count(testChildren) > 0) {
            lastResult = compPerformer.performComparison(
                    new Comparison(ComparisonType.CHILD_NODELIST_LENGTH,
                            control, Linqy.count(controlChildren),
                            test, Linqy.count(testChildren)));
        } else {
            lastResult = compPerformer.performComparison(
                    new Comparison(ComparisonType.HAS_CHILD_NODES,
                            control, Linqy.count(controlChildren) > 0,
                            test, Linqy.count(testChildren) > 0));
        }
        return lastResult;
    }

    /**
     * Suppresses document-type nodes.
     */
    protected static final Predicate<Node> INTERESTING_NODES =
            new Predicate<Node>() {
                @Override
                public boolean matches(Node n) {
                    return n.getNodeType() != Node.DOCUMENT_TYPE_NODE;
                }
            };

}
