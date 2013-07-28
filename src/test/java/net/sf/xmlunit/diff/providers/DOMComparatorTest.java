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

import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.diff.providers.ComparisonProviders;
import net.sf.xmlunit.diff.providers.DOMComparator;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DOMComparatorTest {
	private final DocumentUtils documentUtils = new DocumentUtils();
	private final DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
	private final Document document = documentBuilder.newDocument();

	@Test
	public void should_detect_child_lookup_difference() {
		// given
		Element control = document.createElement("foo");
		Element child = document.createElement("bar");
		control.appendChild(child);

		Element test = document.createElement("foo");

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);

		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(secondDifference.getControlDetails().getValue()).isEqualTo("bar");
		assertThat(secondDifference.getControlDetails().getTarget()).isEqualTo(child);
		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/bar[1]");
		assertThat(secondDifference.getTestDetails().getValue()).isNull();
		assertThat(secondDifference.getTestDetails().getTarget()).isNull();
		assertThat(secondDifference.getTestDetails().getXpath()).isNull();
	}

	@Test
	public void should_detect_child_lookup_difference2() {
		// given
		Element control = document.createElement("foo");

		Element test = document.createElement("foo");
		Element child = document.createElement("bar");
		test.appendChild(child);

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);

		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(secondDifference.getTestDetails().getValue()).isEqualTo("bar");
		assertThat(secondDifference.getTestDetails().getTarget()).isEqualTo(child);
		assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/bar[1]");
		assertThat(secondDifference.getControlDetails().getValue()).isNull();
		assertThat(secondDifference.getControlDetails().getTarget()).isNull();
		assertThat(secondDifference.getControlDetails().getXpath()).isNull();
	}

	@Test
	public void should_detect_no_child_lookup_difference() {
		// given
		Element control = document.createElement("foo");
		Element controlComment = document.createElement("bar");
		control.appendChild(controlComment);

		Element test = document.createElement("foo");
		Element testComment = document.createElement("bar");
		test.appendChild(testComment);

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_no_child_node_list_differences() throws Exception {
		// given
		Document document = documentUtils.buildControlDocument(
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
	public void should_find_different_children() throws Exception {
		// given
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
		assertThat(differences).hasSize(2);
		assertThat(differencesReverse).hasSize(2);

		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
	}

	@Test
	public void should_detect_no_differences_when_the_same_child_nodes() {
		// given
		Element control = document.createElement("x");
		control.appendChild(document.createElement("y"));
		Element test = document.createElement("x");
		test.appendChild(document.createElement("y"));

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_child_node_number() {
		// given
		Element control = document.createElement("x");
		control.appendChild(document.createElement("z"));
		Element test = document.createElement("x");
		test.appendChild(document.createElement("z"));
		test.appendChild(document.createElement("z"));

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo(1);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo(2);

	}

	@Test
	public void should_detect_different_child_nodes_in_test() throws Exception {
		// given
		Document document = documentUtils.buildControlDocument(
		        "<down>" +
		                "<im standing=\"alone\"/>" +
		                "<im><watching/>you all</im></down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control.getNextSibling();

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(5);
		Comparison difference = differences.get(0);
		Comparison attrListLengthDifference = differences.get(1);
		Comparison attrLookupDifference = differences.get(2);
		Comparison childLookupDifference = differences.get(3);
		Comparison textLookupDifference = differences.get(4);

		assertThat(difference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(difference.getControlDetails().getValue()).isEqualTo(false);
		assertThat(difference.getTestDetails().getValue()).isEqualTo(true);

		assertThat(attrListLengthDifference.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(attrListLengthDifference.getControlDetails().getValue()).isEqualTo(1);
		assertThat(attrListLengthDifference.getTestDetails().getValue()).isEqualTo(0);

		assertThat(attrLookupDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(attrLookupDifference.getControlDetails().getValue()).isEqualTo(true);
		assertThat(attrLookupDifference.getTestDetails().getValue()).isEqualTo(false);

		assertThat(childLookupDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(childLookupDifference.getControlDetails().getValue()).isNull();
		assertThat(childLookupDifference.getTestDetails().getValue()).isEqualTo("watching");

		assertThat(textLookupDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(textLookupDifference.getControlDetails().getValue()).isNull();
		assertThat(textLookupDifference.getTestDetails().getValue()).isEqualTo("#text");
	}

	@Test
	public void should_detect_different_child_nodes_list_length() throws Exception {
		// given
		Document document = documentUtils.buildControlDocument(
		        "<down>" +
		                "<im><watching/>you all</im>" +
		                "<im watching=\"you\">sinking</im></down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control.getNextSibling();

		// when
		List<Comparison> differences = findNodeChildrenDifferences(control, test);

		// then
		assertThat(differences).hasSize(6);
		Comparison nodeListLengthDifference = differences.get(0);
		Comparison attrListLengthDifference = differences.get(1);
		Comparison attrLookupDifference = differences.get(2);
		Comparison childSequenceDifference = differences.get(3);
		Comparison textValueDifference = differences.get(4);
		Comparison childLookupDifference = differences.get(5);

		assertThat(nodeListLengthDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(nodeListLengthDifference.getControlDetails().getValue()).isEqualTo(2);
		assertThat(nodeListLengthDifference.getTestDetails().getValue()).isEqualTo(1);

		assertThat(attrListLengthDifference.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(attrListLengthDifference.getControlDetails().getValue()).isEqualTo(0);
		assertThat(attrListLengthDifference.getTestDetails().getValue()).isEqualTo(1);

		assertThat(attrLookupDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(attrLookupDifference.getControlDetails().getValue()).isEqualTo(false);
		assertThat(attrLookupDifference.getTestDetails().getValue()).isEqualTo(true);

		assertThat(childSequenceDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(childSequenceDifference.getControlDetails().getValue()).isEqualTo(1);
		assertThat(childSequenceDifference.getTestDetails().getValue()).isEqualTo(0);

		assertThat(textValueDifference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(textValueDifference.getControlDetails().getValue()).isEqualTo("you all");
		assertThat(textValueDifference.getTestDetails().getValue()).isEqualTo("sinking");

		assertThat(childLookupDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(childLookupDifference.getControlDetails().getValue()).isEqualTo("watching");
		assertThat(childLookupDifference.getTestDetails().getValue()).isNull();
	}

	@Test
	public void should_find_child_node_sequence_differences_when_mixed_content() throws Exception {
		// given
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
		assertThat(differences).hasSize(2);
		assertThat(differencesReverse).hasSize(2);

		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo(0);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo(1);

		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(differences.get(1).getControlDetails().getValue()).isEqualTo(1);
		assertThat(differences.get(1).getTestDetails().getValue()).isEqualTo(0);
	}

	private List<Comparison> findNodeChildrenDifferences(Node controlNode, Node testNode) {
		final List<Comparison> differences = new LinkedList<Comparison>();

		NodeAndXpath<Node> control = new NodeAndXpath<Node>(controlNode, new XPathContext());
		NodeAndXpath<Node> test = new NodeAndXpath<Node>(testNode, new XPathContext());

		DOMComparator comparator = new DOMComparator(new ComparisonProviders()) {
			@Override
			protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {
				if (result == ComparisonResult.DIFFERENT) {
					differences.add(comparison);
				}
			}
		};
		comparator.compare(control, test);
		return differences;
	}

	@Test
	public void should_detect_equal_string_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.ATTR_NAME_LOOKUP)
		        .between(null, "black")
		        .and(null, "black");

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_string_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.ATTR_NAME_LOOKUP)
		        .between(null, "black")
		        .and(null, "white");

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(differences).hasSize(1);
		assertThat(differences).contains(comparison);
	}

	@Test
	public void should_detect_equal_boolean_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
		        .between(null, true)
		        .and(null, true);

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_different_boolean_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
		        .between(null, false)
		        .and(null, true);

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(differences).hasSize(1);
		assertThat(differences).contains(comparison);
	}

	@Test
	public void should_detect_no_differences_when_two_nulls() {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, null)
		        .and(null, null);

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_difference_when_control_is_null_and_test_is_non_null() {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, null)
		        .and(null, "");

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.HAS_DOCTYPE_DECLARATION);
		assertThat(difference.getControlDetails().getValue()).isNull();
		assertThat(difference.getTestDetails().getValue()).isEqualTo("");
	}

	@Test
	public void should_detect_difference_when_control_is_non_null_and_test_is_null() {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, "")
		        .and(null, null);

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.HAS_DOCTYPE_DECLARATION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("");
		assertThat(difference.getTestDetails().getValue()).isNull();
	}

	@Test
	public void should_detect_difference_when_two_different_values() {
		// given
		short expectedControlVal = 1;
		short expectedTestVal = 2;
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, expectedControlVal)
		        .and(null, expectedTestVal);

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.HAS_DOCTYPE_DECLARATION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo(expectedControlVal);
		assertThat(difference.getTestDetails().getValue()).isEqualTo(expectedTestVal);
	}

	@Test
	public void should_detect_no_differences_when_two_equal_values() {
		// given
		short expectedVal = 2;
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, expectedVal)
		        .and(null, expectedVal);

		// when
		DifferenceLister lister = new DifferenceLister();
		ComparisonResult result = performComparison(comparison, lister);
		List<Comparison> differences = lister.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(differences).hasSize(0);
	}

	private ComparisonResult performComparison(Comparison comparison, final DifferenceLister lister) {
		DOMComparator comparator = new DOMComparator(new ComparisonProviders()) {
			@Override
			protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {
				lister.comparisonPerformed(comparison, result);
			};
		};
		return comparator.executeComparison(comparison);
	}

	private static class DifferenceLister {
		private final List<Comparison> differences = new LinkedList<Comparison>();

		public List<Comparison> getDifferences() {
			return differences;
		}

		public void comparisonPerformed(Comparison comparison, ComparisonResult result) {
			if (result == ComparisonResult.DIFFERENT) {
				differences.add(comparison);
			}
		}

	}
}
