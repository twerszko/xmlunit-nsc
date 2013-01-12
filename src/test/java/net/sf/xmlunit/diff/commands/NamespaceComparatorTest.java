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
package net.sf.xmlunit.diff.commands;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.commands.CompareNamespaceCommand;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NamespaceComparatorTest {
	private final DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();

	@Test
	public void should_compare_nodes_different_NS() {
		// given
		Document document = documentBuilder.newDocument();
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
	public void should_compare_nodes_with_different_prefix() {
		// given
		Document document = documentBuilder.newDocument();
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

	private List<Comparison> findNamespaceDifferences(Node control, Node test) {
		ListingComparisonPerformer performer = new ListingComparisonPerformer();

		new CompareNamespaceCommand(performer,
		        new NodeAndXpathCtx<Node>(control, new XPathContext()),
		        new NodeAndXpathCtx<Node>(test, new XPathContext()))
		        .execute();

		return performer.getDifferences();
	}
}
