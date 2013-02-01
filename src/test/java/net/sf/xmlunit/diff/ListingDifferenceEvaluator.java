package net.sf.xmlunit.diff;

import java.util.ArrayList;
import java.util.List;

public class ListingDifferenceEvaluator implements DifferenceEvaluator {

	private final ArrayList<Comparison> differences = new ArrayList<Comparison>();

	@Override
	public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
		if (outcome == ComparisonResult.DIFFERENT || outcome == ComparisonResult.CRITICAL) {
			differences.add(comparison);
		}
		return outcome;
	}

	public List<Comparison> getDifferences() {
		return differences;
	}
}
