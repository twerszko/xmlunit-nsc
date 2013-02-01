package net.sf.xmlunit.diff.strategies;

import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Node;

public abstract class ComparisonStrategy<U extends Node> extends Comparator {

	public ComparisonStrategy(ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	public abstract Comparisons provideComparisons(NodeAndXpath<U> control, NodeAndXpath<U> test);

	public void execute(NodeAndXpath<U> control, NodeAndXpath<U> test) {
		setInterrupted(false);
		Comparisons comparisons = provideComparisons(control, test);
		if (comparisons == null) {
			comparisons = new Comparisons();
		}
		executeComparisons(comparisons);
	}
}
