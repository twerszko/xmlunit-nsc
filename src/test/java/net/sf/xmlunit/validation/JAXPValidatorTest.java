/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package net.sf.xmlunit.validation;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import net.sf.xmlunit.TestResources;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.junit.Test;

public class JAXPValidatorTest {

    @Test
    public void shouldSuccessfullyValidateSchema() throws IOException {
        JAXPValidator v = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = v.validateSchema();
        assertTrue(r.isValid());
        assertFalse(r.getProblems().iterator().hasNext());
    }

    @Test
    public void shouldSuccessfullyValidateInstance() throws IOException {
        JAXPValidator v = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = v.validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));
        assertTrue(r.isValid());
        assertFalse(r.getProblems().iterator().hasNext());
    }

    @Test
    public void shouldFailOnBrokenSchema() throws IOException {
        JAXPValidator v = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSource(new StreamSource(TestResources.BROKEN_XSD.getFile()));
        ValidationResult r = v.validateSchema();
        assertFalse(r.isValid());
        assertTrue(r.getProblems().iterator().hasNext());
    }

    @Test
    public void shouldFailOnBrokenInstance() throws IOException {
        JAXPValidator v = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = v.validateInstance(new StreamSource(TestResources.INVALID_BOOK.getFile()));
        assertFalse(r.isValid());
        assertTrue(r.getProblems().iterator().hasNext());
    }

    @Test
    public void shouldThrowWhenValidatingInstanceAndSchemaIsInvalid() throws IOException {
        JAXPValidator v = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI);
        v.setSchemaSource(new StreamSource(TestResources.BROKEN_XSD.getFile()));
        try {
            v.validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));
            fail("should have thrown an exception");
        } catch (Exception e) {
            assertThat(e, instanceOf(XMLUnitRuntimeException.class));
        }
    }

    @Test
    public void shouldThrowWhenValidatingInstanceAndSchemaIsNotThere() throws IOException {
        JAXPValidator v = new JAXPValidator(Languages.W3C_XML_SCHEMA_NS_URI);
        File tempFile = File.createTempFile("xmlunit", ".xsd");
        tempFile.delete();
        v.setSchemaSource(new StreamSource(tempFile));
        try {
            v.validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));
            fail("should have thrown an exception");
        } catch (Exception e) {
            assertThat(e, instanceOf(XMLUnitRuntimeException.class));
        }
    }
}
