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
import net.sf.xmlunit.diff.DOMDifferenceEngine;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;
import net.sf.xmlunit.diff.OrderPreservingNamedNodeMap;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.commands.CompareElementCommand;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CompareElementCommandTest {
	@Test
	public void should_ignore_different_attribute_sequence() throws Exception {
		// given
		XmlUnitProperties properties = new XmlUnitProperties();

		// when
		List<Comparison> differences = testAttributeSequence(properties);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_differenct_attribute_sequence() throws Exception {
		// given
		XmlUnitProperties properties = new XmlUnitProperties();
		properties.setIgnoreAttributeOrder(false);

		// when
		List<Comparison> differences = testAttributeSequence(properties);

		// then
		assertThat(differences).hasSize(2);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
	}

	private List<Comparison> testAttributeSequence(XmlUnitProperties properties) throws Exception {
		DOMDifferenceEngine engine = new DOMDifferenceEngine(properties);
		DocumentBuilder documentBuilder = new DocumentUtils(properties).newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

		ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
		engine.setDifferenceEvaluator(evaluator);
		ComparisonPerformer performer = engine.getComparisonPerformer();

		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		OrderPreservingNamedNodeMap controlMap = new OrderPreservingNamedNodeMap();
		OrderPreservingNamedNodeMap testMap = new OrderPreservingNamedNodeMap();
		for (int i = 0; i < 2; i++) {
			int j = 1 - i;
			Attr attrI = document.createAttribute("attr" + i);
			attrI.setValue(String.valueOf(i));
			Attr attrJ = document.createAttribute("attr" + j);
			attrJ.setValue(String.valueOf(j));

			control.setAttributeNode(attrI);
			controlMap.add(attrI);
			test.setAttributeNode(attrJ);
			testMap.add(attrJ);
		}
		new CompareElementCommand(performer, properties.getIgnoreAttributeOrder(),
		        NodeAndXpath.from(control, new XPathContext()), controlMap,
		        NodeAndXpath.from(test, new XPathContext()), testMap).execute();
		return evaluator.getDifferences();
	}

	@Test
	public void should_ignore_different_attribute_ns_sequence() throws Exception {
		// given
		XmlUnitProperties properties = new XmlUnitProperties();
		properties.setIgnoreAttributeOrder(true);

		// when
		List<Comparison> differences = testAttributeSequenceNS(properties);

		// then
		assertThat(differences).hasSize(0);
	}

	@Test
	public void should_detect_differenct_ns_attribute_sequence() throws Exception {
		// given
		XmlUnitProperties properties = new XmlUnitProperties();
		properties.setIgnoreAttributeOrder(false);

		// when
		List<Comparison> differences = testAttributeSequenceNS(properties);

		// then
		assertThat(differences).hasSize(2);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
	}

	private List<Comparison> testAttributeSequenceNS(XmlUnitProperties properties) throws Exception {
		DocumentBuilder documentBuilder = new DocumentUtils(properties).newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

		ListingComparisonPerformer performer = new ListingComparisonPerformer();

		Element control = document.createElementNS("ns", "foo");
		Element test = document.createElementNS("ns", "foo");
		OrderPreservingNamedNodeMap controlMap = new OrderPreservingNamedNodeMap();
		OrderPreservingNamedNodeMap testMap = new OrderPreservingNamedNodeMap();
		for (int i = 0; i < 2; i++) {
			int j = 1 - i;
			Attr attrI = document.createAttributeNS("ns", "attr" + i);
			attrI.setValue(String.valueOf(i));
			Attr attrJ = document.createAttributeNS("ns", "attr" + j);
			attrJ.setValue(String.valueOf(j));

			control.setAttributeNode(attrI);
			controlMap.add(attrI);
			test.setAttributeNode(attrJ);
			testMap.add(attrJ);
		}

		new CompareElementCommand(performer, properties.getIgnoreAttributeOrder(),
		        NodeAndXpath.from(control, new XPathContext()), controlMap,
		        NodeAndXpath.from(test, new XPathContext()), testMap).execute();
		return performer.getDifferences();
	}
}
