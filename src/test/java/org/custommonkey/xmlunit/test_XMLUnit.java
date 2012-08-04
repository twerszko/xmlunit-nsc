/*
 ******************************************************************
Copyright (c) 2001-2007, Jeff Martin, Tim Bacon
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

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.diff.DiffBuilder;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.w3c.dom.Document;

/**
 * Test case for XMLUnit
 */
public class test_XMLUnit extends TestCase {

	private XmlUnitProperties properties;

	@Override
	@Before
	public void setUp() {
		properties = new XmlUnitProperties();
	}

	/**
	 * Contructs a new test case.
	 */
	public test_XMLUnit(String name) {
		super(name);
	}

	private String getDocumentBuilderFactoryImplClass() {
		return DocumentBuilderFactory.newInstance().getClass().getName();
	}

	/**
	 * Test overiding the SAX parser used to parse control documents
	 */
	public void testSetControlParser() throws Exception {
		Object before = new DocumentUtils(properties).newControlParser();
		new DocumentUtils(properties).setControlParser(getDocumentBuilderFactoryImplClass());
		assertEquals("should be different", false,
		        before == new DocumentUtils(properties).newControlParser());
	}

	public void testIgnoreWhitespace() throws Exception {
		properties.setIgnoreWhitespace(true);
		String test = "<test>  monkey   </test>";
		String control = "<test>monkey</test>";
		assertEquals("Should be similar", true,
		        new DiffBuilder(properties)
		                .withControlDocument(control)
		                .withTestDocument(test)
		                .build()
		                .similar());

		properties.setIgnoreWhitespace(false);
		assertEquals("Should be different", false,
		        new DiffBuilder(properties)
		                .withControlDocument(control)
		                .withTestDocument(test)
		                .build()
		                .similar());
	}

	/**
	 * Test overiding the SAX parser used to parse test documents
	 */
	public void testSetTestParser() throws Exception {
		Object before = new DocumentUtils(properties).newTestParser();
		new DocumentUtils(properties).setTestParser(getDocumentBuilderFactoryImplClass());
		assertEquals("should be different", false,
		        before == new DocumentUtils(properties).newTestParser());
	}

	public void testSetTransformerFactory() throws Exception {
		Object before = XMLUnit.getTransformerFactory();
		XMLUnit.setTransformerFactory(before.getClass().getName());
		assertEquals("should be different", false,
		        before == XMLUnit.getTransformerFactory());
	}

	public void testStripWhitespaceTransform() throws Exception {
		Document doc = new DocumentUtils(properties).buildTestDocument(
		        test_Constants.XML_WITH_WHITESPACE);
		Transform transform = new XmlUnitBuilder(properties).build().getStripWhitespaceTransform(doc);
		// TODO simpify?
		Diff diff = new DiffBuilder(properties)
		        .withControlDocument(test_Constants.XML_WITHOUT_WHITESPACE)
		        .withTestDocument(transform)
		        .build();
		assertTrue(diff.similar());
	}

	public void testXSLTVersion() {
		XMLUnit xmlUnit = new XmlUnitBuilder().build();

		assertEquals("1.0", xmlUnit.getProperties().getXsltVersion());
		assertEquals(XSLTConstants.XSLT_START, xmlUnit.getXSLTStart());

		xmlUnit = new XmlUnitBuilder().usingXsltVersion("2.0").build();
		assertTrue(xmlUnit.getXSLTStart()
		        .startsWith(XSLTConstants.XSLT_START_NO_VERSION));
		assertTrue(xmlUnit.getXSLTStart().endsWith("\"2.0\">"));

		try {
			new XmlUnitBuilder().usingXsltVersion("foo").build();
			fail("foo is not a number");
		} catch (ConfigurationException expected) {
		}
		try {
			new XmlUnitBuilder().usingXsltVersion("-1.0").build();
			fail("-1.0 is negative");
		} catch (ConfigurationException expected) {
		}
	}
}
