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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementSelectorsTest {
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String SOME_URI = "urn:some:uri";

    private Document doc;

    @Before
    public void createDoc() throws ParserConfigurationException {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @Test
    public void should_check_if_can_be_compared_by_name() {
        pureElementNameComparisons(ElementSelectors.byName);
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndText_NamePart() {
        pureElementNameComparisons(ElementSelectors.byNameAndText);
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndTextRec_NamePart() {
        pureElementNameComparisons(ElementSelectors.byNameAndTextRec);
    }

    private void pureElementNameComparisons(ElementSelector selector) {
        // given
        Element control = doc.createElement(FOO);
        Element equal = doc.createElement(FOO);
        Element different = doc.createElement(BAR);
        Element controlNS = doc.createElementNS(SOME_URI, FOO);
        controlNS.setPrefix(BAR);

        // then
        assertFalse(selector.canBeCompared(null, null));
        assertFalse(selector.canBeCompared(null, control));
        assertFalse(selector.canBeCompared(control, null));
        assertTrue(selector.canBeCompared(control, equal));
        assertFalse(selector.canBeCompared(control, different));
        assertFalse(selector.canBeCompared(control, controlNS));
        assertTrue(selector.canBeCompared(doc.createElementNS(SOME_URI, FOO), controlNS));
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndText() {
        byNameAndText_SingleLevel(ElementSelectors.byNameAndText);
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndAllAttributes_NamePart() {
        pureElementNameComparisons(ElementSelectors.byNameAndAllAttributes);
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndTextRec_Single() {
        byNameAndText_SingleLevel(ElementSelectors.byNameAndTextRec);
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndAttributes_NamePart() {
        // given
        String[] emptyStringArray = new String[] {};
        QName[] emptyQnameArray = new QName[] {};

        // then
        pureElementNameComparisons(ElementSelectors.byNameAndAttributes(emptyStringArray));
        pureElementNameComparisons(ElementSelectors.byNameAndAttributes(emptyQnameArray));
        pureElementNameComparisons(ElementSelectors.byNameAndAttributes(BAR));
        pureElementNameComparisons(ElementSelectors.byNameAndAttributes(new QName(SOME_URI, BAR)));
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndAttributesControlNS_NamePart() {
        pureElementNameComparisons(ElementSelectors.byNameAndAttributesControlNS());
        pureElementNameComparisons(ElementSelectors.byNameAndAttributesControlNS(BAR));
    }

    private void byNameAndText_SingleLevel(ElementSelector selector) {
        // given
        Element control = doc.createElement(FOO);
        control.appendChild(doc.createTextNode(BAR));

        Element equal = doc.createElement(FOO);
        equal.appendChild(doc.createTextNode(BAR));
        Element equalC = doc.createElement(FOO);
        equalC.appendChild(doc.createCDATASection(BAR));
        Element noText = doc.createElement(FOO);
        Element differentText = doc.createElement(FOO);
        differentText.appendChild(doc.createTextNode(BAR));
        differentText.appendChild(doc.createTextNode(BAR));

        // then
        assertTrue(selector.canBeCompared(control, equal));
        assertTrue(selector.canBeCompared(control, equalC));
        assertFalse(selector.canBeCompared(control, noText));
        assertFalse(selector.canBeCompared(control, differentText));
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndTextRec() {
        // given
        Element control = doc.createElement(FOO);
        Element child = doc.createElement(BAR);
        control.appendChild(child);
        child.appendChild(doc.createTextNode(BAR));

        Element equal = doc.createElement(FOO);
        Element child2 = doc.createElement(BAR);
        equal.appendChild(child2);
        child2.appendChild(doc.createTextNode(BAR));
        Element equalC = doc.createElement(FOO);
        Element child3 = doc.createElement(BAR);
        equalC.appendChild(child3);
        child3.appendChild(doc.createCDATASection(BAR));
        Element noText = doc.createElement(FOO);
        Element differentLevel = doc.createElement(FOO);
        differentLevel.appendChild(doc.createTextNode(BAR));
        Element differentElement = doc.createElement(FOO);
        Element child4 = doc.createElement(FOO);
        differentElement.appendChild(child4);
        child4.appendChild(doc.createTextNode(BAR));
        Element differentText = doc.createElement(FOO);
        Element child5 = doc.createElement(BAR);
        differentText.appendChild(child5);
        child5.appendChild(doc.createTextNode(FOO));

        // when
        ElementSelector selector = ElementSelectors.byNameAndTextRec;

        // then
        assertTrue(selector.canBeCompared(control, equal));
        assertTrue(selector.canBeCompared(control, equalC));
        assertFalse(selector.canBeCompared(control, noText));
        assertFalse(selector.canBeCompared(control, differentLevel));
        assertFalse(selector.canBeCompared(control, differentElement));
        assertFalse(selector.canBeCompared(control, differentText));
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndAllAttributes() {
        // given
        Element control = doc.createElement(FOO);
        control.setAttribute(BAR, BAR);

        Element equal = doc.createElement(FOO);
        equal.setAttribute(BAR, BAR);
        Element noAttributes = doc.createElement(FOO);
        Element differentValue = doc.createElement(FOO);
        differentValue.setAttribute(BAR, FOO);
        Element differentName = doc.createElement(FOO);
        differentName.setAttribute(FOO, FOO);
        Element differentNS = doc.createElement(FOO);
        differentNS.setAttributeNS(SOME_URI, BAR, BAR);

        // then
        assertTrue(ElementSelectors.byNameAndAllAttributes.canBeCompared(control, equal));
        assertFalse(ElementSelectors.byNameAndAllAttributes.canBeCompared(control, noAttributes));
        assertFalse(ElementSelectors.byNameAndAllAttributes.canBeCompared(noAttributes, control));
        assertFalse(ElementSelectors.byNameAndAllAttributes.canBeCompared(control, differentValue));
        assertFalse(ElementSelectors.byNameAndAllAttributes.canBeCompared(control, differentName));
        assertFalse(ElementSelectors.byNameAndAllAttributes.canBeCompared(control, differentNS));
    }

    @Test
    public void byNameAndAttributes_QName() {
        // given
        Element control = doc.createElement(FOO);
        control.setAttribute(BAR, BAR);
        Element equal = doc.createElement(FOO);
        equal.setAttribute(BAR, BAR);
        Element noAttributes = doc.createElement(FOO);
        Element differentValue = doc.createElement(FOO);
        differentValue.setAttribute(BAR, FOO);
        Element differentName = doc.createElement(FOO);
        differentName.setAttribute(FOO, FOO);
        Element differentNS = doc.createElement(FOO);
        differentNS.setAttributeNS(SOME_URI, BAR, BAR);

        // then
        assertTrue(ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, equal));
        assertFalse(ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributes(new QName(FOO)).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributes(new QName[] {}).canBeCompared(control, noAttributes));
        assertFalse(ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(noAttributes, control));
        assertFalse(ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, differentValue));
        assertFalse(ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, differentName));
        assertFalse(ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, differentNS));
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndAttributes_String() {
        // given
        Element control = doc.createElement(FOO);
        control.setAttribute(BAR, BAR);

        Element equal = doc.createElement(FOO);
        equal.setAttribute(BAR, BAR);
        Element noAttributes = doc.createElement(FOO);
        Element differentValue = doc.createElement(FOO);
        differentValue.setAttribute(BAR, FOO);
        Element differentName = doc.createElement(FOO);
        differentName.setAttribute(FOO, FOO);
        Element differentNS = doc.createElement(FOO);
        differentNS.setAttributeNS(SOME_URI, BAR, BAR);

        // then
        assertTrue(ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, equal));
        assertFalse(ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributes(FOO).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributes(new String[] {}).canBeCompared(control, noAttributes));
        assertFalse(ElementSelectors.byNameAndAttributes(BAR).canBeCompared(noAttributes, control));
        assertFalse(ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, differentValue));
        assertFalse(ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, differentName));
        assertFalse(ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, differentNS));
    }

    @Test
    public void should_check_if_can_be_compared_by_nameAndAttributesControlNS() {
        // given
        Element control = doc.createElement(FOO);
        control.setAttributeNS(SOME_URI, BAR, BAR);

        Element equal = doc.createElement(FOO);
        equal.setAttributeNS(SOME_URI, BAR, BAR);
        Element noAttributes = doc.createElement(FOO);
        Element differentValue = doc.createElement(FOO);
        differentValue.setAttributeNS(SOME_URI, BAR, FOO);
        Element differentName = doc.createElement(FOO);
        differentName.setAttributeNS(SOME_URI, FOO, FOO);
        Element differentNS = doc.createElement(FOO);
        differentNS.setAttributeNS(SOME_URI + "2", BAR, BAR);
        Element noNS = doc.createElement(FOO);
        noNS.setAttribute(BAR, BAR);

        // then
        assertTrue(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, equal));
        assertFalse(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributesControlNS(FOO).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributesControlNS(new String[] {}).canBeCompared(control, noAttributes));
        assertTrue(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(noAttributes, control));
        assertFalse(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(noAttributes, noNS));
        assertFalse(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, differentValue));
        assertFalse(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, differentName));
        assertFalse(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, differentNS));
        assertFalse(ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, noNS));
    }

}
