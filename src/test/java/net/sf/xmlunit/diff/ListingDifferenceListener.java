package net.sf.xmlunit.diff;

import java.util.ArrayList;
import java.util.List;

public class ListingDifferenceListener implements ComparisonListener {

    private final ArrayList<Comparison> differences = new ArrayList<Comparison>();

    public List<Comparison> getDifferences() {
        return differences;
    }

    @Override
    public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.DIFFERENT || outcome == ComparisonResult.CRITICAL) {
            differences.add(comparison);
        }
    }

}
