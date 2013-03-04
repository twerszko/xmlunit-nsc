package net.sf.xmlunit.diff.strategies;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Attr;

public class CompareAttributeStrategy extends ComparisonStrategy<Attr> {

	public CompareAttributeStrategy(ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	@Override
	public Comparisons provideComparisons(NodeAndXpath<Attr> control, NodeAndXpath<Attr> test) {
		Attr controlAttr = control.getNode();
		Attr testAttr = test.getNode();

		Comparisons comparisons = new Comparisons();

		comparisons.add(
		        Comparison.ofType(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED)
		                .between(control, controlAttr.getSpecified())
		                .and(test, testAttr.getSpecified()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.ATTR_VALUE)
		                .between(control, controlAttr.getValue())
		                .and(test, testAttr.getValue()));

		return comparisons;
	}

}
