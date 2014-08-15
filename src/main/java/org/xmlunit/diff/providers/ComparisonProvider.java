package org.xmlunit.diff.providers;

import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Node;

public abstract class ComparisonProvider<U extends Node> {
    public abstract Comparisons provideComparisons(NodeAndXpath<U> control, NodeAndXpath<U> test);
}
