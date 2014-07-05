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

package org.custommonkey.xmlunit.matchers.xpath;

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXpathValueOf;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXpathValueOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.XmlUnitBuilder;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xmlunit.NamespaceContext;

public class XpathValuesEqualMatcherTest {
    private static final String PREFIX = "foo";
    private static final String TEST_NS = "urn:org.example";
    private static NamespaceContext NS_CONTEXT;

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

    private static final String xpathValuesControlXMLNS = addNamespaceToDocument(xpathValuesControlXML);
    private static final String xpathValuesTestXMLNS = addNamespaceToDocument(xpathValuesTestXML);

    private static String addNamespaceToDocument(String original) {
        int pos = original.indexOf(">");
        return original.substring(0, pos) + " xmlns='" + TEST_NS + "'" + original.substring(pos);
    }

    @BeforeClass
    public static void setUpClass() {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put(PREFIX, TEST_NS);
        NS_CONTEXT = new SimpleNamespaceContext(m);
    }

    private DocumentUtils documentUtils;

    @Before
    public void setUp() {
        documentUtils = new DocumentUtils(new XmlUnitProperties());
    }

    @Test
    public void testXpathValuesEqualUsingDocument() throws Exception {
        Document controlDocument = documentUtils.buildControlDocument(xpathValuesControlXML);
        Document testDocument = documentUtils.buildTestDocument(xpathValuesTestXML);

        XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
        XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
        assertThat(testXpath1, equalToXpathValueOf(controlXpath1));

        XpathWrapper testXpath2 = new XpathWrapper("//outer/@attr", testDocument);
        XpathWrapper controlXpath2 = new XpathWrapper("//inner/@attr", controlDocument);
        assertThat(testXpath2, equalToXpathValueOf(controlXpath2));

        XpathWrapper testXpath3 = new XpathWrapper("//outer/@attr", controlDocument);
        XpathWrapper controlXpath3 = new XpathWrapper("//inner/text()", controlDocument);
        assertThat(testXpath3, is(notEqualToXpathValueOf(controlXpath3)));

        XpathWrapper testXpath4 = new XpathWrapper("//text()", testDocument);
        XpathWrapper controlXpath4 = new XpathWrapper("//inner/text()", controlDocument);
        assertThat(testXpath4, notEqualToXpathValueOf(controlXpath4));
    }

    @Test
    public void testXpathValuesEqualUsingDocumentNS() throws Exception {
        Document controlDocument = documentUtils.buildControlDocument(xpathValuesControlXMLNS);
        Document testDocument = documentUtils.buildTestDocument(xpathValuesTestXMLNS);

        XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
        XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
        assertThat(testXpath1, notEqualToXpathValueOf(controlXpath1));

        XmlUnit xmlUnitWithCotext = new XmlUnitBuilder()
                .withXpathNamespaceContext(NS_CONTEXT)
                .build();

        XpathWrapper testXpath2 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
        XpathWrapper controlXpath2 = new XpathWrapper("//text()", controlDocument);
        assertThat(testXpath2, equalToXpathValueOf(controlXpath2).using(xmlUnitWithCotext));

        XpathWrapper testXpath3 = new XpathWrapper("//" + PREFIX + ":outer/@attr", testDocument);
        XpathWrapper controlXpath3 = new XpathWrapper("//" + PREFIX + ":inner/@attr", controlDocument);
        assertThat(testXpath3, equalToXpathValueOf(controlXpath3).using(xmlUnitWithCotext));

        XpathWrapper testXpath4 = new XpathWrapper("//" + PREFIX + ":outer/@attr", controlDocument);
        XpathWrapper controlXpath4 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
        assertThat(testXpath4, notEqualToXpathValueOf(controlXpath4).using(xmlUnitWithCotext));

        XpathWrapper testXpath5 = new XpathWrapper("//text()", testDocument);
        XpathWrapper controlXpath5 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
        assertThat(testXpath5, notEqualToXpathValueOf(controlXpath5).using(xmlUnitWithCotext));
    }

    @Test
    public void testXpathValuesEqualUsingString() throws Exception {
        Document controlDocument = documentUtils.buildControlDocument(xpathValuesControlXML);
        Document testDocument = documentUtils.buildTestDocument(xpathValuesTestXML);

        XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
        XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
        assertThat(testXpath1, equalToXpathValueOf(controlXpath1));

        XpathWrapper testXpath2 = new XpathWrapper("//outer/@attr", testDocument);
        XpathWrapper controlXpath2 = new XpathWrapper("//inner/@attr", controlDocument);
        assertThat(testXpath2, equalToXpathValueOf(controlXpath2));

        XpathWrapper testXpath3 = new XpathWrapper("//outer/@attr", testDocument);
        XpathWrapper controlXpath3 = new XpathWrapper("//inner/text()", controlDocument);
        assertThat(testXpath3, notEqualToXpathValueOf(controlXpath3));

        XpathWrapper testXpath4 = new XpathWrapper("//text()", testDocument);
        XpathWrapper controlXpath4 = new XpathWrapper("//inner/text()", controlDocument);
        assertThat(testXpath4, notEqualToXpathValueOf(controlXpath4));
    }

    @Test
    public void testXpathValuesEqualUsingStringNS() throws Exception {
        Document controlDocument = documentUtils.buildControlDocument(xpathValuesControlXMLNS);
        Document testDocument = documentUtils.buildControlDocument(xpathValuesTestXMLNS);

        XpathWrapper testXpath1 = new XpathWrapper("//inner/text()", controlDocument);
        XpathWrapper controlXpath1 = new XpathWrapper("//text()", controlDocument);
        assertThat(testXpath1, notEqualToXpathValueOf(controlXpath1));

        XmlUnit xmlUnitWithCotext = new XmlUnitBuilder()
                .withXpathNamespaceContext(NS_CONTEXT)
                .build();

        XpathWrapper testXpath2 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
        XpathWrapper controlXpath2 = new XpathWrapper("//text()", controlDocument);
        assertThat(testXpath2, equalToXpathValueOf(controlXpath2).using(xmlUnitWithCotext));

        XpathWrapper testXpath3 = new XpathWrapper("//" + PREFIX + ":outer/@attr", testDocument);
        XpathWrapper controlXpath3 = new XpathWrapper("//" + PREFIX + ":inner/@attr", controlDocument);
        assertThat(testXpath3, equalToXpathValueOf(controlXpath3).using(xmlUnitWithCotext));

        XpathWrapper testXpath4 = new XpathWrapper("//" + PREFIX + ":outer/@attr", controlDocument);
        XpathWrapper controlXpath4 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
        assertThat(testXpath4, notEqualToXpathValueOf(controlXpath4).using(xmlUnitWithCotext));

        XpathWrapper testXpath5 = new XpathWrapper("//text()", testDocument);
        XpathWrapper controlXpath5 = new XpathWrapper("//" + PREFIX + ":inner/text()", controlDocument);
        assertThat(testXpath5, notEqualToXpathValueOf(controlXpath5).using(xmlUnitWithCotext));
    }
}
