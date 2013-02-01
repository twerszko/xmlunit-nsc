package net.sf.xmlunit.diff.strategies;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;

public abstract class Comparator {
	protected final ComparisonPerformer performer;
	private boolean interrupted;

	public Comparator(ComparisonPerformer compPerformer) {
		this.performer = compPerformer;
	}

	protected void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	public boolean isInterrupted() {
		return interrupted;
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

}
