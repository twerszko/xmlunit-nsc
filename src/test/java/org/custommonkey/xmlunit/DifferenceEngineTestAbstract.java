package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DifferenceEvaluator;
import net.sf.xmlunit.diff.ElementSelector;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
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

	protected class CollectingDifferenceEvaluator implements DifferenceEvaluator {
		public String expected;
		public String actual;
		public Node control;
		public Node test;
		public ComparisonType comparingWhat = null;
		public boolean different = false;
		public boolean nodesSkipped = false;
		public String controlXpath;
		public String testXpath;

		public ComparisonResult evaluate(Comparison difference, ComparisonResult outcome) {
			assertThat(difference).isNotNull();
			assertThat(difference.getControlDetails()).isNotNull();
			assertThat(difference.getTestDetails()).isNotNull();

			this.expected = String.valueOf(difference.getControlDetails().getValue());
			this.actual = String.valueOf(difference.getTestDetails().getValue());
			this.control = difference.getControlDetails().getTarget();
			this.test = difference.getTestDetails().getTarget();
			this.comparingWhat = difference.getType();
			this.different = !difference.isRecoverable();
			this.controlXpath = difference.getControlDetails().getXpath();
			this.testXpath = difference.getTestDetails().getXpath();
			return ComparisonResult.DIFFERENT;
		}

		public void skippedComparison(Node control, Node test) {
			nodesSkipped = true;
		}
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
