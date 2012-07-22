/*
 ******************************************************************
Copyright (c) 2001-2011, Jeff Martin, Tim Bacon
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package org.custommonkey.xmlunit;

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXmlDocument;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXmlReader;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXmlString;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXmlDocument;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXmlReader;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXmlString;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notPassesWith;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.passesWith;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.valueIsEqualToXpathValue;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.valueIsNotEqualToXpathValue;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.xpathEvaluatesTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.matchers.XmlUnitMatchers;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class test_XMLTestCase {
	private static final String PREFIX = "foo";
	private static final String TEST_NS = "urn:org.example";
	private static final NamespaceContext NS_CONTEXT;
	static {
		HashMap<String, String> m = new HashMap<String, String>();
		m.put(PREFIX, TEST_NS);
		NS_CONTEXT = new SimpleNamespaceContext(m);
	}

	@After
	public void tearDown() {
		XMLUnit.setXpathNamespaceContext(null);
	}

	private final String[] control = new String[] { "<root/>", "<root></root>", "<root>test</root>",
	        "<root attr=\"test\">test</root>", "<test/>", "<root>test</root>", "<root attr=\"test\"/>",
	        "<root><outer><inner></inner></outer></root>",
	        "<root attr=\"test\"><outer>test<inner>test</inner></outer></root>",
	        "<root attr=\"test\"><outer>test<inner>test</inner></outer></root>" };
	private final String[] test = new String[] { "<fail/>", "<fail/>", "<fail>test</fail>", "<root>test</root>",
	        "<fail/>", "<root>fail</root>", "<root attr=\"fail\"/>", "<root><outer><inner>test</inner></outer></root>",
	        "<root attr=\"test\"><outer>fail<inner>test</inner></outer></root>",
	        "<root attr=\"fail\"><outer>test<inner>test</inner></outer></root>" };

	/**
	 * Test for the compareXML method.
	 */
	@Test
	public void testCompareXMLStrings() throws Exception {
		for (int i = 0; i < control.length; i++) {
			assertThat(control[i], is(equalToXmlString(control[i])));
			assertThat(test[i], is(notEqualToXmlString(control[i])));
		}
	}

	/**
	 * Test the comparision of two files
	 */
	@Test
	public void testXMLEqualsFiles() throws Exception {
		// Bug 956372
		FileReader testFileReader = new FileReader(TestResources.ANIMAL_FILE.getFile());
		FileReader controlFileReader = new FileReader(TestResources.ANIMAL_FILE.getFile());

		assertThat(testFileReader, is(equalToXmlReader(controlFileReader)));

		FileReader testFileReader2 = new FileReader(TestResources.ANIMAL_FILE.getFile());
		FileReader controlReader2 = new FileReader(TestResources.ANIMAL_FILE2.getFile());

		assertThat(testFileReader2, is(notEqualToXmlReader(controlReader2)));

		try {
			assertThat(new FileReader("nosuchfile.xml"),
			        is(notEqualToXmlReader(new FileReader("nosuchfile.xml"))));
		} catch (FileNotFoundException e) {
		}
	}

	/**
	 * Test for the assertXMLEquals method.
	 */
	@Test
	public void testXMLEqualsStrings() throws Exception {
		for (int i = 0; i < control.length; i++) {
			assertThat(control[i], is(XmlUnitMatchers.equalToXmlString(control[i])));
			assertThat(test[i], is(notEqualToXmlString(control[i])));
		}
	}

	/**
	 * Test for the assertXMLEquals method.
	 */
	@Test
	public void testXMLEqualsDocuments() throws Exception {
		Document controlDocument, testDocument;
		for (int i = 0; i < control.length; i++) {
			controlDocument = XMLUnit.buildControlDocument(control[i]);
			assertThat(controlDocument, is(equalToXmlDocument(controlDocument)));
			testDocument = XMLUnit.buildTestDocument(test[i]);
			assertThat(testDocument, is(notEqualToXmlDocument(controlDocument)));
		}
	}

	private static final String xpathValuesControlXML = "<root><outer attr=\"urk\"><inner attr=\"urk\">"
	        + "controlDocument</inner></outer></root>";
	private static final String xpathValuesTestXML = "<root><outer attr=\"urk\"><inner attr=\"ugh\">"
	        + "testDocument</inner></outer></root>";
	private static final String xpathValuesControlXMLNS = addNamespaceToDocument(xpathValuesControlXML);
	private static final String xpathValuesTestXMLNS = addNamespaceToDocument(xpathValuesTestXML);

	@Test
	public void testXpathValuesEqualUsingDocument() throws Exception {
		Document controlDocument = XMLUnit.buildControlDocument(xpathValuesControlXML);
		Document testDocument = XMLUnit.buildTestDocument(xpathValuesTestXML);

		XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
		XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
		assertThat(testXpath1, valueIsEqualToXpathValue(controlXpath1));

		XpathWrapper testXpath2 = new XpathWrapper("//outer/@attr", testDocument);
		XpathWrapper controlXpath2 = new XpathWrapper("//inner/@attr", controlDocument);
		assertThat(testXpath2, valueIsEqualToXpathValue(controlXpath2));

		XpathWrapper testXpath3 = new XpathWrapper("//outer/@attr", controlDocument);
		XpathWrapper controlXpath3 = new XpathWrapper("//inner/text()", controlDocument);
		assertThat(testXpath3, is(valueIsNotEqualToXpathValue(controlXpath3)));

		XpathWrapper testXpath4 = new XpathWrapper("//text()", testDocument);
		XpathWrapper controlXpath4 = new XpathWrapper("//inner/text()", controlDocument);
		assertThat(testXpath4, valueIsNotEqualToXpathValue(controlXpath4));
	}

	@Test
	public void testXpathValuesEqualUsingDocumentNS() throws Exception {
		Document controlDocument = XMLUnit.buildControlDocument(xpathValuesControlXMLNS);
		Document testDocument = XMLUnit.buildTestDocument(xpathValuesTestXMLNS);

		XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
		XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
		assertThat(testXpath1, valueIsNotEqualToXpathValue(controlXpath1));

		XMLUnit.setXpathNamespaceContext(NS_CONTEXT);
		XpathWrapper testXpath2 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
		XpathWrapper controlXpath2 = new XpathWrapper("//text()", controlDocument);
		assertThat(testXpath2, valueIsEqualToXpathValue(controlXpath2));

		XpathWrapper testXpath3 = new XpathWrapper("//" + PREFIX + ":outer/@attr", testDocument);
		XpathWrapper controlXpath3 = new XpathWrapper("//" + PREFIX + ":inner/@attr", controlDocument);
		assertThat(testXpath3, valueIsEqualToXpathValue(controlXpath3));

		XpathWrapper testXpath4 = new XpathWrapper("//" + PREFIX + ":outer/@attr", controlDocument);
		XpathWrapper controlXpath4 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
		assertThat(testXpath4, valueIsNotEqualToXpathValue(controlXpath4));

		XpathWrapper testXpath5 = new XpathWrapper("//text()", testDocument);
		XpathWrapper controlXpath5 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
		assertThat(testXpath5, valueIsNotEqualToXpathValue(controlXpath5));
	}

	@Test
	public void testXpathValuesEqualUsingString() throws Exception {
		Document controlDocument = XMLUnit.buildControlDocument(xpathValuesControlXML);
		Document testDocument = XMLUnit.buildTestDocument(xpathValuesTestXML);

		XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
		XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
		assertThat(testXpath1, valueIsEqualToXpathValue(controlXpath1));

		XpathWrapper testXpath2 = new XpathWrapper("//outer/@attr", testDocument);
		XpathWrapper controlXpath2 = new XpathWrapper("//inner/@attr", controlDocument);
		assertThat(testXpath2, valueIsEqualToXpathValue(controlXpath2));

		XpathWrapper testXpath3 = new XpathWrapper("//outer/@attr", testDocument);
		XpathWrapper controlXpath3 = new XpathWrapper("//inner/text()", controlDocument);
		assertThat(testXpath3, valueIsNotEqualToXpathValue(controlXpath3));

		XpathWrapper testXpath4 = new XpathWrapper("//text()", testDocument);
		XpathWrapper controlXpath4 = new XpathWrapper("//inner/text()", controlDocument);
		assertThat(testXpath4, valueIsNotEqualToXpathValue(controlXpath4));
	}

	@Test
	public void testXpathValuesEqualUsingStringNS() throws Exception {
		Document controlDocument = XMLUnit.buildControlDocument(xpathValuesControlXMLNS);
		Document testDocument = XMLUnit.buildControlDocument(xpathValuesTestXMLNS);

		XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
		XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
		assertThat(testXpath1, valueIsNotEqualToXpathValue(controlXpath1));

		XMLUnit.setXpathNamespaceContext(NS_CONTEXT);

		XpathWrapper testXpath2 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
		XpathWrapper controlXpath2 = new XpathWrapper("//text()", controlDocument);
		assertThat(testXpath2, valueIsEqualToXpathValue(controlXpath2));

		XpathWrapper testXpath3 = new XpathWrapper("//" + PREFIX + ":outer/@attr", testDocument);
		XpathWrapper controlXpath3 = new XpathWrapper("//" + PREFIX + ":inner/@attr", controlDocument);
		assertThat(testXpath3, valueIsEqualToXpathValue(controlXpath3));

		XpathWrapper testXpath4 = new XpathWrapper("//" + PREFIX + ":outer/@attr", controlDocument);
		XpathWrapper controlXpath4 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
		assertThat(testXpath4, valueIsNotEqualToXpathValue(controlXpath4));

		XpathWrapper testXpath5 = new XpathWrapper("//text()", testDocument);
		XpathWrapper controlXpath5 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
		assertThat(testXpath5, valueIsNotEqualToXpathValue(controlXpath5));
	}

	@Test
	public void testXpathEvaluatesTo() throws Exception {
		Document inDocument1 = XMLUnit.buildControlDocument(xpathValuesControlXML);

		assertThat(new XpathWrapper("//outer/@attr", inDocument1), xpathEvaluatesTo("urk"));
		try {
			assertThat(new XpathWrapper("//inner/@attr", inDocument1), xpathEvaluatesTo("yum"));
			fail("Expected assertion to fail #1");
		} catch (AssertionError e) {
		}

		assertThat(new XpathWrapper("count(//@attr)", inDocument1), xpathEvaluatesTo("2"));

		Document inDocument2 = XMLUnit.buildTestDocument(xpathValuesTestXML);
		assertThat(new XpathWrapper("//inner/@attr", inDocument2), xpathEvaluatesTo("ugh"));
		try {
			assertThat(new XpathWrapper("//outer/@attr", inDocument2), xpathEvaluatesTo("yeah"));
			fail("Expected assertion to fail #2");
		} catch (AssertionError e) {
		}

	}

	@Test
	public void testXpathEvaluatesToNS() throws Exception {
		Document inDocument1 = XMLUnit.buildControlDocument(xpathValuesControlXMLNS);
		try {
			assertThat(new XpathWrapper("//outer/@attr", inDocument1), xpathEvaluatesTo("urk"));
			fail("Expected assertion to fail #1");
		} catch (AssertionError e) {
		}

		XMLUnit.setXpathNamespaceContext(NS_CONTEXT);
		assertThat(new XpathWrapper("//" + PREFIX + ":outer/@attr", inDocument1), xpathEvaluatesTo("urk"));
		try {
			assertThat(new XpathWrapper("//" + PREFIX + ":inner/@attr", inDocument1), xpathEvaluatesTo("yum"));
			fail("Expected assertion to fail #2");
		} catch (AssertionError e) {
		}
		assertThat(new XpathWrapper("count(//@attr)", inDocument1), xpathEvaluatesTo("2"));

		Document inDocument2 = XMLUnit.buildTestDocument(xpathValuesTestXMLNS);
		assertThat(new XpathWrapper("//" + PREFIX + ":inner/@attr", inDocument2), xpathEvaluatesTo("ugh"));
		try {
			assertThat(new XpathWrapper("//" + PREFIX + ":outer/@attr", inDocument2), xpathEvaluatesTo("yeah"));
			fail("Expected assertion to fail #3");
		} catch (AssertionError e) {
		}

	}

	@Test
	public void testNodeTest() throws Exception {
		NodeTester tester = new CountingNodeTester(1);

		assertThat(new NodeTest(xpathValuesControlXML), passesWith(tester, Node.TEXT_NODE));
		try {
			assertThat(new NodeTest(xpathValuesControlXML), passesWith(tester, Node.ELEMENT_NODE));
			fail("Expected node test failure #1!");
		} catch (AssertionError e) {
		}

		NodeTest test = new NodeTest(new StringReader(xpathValuesTestXML));
		tester = new CountingNodeTester(4);
		assertThat(test, passesWith(tester, new short[] { Node.TEXT_NODE, Node.ELEMENT_NODE }));
		assertThat(test, notPassesWith(tester, new short[] { Node.TEXT_NODE, Node.COMMENT_NODE }));

		try {
			assertThat(test, notPassesWith(tester, new short[] { Node.TEXT_NODE, Node.ELEMENT_NODE }));
			fail("Expected node test failure #2!");
		} catch (AssertionError e) {
		}

		try {
			assertThat(test, passesWith(tester, new short[] { Node.TEXT_NODE, Node.COMMENT_NODE }));
			fail("Expected node test failure #3!");
		} catch (AssertionError e) {
		}
	}

	@Test
	public void testXMLValid() {
		// see test_Validator class
	}

	// TODO
	// Bug 585555
	@Test
	public void testUnusedNamespacesDontMatter() throws Exception {
		XMLUnit xmlUnit = new XmlUnitBuilder().ignoringWhitespace(true).build();
		String a = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		        "<outer xmlns:NS2=\"http://namespace2/foo\">\n"
		        + "    <inner xmlns:NS2=\"http://namespace2/\">5</inner>\n" +
		        "</outer>\n";

		String b = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		        "<outer xmlns:NS2=\"http://namespace2\">\n"
		        + "    <inner xmlns:NS2=\"http://namespace2/\">5</inner>\n" +
		        "</outer>\n";

		assertThat(b, is(equalToXmlString(a).using(xmlUnit)));
	}

	// Bug 585555
	@Test
	public void testNamespaceMatters() throws Exception {
		XMLUnit xmlUnit = new XmlUnitBuilder().ignoringWhitespace(true).build();

		String a = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		        "<outer xmlns=\"http://namespace2/\">\n"
		        + "</outer>";

		String b = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		        "<outer xmlns=\"http://namespace2\">\n"
		        + "</outer>\n";

		assertThat(b, is(notEqualToXmlString(a).using(xmlUnit)));
	}

	// Bug 741636
	@Test
	public void testXpathCount() throws Exception {
		String html = "<div><p>" + "</p><table><tr><td><p>" + "</p></td><td><p>"
		        + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td></tr><tr><td><p>"
		        + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td><td><p>"
		        + "</p></td></tr><tr><td><p>" + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td><td><p>"
		        + "</p></td><td><p>" + "</p></td></tr><tr><td><p>" + "</p></td><td><p>" + "</p></td><td><p>"
		        + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td></tr><tr><td><p>" + "</p></td><td><p>"
		        + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td><td><p>" + "</p></td></tr></table></div>";

		Document htmlDoc = XMLUnit.buildControlDocument(html);

		assertThat(new XpathWrapper("count(//td)", htmlDoc), XmlUnitMatchers.xpathEvaluatesTo("25"));
	}

	private static String addNamespaceToDocument(String original) {
		int pos = original.indexOf(">");
		return original.substring(0, pos) + " xmlns='" + TEST_NS + "'" + original.substring(pos);
	}

}
