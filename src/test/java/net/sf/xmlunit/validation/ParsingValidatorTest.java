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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import net.sf.xmlunit.TestResources;

import org.junit.Test;

public class ParsingValidatorTest {

	@Test
	public void shouldSuccessfullyValidateSchemaInstance() throws IOException {
		ParsingValidator v =
		        new ParsingValidator(Languages.W3C_XML_SCHEMA_NS_URI);
		v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
		ValidationResult r = v.validateInstance(new StreamSource(TestResources.BOOK_XSD_GENERATED.getFile()));
		assertTrue(r.isValid());
		assertFalse(r.getProblems().iterator().hasNext());
	}

	@Test
	public void shouldFailOnBrokenSchemaInstance() throws IOException {
		ParsingValidator v =
		        new ParsingValidator(Languages.W3C_XML_SCHEMA_NS_URI);
		v.setSchemaSource(new StreamSource(TestResources.BOOK_XSD.getFile()));
		ValidationResult r = v.validateInstance(new StreamSource(TestResources.INVALID_BOOK.getFile()));
		assertFalse(r.isValid());
		assertTrue(r.getProblems().iterator().hasNext());
	}

	@Test
	public void shouldSuccessfullyValidateDTDInstance() throws IOException {
		ParsingValidator v =
		        new ParsingValidator(Languages.XML_DTD_NS_URI);
		v.setSchemaSource(new StreamSource(TestResources.BOOK_DTD.getFile()));
		ValidationResult r = v.validateInstance(new StreamSource(TestResources.BOOK_WITH_DOC_TYPE.getFile()));
		assertTrue(r.isValid());
		assertFalse(r.getProblems().iterator().hasNext());
	}

	@Test
	public void shouldFailOnBrokenDTDInstance() throws IOException {
		ParsingValidator v =
		        new ParsingValidator(Languages.XML_DTD_NS_URI);
		v.setSchemaSource(new StreamSource(TestResources.BOOK_DTD.getFile()));
		ValidationResult r = v.validateInstance(new StreamSource(TestResources.INVALID_BOOK_WITH_DOC_TYPE.getFile()));
		assertFalse(r.isValid());
		assertTrue(r.getProblems().iterator().hasNext());
	}
}
