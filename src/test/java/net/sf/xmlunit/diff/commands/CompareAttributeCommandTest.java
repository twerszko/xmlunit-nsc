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
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.commands.CompareAttributeCommand;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

public class CompareAttributeCommandTest {
    private final DocumentUtils documentUtils = new DocumentUtils();

    @Test
    public void should_detect_different_attribute_value() throws Exception {
        // given
        String expected = "These boots were made for walking";
        String actual = "The marquis de sade never wore no boots like these";

        DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Attr control = document.createAttribute("testAttr");
        control.setValue(expected);
        Attr test = document.createAttribute("testAttr");
        test.setValue(actual);

        // when
        List<Comparison> differences = findAttrDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_VALUE);
    }

    @Test
    public void should_detect_attr_value_explicitely_specified() throws Exception {
        // given
        String doctypeDeclaration = "<!DOCTYPE manchester [" +
                "<!ELEMENT sound EMPTY><!ATTLIST sound sorted (true|false) \"true\">" +
                "<!ELEMENT manchester (sound)>]>";

        Document controlDoc =
                documentUtils.buildControlDocument(doctypeDeclaration +
                        "<manchester><sound sorted=\"true\"/></manchester>");
        Attr control = (Attr) controlDoc.getDocumentElement().getFirstChild()
                .getAttributes().getNamedItem("sorted");

        Document testDoc = documentUtils.buildTestDocument(doctypeDeclaration
                +
                "<manchester><sound/></manchester>");
        Attr test = (Attr) testDoc.getDocumentElement().getFirstChild()
                .getAttributes().getNamedItem("sorted");

        // when
        List<Comparison> differences = findAttrDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED);
    }

    private List<Comparison> findAttrDifferences(Attr controlAttr, Attr testAttr) {
        ListingComparisonPerformer performer = new ListingComparisonPerformer();

        NodeAndXpath<Attr> control = new NodeAndXpath<Attr>(controlAttr, new XPathContext());
        NodeAndXpath<Attr> test = new NodeAndXpath<Attr>(testAttr, new XPathContext());

        new CompareAttributeCommand(performer, test, control).execute();
        return performer.getDifferences();
    }
}
