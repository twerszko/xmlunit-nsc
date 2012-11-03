package org.custommonkey.xmlunit.comparators;

import java.util.Comparator;

import org.custommonkey.xmlunit.XmlUnitProperties;

public class StringComparator implements Comparator<String> {

    private final XmlUnitProperties properties;

    public StringComparator(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    public int compare(String expected, String actual) {
        if (properties.getIgnoreWhitespace() || properties.getNormalizeWhitespace()) {
            if (expected != null) {
                expected = expected.trim();
            }

            if (actual != null) {
                actual = actual.trim();
            }

            if (properties.getNormalizeWhitespace()) {
                expected = normalizeWhitespace(expected);
                actual = normalizeWhitespace(actual);
            }
        }

        return expected.compareTo(actual);
    }

    /**
     * Replace all whitespace characters with SPACE and collapse consecutive
     * whitespace chars to a single SPACE.
     */
    final static String normalizeWhitespace(String orig) {
        return orig.replaceAll("\\s", " ").replaceAll("\\s+", " ");
    }
}
