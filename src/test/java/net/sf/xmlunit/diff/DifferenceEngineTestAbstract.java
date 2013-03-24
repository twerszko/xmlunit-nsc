package net.sf.xmlunit.diff;

import static net.sf.xmlunit.TestResources.BOOK_DTD;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import net.sf.xmlunit.NullNode;
import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.XMLConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public abstract class DifferenceEngineTestAbstract {
	protected XmlUnitProperties properties;
	protected DocumentUtils documentUtils;

	protected ListingDifferenceEvaluator evaluator;
	protected DOMDifferenceEngine engine;
	protected Document doc;

	protected abstract DOMDifferenceEngine createEngine();

	@Before
	public void init() throws Exception {
		properties = new XmlUnitProperties();
		documentUtils = new DocumentUtils(properties);

		evaluator = new ListingDifferenceEvaluator();
		engine = createEngine();
		engine.setDifferenceEvaluator(evaluator);
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	}

	protected List<Comparison> findDifferences(String control, String test) throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);

		Source controlSource = Input.fromDocument(controlDoc).build();
		Source testSource = Input.fromDocument(testDoc).build();

		return findDifferences(controlSource, testSource);
	}

	protected List<Comparison> findDifferences(Node control, Node test) {
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();

		return findDifferences(controlSource, testSource);
	}

	protected List<Comparison> findDifferences(Source control, Source test) {
		ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
		engine.setDifferenceEvaluator(evaluator);
		engine.compare(control, test);

		return evaluator.getDifferences();
	}

	protected List<Comparison> findDifferencesWithMatcher(String control, String test, NodeMatcher nodeMatcher)
	        throws Exception {
		Document controlDoc = documentUtils.buildControlDocument(control);
		Document testDoc = documentUtils.buildTestDocument(test);

		Source controlSource = Input.fromDocument(controlDoc).build();
		Source testSource = Input.fromDocument(testDoc).build();

		return findDifferencesWithMatcher(controlSource, testSource, nodeMatcher);
	}

	protected List<Comparison> findDifferencesWithMatcher(Node control, Node test, NodeMatcher nodeMatcher) {
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		return findDifferencesWithMatcher(controlSource, testSource, nodeMatcher);
	}

	protected List<Comparison> findDifferencesWithMatcher(
	        Source controlSource, Source testSource, NodeMatcher nodeMatcher) {
		ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
		engine.setDifferenceEvaluator(evaluator);
		engine.setNodeMatcher(nodeMatcher);
		engine.compare(controlSource, testSource);
		return evaluator.getDifferences();
	}

	private static class DocType extends NullNode implements DocumentType {
		private final String name;
		private final String publicId;
		private final String systemId;

		private DocType(String name, String publicId, String systemId) {
			this.name = name;
			this.publicId = publicId;
			this.systemId = systemId;
		}

		@Override
		public short getNodeType() {
			return Node.DOCUMENT_TYPE_NODE;
		}

		@Override
		public NamedNodeMap getEntities() {
			return null;
		}

		@Override
		public String getInternalSubset() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public NamedNodeMap getNotations() {
			return null;
		}

		@Override
		public String getPublicId() {
			return publicId;
		}

		@Override
		public String getSystemId() {
			return systemId;
		}
	}

	@Test
	public void should_detect_different_doctype_name() {
		// given
		DocumentType control = new DocType("name", "pub", "system");
		DocumentType test = new DocType("name2", "pub", "system");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.DOCTYPE_NAME);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("name");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("name2");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_doctype_public_id() {
		// given
		DocumentType control = new DocType("name", "pub", "system");
		DocumentType test = new DocType("name", "pub2", "system");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.DOCTYPE_PUBLIC_ID);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("pub");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("pub2");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_doctype_system_id() {
		// given
		DocumentType control = new DocType("name", "pub", "system");
		DocumentType test = new DocType("name", "pub", "system2");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.DOCTYPE_SYSTEM_ID);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("system");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("system2");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_schema_location() {
		// given
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "baz");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);

		assertThat(difference.getType()).isEqualTo(ComparisonType.SCHEMA_LOCATION);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/");
		assertThat(difference.getControlDetails().getTarget()).isEqualTo(control);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("bar");
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/");
		assertThat(difference.getTestDetails().getTarget()).isEqualTo(test);
		assertThat(difference.getTestDetails().getValue()).isEqualTo("baz");
	}

	@Test
	public void should_detect_element_differences() {
		// given
		Element control = doc.createElement("foo");
		control.setAttribute("attr1", "value1");

		Element test = doc.createElement("bar");
		test.setAttributeNS(null, "attr1", "value1");
		test.setAttributeNS("urn:xmlunit:test", "attr1", "value1");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(3);

		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(2);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
	}

	@Test
	public void should_detect_different_node_types() {
		// given
		Element control = doc.createElement("x");
		Comment test = doc.createComment("x");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.NODE_TYPE);
	}

	@Test
	public void should_detect_no_differences_when_two_nodes_without_NS() {
		// given
		Element control = doc.createElement("x");
		Element test = doc.createElement("x");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_missing_child_node() throws Exception {
		// given
		Element control = doc.createElement("x");
		control.appendChild(doc.createElement("y"));

		Element test = doc.createElement("x");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
	}

	@Test
	public void should_detect_missing_child_node_2() throws Exception {
		// given
		String start = "<a>", end = "</a>";
		String control = "<dvorak><keyboard/><composer/></dvorak>";
		String test = "<dvorak><keyboard/></dvorak>";

		String contrlXml = start + control + end;
		String testXml = start + test + end;

		// when
		List<Comparison> differences = findDifferences(contrlXml, testXml);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/a[1]/dvorak[1]/composer[1]");
		assertThat(secondDifference.getTestDetails().getXpath()).isNull();
	}

	@Test
	public void should_detect_different_number_of_children() {
		// given
		Element control = doc.createElement("x");
		control.appendChild(doc.createElement("y"));

		Element test = doc.createElement("x");
		test.appendChild(doc.createElement("y"));
		test.appendChild(doc.createElement("y"));

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);

		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo(1);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo(2);
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_children_sequence() {
		// given
		Element control = doc.createElement("foo");
		Element firstControlChild = doc.createElement("bar");
		Element secondControlChild = doc.createElement("baz");
		control.appendChild(firstControlChild);
		control.appendChild(secondControlChild);

		Element test = doc.createElement("foo");
		Element firstTestChild = doc.createElement("bar");
		Element secondTestChild = doc.createElement("baz");
		test.appendChild(secondTestChild);
		test.appendChild(firstTestChild);

		// when
		DefaultNodeMatcher nodeMatcher = new DefaultNodeMatcher(ElementSelectors.byName);
		List<Comparison> differences = findDifferencesWithMatcher(control, test, nodeMatcher);

		// then
		assertThat(differences).hasSize(2);

		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/bar[1]");
		assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/bar[1]");
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/baz[1]");
		assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/baz[1]");
	}

	// TODO Investigate test case name
	@Test
	public void testIssue1027863() throws Exception {
		// given
		String control = "<stuff><item id=\"1\"><thing/></item></stuff>";
		String test = "<stuff><item id=\"2\"/></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(3);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(1);
		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.ATTR_VALUE);

		assertThat(firstDifference.getControlDetails().getValue()).isEqualTo(true);
		assertThat(firstDifference.getTestDetails().getValue()).isEqualTo(false);
		assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/item[1]");
		assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/item[1]");
	}

	@Test
	public void should_detect_different_tag_name_with_byName_selector() {
		// given
		properties.setCompareUnmatched(false);
		engine = createEngine();

		Element control = doc.createElement("foo");
		Element controlChild = doc.createElement("bar");
		control.appendChild(controlChild);

		Element test = doc.createElement("foo");
		Element testChild = doc.createElement("baz");
		test.appendChild(testChild);

		// when
		List<Comparison> differences = findDifferencesWithMatcher(
		        control, test, new DefaultNodeMatcher(ElementSelectors.byName));

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(firstDifference.getControlDetails().getValue()).isEqualTo("bar");
		assertThat(firstDifference.getControlDetails().getTarget()).isEqualTo(controlChild);
		assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/bar[1]");
		assertThat(firstDifference.getTestDetails().getValue()).isEqualTo(null);
		assertThat(firstDifference.getTestDetails().getTarget()).isEqualTo(null);
		assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo(null);

		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(secondDifference.getControlDetails().getValue()).isEqualTo(null);
		assertThat(secondDifference.getControlDetails().getTarget()).isEqualTo(null);
		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo(null);
		assertThat(secondDifference.getTestDetails().getValue()).isEqualTo("baz");
		assertThat(secondDifference.getTestDetails().getTarget()).isEqualTo(testChild);
		assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/baz[1]");
	}

	@Test
	public void should_detect_different_tag_name() {
		// given
		// TODO node matcher
		engine.setNodeMatcher(new DefaultNodeMatcher());
		Element control = doc.createElement("foo");
		Element controlChild = doc.createElement("bar");
		control.appendChild(controlChild);

		Element test = doc.createElement("foo");
		Element testChild = doc.createElement("baz");
		test.appendChild(testChild);

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("bar");
		assertThat(difference.getControlDetails().getTarget()).isEqualTo(controlChild);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/bar[1]");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("baz");
		assertThat(difference.getTestDetails().getTarget()).isEqualTo(testChild);
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/baz[1]");
	}

	@Test
	public void should_detect_different_tags_in_child_node_list() throws Exception {
		// given
		// TODO node matcher
		engine.setNodeMatcher(new DefaultNodeMatcher());
		Document document = new DocumentUtils().buildControlDocument(
		        "<down>" +
		                "<im><standing/>alone</im>" +
		                "<im><watching/>you all</im>" +
		                "</down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control.getNextSibling();

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison tagDifference = differences.get(0);
		Comparison textDifference = differences.get(1);
		assertThat(tagDifference.getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
		assertThat(tagDifference.getControlDetails().getValue()).isEqualTo("standing");
		assertThat(tagDifference.getTestDetails().getValue()).isEqualTo("watching");
		assertThat(textDifference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
	}

	@Test
	public void should_detect_additional_doctype_declaration() throws Exception {
		// given
		Document control = Convert.toDocument(Input.fromMemory(
		        "<Book/>")
		        .build());
		Document test = Convert.toDocument(Input.fromMemory(
		        "<!DOCTYPE Book PUBLIC " + "\"XMLUNIT/TEST/PUB\" " + "\"" + BOOK_DTD.getFile() + "\">" + "<Book/>")
		        .build());

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.HAS_DOCTYPE_DECLARATION);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo(false);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo(true);
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_pi_target() {
		// given
		ProcessingInstruction control = doc.createProcessingInstruction("foo", "1");
		ProcessingInstruction test = doc.createProcessingInstruction("bar", "1");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_TARGET);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("foo");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_pi_data() {
		// given
		ProcessingInstruction control = doc.createProcessingInstruction("foo", "1");
		ProcessingInstruction test = doc.createProcessingInstruction("foo", "2");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_DATA);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("1");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("2");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_processing_instructions() throws Exception {
		// given
		String control = "<stuff><list/><?wood rough?><list/></stuff>";
		String test = "<stuff><list/><?glass clear?><list/></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_TARGET);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_DATA);

		assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(firstDifference.getControlDetails().getValue()).isEqualTo("wood");
		assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(firstDifference.getTestDetails().getValue()).isEqualTo("glass");

		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(secondDifference.getControlDetails().getValue()).isEqualTo("rough");
		assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(secondDifference.getTestDetails().getValue()).isEqualTo("clear");
	}

	@Test
	public void should_detect_missing_attribute() throws Exception {
		// given
		Element control = doc.createElement("foo");
		control.setAttribute("bar", "baz");

		Element test = doc.createElement("foo");
		test.setAttribute("baz", "bar");

		// when
		Source controlSource = Input.fromNode(control).build();
		Source testSource = Input.fromNode(test).build();
		engine.compare(controlSource, testSource);

		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
	}

	@Test
	public void should_detect_different_attribute_values() {
		// given
		Attr control = doc.createAttribute("foo");
		control.setValue("foo");

		Attr test = doc.createAttribute("foo");
		test.setValue("bar");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("foo");
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_attribute_values_2() throws Exception {
		// given
		String control = "<stuff><wood type=\"rough\"/></stuff>";
		String test = "<stuff><wood type=\"smooth\"/></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/wood[1]/@type");
		assertThat(difference.getControlDetails().getValue()).isEqualTo("rough");
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/wood[1]/@type");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("smooth");
	}

	@Test
	public void should_detect_different_attribute_values_3() throws Exception {
		// given
		String control = "<stuff><glass colour=\"clear\"/><glass colour=\"green\"/></stuff>";
		String test = "<stuff><glass colour=\"clear\"/><glass colour=\"blue\"/></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/glass[2]/@colour");
		assertThat(difference.getControlDetails().getValue()).isEqualTo("green");
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/glass[2]/@colour");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("blue");
	}

	@Test
	public void should_detect_extra_comment_in_test() {
		// given
		Element control = doc.createElement("foo");
		Element testChild = doc.createElement("baz");
		control.appendChild(testChild);

		Element test = doc.createElement("foo");
		Comment testComment = doc.createComment("bar");
		test.appendChild(testComment);
		Element controlChild = doc.createElement("baz");
		test.appendChild(controlChild);

		// when

		List<Comparison> differences = findDifferences(control, test);
		assertThat(differences).hasSize(3);

		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(2);
		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(thirdDifference.getControlDetails().getTarget()).isNull();
		assertThat(thirdDifference.getTestDetails().getTarget()).isEqualTo(testComment);
	}

	@Test
	public void should_detect_extra_text_node() throws Exception {
		// given
		String control = "<stuff><list><wood/><glass/></list><item/></stuff>";
		String test = "<stuff><list><wood/><glass/></list><item>description</item></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);

		assertThat(secondDifference.getControlDetails().getXpath()).isNull();
		assertThat(secondDifference.getControlDetails().getValue()).isNull();
		assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/item[1]/text()[1]");
		assertThat(secondDifference.getTestDetails().getValue()).isEqualTo("#text");
	}

	@Test
	public void should_detect_extra_comment_in_control() {
		// given
		Element control = doc.createElement("foo");
		Comment controlComment = doc.createComment("bar");
		control.appendChild(controlComment);
		Element testChild = doc.createElement("baz");
		control.appendChild(testChild);

		Element test = doc.createElement("foo");
		Element controlChild = doc.createElement("baz");
		test.appendChild(controlChild);

		// when

		List<Comparison> differences = findDifferences(control, test);
		assertThat(differences).hasSize(3);

		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(2);
		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(thirdDifference.getControlDetails().getTarget()).isEqualTo(controlComment);
		assertThat(thirdDifference.getTestDetails().getTarget()).isNull();
	}

	@Test
	public void should_detect_different_text() throws Exception {
		// given
		String control = "<stuff>string</stuff>";
		String test = "<stuff>  string  </stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/text()[1]");
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/text()[1]");
	}

	@Test
	public void should_detect_different_text_2() throws Exception {
		// given
		String control = "<stuff><wood>maple</wood><wood>oak</wood></stuff>";
		String test = "<stuff><wood>maple</wood><wood>ash</wood></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/stuff[1]/wood[2]/text()[1]");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/stuff[1]/wood[2]/text()[1]");
	}

	@Test
	public void should_detect_different_text_and_type() {
		// given
		Comment control = doc.createComment("foo");
		Text test = doc.createTextNode("bar");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);

		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.NODE_TYPE);

		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(differences.get(1).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(1).getControlDetails().getValue()).isEqualTo("foo");
		assertThat(differences.get(1).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(1).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(1).getTestDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(1).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_text_in_child_nodes() throws Exception {
		// given
		Document document = new DocumentUtils().buildControlDocument(
		        "<down>" +
		                "<im><watching/>you all</im>" +
		                "<im><watching/>you sinking</im>" +
		                "</down>");

		Node control = document.getDocumentElement().getFirstChild();
		Node test = control.getNextSibling();

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison textDifference = differences.get(0);
		assertThat(textDifference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
		assertThat(textDifference.getControlDetails().getValue()).isEqualTo("you all");
		assertThat(textDifference.getTestDetails().getValue()).isEqualTo("you sinking");
	}

	@Test
	public void should_detect_different_comment_value() {
		// given
		Element control = doc.createElement("foo");
		Comment controlComment = doc.createComment("foo");
		control.appendChild(controlComment);
		Element test = doc.createElement("foo");
		Comment testComment = doc.createComment("bar");
		test.appendChild(testComment);

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.COMMENT_VALUE);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(controlComment);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("foo");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/comment()[1]");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(testComment);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/comment()[1]");
	}

	@Test
	public void should_detect_different_comment_value_2() throws Exception {
		// given
		String control = "<stuff><list><!--wood--></list></stuff>";
		String test = "<stuff><list><!--glass--></list></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.COMMENT_VALUE);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/stuff[1]/list[1]/comment()[1]");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/stuff[1]/list[1]/comment()[1]");
	}

	@Test
	public void should_detect_different_cdata_value() {
		// given
		CDATASection control = doc.createCDATASection("bar");
		CDATASection test = doc.createCDATASection("foo");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CDATA_VALUE);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("bar");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("foo");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_cdata_2() throws Exception {
		// given
		String control = "<stuff><list/>list<![CDATA[wood]]></stuff>";
		String test = "<stuff><list/>list<![CDATA[glass]]></stuff>";

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CDATA_VALUE);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/stuff[1]/text()[2]");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/stuff[1]/text()[2]");
	}

	@Test
	public void should_detect_difference_between_text_and_cdata() {
		// given
		Element control = doc.createElement("foo");
		Text fooText = doc.createTextNode("foo");
		control.appendChild(fooText);
		Element test = doc.createElement("foo");
		CDATASection fooCDATASection = doc.createCDATASection("foo");
		test.appendChild(fooCDATASection);

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.NODE_TYPE);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo(Node.TEXT_NODE);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(fooText);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/text()[1]");
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo(Node.CDATA_SECTION_NODE);
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(fooCDATASection);
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/text()[1]");
	}

	@Test
	public void should_detect_multiple_differences() throws Exception {
		// given
		properties.setCompareUnmatched(false);
		engine = createEngine();

		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><?item data?></stuff>";

		// when
		List<Comparison> differences = findDifferencesWithMatcher(control, test, new DefaultNodeMatcher());

		// then
		assertThat(differences).hasSize(4);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(2);
		Comparison fourthDifference = differences.get(3);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]");
		assertThat(firstDifference.getControlDetails().getValue()).isEqualTo(2);
		assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/stuff[1]");
		assertThat(firstDifference.getTestDetails().getValue()).isEqualTo(1);

		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/item[1]");
		assertThat(secondDifference.getControlDetails().getValue()).isEqualTo("item");
		assertThat(secondDifference.getTestDetails().getXpath()).isNull();
		assertThat(secondDifference.getTestDetails().getValue()).isNull();

		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(thirdDifference.getControlDetails().getXpath()).isEqualTo("/stuff[1]/item[2]");
		assertThat(thirdDifference.getControlDetails().getValue()).isEqualTo("item");
		assertThat(thirdDifference.getTestDetails().getXpath()).isNull();
		assertThat(thirdDifference.getTestDetails().getValue()).isNull();

		assertThat(fourthDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(fourthDifference.getControlDetails().getXpath()).isNull();
		assertThat(fourthDifference.getControlDetails().getValue()).isNull();
		assertThat(fourthDifference.getTestDetails().getXpath()).isEqualTo("/stuff[1]/processing-instruction()[1]");
		assertThat(fourthDifference.getTestDetails().getValue()).isEqualTo("item");
	}

	@Test
	public void should_detect_multiple_differences_2() throws Exception {
		// given
		String control = "<dvorak><keyboard/><composer/></dvorak>";
		String test = "<qwerty><keyboard/></qwerty>";

		// when
		List<Comparison> differences = findDifferencesWithMatcher(control, test, new DefaultNodeMatcher());

		// then
		assertThat(differences).hasSize(3);

		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(2);

		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);

		assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/dvorak[1]");
		assertThat(secondDifference.getControlDetails().getValue()).isEqualTo("dvorak");
		assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/qwerty[1]");
		assertThat(secondDifference.getTestDetails().getValue()).isEqualTo("qwerty");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_when_setting_null_evaluator() throws Exception {
		// given
		engine.setDifferenceEvaluator(null);
		// then exception
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_when_adding_null_match_listener() throws Exception {
		// given
		engine.addMatchListener(null);
		// then exception
	}

	@Test
	public void should_ignore_missing_schema_location_in_test() {
		// given
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
		Element test = doc.createElement("foo");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.SCHEMA_LOCATION);
	}

	@Test
	public void should_ignore_missing_schema_location_in_control() {
		// given
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.SCHEMA_LOCATION);
	}

	@Test
	public void should_detect_different_elements_between_doc_and_root() throws Exception {
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
		assertThat(differences).hasSize(4);
		Comparison firstDifference = differences.get(0);
		Comparison secondDifference = differences.get(1);
		Comparison thirdDifference = differences.get(2);
		Comparison fourthDifference = differences.get(3);
		assertThat(firstDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(secondDifference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(thirdDifference.getType()).isEqualTo(ComparisonType.COMMENT_VALUE);
		assertThat(fourthDifference.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_DATA);
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

	@Test
	public void should_detect_missing_no_namespace_location_in_control() {
		// given
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);

		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
		assertThat(difference.getControlDetails().getValue()).isNull();
		assertThat(difference.getTestDetails().getValue()).isEqualTo("bar");
	}

	@Test
	public void should_detect_missing_no_namespace_location_in_test() {
		// given
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
		Element test = doc.createElement("foo");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);

		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("bar");
		assertThat(difference.getTestDetails().getValue()).isNull();
	}

	@Test
	public void should_detect_different_no_namespace_schema_location() {
		// given
		String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

		Element control = doc.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");

		Element test = doc.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "baz");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);

		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("bar");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("baz");
	}
}
