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
import net.sf.xmlunit.diff.commands.CompareNodeCommand;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CompareNodeCommandTest {
	private final DocumentUtils documentUtils = new DocumentUtils();
	private final DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();

	@Test
	public void should_find_no_child_node_list_differences() throws Exception {
		// given
		Document document = documentBuilder.newDocument();

		document = documentUtils.buildControlDocument(
		        "<down><im standing=\"alone\"/><im><watching/>you all</im>"
		                + "<im watching=\"you\">sinking</im></down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control;

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_find_no_child_node_list_differences2() throws Exception {
		// given
		Document document = documentBuilder.newDocument();

		Element control = document.createElement("root");
		control.appendChild(document.createElement("leafElemA"));
		control.appendChild(document.createElement("leafElemB"));

		Element test = document.createElement("root");
		test.appendChild(document.createElement("leafElemB"));
		test.appendChild(document.createElement("leafElemA"));

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);
		List<Comparison> differencesReverse = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
		assertThat(differencesReverse).hasSize(0);
	}

	@Test
	public void should_detect_child_nodes_in_test() throws Exception {
		// given
		Document document = documentBuilder.newDocument();

		document = documentUtils.buildControlDocument(
		        "<down>" +
		                "<im standing=\"alone\"/>" +
		                "<im><watching/>you all</im></down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control.getNextSibling();

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(3);
		Comparison difference = differences.get(0);
		Comparison attrListLengthDifference = differences.get(1);
		Comparison attrLookupDifference = differences.get(2);

		assertThat(difference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(difference.getControlDetails().getValue()).isEqualTo(false);
		assertThat(difference.getTestDetails().getValue()).isEqualTo(true);

		assertThat(attrListLengthDifference.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(attrListLengthDifference.getControlDetails().getValue()).isEqualTo(1);
		assertThat(attrListLengthDifference.getTestDetails().getValue()).isEqualTo(0);

		assertThat(attrLookupDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(attrLookupDifference.getControlDetails().getValue()).isEqualTo(true);
		assertThat(attrLookupDifference.getTestDetails().getValue()).isEqualTo(false);
	}

	@Test
	public void should_detect_different_child_nodes_list_length() throws Exception {
		// given
		Document document = documentBuilder.newDocument();

		document = documentUtils.buildControlDocument(
		        "<down>" +
		                "<im><watching/>you all</im>" +
		                "<im watching=\"you\">sinking</im></down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control.getNextSibling();

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(3);
		Comparison nodeListLengthDifference = differences.get(0);
		Comparison attrListLengthDifference = differences.get(1);
		Comparison attrLookupDifference = differences.get(2);

		assertThat(nodeListLengthDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(nodeListLengthDifference.getControlDetails().getValue()).isEqualTo(2);
		assertThat(nodeListLengthDifference.getTestDetails().getValue()).isEqualTo(1);

		assertThat(attrListLengthDifference.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(attrListLengthDifference.getControlDetails().getValue()).isEqualTo(0);
		assertThat(attrListLengthDifference.getTestDetails().getValue()).isEqualTo(1);

		assertThat(attrLookupDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(attrLookupDifference.getControlDetails().getValue()).isEqualTo(false);
		assertThat(attrLookupDifference.getTestDetails().getValue()).isEqualTo(true);
	}

	@Test
	public void should_find_no_child_node_list_differences_when_mixed_content() throws Exception {
		// given
		Document document = documentBuilder.newDocument();

		Element control = document.createElement("root");
		control.appendChild(document.createTextNode("text leaf"));
		control.appendChild(document.createElement("leafElem"));

		Element test = document.createElement("root");
		test.appendChild(document.createElement("leafElem"));
		test.appendChild(document.createTextNode("text leaf"));

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);
		List<Comparison> differencesReverse = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
		assertThat(differencesReverse).hasSize(0);
	}

	private List<Comparison> findNodeChildrenDifferences(Node controlNode, Node testNode) {
		ListingComparisonPerformer performer = new ListingComparisonPerformer();

		NodeAndXpath<Node> control = new NodeAndXpath<Node>(controlNode, new XPathContext());
		NodeAndXpath<Node> test = new NodeAndXpath<Node>(testNode, new XPathContext());

		new CompareNodeCommand(performer, false, control, test).execute();
		return performer.getDifferences();
	}
}
