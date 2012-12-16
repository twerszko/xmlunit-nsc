/*  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.custommonkey.xmlunit.matchers.comparison;

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXmlReader;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXmlReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileReader;
import java.io.StringReader;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.xmlunit.TestResources;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class EqualToReaderMatcherTest extends AbstractComparisonTest {

	@Test
	@Parameters(method = "provideEqualXmlStringPairs")
	public void should_compare_equal_xml_strings(String testString, String expectedString) throws Exception {
		// given
		StringReader testReader = new StringReader(testString);
		StringReader expectedReader = new StringReader(expectedString);

		// then
		assertThat(testReader, is(equalToXmlReader(expectedReader)));
	}

	@Test(expected = AssertionError.class)
	@Parameters(method = "provideNotEqualXmlStringPairs")
	public void should_throw_when_not_equal_xml_strings(String testString, String expectedString) throws Exception {
		// given
		StringReader testReader = new StringReader(testString);
		StringReader expectedReader = new StringReader(expectedString);

		// then
		assertThat(testReader, is(equalToXmlReader(expectedReader)));
	}

	@Test
	@Parameters(method = "provideNotEqualXmlStringPairs")
	public void should_compare_not_equal_xml_strings(String testString, String expectedString) throws Exception {
		// given
		StringReader testReader = new StringReader(testString);
		StringReader expectedReader = new StringReader(expectedString);

		// then
		assertThat(testReader, is(notEqualToXmlReader(expectedReader)));
	}

	@Test(expected = AssertionError.class)
	@Parameters(method = "provideEqualXmlStringPairs")
	public void should_throw_when_equal_xml_strings(String testString, String expectedString) throws Exception {
		// given
		StringReader testReader = new StringReader(testString);
		StringReader expectedReader = new StringReader(expectedString);

		// then
		assertThat(testReader, is(notEqualToXmlReader(expectedReader)));
	}

	// Bug 956372
	@Test
	public void should_pass_when_comparing_the_same_readers() throws Exception {
		// given
		FileReader testFileReader = new FileReader(TestResources.ANIMAL_FILE.getFile());
		FileReader expectedFileReader = new FileReader(TestResources.ANIMAL_FILE.getFile());

		// then
		assertThat(testFileReader, is(equalToXmlReader(expectedFileReader)));
	}

	@Test
	public void should_pass_when_comparing_different_readers() throws Exception {
		FileReader testFileReader2 = new FileReader(TestResources.ANIMAL_FILE.getFile());
		FileReader controlReader2 = new FileReader(TestResources.ANIMAL_FILE2.getFile());

		assertThat(testFileReader2, is(notEqualToXmlReader(controlReader2)));
	}
}
