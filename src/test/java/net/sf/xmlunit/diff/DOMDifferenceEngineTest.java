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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.comparators.DOMComparator;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DOMDifferenceEngineTest extends DifferenceEngineTestAbstract {

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

	@Override
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

	@Test
	public void should_notify_listener_once_on_comparison() {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, new Short("2"))
		        .and(null, new Short("2"));

		ComparisonListenerSupportTest.Listener listener =
		        new ComparisonListenerSupportTest.Listener(ComparisonResult.EQUAL);

		engine.addComparisonListener(listener);

		// when
		DOMComparator comparator = engine.createComparator();
		ComparisonResult result = comparator.executeComparison(comparison);

		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(listener.getInvocations()).isEqualTo(1);
		assertEquals(1, listener.getInvocations());
	}

	@Test
	public void should_notify_similarity_listener_once_on_comparison() {
		// given
		Comparison comparison = Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		        .between(null, new Short("2"))
		        .and(null, new Short("2"));

		ComparisonListenerSupportTest.Listener listener =
		        new ComparisonListenerSupportTest.Listener(ComparisonResult.SIMILAR);

		engine.addComparisonListener(listener);

		DifferenceEvaluator evaluator = new DifferenceEvaluator() {
			@Override
			public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
				return ComparisonResult.SIMILAR;
			}
		};
		engine.setDifferenceEvaluator(evaluator);

		// when
		DOMComparator comparator = engine.createComparator();
		ComparisonResult result = comparator.executeComparison(comparison);

		// then
		assertThat(result).isEqualTo(ComparisonResult.SIMILAR);
		assertThat(listener.getInvocations()).isEqualTo(1);
		assertEquals(1, listener.getInvocations());
	}

	@Test
	public void listener_should_stop_comparison_after_first_difference() {
		// given
		Element control = doc.createElement("foo");
		control.appendChild(doc.createTextNode("text for difference"));
		Element test = doc.createElement("bar");

		// when
		ComparisonListener stoppingListener = spy(new ComparisonListener() {
			@Override
			public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
				engine.stop();
			}
		});
		engine.addDifferenceListener(stoppingListener);
		List<Comparison> differences = findDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		verify(stoppingListener, times(1)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
	}
}
