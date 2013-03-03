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
package net.sf.xmlunit.diff.strategies;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DOMDifferenceEngine;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;
import net.sf.xmlunit.diff.OrderPreservingNamedNodeMap;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CompareElementStrategyTest {
    private final DocumentUtils documentUtils = new DocumentUtils();
    private final DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
    private final Document document = documentBuilder.newDocument();

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
        DOMDifferenceEngine engine = new DOMDifferenceEngine();
        engine.setIgnoreAttributeOrder(properties.getIgnoreAttributeOrder());
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

        CompareElementStrategy strategy = new CompareElementStrategy(performer, properties.getIgnoreAttributeOrder());
        Comparisons comparisons = strategy.provideComparisons(
                NodeAndXpath.from(control, new XPathContext()), controlMap,
                NodeAndXpath.from(test, new XPathContext()), testMap);
        strategy.executeComparisons(comparisons);
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

    @Test
    public void should_detect_no_tag_name_differences() {
        // given
        Element control = document.createElement("foo");
        Element test = document.createElement("foo");

        // when
        List<Comparison> differences = findElementDifferences(control, test);

        // then
        assertThat(differences).hasSize(0);
    }

    @Test
    public void should_detect_different_tag_name() {
        // given
        Element control = document.createElement("foo");
        Element test = document.createElement("bar");

        // when
        List<Comparison> differences = findElementDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
        assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("foo");
        assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
        assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
        assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("bar");
        assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
        assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");
    }

    @Test
    public void should_detect_different_number_of_attributes() {
        // given
        Element control = document.createElement("foo");
        control.setAttribute("attr1", "value1");
        Element test = document.createElement("foo");

        // when
        List<Comparison> differences = findElementDifferences(control, test);

        // then
        assertThat(differences).hasSize(2);
        Comparison firstDifference = differences.get(0);
        Comparison secondDifference = differences.get(1);

        assertThat(firstDifference.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
        assertThat(firstDifference.getControlDetails().getValue()).isEqualTo(1);
        assertThat(firstDifference.getControlDetails().getTarget()).isEqualTo(control);
        assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/");
        assertThat(firstDifference.getTestDetails().getValue()).isEqualTo(0);
        assertThat(firstDifference.getTestDetails().getTarget()).isEqualTo(test);
        assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/");

        assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
        assertThat(secondDifference.getControlDetails().getValue()).isEqualTo(true);
        assertThat(secondDifference.getControlDetails().getTarget()).isEqualTo(control);
        assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/@attr1");
        assertThat(secondDifference.getTestDetails().getValue()).isEqualTo(false);
        assertThat(secondDifference.getTestDetails().getTarget()).isEqualTo(test);
        assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/");
    }

    @Test
    public void should_detect_different_attributes() {
        // given
        Element control = document.createElement("foo");
        control.setAttribute("attr1", "value1");
        Element test = document.createElement("foo");
        test.setAttributeNS("urn:xmlunit:test", "attr1", "value1");

        // when
        List<Comparison> differences = findElementDifferences(control, test);

        // then
        assertThat(differences).hasSize(2);
        Comparison firstDifference = differences.get(0);
        Comparison secondDifference = differences.get(1);

        assertThat(firstDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
        assertThat(firstDifference.getControlDetails().getValue()).isEqualTo(true);
        assertThat(firstDifference.getControlDetails().getTarget()).isEqualTo(control);
        assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/@attr1");
        assertThat(firstDifference.getTestDetails().getValue()).isEqualTo(false);
        assertThat(firstDifference.getTestDetails().getTarget()).isEqualTo(test);
        assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/");

        assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
        assertThat(secondDifference.getControlDetails().getValue()).isEqualTo(false);
        assertThat(secondDifference.getControlDetails().getTarget()).isEqualTo(control);
        assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/");
        assertThat(secondDifference.getTestDetails().getValue()).isEqualTo(true);
        assertThat(secondDifference.getTestDetails().getTarget()).isEqualTo(test);
        assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/@attr1");
    }

    @Test
    public void should_detect_different_attributes_when_namespace_different() {
        // given
        Document doc = documentBuilder.newDocument();

        Element control = doc.createElement("foo");
        control.setAttributeNS("urn:xmlunit:test", "attr1", "value1");
        Element test = doc.createElement("foo");
        test.setAttributeNS(null, "attr1", "value1");

        // when
        List<Comparison> differences = findElementDifferences(control, test);

        // then
        assertThat(differences).hasSize(2);
        Comparison firstDifference = differences.get(0);
        Comparison secondDifference = differences.get(1);

        assertThat(firstDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
        assertThat(firstDifference.getControlDetails().getValue()).isEqualTo(true);
        assertThat(firstDifference.getControlDetails().getTarget()).isEqualTo(control);
        assertThat(firstDifference.getControlDetails().getXpath()).isEqualTo("/@attr1");
        assertThat(firstDifference.getTestDetails().getValue()).isEqualTo(false);
        assertThat(firstDifference.getTestDetails().getTarget()).isEqualTo(test);
        assertThat(firstDifference.getTestDetails().getXpath()).isEqualTo("/");

        assertThat(secondDifference.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
        assertThat(secondDifference.getControlDetails().getValue()).isEqualTo(false);
        assertThat(secondDifference.getControlDetails().getTarget()).isEqualTo(control);
        assertThat(secondDifference.getControlDetails().getXpath()).isEqualTo("/");
        assertThat(secondDifference.getTestDetails().getValue()).isEqualTo(true);
        assertThat(secondDifference.getTestDetails().getTarget()).isEqualTo(test);
        assertThat(secondDifference.getTestDetails().getXpath()).isEqualTo("/@attr1");
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

        CompareElementStrategy strategy = new CompareElementStrategy(performer, properties.getIgnoreAttributeOrder());
        Comparisons comparisons = strategy.provideComparisons(
                NodeAndXpath.from(control, new XPathContext()), controlMap,
                NodeAndXpath.from(test, new XPathContext()), testMap);
        strategy.executeComparisons(comparisons);
        return performer.getDifferences();
    }

    private List<Comparison> findElementDifferences(Element controlNode, Element testNode) {
        ListingComparisonPerformer performer = new ListingComparisonPerformer();

        NodeAndXpath<Element> control = NodeAndXpath.from(controlNode);
        NodeAndXpath<Element> test = NodeAndXpath.from(testNode);

        new CompareElementStrategy(performer, false)
                .execute(control, test);
        return performer.getDifferences();
    }
}
