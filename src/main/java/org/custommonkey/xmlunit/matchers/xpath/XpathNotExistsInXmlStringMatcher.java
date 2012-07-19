package org.custommonkey.xmlunit.matchers.xpath;

import java.io.IOException;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Assert that a specific XPath does not exists in some given XML *
 */
public class XpathNotExistsInXmlStringMatcher extends AbstractXmlUnitMatcher<String> {

    private String expectedXpath;

    public XpathNotExistsInXmlStringMatcher(String expectedXpath) {
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        description.appendText("Xml with not containing given xpath");

    }

    @Override
    public boolean matchesSafely(String actualXmlDoc) {
        Document inDocument;
        int matches;

        try {
            inDocument = XMLUnit.buildControlDocument(actualXmlDoc);

            XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
            NodeList nodeList = simpleXpathEngine.getMatchingNodes(expectedXpath, inDocument);
            matches = nodeList.getLength();
        } catch (SAXException e) {
            throw new IllegalStateException("Couldn't check xpath", e);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't check xpath", e);
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }

        return !(matches > 0);
    }

    @Factory
    public static XpathNotExistsInXmlStringMatcher notContainsXpath(String expectedXpath) {
        return new XpathNotExistsInXmlStringMatcher(expectedXpath);
    }

}
