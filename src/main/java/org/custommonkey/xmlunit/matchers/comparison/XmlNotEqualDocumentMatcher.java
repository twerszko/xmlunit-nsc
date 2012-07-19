package org.custommonkey.xmlunit.matchers.comparison;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;

/**
 * Compares two XML documents end checks if these are not similar.
 * 
 */
public class XmlNotEqualDocumentMatcher extends AbstractXmlUnitMatcher<Document> {

    private Document expectedDoc;

    public XmlNotEqualDocumentMatcher(Document expectedDoc) {
        this.expectedDoc = expectedDoc;
    }

    public void describeTo(Description description) {
        description.appendText("Xml document not equal to given document");

    }

    @Override
    public boolean matchesSafely(Document actualDoc) {
        XMLUnit xmlUnit = getXmlUnit();

        Diff diff = xmlUnit.compareXML(expectedDoc, actualDoc);
        return !diff.similar();
    }

    @Factory
    public static XmlNotEqualDocumentMatcher notEqualToXmlDocument(Document expectedDoc) {
        return new XmlNotEqualDocumentMatcher(expectedDoc);
    }

}
