/*
 ******************************************************************
Copyright (c) 200, Jeff Martin, Tim Bacon
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringBufferInputStream;
import java.io.StringReader;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;
import net.sf.xmlunit.TestResources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * JUnit test for Validator Also includes tests for XMLTestCase
 * <code>assertValidXML</code> methods because test values etc are here
 */
public class ValidatorTest extends XMLTestCase {
	private Validator validator;
	private File tempDTDFile;

	@Test
	public void testXSchema() throws Exception {
		File xmlFile = TestResources.BOOK_XSD_GENERATED.getFile();
		assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists", xmlFile.exists());

		validator = new Validator(new InputSource(new FileInputStream(xmlFile)));
		validator.useXMLSchema(true);

		assertTrue("Schema " + validator.toString(), validator.isValid());
	}

	@Test
	public void testIsValidGood() throws Exception {
		String toonXML = test_Constants.XML_DECLARATION
		        + test_Constants.CHUCK_JONES_RIP_DTD_DECL
		        + test_Constants.CHUCK_JONES_RIP_XML;
		validator = new Validator(new StringReader(toonXML));
		assertTrue("toonXML " + validator.toString(), validator.isValid());
		// test XMLTestCase
		passXMLTestCaseTest(toonXML);
		passXMLTestCaseTest(validator);

		String noXMLDeclaration = test_Constants.CHUCK_JONES_RIP_DTD_DECL
		        + test_Constants.CHUCK_JONES_RIP_XML;
		validator = new Validator(new StringReader(noXMLDeclaration));
		assertEquals("noXMLDeclaration " + validator.toString(),
		        true, validator.isValid());
		// test XMLTestCase
		passXMLTestCaseTest(noXMLDeclaration);
		passXMLTestCaseTest(validator);
	}

	@Test
	public void testIsValidExternalSystemId() throws Exception {
		writeTempDTDFile();
		assertTrue(tempDTDFile.getAbsolutePath(), tempDTDFile.exists());

		String externalDTD = test_Constants.XML_DECLARATION
		        + test_Constants.DOCUMENT_WITH_GOOD_EXTERNAL_DTD;
		String tempDTDUrl = tempDTDFile.toURI().toURL().toExternalForm();
		validator = new Validator(new StringReader(externalDTD),
		        tempDTDUrl);

		assertTrue("externalDTD " + validator.toString(), validator.isValid());
		// test XMLTestCase
		passXMLTestCaseTest(externalDTD, tempDTDFile.toURI().toURL().toExternalForm());
		passXMLTestCaseTest(validator);

		String noDTD = test_Constants.XML_DECLARATION
		        + test_Constants.DOCUMENT_WITH_NO_EXTERNAL_DTD;
		validator = new Validator(new StringReader(noDTD),
		        tempDTDFile.toURI().toURL().toExternalForm());

		assertFalse("noDTD " + validator.toString(), validator.isValid());
		// test XMLTestCase
		failXMLTestCaseTest(noDTD, tempDTDFile.toURI().toURL().toExternalForm());
		failXMLTestCaseTest(validator);
	}

	@Test
	public void testIsValidNoDTD() throws Exception {
		writeTempDTDFile();
		assertTrue(tempDTDFile.getAbsolutePath(), tempDTDFile.exists());

		String noDTD = test_Constants.CHUCK_JONES_RIP_XML;
		String systemid = tempDTDFile.toURI().toURL().toExternalForm();
		String doctype = "cartoons";
		String notDoctype = "anima";
		validator = new Validator(new StringReader(noDTD),
		        systemid, doctype);
		assertTrue(validator.toString(), validator.isValid());
		// test XMLTestCase
		passXMLTestCaseTest(noDTD, systemid, doctype);
		passXMLTestCaseTest(validator);
		// and Document constructor
		Document document = getDocument(noDTD);
		validator = new Validator(document, systemid, doctype);
		assertTrue("Document " + validator.toString(), validator.isValid());

		validator = new Validator(new StringReader(noDTD),
		        systemid, notDoctype);
		assertFalse(validator.toString(), validator.isValid());
		// test XMLTestCase
		failXMLTestCaseTest(noDTD, systemid, notDoctype);
		failXMLTestCaseTest(validator);
		// and Document constructor
		validator = new Validator(document, systemid, notDoctype);
		assertFalse("Document " + validator.toString(), validator.isValid());
	}

	@Test
	public void testIsValidBad() throws Exception {
		String noDTD = test_Constants.XML_DECLARATION
		        + test_Constants.CHUCK_JONES_RIP_XML;
		validator = new Validator(new StringReader(noDTD));
		assertFalse("noDTD " + validator.toString(), validator.isValid());
		// test XMLTestCase
		failXMLTestCaseTest(noDTD);
		failXMLTestCaseTest(validator);

		String dtdTwice = test_Constants.XML_DECLARATION
		        + test_Constants.CHUCK_JONES_RIP_DTD_DECL
		        + test_Constants.CHUCK_JONES_RIP_DTD_DECL
		        + test_Constants.CHUCK_JONES_RIP_XML;
		validator = new Validator(new StringReader(dtdTwice));
		assertFalse("dtdTwice " + validator.toString(), validator.isValid());
		// test XMLTestCase
		failXMLTestCaseTest(dtdTwice);
		failXMLTestCaseTest(validator);

		String invalidXML = test_Constants.XML_DECLARATION
		        + test_Constants.CHUCK_JONES_RIP_DTD_DECL
		        + test_Constants.CHUCK_JONES_SPINNING_IN_HIS_GRAVE_XML;
		validator = new Validator(new StringReader(invalidXML));
		assertFalse("invalidXML " + validator.toString(), validator.isValid());
		// test XMLTestCase
		failXMLTestCaseTest(invalidXML);
		failXMLTestCaseTest(validator);
	}

	private Document getDocument(String fromXML) throws Exception {
		return XMLUnit.buildControlDocument(fromXML);
	}

	private void removeTempDTDFile() throws Exception {
		if (tempDTDFile.exists()) {
			tempDTDFile.delete();
		}
	}

	private void writeTempDTDFile() throws Exception {
		FileWriter writer = new FileWriter(tempDTDFile);
		writer.write(test_Constants.CHUCK_JONES_RIP_DTD);
		writer.close();
	}

	@Before
	public void setUp() throws Exception {
		tempDTDFile = File.createTempFile("xmlunit", "cartoons.xml");
	}

	@After
	public void tearDown() throws Exception {
		removeTempDTDFile();
	}

	// ---- XMLTestCase methods ----
	private void passXMLTestCaseTest(String xml) throws Exception {
		assertXMLValid(xml);
		assertXMLValid(new InputSource(new StringReader(xml)));
		assertXMLValid(new InputSource(new StringBufferInputStream(xml)));
	}

	private void passXMLTestCaseTest(String xml, String systemId) throws Exception {
		assertXMLValid(xml, systemId);
		assertXMLValid(new InputSource(new StringReader(xml)), systemId);
		assertXMLValid(new InputSource(new StringBufferInputStream(xml)),
		        systemId);
	}

	private void passXMLTestCaseTest(String xml, String systemId, String doctype)
	        throws Exception {
		assertXMLValid(xml, systemId, doctype);
		assertXMLValid(new InputSource(new StringReader(xml)), systemId,
		        doctype);
		assertXMLValid(new InputSource(new StringBufferInputStream(xml)),
		        systemId, doctype);
	}

	private void passXMLTestCaseTest(Validator validator) throws Exception {
		assertXMLValid(validator);
	}

	private void failXMLTestCaseTest(String xml, String systemId) throws Exception {
		try {
			assertXMLValid(xml, systemId);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
		try {
			assertXMLValid(new InputSource(new StringReader(xml)), systemId);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
		try {
			assertXMLValid(new InputSource(new StringBufferInputStream(xml)),
			        systemId);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
	}

	private void failXMLTestCaseTest(String xml) throws Exception {
		try {
			assertXMLValid(xml);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
		try {
			assertXMLValid(new InputSource(new StringReader(xml)));
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
		try {
			assertXMLValid(new InputSource(new StringBufferInputStream(xml)));
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
	}

	private void failXMLTestCaseTest(String xml, String systemId, String doctype)
	        throws Exception {
		try {
			assertXMLValid(xml, systemId, doctype);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
		try {
			assertXMLValid(new InputSource(new StringReader(xml)), systemId,
			        doctype);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
		try {
			assertXMLValid(new InputSource(new StringBufferInputStream(xml)),
			        systemId, doctype);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
	}

	private void failXMLTestCaseTest(Validator validator) throws Exception {
		try {
			assertXMLValid(validator);
			fail("Expected assertion to fail!");
		} catch (AssertionFailedError e) {
			// expecting this
		}
	}

	public ValidatorTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(ValidatorTest.class);
	}

}
