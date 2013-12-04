//  Copyright 2012 Tomasz Werszko
//      
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.custommonkey.xmlunit.matchers;

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notPassesWith;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.passesWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.custommonkey.xmlunit.NodeTest;
import org.custommonkey.xmlunit.NodeTester;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.examples.CountingNodeTester;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


//TODO Refactor me
public class NoteTestPassesMatcherTest {
    private static final String CONTROL_XML = "<root>" +
            "<outer attr=\"urk\">" +
            "<inner attr=\"urk\">controlDocument</inner>" +
            "</outer>" +
            "</root>";
    private static final String TEST_XML = "<root>" +
            "<outer attr=\"urk\">" +
            "<inner attr=\"ugh\">testDocument</inner>" +
            "</outer>" +
            "</root>";
    private NodeTester tester;

    @Test
    public void testNodeTest() throws Exception {
        DocumentUtils documentUtils = new DocumentUtils(new XmlUnitProperties());

        NodeTest test = new NodeTest(documentUtils, new StringReader(TEST_XML));
        tester = new CountingNodeTester(4);
        assertThat(test, passesWith(tester, new short[] { Node.TEXT_NODE, Node.ELEMENT_NODE }));
        assertThat(test, notPassesWith(tester, new short[] { Node.TEXT_NODE, Node.COMMENT_NODE }));

        try {
            assertThat(test, notPassesWith(tester, new short[] { Node.TEXT_NODE, Node.ELEMENT_NODE }));
            fail("Expected node test failure #2!");
        } catch (AssertionError e) {
        }

        try {
            assertThat(test, passesWith(tester, new short[] { Node.TEXT_NODE, Node.COMMENT_NODE }));
            fail("Expected node test failure #3!");
        } catch (AssertionError e) {
        }
    }

    @Test
    public void node_test_should_pass_with_a_simple_tester() throws SAXException,
            IOException {
        // given
        tester = new CountingNodeTester(1);
        NodeTest nodeTest = createNodeTest(CONTROL_XML);
        assertThat(nodeTest, passesWith(tester, Node.TEXT_NODE));
        try {
            assertThat(nodeTest, passesWith(tester, Node.ELEMENT_NODE));
            fail("Expected node test failure #1!");
        } catch (AssertionError e) {
        }
    }

    NodeTest createNodeTest(String xml) throws SAXException, IOException {
        DocumentUtils documentUtils = new DocumentUtils(new XmlUnitProperties());
        NodeTest nodeTest = new NodeTest(documentUtils, xml);
        return nodeTest;
    }
}
