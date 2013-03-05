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
package net.sf.xmlunit.diff.strategies;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

public class CompareCharacterDataStrategyTest {
	private final DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
	private final Document document = documentBuilder.newDocument();

	@Test
	public void should_detect_different_cdata() throws Exception {
		// given
		String expected = "I'm standing alone, you're weighing the gold";
		String actual = "I'm watching you sinking... Fools Gold";

		CDATASection control = document.createCDATASection(expected);
		CDATASection test = document.createCDATASection(actual);

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CDATA_VALUE);
	}

	@Test
	public void should_detect_no_differences_in_cdata() throws Exception {
		// given
		String value = "cdata";
		CDATASection control = document.createCDATASection(value);
		CDATASection test = document.createCDATASection(value);

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_text_nodes() throws Exception {
		// given
		String expected = "the pack on my back is aching";
		String actual = "the straps seem to cut me like a knife";
		Text control = document.createTextNode(expected);
		Text test = document.createTextNode(actual);

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.TEXT_VALUE);
	}

	@Test
	public void should_detect_no_differences_in_text_nodes() throws Exception {
		// given
		String value = "text";
		Text control = document.createTextNode(value);
		Text test = document.createTextNode(value);

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_comments() throws Exception {
		// given
		String expected = "Im no clown I wont back down";
		String actual = "dont need you to tell me whats going down";

		Comment control = document.createComment(expected);
		Comment test = document.createComment(actual);

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.COMMENT_VALUE);
	}

	@Test
	public void should_detect_no_differences_in_comments() throws Exception {
		// given
		String value = "Hello, I'm a comment.";

		Comment control = document.createComment(value);
		Comment test = document.createComment(value);

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_no_differences_between_comment_and_text() {
		// given
		Comment control = document.createComment("foo");
		Text test = document.createTextNode("foo");

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_text_between_comment_and_text() {
		// given
		Text control = document.createTextNode("bar");
		Comment test = document.createComment("foo");

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("foo");
	}

	@Test
	public void should_detect_no_differences_between_comment_and_cdata() {
		// given
		Comment control = document.createComment("foo");
		CDATASection test = document.createCDATASection("foo");

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_text_between_comment_and_cdata() {
		// given
		CDATASection control = document.createCDATASection("foo");
		Comment test = document.createComment("bar");

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("foo");
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("bar");
	}

	@Test
	public void should_detect_no_differences_between_text_and_cdata() {
		// given
		Text control = document.createTextNode("foo");
		CDATASection test = document.createCDATASection("foo");

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_text_between_text_and_cdata() {
		// given
		CDATASection control = document.createCDATASection("bar");
		Text test = document.createTextNode("foo");

		// when
		List<Comparison> differences = findCharacterDataDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("foo");
	}

	private List<Comparison> findCharacterDataDifferences(CharacterData controlText, CharacterData testText) {
		ListingComparator comparator = new ListingComparator();

		NodeAndXpath<CharacterData> control = new NodeAndXpath<CharacterData>(controlText, new XPathContext());
		NodeAndXpath<CharacterData> test = new NodeAndXpath<CharacterData>(testText, new XPathContext());

		Comparisons comparisons = new CharacterDataComparisonProvider().provideComparisons(control, test);
		comparator.executeComparisons(comparisons);
		return comparator.getDifferences();
	}
}
