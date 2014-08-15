/*
 ******************************************************************
Copyright (c) 2008,2011, Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit.jaxp13;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlunit.TestResources;

public class ValidatorTest {

    // RELAX NG support; read more: http://stackoverflow.com/a/2104332/563175
    private final String RELAX_NG_SCHEMA_FACTORY_KEY =
            SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI;
    private final String RELAX_NG_SCHEMA_FACTORY_CLASS = "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory";
    private final String systemRelaxNgSchemaFactory = System.getProperty(RELAX_NG_SCHEMA_FACTORY_KEY);

    private void replaceRelaxNgSchemaFactorySysProp() {
        System.setProperty(RELAX_NG_SCHEMA_FACTORY_KEY, RELAX_NG_SCHEMA_FACTORY_CLASS);
    }

    private void restoreRelaxNgSchemaFactorySysProp() {
        if (systemRelaxNgSchemaFactory == null) {
            Properties sysProps = System.getProperties();
            sysProps.remove(RELAX_NG_SCHEMA_FACTORY_KEY);
        } else {
            System.setProperty(RELAX_NG_SCHEMA_FACTORY_KEY, systemRelaxNgSchemaFactory);
        }
    }

    @Test
    public void should_check_if_schema_is_valid() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BOOK_XSD.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);

        // then
        assertTrue(validator.isSchemaValid());
    }

    @Test
    public void should_check_if_schema_with_custom_prefix_is_valid() throws Exception {
        // given
        StreamSource source = new StreamSource(TestResources.BOOK_XSD2.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);

        // then
        assertTrue(validator.isSchemaValid());
    }

    @Test
    public void should_get_no_schema_errors() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BOOK_XSD.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);

        // then
        assertThat(validator.getSchemaErrors().size(), is(equalTo(0)));
    }

    @Test
    public void should_check_if_schema_is_invalid() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BROKEN_XSD.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);

        // then
        assertFalse(validator.isSchemaValid());
    }

    @Test
    public void should_check_list_of_scheam_errors() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BROKEN_XSD.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);
        List<SAXParseException> listOfErrors = validator.getSchemaErrors();

        // then
        assertThat(listOfErrors.size(), is(greaterThan(0)));
        for (SAXParseException ex : listOfErrors) {
            assertTrue(ex instanceof SAXParseException);
        }
    }

    @Test
    public void should_chceck_if_schema_is_valid_according_to_definition() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BOOK_XSD.getFile());
        StreamSource instance = new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);

        // then
        assertTrue(validator.isInstanceValid(instance));
    }

    @Test
    public void should_chceck_if_schema_is_invalid_according_to_definition() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BOOK_XSD.getFile());
        StreamSource instance = new StreamSource(TestResources.INVALID_BOOK.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);

        // then
        assertFalse(validator.isInstanceValid(instance));
    }

    @Test
    public void should_check_list_of_errors_in_invalid_instance() throws IOException {
        // given
        StreamSource source = new StreamSource(TestResources.BOOK_XSD.getFile());
        StreamSource instance = new StreamSource(TestResources.INVALID_BOOK.getFile());
        Validator validator = new Validator();

        // when
        validator.addSchemaSource(source);
        List<SAXParseException> listOfErrors = validator.getInstanceErrors(instance);

        // then
        assertTrue(listOfErrors.size() > 0);
        for (SAXParseException ex : listOfErrors) {
            assertTrue(ex instanceof SAXParseException);
        }
    }

    @Test(expected = SAXException.class)
    public void should_throw_exception_when_invalid_schema() throws Throwable {
        // given
        StreamSource source = new StreamSource(TestResources.BROKEN_XSD.getFile());
        Validator validator = new Validator();
        StreamSource instance = new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile());

        // when
        validator.addSchemaSource(source);
        try {
            validator.isInstanceValid(instance);
        } catch (XMLUnitRuntimeException e) {
            throw e.getCause();
        }
    }

    @Test(expected = IOException.class)
    public void testInstanceValidationOfMissingFile() throws Throwable {
        // given
        StreamSource source;
        try {
            source = new StreamSource(TestResources.BOOK_XSD.getFile());
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't load required resource!");
        }
        File tempFile = File.createTempFile("xmlunit", "not there.xml");
        tempFile.delete();

        // when
        Validator validator = new Validator();
        validator.addSchemaSource(source);

        StreamSource instance = new StreamSource(tempFile);
        try {
            validator.isInstanceValid(instance);
        } catch (XMLUnitRuntimeException e) {
            throw e.getCause();
        }
    }

    @Test
    public void should_check_if_relaxNG_schema_is_valid() throws IOException {
        replaceRelaxNgSchemaFactorySysProp();
        try {
            // given
            StreamSource source = new StreamSource(TestResources.BOOK_RNG.getFile());
            Validator validator = new Validator(XMLConstants.RELAXNG_NS_URI);

            // when
            validator.addSchemaSource(source);

            // then
            assertTrue(validator.isSchemaValid());
        } finally {
            restoreRelaxNgSchemaFactorySysProp();
        }
    }

    @Test
    public void should_check_valid_instance_with_relaxNG_schema() throws IOException {
        replaceRelaxNgSchemaFactorySysProp();
        try {
            // given
            StreamSource source = new StreamSource(TestResources.BOOK_RNG.getFile());
            StreamSource instance = new StreamSource(TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile());
            Validator validator = new Validator(XMLConstants.RELAXNG_NS_URI);

            // when
            validator.addSchemaSource(source);

            // then
            assertTrue(validator.isInstanceValid(instance));
        } finally {
            restoreRelaxNgSchemaFactorySysProp();
        }
    }

    @Test
    public void XtestBadInstanceIsInvalidRNG() throws IOException {
        replaceRelaxNgSchemaFactorySysProp();
        try {
            // given
            StreamSource source = new StreamSource(TestResources.BOOK_RNG.getFile());
            StreamSource instance = new StreamSource(TestResources.INVALID_BOOK.getFile());
            Validator validator = new Validator(XMLConstants.RELAXNG_NS_URI);

            // when
            validator.addSchemaSource(source);
            List<SAXParseException> listOfErrors = validator.getInstanceErrors(instance);

            // then
            assertThat(listOfErrors.size(), is(greaterThan(0)));
            for (SAXParseException ex : listOfErrors) {
                assertTrue(ex instanceof SAXParseException);
            }
        } finally {
            restoreRelaxNgSchemaFactorySysProp();
        }
    }

    // TODO: Test not refactored. Invastigate what's wrong
    /**
     * fails even using the setup in XtestGoodRelaxNGSchemaIsValid() since a
     * SAXParser is trying to read the compact syntax definition and chokes on
     * it not being XML.
     * 
     * @throws IOException
     * 
     * @see #XtestGoodRelaxNGSchemaIsValid()
     */
    @Ignore
    @Test
    public void XtestGoodRelaxNGCompactSyntaxIsValid() throws IOException {
        Validator v = new Validator(javax.xml.XMLConstants.RELAXNG_NS_URI);
        File tempFile = File.createTempFile("xmlunit", "Book.rngc");
        tempFile.delete();
        v.addSchemaSource(new StreamSource(tempFile));
        assertTrue(v.isSchemaValid());
        StreamSource s =
                new StreamSource(TestResources.BOOK_XSD_GENERATED_NO_SCHEMA.getFile());
        assertTrue(v.isInstanceValid(s));
    }
}
