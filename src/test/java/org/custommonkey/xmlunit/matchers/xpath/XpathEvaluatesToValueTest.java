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

package org.custommonkey.xmlunit.matchers.xpath;

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.xpathEvaluatesTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;

public class XpathEvaluatesToValueTest {
    private static final String PREFIX = "foo";
    private static final String TEST_NS = "urn:org.example";
    private static final NamespaceContext NS_CONTEXT;
    static {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put(PREFIX, TEST_NS);
        NS_CONTEXT = new SimpleNamespaceContext(m);
    }

    private static final String XPATH_VALUES_CONTROL_XML =
            "<root>" +
                    "<outer attr=\"urk\">" +
                    "<inner attr=\"urk\">controlDocument</inner>" +
                    "</outer>" +
                    "</root>";
    private static final String XPATH_VALUES_TEST_XML =
            "<root>" +
                    "<outer attr=\"urk\">" +
                    "<inner attr=\"ugh\">testDocument</inner>" +
                    "</outer>" +
                    "</root>";

    private static final String XPATH_VALUES_CONTROL_XMLNS = addNamespaceToDocument(XPATH_VALUES_CONTROL_XML);
    private static final String XPATH_VALUES_TEST_XMLNS = addNamespaceToDocument(XPATH_VALUES_TEST_XML);

    // TODO Refactor
    @Test
    public void testXpathEvaluatesTo() throws Exception {
        Document inDocument1 = XMLUnit.buildControlDocument(XPATH_VALUES_CONTROL_XML);

        assertThat(new XpathWrapper("//outer/@attr", inDocument1), xpathEvaluatesTo("urk"));
        try {
            assertThat(new XpathWrapper("//inner/@attr", inDocument1), xpathEvaluatesTo("yum"));
            fail("Expected assertion to fail #1");
        } catch (AssertionError e) {
        }

        assertThat(new XpathWrapper("count(//@attr)", inDocument1), xpathEvaluatesTo("2"));

        Document inDocument2 = XMLUnit.buildTestDocument(XPATH_VALUES_TEST_XML);
        assertThat(new XpathWrapper("//inner/@attr", inDocument2), xpathEvaluatesTo("ugh"));
        try {
            assertThat(new XpathWrapper("//outer/@attr", inDocument2), xpathEvaluatesTo("yeah"));
            fail("Expected assertion to fail #2");
        } catch (AssertionError e) {
        }

    }

    @Test
    public void testXpathEvaluatesToNS() throws Exception {
        Document inDocument1 = XMLUnit.buildControlDocument(XPATH_VALUES_CONTROL_XMLNS);
        try {
            assertThat(new XpathWrapper("//outer/@attr", inDocument1), xpathEvaluatesTo("urk"));
            fail("Expected assertion to fail #1");
        } catch (AssertionError e) {
        }

        XMLUnit.setXpathNamespaceContext(NS_CONTEXT);
        assertThat(new XpathWrapper("//" + PREFIX + ":outer/@attr", inDocument1), xpathEvaluatesTo("urk"));
        try {
            assertThat(new XpathWrapper("//" + PREFIX + ":inner/@attr", inDocument1), xpathEvaluatesTo("yum"));
            fail("Expected assertion to fail #2");
        } catch (AssertionError e) {
        }
        assertThat(new XpathWrapper("count(//@attr)", inDocument1), xpathEvaluatesTo("2"));

        Document inDocument2 = XMLUnit.buildTestDocument(XPATH_VALUES_TEST_XMLNS);
        assertThat(new XpathWrapper("//" + PREFIX + ":inner/@attr", inDocument2), xpathEvaluatesTo("ugh"));
        try {
            assertThat(new XpathWrapper("//" + PREFIX + ":outer/@attr", inDocument2), xpathEvaluatesTo("yeah"));
            fail("Expected assertion to fail #3");
        } catch (AssertionError e) {
        }
    }

    // Bug 741636
    @Test
    public void should_evalueate_xpath_count() throws Exception {
        // given
        String html =
                "<div><p></p>" +
                        "<table>" +
                        "<tr>" +
                        "<td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td>" +
                        "</tr><tr>" +
                        "<td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td>" +
                        "</tr><tr>" +
                        "<td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td>" +
                        "</tr><tr>" +
                        "<td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td>" +
                        "</tr><tr>" +
                        "<td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td><td><p></p></td>" +
                        "</tr>" +
                        "</table>" +
                        "</div>";

        Document htmlDoc = XMLUnit.buildControlDocument(html);

        // then
        assertThat(new XpathWrapper("count(//td)", htmlDoc), xpathEvaluatesTo("25"));
    }

    private static String addNamespaceToDocument(String original) {
        int pos = original.indexOf(">");
        return original.substring(0, pos) + " xmlns='" + TEST_NS + "'" + original.substring(pos);
    }
}
