package org.custommonkey.xmlunit.matchers.comparison;

import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.xml.sax.SAXException;

/**
 * Compares two XML documents end checks if these are similar.
 */
public class XmlEqualStringMatcher extends AbstractXmlUnitMatcher<String> {

    private String expectedString;

    public XmlEqualStringMatcher(String expectedString) {
        this.expectedString = expectedString;
    }

    public void describeTo(Description description) {
        description.appendText("Xml string equal to " + quote(expectedString));

    }

    @Override
    public boolean matchesSafely(String actualDoc) {
        XMLUnit xmlUnit = getXmlUnit();

        Diff diff;
        try {
            diff = xmlUnit.compareXML(expectedString, actualDoc);
        } catch (SAXException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        }
        return diff.similar();
    }

    @Factory
    public static XmlEqualStringMatcher equalToXmlString(String expectedString) {
        return new XmlEqualStringMatcher(expectedString);
    }

}
