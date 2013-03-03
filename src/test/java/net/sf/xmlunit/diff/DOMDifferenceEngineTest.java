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

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DOMDifferenceEngineTest extends DOMDifferenceEngineTestAbstract {

	@Override
	protected DOMDifferenceEngine createEngine() {
		return new DOMDifferenceEngine();
	}

	@Test
	public void should_detect_different_xml_version() throws Exception {
		// given
		Document control = Convert.toDocument(Input.fromMemory(
		        "<?xml version=\"1.0\"" + " encoding=\"UTF-8\"?>" + "<Book/>")
		        .build());
		Document test = Convert.toDocument(Input.fromMemory(
		        "<?xml version=\"1.1\"" + " encoding=\"UTF-8\"?>" + "<Book/>")
		        .build());

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.XML_VERSION);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("1.0");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("1.1");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_xml_standalone() throws Exception {
		// given
		Document control = Convert.toDocument(Input.fromMemory(
		        "<?xml version=\"1.0\"" + " standalone=\"yes\"?>" + "<Book/>")
		        .build());
		Document test = Convert.toDocument(Input.fromMemory(
		        "<?xml version=\"1.0\"" + " standalone=\"no\"?>" + "<Book/>")
		        .build());

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.XML_STANDALONE);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo(true);
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo(false);
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_xml_encoding() throws Exception {
		// given
		Document control = Convert.toDocument(Input.fromMemory(
		        "<?xml version=\"1.0\"" + " encoding=\"UTF-8\"?>" + "<Book/>")
		        .build());
		Document test = Convert.toDocument(Input.fromMemory(
		        "<?xml version=\"1.0\"" + " encoding=\"UTF-16\"?>" + "<Book/>")
		        .build());

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.XML_ENCODING);
		assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
		assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("UTF-8");
		assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
		assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
		assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("UTF-16");
		assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_schema_location() {
		// given
		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		control.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "somewhere");
		test.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "somewhere else");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.SCHEMA_LOCATION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("somewhere");
		assertThat(difference.getControlDetails().getTarget()).isEqualTo(control);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("somewhere else");
		assertThat(difference.getTestDetails().getTarget()).isEqualTo(test);
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_no_namespace_schema_location() {
		// given
		Element control = doc.createElement("foo");
		Element test = doc.createElement("foo");
		control.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation", "somewhere");
		test.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation", "somewhere else");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("somewhere");
		assertThat(difference.getControlDetails().getTarget()).isEqualTo(control);
		assertThat(difference.getControlDetails().getXpath()).isEqualTo("/");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("somewhere else");
		assertThat(difference.getTestDetails().getTarget()).isEqualTo(test);
		assertThat(difference.getTestDetails().getXpath()).isEqualTo("/");
	}

	@Test
	public void should_detect_different_namespace_uri() throws Exception {
		DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

		String namespaceA = "http://example.org/StoneRoses";
		String namespaceB = "http://example.org/Stone/Roses";
		String prefixA = "music";
		String elemName = "nowPlaying";

		Element control = document.createElementNS(namespaceA, prefixA + ':' + elemName);
		Element test = document.createElementNS(namespaceB, prefixA + ':' + elemName);

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_URI);
	}

	@Test
	public void should_detect_different_namespace_prefix() {
		// given
		Element control = doc.createElementNS("urn:xmlunit:test", "foo");
		control.setPrefix("p1");
		Element test = doc.createElementNS("urn:xmlunit:test", "foo");
		test.setPrefix("p2");

		// when
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);

		Comparison difference = differences.get(0);
		assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_PREFIX);
		assertThat(difference.getControlDetails().getValue()).isEqualTo("p1");
		assertThat(difference.getTestDetails().getValue()).isEqualTo("p2");
	}

	// TODO Refactor tests below

	@Test
	public void compareNotifiesListener() {
		ComparisonListenerSupportTest.Listener l =
		        new ComparisonListenerSupportTest.Listener(ComparisonResult.EQUAL);
		engine.addComparisonListener(l);
		assertEquals(ComparisonResult.EQUAL,
		        engine.performComparison(
		                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		                        .between(null, new Short("2"))
		                        .and(null, new Short("2"))));
		assertEquals(1, l.getInvocations());
	}

	@Test
	public void compareUsesResultOfEvaluator() {
		ComparisonListenerSupportTest.Listener l =
		        new ComparisonListenerSupportTest.Listener(ComparisonResult.SIMILAR);
		engine.addComparisonListener(l);
		engine.setDifferenceEvaluator(new DifferenceEvaluator() {
			@Override
			public ComparisonResult evaluate(Comparison comparison,
			        ComparisonResult outcome) {
				return ComparisonResult.SIMILAR;
			}
		});
		assertEquals(ComparisonResult.SIMILAR,
		        engine.performComparison(
		                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		                        .between(null, new Short("2"))
		                        .and(null, new Short("2"))));
		assertEquals(1, l.getInvocations());
	}

	@Test
	public void should_detect_equal_string_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.ATTR_NAME_LOOKUP)
		        .between(null, "black")
		        .and(null, "black");

		// when
		ComparisonResult result = engine.performComparison(comparison);

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(evaluator.getDifferences()).hasSize(0);
	}

	@Test
	public void should_detect_different_string_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.ATTR_NAME_LOOKUP)
		        .between(null, "black")
		        .and(null, "white");

		// when
		ComparisonResult result = engine.performComparison(comparison);

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.getDifferences()).hasSize(1);
		assertThat(evaluator.getDifferences()).contains(comparison);
	}

	@Test
	public void should_detect_equal_boolean_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
		        .between(null, true)
		        .and(null, true);

		// when
		ComparisonResult result = engine.performComparison(comparison);

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(evaluator.getDifferences()).hasSize(0);
	}

	@Test
	public void should_detect_different_boolean_values() throws Exception {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
		        .between(null, false)
		        .and(null, true);

		// when
		ComparisonResult result = engine.performComparison(comparison);

		// then
		assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.getDifferences()).hasSize(1);
		assertThat(evaluator.getDifferences()).contains(comparison);
	}

	@Test
	public void should_detect_no_differences_when_two_nulls() {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, null)
		        .and(null, null);

		// when
		ComparisonResult result = engine.performComparison(comparison);
		List<Comparison> differences = evaluator.getDifferences();

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
		ComparisonResult result = engine.performComparison(comparison);
		List<Comparison> differences = evaluator.getDifferences();

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
		ComparisonResult result = engine.performComparison(comparison);
		List<Comparison> differences = evaluator.getDifferences();

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
		ComparisonResult result = engine.performComparison(comparison);
		List<Comparison> differences = evaluator.getDifferences();

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
		ComparisonResult result = engine.performComparison(comparison);
		List<Comparison> differences = evaluator.getDifferences();

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(differences).hasSize(0);
	}
}
