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
 * Assert that a specific XPath exists in some given XML *
 */
public class XpathExistsInXmlStringMatcher extends AbstractXmlUnitMatcher<String> {

	private final String expectedXpath;
	private String actualXml;

	public XpathExistsInXmlStringMatcher(String expectedXpath) {
		this.expectedXpath = expectedXpath;
	}

	public void describeTo(Description description) {
		description.appendText("xpath " + quote(expectedXpath) +
		        " exists in document " + quote(actualXml));

	}

	@Override
	protected void describeMismatchSafely(String item, Description description) {
		description.appendText("xpath " + quote(expectedXpath) +
		        " does not exist in document " + quote(actualXml));
	}

	@Override
	public boolean matchesSafely(String actualXmlString) {
		if (actualXmlString == null || actualXmlString.isEmpty()) {
			throw new IllegalArgumentException("Actual document cannot be null or empty!");
		}

		this.actualXml = actualXmlString;

		Document inDocument;
		int matches;

		try {
			inDocument = XMLUnit.buildControlDocument(actualXmlString);

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

		return matches > 0;
	}

	@Factory
	public static XpathExistsInXmlStringMatcher containsXpath(String expectedXpath) {
		return new XpathExistsInXmlStringMatcher(expectedXpath);
	}

}
