package org.custommonkey.xmlunit.matchers.xpath;

import static junitparams.JUnitParamsRunner.$;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.containsXpath;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.documentContainsXpath;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.documentNotContainsXpath;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notContainsXpath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

@RunWith(JUnitParamsRunner.class)
public class XpathExistsMatcherTest {
	private static final String xpathNodesControlXML =
	        "<trees>" +
	                "<tree evergreen=\"false\">oak</tree>" +
	                "<tree evergreen=\"false\">ash</tree>" +
	                "<tree evergreen=\"true\">scots pine</tree>" +
	                "<tree evergreen=\"true\">spruce</tree>" +
	                "<favourite><!-- is this a tree or a bush?! -->" +
	                "<tree evergreen=\"false\">magnolia</tree>" +
	                "</favourite>" +
	                "<fruit>" +
	                "<apples>" +
	                "<crunchy/>" +
	                "<yum/>" +
	                "<tree evergreen=\"false\">apple</tree></apples>" +
	                "</fruit>" +
	                "</trees>";

	@SuppressWarnings("unused")
	private Object[] provideExistingXpaths() throws Exception {
		return $(
		        "/trees/fruit/apples/yum",
		        "//tree[@evergreen='false']");
	}

	@SuppressWarnings("unused")
	private Object[] provideNotExistingXpaths() throws Exception {
		return $("//tree[@evergreen='idunno']");
	}

	@Test
	@Parameters(method = "provideExistingXpaths")
	public void should_pass_when_xpath_exists_in_document(String xpath) throws Exception {
		// given
		Document controlDoc = XMLUnit.buildControlDocument(xpathNodesControlXML);

		// then
		assertThat(controlDoc, documentContainsXpath(xpath));
	}

	@Test
	@Parameters(method = "provideExistingXpaths")
	public void should_pass_when_xpath_exists_in_xml_string(String xpath) throws Exception {

		// then
		assertThat(xpathNodesControlXML, containsXpath(xpath));
	}

	@Test
	@Parameters(method = "provideNotExistingXpaths")
	public void should_pass_when_assert_fail_on_not_existing_xpath(String xpath) throws Exception {
		// given
		Document controlDoc = XMLUnit.buildControlDocument(xpathNodesControlXML);

		// then
		try {
			assertThat(controlDoc, documentContainsXpath(xpath));
			fail("Xpath does not exist");
		} catch (AssertionError e) {
			// expected
		}
	}

	@Test
	@Parameters(method = "provideNotExistingXpaths")
	public void should_pass_when_assert_fail_on_not_existing_xpath_in_string(String xpath) throws Exception {

		// then
		try {
			assertThat(xpathNodesControlXML, containsXpath(xpath));
			fail("Xpath does not exist");
		} catch (AssertionError e) {
			// expected
		}
	}

	@Test
	@Parameters(method = "provideNotExistingXpaths")
	public void should_pass_when_xpath_not_exists_in_document(String xpath) throws Exception {
		// given
		Document controlDoc = XMLUnit.buildControlDocument(xpathNodesControlXML);

		// then
		assertThat(controlDoc, documentNotContainsXpath(xpath));
	}

	@Test
	@Parameters(method = "provideNotExistingXpaths")
	public void should_pass_when_xpath_not_exists_in_xml_string(String xpath) throws Exception {

		// then
		assertThat(xpathNodesControlXML, notContainsXpath(xpath));
	}

	@Test
	@Parameters(method = "provideExistingXpaths")
	public void should_pass_when_assert_fail_on_existing_xpath(String xpath) throws Exception {
		// given
		Document controlDoc = XMLUnit.buildControlDocument(xpathNodesControlXML);

		// then
		try {
			assertThat(controlDoc, documentNotContainsXpath(xpath));
			fail("Xpath exists");
		} catch (AssertionError e) {
			// expected
		}
	}

	@Test
	@Parameters(method = "provideExistingXpaths")
	public void should_pass_when_assert_fail_on_existing_xpath_in_xml_string(String xpath) throws Exception {

		// then
		try {
			assertThat(xpathNodesControlXML, notContainsXpath(xpath));
			fail("Xpath exists");
		} catch (AssertionError e) {
			// expected
		}
	}

	// bug 1418497
	@Test
	public void should_pass_when_xml_string_contains_xpath() throws Exception {
		String xmlDocument = "<axrtable> <schema name=\"emptySchema\"><relation name=\"\"></relation></schema></axrtable>";
		assertThat(xmlDocument, containsXpath("/axrtable/schema"));
	}
}
