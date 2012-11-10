package org.custommonkey.xmlunit.diff;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.Comparison.Detail;
import net.sf.xmlunit.diff.ComparisonType;

import org.custommonkey.xmlunit.NodeDescriptor;

public class DifferenceFormater {
	private final Comparison comparison;

	public DifferenceFormater(Comparison comparison) {
		this.comparison = comparison;
	}

	/**
	 * @return a basic representation of the object state and identity and if
	 *         <code>NodeDetail</code> instances are populated append their
	 *         details also
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (this.comparison.getControlDetails().getTarget() == null ||
		        comparison.getTestDetails().getTarget() == null) {
			appendBasicRepresentation(buf);
		} else {
			appendDetailedRepresentation(buf);
		}
		return buf.toString();
	}

	private void appendBasicRepresentation(StringBuilder buf) {
		ComparisonType type = comparison.getType();
		buf.append("Difference (#").append(type).
		        append(") ").append(type.getDescription());
	}

	private void appendDetailedRepresentation(StringBuilder buf) {
		ComparisonType type = comparison.getType();
		Detail controlDetails = comparison.getControlDetails();
		Detail testDetails = comparison.getTestDetails();
		buf.append("Expected ").append(type.getDescription())
		        .append(" '").append(controlDetails.getValue())
		        .append("' but was '").append(testDetails.getValue())
		        .append("' - comparing ");
		NodeDescriptor.appendDetail(buf, controlDetails);
		buf.append(" to ");
		NodeDescriptor.appendDetail(buf, testDetails);
	}
}
