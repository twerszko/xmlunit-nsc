package org.xmlunit.input;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;

public class WhitespaceNormalizedSourceTest {
    private static final String XML_WITH_WHITESPACE =
            "<aakture>  <node>text</node>\t<node>text2</node> \n </aakture>";

    private static final String XML_WITHOUT_WHITESPACE =
            "<aakture><node>text</node><node>text2</node></aakture>";

    @Test
    public void should_normalize_whitespace() throws Exception {
        // given
        Source s = new StreamSource(new StringReader(XML_WITH_WHITESPACE));

        // when
        WhitespaceNormalizedSource wns = new WhitespaceNormalizedSource(s);
        Document doc = Convert.toDocument(wns);
        String string = DocumentUtils.documentToString(doc);

        // then
        assertThat(string).isEqualTo(XML_WITHOUT_WHITESPACE);
    }

    @Test
    public void should_not_modify_source_when_not_whitespace_to_be_normalized() throws Exception {
        // given
        Source s = new StreamSource(new StringReader(XML_WITHOUT_WHITESPACE));

        // when
        WhitespaceNormalizedSource wns = new WhitespaceNormalizedSource(s);
        Document doc = Convert.toDocument(wns);
        String string = DocumentUtils.documentToString(doc);

        // then
        assertThat(string).isEqualTo(XML_WITHOUT_WHITESPACE);
    }
}
