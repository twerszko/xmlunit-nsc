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

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.TestResources;

/**
 * JUnit test for Validator Also includes tests for XMLTestCase
 * <code>assertValidXML</code> methods because test values etc are here
 */
// TODO Refactor
public class ValidatorTest {
    private Validator validator;
    private File tempDTDFile;

    private static final String DOCUMENT_START = "<cartoons>";
    private static final String DOCUMENT_END = "</cartoons>";

    private static final String CHUCK_JONES_RIP_DTD =
            "<!ELEMENT name (#PCDATA)> \n"
                    + "<!ELEMENT toon (name)> \n"
                    + "<!ELEMENT cartoons (toon*)> \n";

    private static final String CHUCK_JONES_RIP_DTD_DECL =
            "<!DOCTYPE cartoons ["
                    + CHUCK_JONES_RIP_DTD + "\n]>";

    private static final String CHUCK_JONES_RIP_XML = DOCUMENT_START
            + "<toon><name>bugs bunny</name></toon>"
            + "<!--meep meep--><toon><name>roadrunnner</name></toon>"
            + DOCUMENT_END;

    private static final String DOCUMENT_WITH_GOOD_EXTERNAL_DTD =
            "<!DOCTYPE cartoons SYSTEM \"yak.dtd\">"
                    + CHUCK_JONES_RIP_XML;
    private static final String DOCUMENT_WITH_NO_EXTERNAL_DTD =
            CHUCK_JONES_RIP_XML;

    private static final String CHUCK_JONES_SPINNING_IN_HIS_GRAVE_XML =
            DOCUMENT_START + "<kidsTv><toon><name>Yo ho ahoy</name></toon></kidsTV>"
                    + DOCUMENT_END;

    @Test
    public void testXSchema() throws Exception {
        File xmlFile = TestResources.BOOK_XSD_GENERATED.getFile();
        assertThat(xmlFile.exists()).isTrue();

        validator = new Validator(new InputSource(new FileInputStream(xmlFile)));
        validator.useXMLSchema(true);

        assertTrue("Schema " + validator.toString(), validator.isValid());
    }

    @Test
    public void testIsValidGood() throws Exception {
        String toonXML = XMLConstants.XML_DECLARATION
                + CHUCK_JONES_RIP_DTD_DECL
                + CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(toonXML));
        assertTrue("toonXML " + validator.toString(), validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(toonXML);
        passXMLTestCaseTest(validator);

        String noXMLDeclaration = CHUCK_JONES_RIP_DTD_DECL
                + CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(noXMLDeclaration));
        assertThat(validator.isValid()).isTrue();
        // test XMLTestCase
        passXMLTestCaseTest(noXMLDeclaration);
        passXMLTestCaseTest(validator);
    }

    @Test
    public void testIsValidExternalSystemId() throws Exception {
        writeTempDTDFile();
        assertTrue(tempDTDFile.getAbsolutePath(), tempDTDFile.exists());

        String externalDTD = XMLConstants.XML_DECLARATION
                + DOCUMENT_WITH_GOOD_EXTERNAL_DTD;
        String tempDTDUrl = tempDTDFile.toURI().toURL().toExternalForm();
        validator = new Validator(new StringReader(externalDTD),
                tempDTDUrl);

        assertTrue("externalDTD " + validator.toString(), validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(externalDTD, tempDTDFile.toURI().toURL().toExternalForm());
        passXMLTestCaseTest(validator);

        String noDTD = XMLConstants.XML_DECLARATION
                + DOCUMENT_WITH_NO_EXTERNAL_DTD;
        validator = new Validator(new StringReader(noDTD),
                tempDTDFile.toURI().toURL().toExternalForm());

        assertThat(validator.isValid()).isFalse();
        // test XMLTestCase
        failXMLTestCaseTest(noDTD, tempDTDFile.toURI().toURL().toExternalForm());
        failXMLTestCaseTest(validator);
    }

    @Test
    public void testIsValidNoDTD() throws Exception {
        writeTempDTDFile();
        assertTrue(tempDTDFile.getAbsolutePath(), tempDTDFile.exists());

        String noDTD = CHUCK_JONES_RIP_XML;
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
        validator = new Validator(document, systemid, doctype, new XmlUnitProperties());
        assertTrue("Document " + validator.toString(), validator.isValid());

        validator = new Validator(new StringReader(noDTD),
                systemid, notDoctype);
        assertFalse(validator.toString(), validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(noDTD, systemid, notDoctype);
        failXMLTestCaseTest(validator);
        // and Document constructor
        validator = new Validator(document, systemid, notDoctype, new XmlUnitProperties());
        assertFalse("Document " + validator.toString(), validator.isValid());
    }

    @Test
    public void testIsValidBad() throws Exception {
        String noDTD = XMLConstants.XML_DECLARATION
                + CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(noDTD));
        assertFalse("noDTD " + validator.toString(), validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(noDTD);
        failXMLTestCaseTest(validator);

        String dtdTwice = XMLConstants.XML_DECLARATION
                + CHUCK_JONES_RIP_DTD_DECL
                + CHUCK_JONES_RIP_DTD_DECL
                + CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(dtdTwice));
        assertFalse("dtdTwice " + validator.toString(), validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(dtdTwice);
        failXMLTestCaseTest(validator);

        String invalidXML = XMLConstants.XML_DECLARATION
                + CHUCK_JONES_RIP_DTD_DECL
                + CHUCK_JONES_SPINNING_IN_HIS_GRAVE_XML;
        validator = new Validator(new StringReader(invalidXML));
        assertFalse("invalidXML " + validator.toString(), validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(invalidXML);
        failXMLTestCaseTest(validator);
    }

    private Document getDocument(String fromXML) throws Exception {
        return new DocumentUtils(new XmlUnitProperties()).buildControlDocument(fromXML);
    }

    private void removeTempDTDFile() throws Exception {
        if (tempDTDFile.exists()) {
            tempDTDFile.delete();
        }
    }

    private void writeTempDTDFile() throws Exception {
        FileWriter writer = new FileWriter(tempDTDFile);
        writer.write(CHUCK_JONES_RIP_DTD);
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
        Assert.assertThat(xml, is(validXmlString()));
        Assert.assertThat(new InputSource(new StringReader(xml)), is(validXmlSource()));
        Assert.assertThat(new InputSource(new StringBufferInputStream(xml)), is(validXmlSource()));
    }

    private void passXMLTestCaseTest(String xml, String systemId) throws Exception {
        Assert.assertThat(xml, is(validXmlString().withSystemId(systemId)));
        Assert.assertThat(new InputSource(new StringReader(xml)), is(validXmlSource().withSystemId(systemId)));
        Assert.assertThat(new InputSource(new StringBufferInputStream(xml)),
                is(validXmlSource().withSystemId(systemId)));
    }

    private void passXMLTestCaseTest(String xml, String systemId, String doctype)
            throws Exception {

        InputSource inputSource1 = new InputSource(new StringReader(xml));
        InputSource inputSource2 = new InputSource(new StringBufferInputStream(xml));

        Assert.assertThat(xml, is(validXmlString().withSystemId(systemId).withDoctype(doctype)));
        Assert.assertThat(inputSource1, is(validXmlSource().withSystemId(systemId).withDoctype(doctype)));
        Assert.assertThat(inputSource2, is(validXmlSource().withSystemId(systemId).withDoctype(doctype)));
    }

    private void passXMLTestCaseTest(Validator validator) throws Exception {
        Assert.assertThat(validator, is(validXml()));
    }

    private void failXMLTestCaseTest(String xml, String systemId) throws Exception {
        InputSource inputSource1 = new InputSource(new StringReader(xml));
        InputSource inputSource2 = new InputSource(new StringBufferInputStream(xml));

        Assert.assertThat(xml, is(invalidXmlString().withSystemId(systemId)));
        Assert.assertThat(inputSource1, is(invalidXmlSource().withSystemId(systemId)));
        Assert.assertThat(inputSource2, is(invalidXmlSource().withSystemId(systemId)));
    }

    private void failXMLTestCaseTest(String xml) throws Exception {
        Assert.assertThat(xml, is(invalidXmlString()));
        Assert.assertThat(new InputSource(new StringReader(xml)), is(invalidXmlSource()));
        Assert.assertThat(new InputSource(new StringBufferInputStream(xml)), is(invalidXmlSource()));
    }

    private void failXMLTestCaseTest(String xml, String systemId, String doctype) throws Exception {
        InputSource inputSource = new InputSource(new StringReader(xml));
        InputSource inputSource2 = new InputSource(new StringBufferInputStream(xml));

        Assert.assertThat(xml, is(invalidXmlString().withSystemId(systemId).withDoctype(doctype)));
        Assert.assertThat(inputSource, is(invalidXmlSource().withSystemId(systemId).withDoctype(doctype)));
        Assert.assertThat(inputSource2, is(invalidXmlSource().withSystemId(systemId).withDoctype(doctype)));
    }

    private void failXMLTestCaseTest(Validator validator) throws Exception {
        Assert.assertThat(validator, is(invalidXml()));
    }

}
