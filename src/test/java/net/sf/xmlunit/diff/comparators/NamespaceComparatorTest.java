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
package net.sf.xmlunit.diff.comparators;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.comparators.NamespaceComparisonProvider;
import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NamespaceComparatorTest {
	private final DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
	private final Document document = documentBuilder.newDocument();

	@Test
	public void should_detect_different_namespace_uri() {
		// given
		Element control = document.createElementNS("x", "y");
		Element test = document.createElementNS("z", "y");

		// when
		List<Comparison> differences = findNamespaceDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_URI);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("x");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("z");
	}

	@Test
	public void should_detect_different_namespace_prefix() {
		// given
		Element control = document.createElementNS("x", "x:y");
		Element test = document.createElementNS("x", "z:y");

		// when
		List<Comparison> differences = findNamespaceDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_PREFIX);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("x");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("z");
	}

	@Test
	public void should_detect_different_namespace_prefix_2() throws Exception {
		// given
		DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

		String namespaceA = "http://example.org/StoneRoses";
		String prefixA = "music";
		String prefixB = "cd";
		String elemName = "nowPlaying";

		Element control = document.createElementNS(namespaceA, prefixA + ':' + elemName);
		Element test = document.createElementNS(namespaceA, prefixB + ':' + elemName);

		// when
		List<Comparison> differences = findNamespaceDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_PREFIX);
	}

	@Test
	public void should_detect_no_differences_in_namespace() throws Exception {
		DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

		String namespace = "http://example.org/StoneRoses";
		String prefix = "music";
		String elemName = "nowPlaying";

		Element control = document.createElementNS(namespace, prefix + ':' + elemName);

		// when
		List<Comparison> differences = findNamespaceDifferences(control, control);

		// then
		assertThat(differences).hasSize(0);
	}

	private List<Comparison> findNamespaceDifferences(Node control, Node test) {
		ListingComparator comparator = new ListingComparator();
		Comparisons comparisons = new NamespaceComparisonProvider().provideComparisons(
		        new NodeAndXpath<Node>(control, new XPathContext()),
		        new NodeAndXpath<Node>(test, new XPathContext()));
		comparator.executeComparisons(comparisons);

		return comparator.getDifferences();
	}
}
