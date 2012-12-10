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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.custommonkey.xmlunit.util.XsltUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLUnitTest {

	private static final String XML_WITH_WHITESPACE =
	        "<aakture>  <node>text</node>\t<node>text2</node> \n </aakture>";

	private static final String XML_WITHOUT_WHITESPACE =
	        "<aakture><node>text</node><node>text2</node></aakture>";

	private XmlUnitProperties properties;

	@Before
	public void setUp() {
		properties = new XmlUnitProperties();
	}

	/**
	 * Test overiding the SAX parser used to parse control documents
	 */
	@Test
	public void testSetControlParser() throws Exception {
		// given
		DocumentBuilder builderBefore = new DocumentUtils(properties).newControlDocumentBuilder();
		DocumentBuilderFactory controlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

		// when
		properties.setControlDocumentBuilderFactoryClass(controlDocumentBuilderFactory.getClass());
		DocumentBuilder builderAfter = new DocumentUtils(properties).newControlDocumentBuilder();

		// then
		assertFalse(builderBefore == builderAfter);
	}

	@Test
	public void testSetDocumentBuilderFactory() throws Exception {
		// given
		DocumentBuilder builderBefore = new DocumentUtils(properties).newTestDocumentBuilder();
		DocumentBuilderFactory testDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

		// when
		properties.setTestDocumentBuilderFactoryClass(testDocumentBuilderFactory.getClass());
		DocumentBuilder builderAfter = new DocumentUtils(properties).newTestDocumentBuilder();

		// then
		assertThat(builderBefore).isNotSameAs(builderAfter);
	}

	// TODO more casses of loading different document builder factories

	@Test
	public void testIgnoreWhitespace() throws Exception {
		properties.setIgnoreWhitespace(true);
		String test = "<test>  monkey   </test>";
		String control = "<test>monkey</test>";
		assertThat(Diff.newDiff(properties)
		        .betweenControlDocument(control)
		        .andTestDocument(test)
		        .build()
		        .similar())
		        .isTrue();

		properties.setIgnoreWhitespace(false);
		assertThat(Diff.newDiff(properties)
		        .betweenControlDocument(control)
		        .andTestDocument(test)
		        .build()
		        .similar())
		        .isFalse();
	}

	@Test
	public void testSetTransformerFactory() throws Exception {
		// given
		TransformerFactory factoryBefore = new XsltUtils(properties).newTransformerFactory();
		Class<? extends TransformerFactory> factoryClass = factoryBefore.getClass();

		// when
		properties.setTransformerFactoryClass(factoryClass);
		TransformerFactory factoryAfter = new XsltUtils(properties).newTransformerFactory();

		// then
		assertThat(factoryBefore).isNotSameAs(factoryAfter);
		assertThat(factoryAfter.getClass().getName()).isEqualTo(factoryClass.getName());

	}

	@Test
	public void testStripWhitespaceTransform() throws Exception {
		Document doc = new DocumentUtils(properties).buildTestDocument(
		        XML_WITH_WHITESPACE);

		Document transformedDocument = new XsltUtils(properties).getStripWhitespaceTransform(doc)
		        .toDocument();
		// TODO simpify?
		Diff diff = Diff.newDiff(properties)
		        .betweenControlDocument(XML_WITHOUT_WHITESPACE)
		        .andTestDocument(transformedDocument)
		        .build();
		assertTrue(diff.similar());
	}

	@Test
	public void should_use_default_xslt_version() {
		// given
		XmlUnitProperties properties = new XmlUnitProperties();

		// when
		XsltUtils xsltUtils = new XsltUtils(properties);

		// then
		assertThat(properties.getXsltVersion()).isEqualTo("1.0");
		assertThat(xsltUtils.getXSLTStart()).isEqualTo(XSLTConstants.XSLT_START);
	}

	@Test
	public void should_use_other_xslt_version() {
		// given
		XmlUnitProperties properties = new XmlUnitProperties();
		properties.setXsltVersion("2.0");

		// when
		XsltUtils xsltUtils = new XsltUtils(properties);

		// then
		assertThat(properties.getXsltVersion()).isEqualTo("2.0");
		assertThat(xsltUtils.getXSLTStart()).startsWith(XSLTConstants.XSLT_START_NO_VERSION);
		assertThat(xsltUtils.getXSLTStart()).endsWith("\"2.0\">");
	}

	@Test(expected = ConfigurationException.class)
	public void should_not_use_incorrect_xslt_version() {
		// given - when
		new XmlUnitBuilder().usingXsltVersion("foo").build();

		// then exception
	}

	@Test(expected = ConfigurationException.class)
	public void should_not_use_negative_xslt_version() {
		// given - when
		new XmlUnitBuilder().usingXsltVersion("-1.0").build();

		// then exception
	}
}
