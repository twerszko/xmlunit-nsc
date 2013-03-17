package net.sf.xmlunit.diff;

public class DefaultComparisonFilter implements ComparisonFilter {

	@Override
	public boolean ignore(Comparison comparison) {
		return false;
	}

}
