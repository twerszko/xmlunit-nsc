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
import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;

import net.sf.xmlunit.builder.Input;

import org.custommonkey.xmlunit.CollectingDifferenceEvaluator;
import org.custommonkey.xmlunit.XMLConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DefaultDifferenceEngineTest extends DOMDifferenceEngineTestAbstract {
	// TODO consider extracting common part with DOMDifferenceEngineTests

	// TODO refactor this mess

	public final static String TEXT_A = "the pack on my back is aching";
	public final static String TEXT_B = "the straps seem to cut me like a knife";
	public final static String COMMENT_A = "Im no clown I wont back down";
	public final static String COMMENT_B = "dont need you to tell me whats going down";
	public final static String[] PROC_A = { "down", "down down" };
	public final static String[] PROC_B = { "dadada", "down" };
	public final static String CDATA_A = "I'm standing alone, you're weighing the gold";
	public final static String CDATA_B = "I'm watching you sinking... Fools Gold";
	public final static String ATTR_A = "These boots were made for walking";
	public final static String ATTR_B = "The marquis de sade never wore no boots like these";

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

	protected void listenToDifferences(String control, String test) throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);

		DifferenceEvaluator evaluator = new StoppingOnFirstNotRecoverableDifferenceEvaluator(this.collectingEvaluator);
		engine.setDifferenceEvaluator(evaluator);

		Source controlSource = Input.fromDocument(controlDoc).build();
		Source testSource = Input.fromDocument(testDoc).build();
		engine.compare(controlSource, testSource);
	}

	protected void listenToAllDifferences(String control, String test) throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);

		DifferenceEvaluator evaluator = new NeverStoppingDifferenceEvaluator(this.collectingEvaluator);
		engine.setDifferenceEvaluator(evaluator);
		Source controlSource = Input.fromDocument(controlDoc).build();
		Source testSource = Input.fromDocument(testDoc).build();
		engine.compare(controlSource, testSource);
	}

	@Test
	public void should_compare_nodes() throws Exception {
		// given
		Document controlDocument = documentUtils.buildControlDocument("<root>"
		        + "<!-- " + COMMENT_A + " -->"
		        + "<?" + PROC_A[0] + " " + PROC_A[1] + " ?>"
		        + "<elem attr=\"" + ATTR_A + "\">" + TEXT_A + "</elem></root>");
		Document testDocument = documentUtils.buildTestDocument("<root>"
		        + "<!-- " + COMMENT_B + " -->"
		        + "<?" + PROC_B[0] + " " + PROC_B[1] + " ?>"
		        + "<elem attr=\"" + ATTR_B + "\">" + TEXT_B + "</elem></root>");

		// when - then
		Node control = controlDocument.getDocumentElement().getFirstChild();
		Node test = testDocument.getDocumentElement().getFirstChild();
		while (control != null) {
			engine.setDifferenceEvaluator(collectingEvaluator);
			Source controlSource = Input.fromNode(control).build();
			Source testSource = Input.fromNode(test).build();
			engine.compare(controlSource, testSource);

			assertThat(collectingEvaluator.comparingWhat).isNotNull();
			assertThat(collectingEvaluator.nodesSkipped).isFalse();

			resetEvaluator();
			engine.setDifferenceEvaluator(collectingEvaluator);
			controlSource = Input.fromNode(control).build();
			engine.compare(controlSource, controlSource);
			assertThat(collectingEvaluator.comparingWhat).isNull();

			control = control.getNextSibling();
			test = test.getNextSibling();
		}
	}

	@Test
	public void should_ignore_different_schema_location() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

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
	 * @see http 
	 *      ://sourceforge.net/forum/forum.php?thread_id=3284504&forum_id=73274
	 */
	@Test
	public void should_ignore_namespace_attribute_differences() throws Exception {
		// given
		String control = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		        + "<ns0:Message xmlns:ns0 = \"http://mynamespace\">"
		        + "<ns0:EventHeader>"
		        + "<ns0:EventID>9999</ns0:EventID>"
		        + "<ns0:MessageID>1243409665297</ns0:MessageID>"
		        + "<ns0:MessageVersionID>1.0</ns0:MessageVersionID>"
		        + "<ns0:EventName>TEST-EVENT</ns0:EventName>"
		        + "<ns0:BWDomain>TEST</ns0:BWDomain>"
		        + "<ns0:DateTimeStamp>2009-01-01T12:00:00</ns0:DateTimeStamp>"
		        + "<ns0:SchemaPayloadRef>anything</ns0:SchemaPayloadRef>"
		        + "<ns0:MessageURI>anything</ns0:MessageURI>"
		        + "<ns0:ResendFlag>F</ns0:ResendFlag>"
		        + "</ns0:EventHeader>"
		        + "<ns0:EventBody>"
		        + "<ns0:XMLContent>"
		        + "<xyz:root xmlns:xyz=\"http://test.com/xyz\">"
		        + "<xyz:test1>A</xyz:test1>"
		        + "<xyz:test2>B</xyz:test2>"
		        + "</xyz:root>"
		        + "</ns0:XMLContent>"
		        + "</ns0:EventBody>"
		        + "</ns0:Message>";

		String test = "<abc:Message xmlns:abc=\"http://mynamespace\" xmlns:xyz=\"http://test.com/xyz\">"
		        + "<abc:EventHeader>"
		        + "<abc:EventID>9999</abc:EventID>"
		        + "<abc:MessageID>1243409665297</abc:MessageID>"
		        + "<abc:MessageVersionID>1.0</abc:MessageVersionID>"
		        + "<abc:EventName>TEST-EVENT</abc:EventName>"
		        + "<abc:BWDomain>TEST</abc:BWDomain>"
		        + "<abc:DateTimeStamp>2009-01-01T12:00:00</abc:DateTimeStamp>"
		        + "<abc:SchemaPayloadRef>anything</abc:SchemaPayloadRef>"
		        + "<abc:MessageURI>anything</abc:MessageURI>"
		        + "<abc:ResendFlag>F</abc:ResendFlag>"
		        + "</abc:EventHeader>"
		        + "<abc:EventBody>"
		        + "<abc:XMLContent>"
		        + "<xyz:root>"
		        + "<xyz:test1>A</xyz:test1>"
		        + "<xyz:test2>B</xyz:test2>"
		        + "</xyz:root>"
		        + "</abc:XMLContent>"
		        + "</abc:EventBody>"
		        + "</abc:Message>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.different).isFalse();
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
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.different).isFalse();

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
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.different).isFalse();
	}

	@Test
	public void hould_ignore_different_elements_between_doc_and_root() throws Exception {
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
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.different).isFalse();
	}

	@Test
	public void should_obtain_xpath_of_first_child_attribute() throws Exception {
		// given
		String control = "<stuff><wood type=\"rough\"/></stuff>";
		String test = "<stuff><wood type=\"smooth\"/></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.controlXpath).isEqualTo("/stuff[1]/wood[1]/@type");
		assertThat(collectingEvaluator.testXpath).isEqualTo("/stuff[1]/wood[1]/@type");
	}

	@Test
	public void should_obtain_xpath_of_second_child_attribute() throws Exception {
		// given
		String control = "<stuff><glass colour=\"clear\"/><glass colour=\"green\"/></stuff>";
		String test = "<stuff><glass colour=\"clear\"/><glass colour=\"blue\"/></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.controlXpath).isEqualTo("/stuff[1]/glass[2]/@colour");
		assertThat(collectingEvaluator.testXpath).isEqualTo("/stuff[1]/glass[2]/@colour");
	}

	@Test
	public void should_obtain_xpath_of_empty_node() throws Exception {
		// given
		String control = "<stuff><list><wood/><glass/></list><item/></stuff>";
		String test = "<stuff><list><wood/><glass/></list><item>description</item></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.controlXpath).isEqualTo("/stuff[1]/item[1]");
		assertThat(collectingEvaluator.testXpath).isEqualTo("/stuff[1]/item[1]");
	}

	@Test
	public void should_obtain_xpath_of_processing_instruction() throws Exception {
		// given
		String control = "<stuff><list/><?wood rough?><list/></stuff>";
		String test = "<stuff><list/><?glass clear?><list/></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(collectingEvaluator.controlXpath).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(collectingEvaluator.testXpath).isEqualTo("/stuff[1]/processing-instruction()[1]");
	}

	protected class StoppingOnFirstNotRecoverableDifferenceEvaluator implements DifferenceEvaluator {

		private final DifferenceEvaluator delegation;

		public StoppingOnFirstNotRecoverableDifferenceEvaluator(DifferenceEvaluator delegation) {
			this.delegation = delegation;
		}

		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			ComparisonResult result = delegation.evaluate(comparison, outcome);
			if (!comparison.isRecoverable()) {
				result = ComparisonResult.CRITICAL;
			}
			return result;
		}
	}

	protected class NeverStoppingDifferenceEvaluator implements DifferenceEvaluator {
		private final DifferenceEvaluator delegation;

		public NeverStoppingDifferenceEvaluator(DifferenceEvaluator delegation) {
			this.delegation = delegation;
		}

		@Override
		public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
			ComparisonResult result = delegation.evaluate(comparison, outcome);
			if (result == ComparisonResult.CRITICAL) {
				result = ComparisonResult.DIFFERENT;
			}
			return result;
		}
	}

	@Test
	public void testMatchTrackerSetViaConstructor() throws Exception {
		// TODO duplicated test - maybe should check 2 different listeners?
		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");

		final int[] count = new int[1];
		DefaultDifferenceEngine d = new DefaultDifferenceEngine(properties);
		d.setNodeMatcher(DEFAULT_MATCHER);
		d.addMatchListener(
		        new ComparisonListener() {
			        @Override
			        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
				        count[0]++;
			        }
		        });
		d.setDifferenceEvaluator(collectingEvaluator);
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		d.compare(controlSource, testSource);
		// NODE_TYPE(Element), NAMESPACE_URI(none),
		// NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
		// ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
		// SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
		assertEquals(8, count[0]);
	}

	@Test
	public void testMatchTrackerSetViaSetter() throws Exception {
		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		final int[] count = new int[1];
		engine.setDifferenceEvaluator(collectingEvaluator);
		engine.addMatchListener(new ComparisonListener() {
			@Override
			public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
				count[0]++;
			}
		});
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);
		// NODE_TYPE(Element), NAMESPACE_URI(none),
		// NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
		// ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
		// SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
		assertEquals(8, count[0]);
	}

	@Test
	public void should_obtain_xpath_of_extra_node() throws Exception {
		// TODO Investigate difference
		// given
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"1\"/></stuff>";

		// when
		listenToAllDifferences(control, test);

		// then
		assertThat(collectingEvaluator.controlXpath).isEqualTo("/stuff[1]/item[2]");
		// this is different from DifferenceEngine - the test node is null
		// if there is no match
		assertThat(collectingEvaluator.testXpath).isNull();
	}
}
