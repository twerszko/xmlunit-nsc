package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DifferenceEvaluator;

import org.w3c.dom.Node;

public class CollectingDifferenceEvaluator implements DifferenceEvaluator {
	public String expected;
	public String actual;
	public Node control;
	public Node test;
	public ComparisonType comparingWhat = null;
	public boolean different = false;
	public boolean nodesSkipped = false;
	public String controlXpath;
	public String testXpath;

	public ComparisonResult evaluate(Comparison difference, ComparisonResult outcome) {
		assertThat(difference).isNotNull();
		assertThat(difference.getControlDetails()).isNotNull();
		assertThat(difference.getTestDetails()).isNotNull();

		this.expected = String.valueOf(difference.getControlDetails().getValue());
		this.actual = String.valueOf(difference.getTestDetails().getValue());
		this.control = difference.getControlDetails().getTarget();
		this.test = difference.getTestDetails().getTarget();
		this.comparingWhat = difference.getType();
		this.different = !difference.isRecoverable();
		this.controlXpath = difference.getControlDetails().getXpath();
		this.testXpath = difference.getTestDetails().getXpath();
		return ComparisonResult.DIFFERENT;
	}

	public void skippedComparison(Node control, Node test) {
		nodesSkipped = true;
	}
}
