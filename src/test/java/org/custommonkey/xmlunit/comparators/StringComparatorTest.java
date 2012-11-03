package org.custommonkey.xmlunit.comparators;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class StringComparatorTest {

    @Test
    public void should_normalize_whitespace() {
        testNormalizeWhitespace("a\rb", "a b");
        testNormalizeWhitespace("a  b", "a b");
        testNormalizeWhitespace("a\rb c\nd\te\r\n   \tf", "a b c d e f");
    }

    public void testNormalizeWhitespace(String given, String expected) {
        // given - when
        String normalized = StringComparator.normalizeWhitespace(given);

        // then
        assertThat(normalized).isEqualTo(expected);
    }

    // TODO more tests
}
