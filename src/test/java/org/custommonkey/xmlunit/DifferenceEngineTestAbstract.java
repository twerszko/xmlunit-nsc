package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DifferenceEvaluator;
import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DifferenceEngineTestAbstract {
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

	protected CollectingDifferenceEvaluator evaluator;
	protected final static ElementSelector DEFAULT_ELEMENT_QUALIFIER = new ElementNameSelector();
	protected DifferenceEngineContract engine;
	protected Document document;

	protected XmlUnitProperties properties;
	protected DocumentUtils documentUtils;

	protected abstract DifferenceEngineContract newDifferenceEngine();

	protected abstract DifferenceEngineContract newDifferenceEngine(XmlUnitProperties properties);

	@Before
	public void setUp() throws Exception {
		properties = new XmlUnitProperties();
		documentUtils = new DocumentUtils(properties);

		resetEvaluator();
		engine = newDifferenceEngine();
		DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
		document = documentBuilder.newDocument();
	}

	protected void resetEvaluator() {
		evaluator = new CollectingDifferenceEvaluator();
	}

	protected void listenToDifferences(String control, String test) throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);
		DifferenceEvaluator evaluator = new StoppingOnFirstNotRecoverableDifferenceEvaluator(this.evaluator);
		engine.compare(controlDoc, testDoc, evaluator, DEFAULT_ELEMENT_QUALIFIER);
	}

	protected List<Comparison> checkDifferences(String control, String test) throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);
		ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
		engine.compare(controlDoc, testDoc, evaluator, DEFAULT_ELEMENT_QUALIFIER);
		return evaluator.getDifferences();
	}

	protected void listenToAllDifferences(String control, String test) throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);
		DifferenceEvaluator evaluator = new NeverStoppingDifferenceEvaluator(this.evaluator);
		engine.compare(controlDoc, testDoc, evaluator, DEFAULT_ELEMENT_QUALIFIER);
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
			engine.compare(control, test, evaluator, null);

			assertThat(evaluator.comparingWhat).isNotNull();
			assertThat(evaluator.nodesSkipped).isFalse();

			resetEvaluator();
			engine.compare(control, control, evaluator, null);
			assertThat(evaluator.comparingWhat).isNull();

			control = control.getNextSibling();
			test = test.getNextSibling();
		}
	}

	@Test
	public void should_detect_different_comments() {
		// given
		Element control = document.createElement("foo");
		Comment controlComment = document.createComment("bar");
		control.appendChild(controlComment);

		Element test = document.createElement("foo");
		Comment testComment = document.createComment("baz");
		test.appendChild(testComment);

		ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();

		// when
		engine.compare(control, test, evaluator, null);
		ArrayList<Comparison> differences = evaluator.getDifferences();

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.COMMENT_VALUE);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/comment()[1]");
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/comment()[1]");
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("baz");
	}

	@Test
	public void should_ignore_different_comments() {
		// given
		properties.setIgnoreComments(true);
		engine = newDifferenceEngine(properties);

		Element control = document.createElement("foo");
		Comment controlComment = document.createComment("bar");
		control.appendChild(controlComment);

		Element test = document.createElement("foo");
		Comment testComment = document.createComment("baz");
		test.appendChild(testComment);

		ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.getDifferences()).isEmpty();
	}

	@Test
	public void should_detect_extra_comment_in_control() {
		// given
		Element control = document.createElement("foo");
		Comment controlComment = document.createComment("bar");
		control.appendChild(controlComment);
		Element controlChild = document.createElement("baz");
		control.appendChild(controlChild);

		Element test = document.createElement("foo");
		Element testChild = document.createElement("baz");
		test.appendChild(testChild);

		// when
		DifferenceEvaluator evaluator = new StoppingOnFirstNotRecoverableDifferenceEvaluator(this.evaluator);
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(this.evaluator.different).isTrue();
	}

	@Test
	public void should_detect_extra_comment_in_test() {
		// given
		Element control = document.createElement("foo");
		Element testChild = document.createElement("baz");
		control.appendChild(testChild);

		Element test = document.createElement("foo");
		Comment controlComment = document.createComment("bar");
		test.appendChild(controlComment);
		Element controlChild = document.createElement("baz");
		test.appendChild(controlChild);

		// when
		DifferenceEvaluator evaluator = new StoppingOnFirstNotRecoverableDifferenceEvaluator(this.evaluator);
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(this.evaluator.different).isTrue();
	}

	@Test
	public void should_ignore_extra_comment() {
		// given
		properties.setIgnoreComments(true);
		engine = newDifferenceEngine(properties);

		Element control = document.createElement("foo");
		Comment controlComment = document.createComment("bar");
		control.appendChild(controlComment);
		Element controlChild = document.createElement("baz");
		control.appendChild(controlChild);

		Element test = document.createElement("foo");
		Element testChild = document.createElement("baz");
		test.appendChild(testChild);

		// when
		DifferenceEvaluator evaluator = new StoppingOnFirstNotRecoverableDifferenceEvaluator(this.evaluator);
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(this.evaluator.different).isFalse();
	}

	@Test
	public void should_detect_missing_attribute() throws Exception {
		// given
		Element control = document.createElement("foo");
		control.setAttribute("bar", "baz");

		Element test = document.createElement("foo");
		test.setAttribute("baz", "bar");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isTrue();
		assertThat(evaluator.comparingWhat).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
	}

	@Test
	public void should_ignore_different_schema_location() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = document.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		Element test = document.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "baz");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isFalse();
		assertThat(evaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_different_no_namespace_schema_location() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = document.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		Element test = document.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "baz");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isFalse();
		assertThat(evaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_schema_location_in_test() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = document.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
		Element test = document.createElement("foo");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isFalse();
		assertThat(evaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_schema_location_in_control() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isFalse();
		assertThat(evaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_no_namespace_location_in_test() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = document.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
		Element test = document.createElement("foo");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isFalse();
		assertThat(evaluator.comparingWhat).isEqualTo(expectedComparisonType);
	}

	@Test
	public void should_ignore_missing_no_namespace_location_in_control() {
		// given
		ComparisonType expectedComparisonType = ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		// when
		engine.compare(control, test, evaluator, null);

		// then
		assertThat(evaluator.different).isFalse();
		assertThat(evaluator.comparingWhat).isEqualTo(expectedComparisonType);
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
		assertThat(evaluator.different).isFalse();
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
		assertThat(evaluator.different).isFalse();

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
		assertThat(evaluator.different).isFalse();
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
		assertThat(evaluator.different).isFalse();
	}

	@Test
	public void should_obtain_xpath_of_first() throws Exception {
		// given
		String control = "<dvorak><keyboard/><composer/></dvorak>";
		String test = "<qwerty><keyboard/></qwerty>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/dvorak[1]");
		assertThat(evaluator.testXpath).isEqualTo("/qwerty[1]");
	}

	@Test
	public void should_obtain_xpath_of_first_child() throws Exception {
		// given
		String start = "<a>", end = "</a>";
		String control = "<dvorak><keyboard/><composer/></dvorak>";
		String test = "<qwerty><keyboard/></qwerty>";

		String contrlXml = start + control + end;
		String testXml = start + test + end;

		// when
		listenToDifferences(contrlXml, testXml);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/a[1]/dvorak[1]");
		assertThat(evaluator.testXpath).isEqualTo("/a[1]/qwerty[1]");
	}

	@Test
	public void should_obtain_xpath_of_first_child_attribute() throws Exception {
		// given
		String control = "<stuff><wood type=\"rough\"/></stuff>";
		String test = "<stuff><wood type=\"smooth\"/></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/wood[1]/@type");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/wood[1]/@type");
	}

	@Test
	public void should_obtain_xpath_of_second_child_attribute() throws Exception {
		// given
		String control = "<stuff><glass colour=\"clear\"/><glass colour=\"green\"/></stuff>";
		String test = "<stuff><glass colour=\"clear\"/><glass colour=\"blue\"/></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/glass[2]/@colour");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/glass[2]/@colour");
	}

	@Test
	public void should_obtain_xpath_of_second_child_text() throws Exception {
		// given
		String control = "<stuff><wood>maple</wood><wood>oak</wood></stuff>";
		String test = "<stuff><wood>maple</wood><wood>ash</wood></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/wood[2]/text()[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/wood[2]/text()[1]");
	}

	@Test
	public void should_obtain_xpath_of_empty_node() throws Exception {
		// given
		String control = "<stuff><list><wood/><glass/></list><item/></stuff>";
		String test = "<stuff><list><wood/><glass/></list><item>description</item></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/item[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/item[1]");
	}

	@Test
	public void should_obtain_xpath_of_first_child_of_child() throws Exception {
		// given
		String control = "<stuff><list><wood/></list></stuff>";
		String test = "<stuff><list><glass/></list></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/list[1]/wood[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/list[1]/glass[1]");
	}

	@Test
	public void should_obtain_xpath_of_comment() throws Exception {
		// given
		String control = "<stuff><list><!--wood--></list></stuff>";
		String test = "<stuff><list><!--glass--></list></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/list[1]/comment()[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/list[1]/comment()[1]");
	}

	@Test
	public void should_obtain_xpath_of_processing_instruction() throws Exception {
		// given
		String control = "<stuff><list/><?wood rough?><list/></stuff>";
		String test = "<stuff><list/><?glass clear?><list/></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/processing-instruction()[1]");
	}

	@Test
	public void should_obtain_xpath_of_text_with_cdata() throws Exception {
		// given
		String control = "<stuff><list/>list<![CDATA[wood]]></stuff>";
		String test = "<stuff><list/>list<![CDATA[glass]]></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/text()[2]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/text()[2]");
	}

	@Test
	public void should_obtain_xpath_of_different_type_elements() throws Exception {
		// given
		String control = "<stuff><list><item/></list></stuff>";
		String test = "<stuff><list>item text</list></stuff>";

		// when
		listenToDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/list[1]/item[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/list[1]/text()[1]");
	}

	@Test
	public void should_obtain_xpath_of_extra_node() throws Exception {
		// given
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"1\"/></stuff>";

		// when
		listenToAllDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/item[2]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/item[1]");
	}

	@Test
	public void should_obtain_xpath_of_last_difference() throws Exception {
		// given
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><?item data?></stuff>";

		// when
		listenToAllDifferences(control, test);

		// then
		// mutiple Differences, we only see the last one, missing second element
		assertThat(evaluator.comparingWhat).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/item[2]");
		assertThat(evaluator.testXpath).isNull();
	}

	@Test
	public void should_obtain_xpath_of_attribute() throws Exception {
		// given
		String control = "<stuff><thing id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"2\"/><item id=\"1\"/></stuff>";

		// when
		listenToAllDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/item[1]/@id");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/item[2]/@id");
	}

	@Test
	public void should_detect_different_text() throws Exception {
		// given
		String control = "<stuff>string</stuff>";
		String test = "<stuff>  string  </stuff>";

		// when
		List<Comparison> differences = checkDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/text()[1]");
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/text()[1]");
	}

	@Test
	public void should_ignore_different_text_when_whitespace_normalized() throws Exception {
		// given
		String control = "<stuff>string</stuff>";
		String test = "<stuff>  string  </stuff>";

		properties.setNormalizeWhitespace(true);
		engine = newDifferenceEngine(properties);

		// when
		List<Comparison> differences = checkDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_ignore_different_text_when_whitespace_ignored() throws Exception {
		// given
		String control = "<stuff>string</stuff>";
		String test = "<stuff>  string  </stuff>";

		properties.setIgnoreWhitespace(true);
		engine = newDifferenceEngine(properties);

		// when
		List<Comparison> differences = checkDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
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
}
