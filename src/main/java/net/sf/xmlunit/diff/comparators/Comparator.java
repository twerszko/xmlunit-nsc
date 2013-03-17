package net.sf.xmlunit.diff.comparators;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.internal.Comparisons;

public abstract class Comparator {
	private boolean interrupted;

	protected final void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	public final boolean isInterrupted() {
		return interrupted;
	}

	public final void executeComparisons(Comparisons comparisons) {
		for (Comparison comparison : comparisons.getAll()) {
			if (ignoreComparison(comparison)) {
				continue;
			}
			executeComparison(comparison);
			if (isInterrupted()) {
				return;
			}
		}
	}

	public final ComparisonResult executeComparison(Comparison comparison) {
		Object controlValue = comparison.getControlDetails().getValue();
		Object testValue = comparison.getTestDetails().getValue();
		boolean equal = controlValue == null ? testValue == null : controlValue.equals(testValue);
		ComparisonResult initialResult = equal ? ComparisonResult.EQUAL : ComparisonResult.DIFFERENT;
		ComparisonResult finalResult = evaluateResult(comparison, initialResult);

		comparisonPerformed(comparison, finalResult);

		if (finalResult == ComparisonResult.CRITICAL) {
			interrupted = true;
		}

		return finalResult;
	}

	protected boolean ignoreComparison(Comparison comparison) {
		return false;
	}

	protected ComparisonResult evaluateResult(Comparison comparison, ComparisonResult result) {
		return result;
	}

	protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {

	}

}
