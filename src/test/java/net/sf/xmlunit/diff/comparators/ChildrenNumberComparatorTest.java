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
package net.sf.xmlunit.diff.comparators;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DOMDifferenceEngine;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ChildrenNumberComparatorTest {
    private final DocumentUtils documentUtils = new DocumentUtils();
    private final DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();

    @Test
    public void should_find_no_child_node_list_differences() throws Exception {
        // given
        Document document = documentBuilder.newDocument();

        document = documentUtils.buildControlDocument(
                "<down><im standing=\"alone\"/><im><watching/>you all</im>"
                        + "<im watching=\"you\">sinking</im></down>");

        Node control = document.getDocumentElement().getFirstChild();
        Node test = control;

        // when
        List<Comparison> differences = findNodeChildrenDifferences(control, test);

        // then
        assertThat(differences).hasSize(0);
    }

    @Test
    public void should_find_no_child_node_list_differences2() throws Exception {
        // given
        Document document = documentBuilder.newDocument();

        Element control = document.createElement("root");
        control.appendChild(document.createElement("leafElemA"));
        control.appendChild(document.createElement("leafElemB"));

        Element test = document.createElement("root");
        test.appendChild(document.createElement("leafElemB"));
        test.appendChild(document.createElement("leafElemA"));

        // when
        List<Comparison> differences = findNodeChildrenDifferences(control, test);
        List<Comparison> differencesReverse = findNodeChildrenDifferences(control, test);

        // then
        assertThat(differences).hasSize(0);
        assertThat(differencesReverse).hasSize(0);
    }

    @Test
    public void should_detect_child_nodes_in_test() throws Exception {
        // given
        Document document = documentBuilder.newDocument();

        document = documentUtils.buildControlDocument(
                "<down>" +
                        "<im standing=\"alone\"/>" +
                        "<im><watching/>you all</im></down>");

        Node control = document.getDocumentElement().getFirstChild();
        Node test = control.getNextSibling();

        // when
        List<Comparison> differences = findNodeChildrenDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        Comparison difference = differences.get(0);
        assertThat(difference.getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
        assertThat(difference.getControlDetails().getValue()).isEqualTo(false);
        assertThat(difference.getTestDetails().getValue()).isEqualTo(true);
    }

    @Test
    public void should_detect_different_child_nodes_list_length() throws Exception {
        // given
        Document document = documentBuilder.newDocument();

        document = documentUtils.buildControlDocument(
                "<down>" +
                        "<im><watching/>you all</im>" +
                        "<im watching=\"you\">sinking</im></down>");

        Node control = document.getDocumentElement().getFirstChild();
        Node test = control.getNextSibling();

        // when
        List<Comparison> differences = findNodeChildrenDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        Comparison difference = differences.get(0);
        assertThat(difference.getType()).isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
        assertThat(difference.getControlDetails().getValue()).isEqualTo(2);
        assertThat(difference.getTestDetails().getValue()).isEqualTo(1);
    }

    @Test
    public void should_find_no_child_node_list_differences_when_mixed_content() throws Exception {
        // given
        Document document = documentBuilder.newDocument();

        Element control = document.createElement("root");
        control.appendChild(document.createTextNode("text leaf"));
        control.appendChild(document.createElement("leafElem"));

        Element test = document.createElement("root");
        test.appendChild(document.createElement("leafElem"));
        test.appendChild(document.createTextNode("text leaf"));

        // when
        List<Comparison> differences = findNodeChildrenDifferences(control, test);
        List<Comparison> differencesReverse = findNodeChildrenDifferences(control, test);

        // then
        assertThat(differences).hasSize(0);
        assertThat(differencesReverse).hasSize(0);
    }

    private List<Comparison> findNodeChildrenDifferences(Node controlNode, Node testNode) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);

        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);
        ComparisonPerformer performer = engine.getComparisonPerformer();

        NodeAndXpathCtx<Node> control = new NodeAndXpathCtx<Node>(controlNode, new XPathContext());
        NodeAndXpathCtx<Node> test = new NodeAndXpathCtx<Node>(testNode, new XPathContext());

        new ChildrenNumberComparator(performer).compare(control, test);
        return evaluator.getDifferences();
    }
}
