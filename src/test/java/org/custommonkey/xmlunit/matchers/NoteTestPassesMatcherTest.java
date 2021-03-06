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

import java.io.StringReader;

import org.custommonkey.xmlunit.CountingNodeTester;
import org.custommonkey.xmlunit.NodeTest;
import org.custommonkey.xmlunit.NodeTester;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Node;

//TODO Refactor me
public class NoteTestPassesMatcherTest {
    private static final String xpathValuesControlXML =
            "<root>" +
                    "<outer attr=\"urk\">" +
                    "<inner attr=\"urk\">controlDocument</inner>" +
                    "</outer>" +
                    "</root>";
    private static final String xpathValuesTestXML =
            "<root>" +
                    "<outer attr=\"urk\">" +
                    "<inner attr=\"ugh\">testDocument</inner>" +
                    "</outer>" +
                    "</root>";

    @Test
    public void testNodeTest() throws Exception {
        DocumentUtils documentUtils = new DocumentUtils(new XmlUnitProperties());

        NodeTester tester = new CountingNodeTester(1);

        assertThat(new NodeTest(documentUtils, xpathValuesControlXML), passesWith(tester, Node.TEXT_NODE));
        try {
            assertThat(new NodeTest(documentUtils, xpathValuesControlXML), passesWith(tester, Node.ELEMENT_NODE));
            fail("Expected node test failure #1!");
        } catch (AssertionError e) {
        }

        NodeTest test = new NodeTest(documentUtils, new StringReader(xpathValuesTestXML));
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
}
