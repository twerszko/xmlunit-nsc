package net.sf.xmlunit.diff;

public interface ComparisonFilter {
	boolean ignore(Comparison comparison);
}
