/*      
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package net.sf.xmlunit.xpath;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.util.IterableNodeList;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractXpathEngineTest {

    protected static final String[] testAttrNames = { "attrOne", "attrTwo" };

    protected static final String testString =
            "<test><nodeWithoutAttributes>intellectual property rights"
                    + " </nodeWithoutAttributes>"
                    + "<nodeWithoutAttributes>make us all poorer </nodeWithoutAttributes>"
                    + "<nodeWithAttributes " + testAttrNames[0] + "=\"open source \" "
                    + testAttrNames[1]
                    + "=\"is the answer \">free your code from its chains"
                    + "</nodeWithAttributes></test>";

    protected DocumentUtils documentUtils;
    protected Document testDocument;
    protected Source testSource;

    protected abstract XpathEngine newXpathEngine();

    @Before
    public void setUp() throws Exception {
        documentUtils = new DocumentUtils(new XmlUnitProperties());
        testDocument = documentUtils.buildControlDocument(testString);
        testSource = Input.fromFile(TestResources.BLAME_FILE.getFile()).build();
    }

    @Test
    public void should_not_get_any_matching_nodes() throws Exception {
        // given
        DOMSource source = new DOMSource(testDocument);
        XpathEngine engine = newXpathEngine();

        // when
        Iterable<Node> nodeList = engine.selectNodes("toast", source);

        // then
        assertThat(nodeList).isEmpty();
    }

    @Test
    public void should_get_matching_root_element() throws Exception {
        // given - when
        IterableNodeList nodes = newXpathEngine().selectNodes("test", new DOMSource(testDocument));
        List<Node> nodeList = nodes.asList();

        // then
        assertThat(nodeList.size()).isEqualTo(1);
        assertThat(nodeList.get(0).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
    }

    @Test
    public void should_get_matching_element_node() throws Exception {
        // given
        DOMSource source = new DOMSource(testDocument);
        XpathEngine xpathEngine = newXpathEngine();

        // when
        IterableNodeList nodes = xpathEngine.selectNodes("test/nodeWithoutAttributes", source);
        List<Node> nodeList = nodes.asList();

        // then
        assertThat(nodeList.size()).isEqualTo(2);
        assertThat(nodeList.get(0).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
    }

    @Test
    public void should_get_matching_text_node() throws Exception {
        // given
        String expectedText = "intellectual property rights make us all poorer free your code from its chains";
        DOMSource source = new DOMSource(testDocument);

        // when
        IterableNodeList nodes = newXpathEngine().selectNodes("test//text()", source);
        List<Node> nodeList = nodes.asList();

        // then
        assertThat(nodeList.size()).isGreaterThan(0);
        String allText = "";
        for (Node node : nodeList) {
            assertThat(node.getNodeType()).isEqualTo(Node.TEXT_NODE);
            allText += node.getNodeValue();
        }
        assertThat(allText).isEqualTo(expectedText);
    }

    @Test
    public void should_get_matching_node_with_children() throws Exception {
        // given
        DOMSource source = new DOMSource(testDocument);
        XpathEngine xpathEngine = newXpathEngine();

        // when
        IterableNodeList nodes = xpathEngine.selectNodes("test/nodeWithAttributes", source);
        List<Node> nodeList = nodes.asList();

        Node node = nodeList.get(0);
        IterableNodeList children = new IterableNodeList(node.getChildNodes());
        NamedNodeMap attributes = node.getAttributes();

        // then
        assertThat(nodeList.size()).isEqualTo(1);
        assertThat(node.getNodeType()).isEqualTo(Node.ELEMENT_NODE);
        assertThat(node.hasAttributes()).isTrue();
        assertThat(node.hasChildNodes()).isTrue();

        assertThat(children).hasSize(1);
        for (Node child : children) {
            assertThat(child.getNodeType()).isEqualTo(Node.TEXT_NODE);
        }

        assertThat(attributes.getLength()).isEqualTo(testAttrNames.length);
        for (String attrName : testAttrNames) {
            Node attrNode = attributes.getNamedItem(attrName);
            assertThat(attrNode).isNotNull();
            assertThat(attrNode.getNodeType()).isEqualTo(Node.ATTRIBUTE_NODE);
        }
    }

    @Test
    public void should_evaluate_node_count() throws Exception {
        // given
        DOMSource source = new DOMSource(testDocument);
        XpathEngine xpathEngine = newXpathEngine();

        // when
        String result = xpathEngine.evaluate("count(test//node())", source);

        // then
        // 3 elements and 3 text nodes
        assertThat(result).isEqualTo("6");
    }

    @Test
    public void should_get_matching_nodes_with_customized_namespace_context() throws Exception {
        // given
        String testXml = "<t:test xmlns:t=\"urn:foo\"><t:bar/></t:test>";
        Source source = Input.fromMemory(testXml).build();

        HashMap<String, String> namespaceCtx = new HashMap<String, String>();
        namespaceCtx.put("foo", "urn:foo");
        XpathEngine engine = newXpathEngine();

        // when
        engine.setNamespaceContext(namespaceCtx);

        IterableNodeList nodes = engine.selectNodes("//foo:bar", source);
        List<Node> nodeList = nodes.asList();

        // then
        assertThat(nodeList).hasSize(1);
        assertThat(nodeList.get(0).getNodeType()).isEqualTo(Node.ELEMENT_NODE);
    }

    @Test
    public void should_evaluate_with_customized_namespace_context() throws Exception {
        // given
        String testXml = "<t:test xmlns:t=\"urn:foo\"><t:bar/></t:test>";
        Source source = Input.fromMemory(testXml).build();

        HashMap<String, String> namespaceCtx = new HashMap<String, String>();
        namespaceCtx.put("foo", "urn:foo");
        XpathEngine engine = newXpathEngine();

        // when
        engine.setNamespaceContext(namespaceCtx);
        String evalueatedVal = engine.evaluate("count(foo:test//node())", source);

        // then
        assertThat(evalueatedVal).isEqualTo("1");
    }

    // http://sourceforge.net/forum/forum.php?thread_id=1832061&forum_id=73274
    @Test
    public void should_get_matching_node_with_ns_and_local_name_selector() throws Exception {
        // given
        String testXml =
                "<MtcEnv Version=\"1.0\" xmlns=\"http://www.Mtc.com/schemas\" xmlns:bms=\"http://www.cieca.com/BMS\"> "
                        + "<EnvContext> "
                        + "<NameValuePair> "
                        + "<Name>Timestamp</Name> "
                        + "<Value>2007-07-26T11:59:00</Value> "
                        + "</NameValuePair> "
                        + "</EnvContext> "
                        + "<EnvBodyList> "
                        + "<EnvBody> "
                        + "<Metadata> "
                        + "<Identifier>CIECABMSAssignmentAddRq</Identifier> "
                        + "</Metadata> "
                        + "<Content> "
                        + "<bms:CIECA> "
                        + "<bms:AssignmentAddRq> "
                        + "<bms:RqUID>3744f84b-ac18-5303-0082-764bdeb20df9</bms:RqUID> "
                        + "</bms:AssignmentAddRq> "
                        + "</bms:CIECA> "
                        + "</Content> "
                        + "</EnvBody> "
                        + "</EnvBodyList> "
                        + "</MtcEnv>";

        Source source = Input.fromMemory(testXml).build();
        XpathEngine engine = newXpathEngine();

        // when
        Iterable<Node> nodeList = engine.selectNodes(
                "//*[local-name()='RqUID'][namespace-uri()='http://www.cieca.com/BMS']",
                source);

        // then
        assertThat(nodeList).hasSize(1);
    }

    @Test(expected = XpathException.class)
    public void should_not_evaluate_when_invalid_xpath() throws Exception {
        // given
        String invalidXpath = "count(test//*[@attrOne='open source])";
        XpathEngine engine = newXpathEngine();

        // when
        engine.evaluate(invalidXpath, new DOMSource(testDocument));

        // then exception
    }

    @Test
    public void should_not_get_any_matching_nodes_2() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();

        // when
        IterableNodeList nodeList = engine.selectNodes("foo", testSource);

        // then
        assertThat(nodeList).isNotNull();
        assertThat(nodeList).hasSize(0);
    }

    @Test
    public void should_get_one_matching_node() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();

        // when
        IterableNodeList nodes = engine.selectNodes("//ul", testSource);

        // then
        assertThat(nodes).isNotNull();
        assertThat(nodes).hasSize(1);
        assertThat(nodes.iterator().next().getNodeName()).isEqualTo("ul");
    }

    @Test
    public void should_get_multiple_matching_nodes() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();

        // when
        IterableNodeList nodes = engine.selectNodes("//li", testSource);

        // then
        assertThat(nodes).isNotNull();
        assertThat(nodes).hasSize(4);
        for (Node node : nodes) {
            assertThat(node.getNodeName()).isEqualTo("li");
        }
    }

    @Test(expected = XpathException.class)
    public void should_not_get_matching_nodes_when_invalid_xpath() throws Exception {
        // given - when
        newXpathEngine().selectNodes("//li[", testSource);

        // then exception
    }

    @Test
    public void should_evaluate_with_no_matches() throws Exception {
        // given - when
        String result = newXpathEngine().evaluate("foo", testSource);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void should_evaluate_with_one_match() throws Exception {
        // given
        String expectedResult = "Don't blame it on the...";

        // when
        String result = newXpathEngine().evaluate("//title", testSource);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void should_evaluate_with_one_match_using_text_selector() throws Exception {
        // given
        String expectedResult = "Don't blame it on the...";

        // when
        String result = newXpathEngine().evaluate("//title/text()", testSource);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void should_evaluate_with_multiple_matches() throws Exception {
        // given
        String expectedResult = "sunshine";

        // when
        String result = newXpathEngine().evaluate("//li", testSource);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test(expected = XpathException.class)
    public void should_not_evaluate_when_invalid_xpath_2() throws Exception {
        // given - when
        newXpathEngine().evaluate("//li[", testSource);

        // then exception
    }

    @Test
    public void should_get_matching_nodes_with_custonmized_namespace_context() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();
        testSource = Input.fromMemory("<n:d xmlns:n='urn:test:1'><n:e/></n:d>").build();

        HashMap<String, String> namespaceCtx = new HashMap<String, String>();
        namespaceCtx.put("x", "urn:test:1");

        // when
        engine.setNamespaceContext(namespaceCtx);
        Iterable<Node> nodes = engine.selectNodes("/x:d/x:e", testSource);

        // then
        assertThat(nodes).hasSize(1);
    }

    @Test
    public void should_get_matching_nodes_with_default_namespace() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();
        testSource = Input.fromMemory("<d xmlns='urn:test:1'><e/></d>").build();

        HashMap<String, String> namespaceCtx = new HashMap<String, String>();
        namespaceCtx.put("x", "urn:test:1");

        // when
        engine.setNamespaceContext(namespaceCtx);
        Iterable<Node> nodes = engine.selectNodes("/x:d/x:e", testSource);

        // then
        assertThat(nodes).hasSize(1);
    }

    @Test
    public void should_get_matching_node_with_default_ns_and_empty_prefix() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();
        testSource = Input.fromMemory("<d xmlns='urn:test:1'><e/></d>").build();

        HashMap<String, String> namespaceCtx = new HashMap<String, String>();
        namespaceCtx.put("", "urn:test:1");

        // when
        engine.setNamespaceContext(namespaceCtx);
        Iterable<Node> nodes = engine.selectNodes("/:d/:e", testSource);

        // then
        assertThat(nodes).hasSize(1);
    }

    @Test
    public void should_not_get_matching_node_with_default_ns_and_no_prefix() throws Exception {
        // given
        XpathEngine engine = newXpathEngine();
        testSource = Input.fromMemory("<d xmlns='urn:test:1'><e/></d>").build();

        HashMap<String, String> namespaceCtx = new HashMap<String, String>();
        namespaceCtx.put("", "urn:test:1");

        // when
        engine.setNamespaceContext(namespaceCtx);
        Iterable<Node> it = engine.selectNodes("/d/e", testSource);

        // then
        assertThat(it).hasSize(0);
    }
}
