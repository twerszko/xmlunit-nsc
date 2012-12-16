package net.sf.xmlunit.diff;

import java.util.ArrayList;

public class ListingDifferenceEvaluator implements DifferenceEvaluator {

	private final ArrayList<Comparison> differences = new ArrayList<Comparison>();

	@Override
	public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
		if (outcome == ComparisonResult.DIFFERENT) {
			differences.add(comparison);
		}
		return outcome;
	}

	public ArrayList<Comparison> getDifferences() {
		return differences;
	}
}
