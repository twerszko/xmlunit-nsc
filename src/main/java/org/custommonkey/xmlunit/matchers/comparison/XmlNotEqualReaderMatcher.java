package org.custommonkey.xmlunit.matchers.comparison;

import java.io.IOException;
import java.io.Reader;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.xml.sax.SAXException;

/**
 * Assert that two XML documents are not similar.
 * 
 */
public class XmlNotEqualReaderMatcher extends AbstractXmlUnitMatcher<Reader> {

    private Reader expectedReader;

    public XmlNotEqualReaderMatcher(Reader expectedReader) {
        this.expectedReader = expectedReader;
    }

    public void describeTo(Description description) {
        description.appendText("Reader containing xml not equal to xml contained in given reader");

    }

    @Override
    public boolean matchesSafely(Reader actualReader) {
        XMLUnit xmlUnit = getXmlUnit();

        Diff diff;
        try {
            diff = xmlUnit.compareXML(expectedReader, actualReader);
        } catch (SAXException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        }
        return !diff.similar();
    }

    @Factory
    public static XmlNotEqualReaderMatcher notEqualToXmlReader(Reader expectedReader) {
        return new XmlNotEqualReaderMatcher(expectedReader);
    }

}
