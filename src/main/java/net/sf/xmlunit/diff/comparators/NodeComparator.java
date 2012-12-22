package net.sf.xmlunit.diff.comparators;

import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Node;

public abstract class NodeComparator<T extends Node> {
    protected final ComparisonPerformer compPerformer;

    public NodeComparator(ComparisonPerformer compPerformer) {
        this.compPerformer = compPerformer;
    }

    public abstract ComparisonResult compare(NodeAndXpathCtx<T> control, NodeAndXpathCtx<T> test);
}
