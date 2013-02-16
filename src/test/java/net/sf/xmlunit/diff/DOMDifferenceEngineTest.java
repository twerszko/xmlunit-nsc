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

import static net.sf.xmlunit.TestResources.BOOK_DTD;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.xmlunit.NullNode;
import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.util.Convert;

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

public class DOMDifferenceEngineTest {

    private DOMDifferenceEngine engine;
    private Document doc;

    @Before
    public void createDoc() throws Exception {
        engine = new DOMDifferenceEngine(null);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
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
    public void should_detect_different_comment_value() {
        // given
        Comment control = doc.createComment("foo");
        Comment test = doc.createComment("bar");

        // when
        List<Comparison> differences = findDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.COMMENT_VALUE);
        assertThat(differences.get(0).getControlDetails().getTarget()).isEqualTo(control);
        assertThat(differences.get(0).getControlDetails().getValue()).isEqualTo("foo");
        assertThat(differences.get(0).getControlDetails().getXpath()).isEqualTo("/");
        assertThat(differences.get(0).getTestDetails().getTarget()).isEqualTo(test);
        assertThat(differences.get(0).getTestDetails().getValue()).isEqualTo("bar");
        assertThat(differences.get(0).getTestDetails().getXpath()).isEqualTo("/");

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
    public void should_detect_different_text_value() {
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
    public void should_detect_different_tag_name_with_byName_selector() {
        // given
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
    public void should_detect_equal_string_values() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

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
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

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
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

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
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

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
    public void should_detect_different_tags_in_child_node_list() throws Exception {
        // given
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

    private List<Comparison> findDifferences(Node control, Node test) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);

        engine.compareNodes(
                NodeAndXpath.from(control),
                NodeAndXpath.from(test));

        return evaluator.getDifferences();
    }

    private List<Comparison> findDifferencesWithMatcher(Node control, Node test, NodeMatcher nodeMatcher) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);
        engine.setNodeMatcher(nodeMatcher);

        engine.compareNodes(
                NodeAndXpath.from(control),
                NodeAndXpath.from(test));

        return evaluator.getDifferences();
    }

    // TODO Refactor tests below

    private static class ResultGrabber implements DifferenceEvaluator {
        private ComparisonResult outcome = ComparisonResult.CRITICAL;

        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            this.outcome = outcome;
            return outcome;
        }
    }

    @Test
    public void should_compare_two_nulls() {
        // given
        ResultGrabber grabber = new ResultGrabber();
        engine.setDifferenceEvaluator(grabber);

        Comparison comparison =
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(null, null)
                        .and(null, null);

        // then
        assertEquals(ComparisonResult.EQUAL, engine.performComparison(comparison));
        assertEquals(ComparisonResult.EQUAL, grabber.outcome);
    }

    @Test
    public void should_compare_control_null_and_test_non_null() {
        // given
        ResultGrabber grabber = new ResultGrabber();
        engine.setDifferenceEvaluator(grabber);
        Comparison comparison =
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(null, null)
                        .and(null, "");

        // then
        assertEquals(ComparisonResult.DIFFERENT, engine.performComparison(comparison));
        assertEquals(ComparisonResult.DIFFERENT, grabber.outcome);
    }

    @Test
    public void should_compare_control_non_null_and_test_null() {
        // given
        ResultGrabber grabber = new ResultGrabber();
        engine.setDifferenceEvaluator(grabber);
        Comparison comparison =
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(null, "")
                        .and(null, null);

        // then
        assertEquals(ComparisonResult.DIFFERENT, engine.performComparison(comparison));
        assertEquals(ComparisonResult.DIFFERENT, grabber.outcome);
    }

    @Test
    public void compareTwoDifferentNonNulls() {
        ResultGrabber g = new ResultGrabber();
        engine.setDifferenceEvaluator(g);
        assertEquals(ComparisonResult.DIFFERENT,
                engine.performComparison(
                        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                                .between(null, new Short("1"))
                                .and(null, new Short("2"))));
        assertEquals(ComparisonResult.DIFFERENT, g.outcome);
    }

    @Test
    public void compareTwoEqualNonNulls() {
        ResultGrabber g = new ResultGrabber();
        engine.setDifferenceEvaluator(g);
        assertEquals(ComparisonResult.EQUAL,
                engine.performComparison(
                        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                                .between(null, new Short("2"))
                                .and(null, new Short("2"))));
        assertEquals(ComparisonResult.EQUAL, g.outcome);
    }

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
}
