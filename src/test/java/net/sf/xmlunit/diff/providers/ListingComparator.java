package net.sf.xmlunit.diff.providers;

import java.util.ArrayList;
import java.util.List;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.providers.Comparator;

public class ListingComparator extends Comparator {
	private final List<Comparison> differences = new ArrayList<Comparison>();

	@Override
	protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {
		if (result == ComparisonResult.DIFFERENT) {
			differences.add(comparison);
		}
	}

	public List<Comparison> getDifferences() {
		return differences;
	}
}
