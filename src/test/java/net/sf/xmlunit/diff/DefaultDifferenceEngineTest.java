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

package net.sf.xmlunit.diff;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;

import net.sf.xmlunit.builder.Input;

import org.custommonkey.xmlunit.CollectingDifferenceEvaluator;
import org.custommonkey.xmlunit.XMLConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class DefaultDifferenceEngineTest extends DOMDifferenceEngineTestAbstract {
	// TODO consider extracting common part with DOMDifferenceEngineTests

	// TODO refactor this mess

	protected CollectingDifferenceEvaluator collectingEvaluator;
	protected final static NodeMatcher DEFAULT_MATCHER = new DefaultNodeMatcher(ElementSelectors.byName);

	@Override
	protected DOMDifferenceEngine createEngine() {
		return new DefaultDifferenceEngine(properties);
	}

	@Override
	@Before
	public void init() throws Exception {
		properties = new XmlUnitProperties();
		documentUtils = new DocumentUtils(properties);
		DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
		doc = documentBuilder.newDocument();

		resetEvaluator();
		engine = new DefaultDifferenceEngine(properties);
		engine.setNodeMatcher(DEFAULT_MATCHER);
		engine.setDifferenceEvaluator(collectingEvaluator);

		evaluator = new ListingDifferenceEvaluator();
	}

	private void resetEvaluator() {
		collectingEvaluator = new CollectingDifferenceEvaluator();
	}

	@Test
	public void should_ignore_different_no_namespace_schema_location() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "baz");

		// when
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);

		// then
		assertThat(collectingEvaluator.different).isFalse();
		assertThat(collectingEvaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_schema_location_in_test() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
		Element test = doc.createElement("foo");

		// when
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);

		// then
		assertThat(collectingEvaluator.different).isFalse();
		assertThat(collectingEvaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_schema_location_in_control() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		// when
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);

		// then
		assertThat(collectingEvaluator.different).isFalse();
		assertThat(collectingEvaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_no_namespace_location_in_test() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
		Element test = doc.createElement("foo");

		// when
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);

		// then
		assertThat(collectingEvaluator.different).isFalse();
		assertThat(collectingEvaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_no_namespace_location_in_control() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		// when
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);

		// then
		assertThat(collectingEvaluator.different).isFalse();
		assertThat(collectingEvaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	/**
	 * XMLUnit 1.3 jumps from the document node straight to the root element,
	 * ignoring any other children the document might have. Some people consider
	 * this a bug (Issue 2770386) others rely on it.
	 * 
	 * <p>
	 * XMLUnit 2.x doesn't ignore differences in the prelude but we want to keep
	 * the behavior for the legacy code base.
	 * </p>
	 */
	@Test
	public void should_ignore_missing_elements_between_doc_and_root_element_in_test() throws Exception {
		// given
		String control = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		        + "<!-- some comment -->"
		        + "<?foo some PI ?>"
		        + "<bar/>";

		String test = "<bar/>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).isEmpty();

	}

	@Test
	public void should_ignore_missing_elements_between_doc_and_root_element_in_control() throws Exception {
		// given
		String test =
		        "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		                + "<!-- some comment -->"
		                + "<?foo some PI ?>"
		                + "<bar/>";

		String control = "<bar/>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).isEmpty();
	}

	@Test
	public void should_ignore_different_elements_between_doc_and_root() throws Exception {
		// given
		String control = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		        + "<!-- some comment -->"
		        + "<?foo some PI ?>"
		        + "<bar/>";

		String test = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		        + "<?foo some other PI ?>"
		        + "<!-- some other comment -->"
		        + "<bar/>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).isEmpty();
	}

	@Test
	public void should_verify_number_of_calls_on_match_liteners() throws Exception {
		// given
		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");

		ComparisonListener mockListener1 = mock(ComparisonListener.class);
		ComparisonListener mockListener2 = mock(ComparisonListener.class);

		// when
		engine.addMatchListener(mockListener1);
		engine.addMatchListener(mockListener2);
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
		// NODE_TYPE(Element), NAMESPACE_URI(none),
		// NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
		// ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
		// SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
		verify(mockListener1, times(8)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
		verify(mockListener2, times(8)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
	}

	@Test
	public void should_obtain_xpath_of_extra_node() throws Exception {
		// TODO Investigate difference
		// given
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"1\"/></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);

		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/item[2]");
		assertThat(secondDifference.getControlDetails().getValue()).isEqualTo("item");
		assertThat(secondDifference.getTestDetails().getXpath()).isNull();
		assertThat(secondDifference.getTestDetails().getValue()).isNull();
	}
}
