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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xmlunit.TestResources;

//TODO refactor
public class Jaxp1_2SchemaValidationTest {

    private Validator validator;

    @Test
    public void testUsingStringURI() throws Exception {
        // given
        File xsdFile = TestResources.BOOK_XSD.getFile();
        File xmlFile = TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue(xsdFile.exists());
        assertTrue(xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        Source[] sources = new Source[] { new StreamSource(xsdFile) };
        validator.setSchemaSources(sources);

        // then
        assertThat(validator.isValid()).isTrue();
    }

    @Test
    public void testUsingInputStream() throws Exception {
        File xsdFile = TestResources.BOOK_XSD.getFile();
        assertTrue("xsdFile " + xsdFile.getAbsolutePath() + " exists",
                xsdFile.exists());

        File xmlFile = TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists",
                xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        StreamSource source = new StreamSource(new FileInputStream(xsdFile));
        validator.setSchemaSources(new Source[] { source });

        // then
        assertThat(validator.isValid()).isTrue();
    }

    @Test
    public void testUsingInputSource() throws Exception {
        File xsdFile = TestResources.BOOK_XSD.getFile();
        assertTrue("xsdFile " + xsdFile.getAbsolutePath() + " exists",
                xsdFile.exists());

        File xmlFile = TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists",
                xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        InputSource saxSource = new InputSource(new FileReader(xsdFile));
        validator.setSchemaSources(new Source[] { new SAXSource(saxSource) });

        // then
        assertThat(validator.isValid()).isTrue();
    }

    @Test
    public void testUsingAFile() throws Exception {
        File xsdFile = TestResources.BOOK_XSD.getFile();
        assertTrue("xsdFile " + xsdFile.getAbsolutePath() + " exists",
                xsdFile.exists());

        File xmlFile = TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists",
                xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        validator.setSchemaSources(new Source[] { new StreamSource(xsdFile) });

        // then
        assertThat(validator.isValid()).isTrue();
    }

    @Test
    public void testUsingStringArrayContainingStringURI() throws Exception {
        File xsdFile = TestResources.BOOK_XSD.getFile();
        assertTrue("xsdFile " + xsdFile.getAbsolutePath() + " exists",
                xsdFile.exists());

        File xmlFile = TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists",
                xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        validator.setSchemaSources(new Source[] {
                new StreamSource(xsdFile.getAbsolutePath())
        });

        // then
        assertThat(validator.isValid()).isTrue();
    }

    @Test
    public void testUsingNonExistentFile() throws Exception {
        File xsdFile = File.createTempFile("xmlunit", "BookDoesNotExist.xsd");
        xsdFile.delete();

        assertFalse(xsdFile.exists());

        File xmlFile = TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists",
                xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        validator.setSchemaSources(new Source[] { new StreamSource(xsdFile) });

        // then
        assertThat(validator.isValid()).isFalse();
    }

    @Test
    public void testUsingInvalidXML() throws Exception {
        File xsdFile = TestResources.BOOK_XSD.getFile();
        assertTrue("xsdFile " + xsdFile.getAbsolutePath() + " exists", xsdFile.exists());

        File xmlFile = TestResources.INVALID_BOOK_XSD_GENERATED_NO_SCHEMA.getFile();
        assertTrue("xmlFile " + xmlFile.getAbsolutePath() + " exists",
                xmlFile.exists());

        validator = new Validator(new FileReader(xmlFile));

        validator.useXMLSchema(true);
        validator.setSchemaSources(new Source[] { new StreamSource(xsdFile) });

        // then
        assertThat(validator.isValid()).isFalse();
    }
}
