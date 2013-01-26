package net.sf.xmlunit.diff.strategies;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Predicate;

import org.w3c.dom.Node;

public abstract class ComparisonStrategyBase<U extends Node> implements ComparisonStrategy<U> {

	protected final ComparisonPerformer performer;
	private boolean interrupted;

	public ComparisonStrategyBase(ComparisonPerformer compPerformer) {
		this.performer = compPerformer;
	}

	public abstract Comparisons provideComparisons(NodeAndXpath<U> control, NodeAndXpath<U> test);

	@Override
	public void compare(NodeAndXpath<U> control, NodeAndXpath<U> test) {
		interrupted = false;
		compareInternal(control, test);
	}

	protected void compareInternal(NodeAndXpath<U> control, NodeAndXpath<U> test) {
		Comparisons comparisons = provideComparisons(control, test);
		if (comparisons == null) {
			comparisons = new Comparisons();
		}
		executeComparisons(comparisons);
	}

	protected final void executeComparisons(Comparisons comparisons) {
		for (Comparison comparison : comparisons.getAll()) {
			executeComparison(comparison);
			if (isInterrupted()) {
				return;
			}
		}
	}

	protected final void executeComparison(Comparison comparison) {
		ComparisonResult result = performer.performComparison(comparison);
		if (result == ComparisonResult.CRITICAL) {
			interrupted = true;
			return;
		}
	}

	protected void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	@Override
	public boolean isInterrupted() {
		return interrupted;
	}

	protected Iterable<Node> getFilteredChildren(Node parentNode) {
		return Linqy.filter(new IterableNodeList(parentNode.getChildNodes()), INTERESTING_NODES);
	}

	private static final Predicate<Node> INTERESTING_NODES =
	        new Predicate<Node>() {
		        @Override
		        public boolean matches(Node n) {
			        return n.getNodeType() != Node.DOCUMENT_TYPE_NODE;
		        }
	        };

}
