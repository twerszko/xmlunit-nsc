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

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.transform.stream.StreamSource;

import net.sf.xmlunit.TestResources;

import org.junit.Test;
import org.xmlunit.validation.Language;
import org.xmlunit.validation.ParsingValidator;
import org.xmlunit.validation.ValidationResult;

public class ParsingValidatorTest {

    @Test
    public void should_successfully_validate_schema_instance() throws Exception {
        // given
        ParsingValidator v = new ParsingValidator(Language.XML_SCHEMA);

        // when
        v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = v.validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));

        // then
        assertThat(r.isValid()).isTrue();
        assertThat(r.getProblems().iterator().hasNext()).isFalse();
    }

    @Test
    public void should_fail_on_broken_schema_instance() throws Exception {
        // given
        ParsingValidator v = new ParsingValidator(Language.XML_SCHEMA);

        // when
        v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
        ValidationResult r = v.validateInstance(new StreamSource(TestResources.INVALID_BOOK.getFile()));

        // then
        assertThat(r.isValid()).isFalse();
        assertThat(r.getProblems().iterator().hasNext()).isTrue();
    }

    @Test
    public void should_successfully_validate_DTD_instance() throws Exception {
        // given
        ParsingValidator v = new ParsingValidator(Language.XML_DTD);

        // when
        v.setSchemaSource(new StreamSource(TestResources.BOOK_DTD.getFile()));
        ValidationResult r = v.validateInstance(new StreamSource(TestResources.BOOK_WITH_DOC_TYPE.getFile()));

        // then
        assertThat(r.isValid()).isTrue();
        assertThat(r.getProblems().iterator().hasNext()).isFalse();
    }

    @Test
    public void should_fail_on_broken_DTD_instance() throws Exception {
        // given
        ParsingValidator v = new ParsingValidator(Language.XML_DTD);

        // when
        v.setSchemaSource(new StreamSource(TestResources.BOOK_DTD.getFile()));
        ValidationResult r = v.validateInstance(new StreamSource(TestResources.INVALID_BOOK_WITH_DOC_TYPE.getFile()));

        // then
        assertThat(r.isValid()).isFalse();
        assertThat(r.getProblems().iterator().hasNext()).isTrue();
    }
}
