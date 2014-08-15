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
package org.xmlunit.validation;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionBdd.when;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.TestResources;

public class JAXPValidatorTest {

    private JAXPValidator validator;

    @Before
    public void setUp() {
        String langNsUri = Language.XML_SCHEMA.getNsUri();
        validator = new JAXPValidator(langNsUri);
    }

    @Test
    public void should_successfully_validate_schema() throws Exception {
        // when
        validator.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = validator.validateSchema();

        // then
        assertThat(r.isValid()).isTrue();
        assertThat(r.getProblems().iterator().hasNext()).isFalse();
    }

    @Test
    public void should_successfully_validate_instance() throws IOException {
        // when
        validator.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = validator.validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));

        // then
        assertThat(r.isValid()).isTrue();
        assertThat(r.getProblems().iterator().hasNext()).isFalse();
    }

    @Test
    public void should_fail_on_broken_schema() throws Exception {
        // when
        validator.setSchemaSource(new StreamSource(TestResources.BROKEN_XSD.getFile()));
        ValidationResult r = validator.validateSchema();

        // then
        assertThat(r.isValid()).isFalse();
        assertThat(r.getProblems().iterator().hasNext()).isTrue();
    }

    @Test
    public void should_fail_on_broken_instance() throws Exception {
        // when
        validator.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = validator.validateInstance(new StreamSource(TestResources.INVALID_BOOK.getFile()));

        // then
        assertThat(r.isValid()).isFalse();
        assertThat(r.getProblems().iterator().hasNext()).isTrue();
    }

    @Test
    public void should_throw_when_validating_instance_and_schema_is_invalid() throws Exception {
        // when
        validator.setSchemaSource(new StreamSource(TestResources.BROKEN_XSD.getFile()));
        when(validator).validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));

        // then
        assertThat(caughtException()).isInstanceOf(XMLUnitRuntimeException.class);
    }

    @Test
    public void should_throw_when_validating_instance_and_schema_is_not_there() throws Exception {
        // when
        File notExistingFile = File.createTempFile("xmlunit", ".xsd");
        notExistingFile.delete();
        validator.setSchemaSource(new StreamSource(notExistingFile));
        when(validator).validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));

        // then
        assertThat(caughtException()).isInstanceOf(XMLUnitRuntimeException.class);
    }
}
