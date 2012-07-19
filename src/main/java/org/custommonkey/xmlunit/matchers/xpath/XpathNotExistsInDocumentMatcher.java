package org.custommonkey.xmlunit.matchers.xpath;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Assert that a specific XPath exists in some given XML document *
 */
public class XpathNotExistsInDocumentMatcher extends AbstractXmlUnitMatcher<Document> {

    private String expectedXpath;

    public XpathNotExistsInDocumentMatcher(String expectedXpath) {
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        description.appendText("Xml document containing given xpath");

    }

    @Override
    public boolean matchesSafely(Document actualXmlDoc) {

        NodeList nodeList;
        try {
            XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
            nodeList = simpleXpathEngine.getMatchingNodes(expectedXpath, actualXmlDoc);
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }
        int matches = nodeList.getLength();

        return matches > 0;
    }

    @Factory
    public static XpathNotExistsInDocumentMatcher documentContainsXpath(String expectedXpath) {
        return new XpathNotExistsInDocumentMatcher(expectedXpath);
    }

}
