package org.custommonkey.xmlunit.matchers.xpath;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.custommonkey.xmlunit.util.DomUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Assert that a specific XPath does not exist in some given XML document *
 */
public class XpathNotExistsInDocumentMatcher extends AbstractXmlUnitMatcher<Document> {

	private final String expectedXpath;
	private Document actualDocument;

	public XpathNotExistsInDocumentMatcher(String expectedXpath) {
		this.expectedXpath = expectedXpath;
	}

	public void describeTo(Description description) {
		String documentString;
		try {
			documentString = DomUtils.documentToString(actualDocument);
		} catch (TransformerException e) {
			documentString = "";
		}
		description.appendText("xpath " + quote(expectedXpath) +
		        " does not exist in document " + quote(documentString));

	}

	@Override
	protected void describeMismatchSafely(Document item, Description description) {
		String documentString;
		try {
			documentString = DomUtils.documentToString(actualDocument);
		} catch (TransformerException e) {
			documentString = "";
		}
		description.appendText("xpath " + quote(expectedXpath) +
		        " exists in document " + quote(documentString));
	}

	@Override
	public boolean matchesSafely(Document actualXmlDoc) {
		if (actualXmlDoc == null) {
			throw new IllegalArgumentException("Actual document cannot be null!");
		}

		this.actualDocument = actualXmlDoc;

		NodeList nodeList;
		try {
			XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
			nodeList = simpleXpathEngine.getMatchingNodes(expectedXpath, actualXmlDoc);
		} catch (XpathException e) {
			throw new IllegalArgumentException("Invalid xpath!", e);
		}
		int matches = nodeList.getLength();

		return !(matches > 0);
	}

	@Factory
	public static XpathNotExistsInDocumentMatcher documentNotContainsXpath(String expectedXpath) {
		return new XpathNotExistsInDocumentMatcher(expectedXpath);
	}

}
