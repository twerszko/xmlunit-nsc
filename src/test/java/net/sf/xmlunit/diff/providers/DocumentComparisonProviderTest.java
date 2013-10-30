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
package net.sf.xmlunit.diff.providers;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.diff.providers.DocumentComparisonProvider;
import net.sf.xmlunit.util.Convert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xmlunit.builder.Input;

public class DocumentComparisonProviderTest {
	@Test
	public void should_detect_different_xml_version() throws Exception {
		// given
		Document control = Convert.toDocument(
		        Input.fromMemory("<?xml version=\"1.0\"" + " encoding=\"UTF-8\"?>" + "<Book/>").build());
		Document test = Convert.toDocument(
		        Input.fromMemory("<?xml version=\"1.1\"" + " encoding=\"UTF-8\"?>" + "<Book/>").build());

		// when
		List<Comparison> differences = findHeaderDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.XML_VERSION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("1.0");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("1.1");
	}

	@Test
	public void should_detect_different_standalone() throws Exception {
		// given
		Document control = Convert.toDocument(
		        Input.fromMemory("<?xml version=\"1.0\"" + " standalone=\"yes\"?>" + "<Book/>").build());
		Document test = Convert.toDocument(
		        Input.fromMemory("<?xml version=\"1.0\"" + " standalone=\"no\"?>" + "<Book/>").build());

		// when
		List<Comparison> differences = findHeaderDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.XML_STANDALONE);
		assertThat(difference.getControlDetails().getValue()).isEqualTo(true);
		assertThat(difference.getTestDetails().getValue()).isEqualTo(false);
	}

	@Test
	public void should_detect_different_encoding() throws Exception {
		// given
		Document control = Convert.toDocument(
		        Input.fromMemory("<?xml version=\"1.0\"" + " encoding=\"UTF-8\"?>" + "<Book/>").build());
		Document test = Convert.toDocument(
		        Input.fromMemory("<?xml version=\"1.0\"" + " encoding=\"UTF-16\"?>" + "<Book/>").build());

		// when
		List<Comparison> differences = findHeaderDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.XML_ENCODING);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("UTF-8");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("UTF-16");
	}

	private List<Comparison> findHeaderDifferences(Document controlDoc, Document testDoc) {
		ListingComparator comparator = new ListingComparator();

		NodeAndXpath<Document> control = NodeAndXpath.from(controlDoc);
		NodeAndXpath<Document> test = NodeAndXpath.from(testDoc);
		Comparisons comparisons = new DocumentComparisonProvider().provideComparisons(control, test);
		comparator.executeComparisons(comparisons);

		return comparator.getDifferences();
	}
}
