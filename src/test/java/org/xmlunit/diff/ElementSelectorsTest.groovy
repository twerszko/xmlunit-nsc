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
package org.xmlunit.diff

import org.w3c.dom.Document
import spock.lang.Specification

import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory

class ElementSelectorsTest extends Specification {
    private static final String FOO = "foo"
    private static final String BAR = "bar"
    private static final String SOME_URI = "urn:some:uri"

    private Document document

    def setup() {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    }

    def void "selectors should be able to compare simple nodes"() {
        expect:
        correctlyClassifiesSimpleNodes(selector)

        where:
        selector << [
                ElementSelectors.byName,
                ElementSelectors.byNameAndText,
                ElementSelectors.byNameAndTextRec,
                ElementSelectors.byNameAndAllAttributes,
                ElementSelectors.byNameAndAttributes([] as String[]),
                ElementSelectors.byNameAndAttributes([] as QName[]),
                ElementSelectors.byNameAndAttributes(BAR),
                ElementSelectors.byNameAndAttributes(new QName(SOME_URI, BAR)),
                ElementSelectors.byNameAndAttributesControlNS(),
                ElementSelectors.byNameAndAttributesControlNS(BAR)
        ]
    }

    void correctlyClassifiesSimpleNodes(ElementSelector selector) {
        def control = document.createElement(FOO)
        def equal = document.createElement(FOO)
        def different = document.createElement(BAR)
        def controlNS = document.createElementNS(SOME_URI, FOO)
        controlNS.setPrefix(BAR)

        assert selector.canBeCompared(null, null) == false
        assert selector.canBeCompared(null, control) == false
        assert selector.canBeCompared(control, null) == false
        assert selector.canBeCompared(control, equal) == true
        assert selector.canBeCompared(control, different) == false
        assert selector.canBeCompared(control, controlNS) == false
        assert selector.canBeCompared(document.createElementNS(SOME_URI, FOO), controlNS) == true
    }

    def "by name selectors should be able to recognize text child nodes"() {
        expect:
        correctlyRecognizesTextChildren(selector)

        where:
        selector << [
                ElementSelectors.byNameAndText,
                ElementSelectors.byNameAndTextRec
        ]
    }

    void correctlyRecognizesTextChildren(ElementSelector selector) {
        def control = document.createElement(FOO)
        control.appendChild(document.createTextNode(BAR))

        def equal = document.createElement(FOO)
        equal.appendChild(document.createTextNode(BAR))

        def equalCDATA = document.createElement(FOO)
        equalCDATA.appendChild(document.createCDATASection(BAR))

        def noText = document.createElement(FOO)

        def differentText = document.createElement(FOO)
        differentText.appendChild(document.createTextNode(BAR))
        differentText.appendChild(document.createTextNode(BAR))

        assert selector.canBeCompared(control, equal) == true
        assert selector.canBeCompared(control, equalCDATA) == true
        assert selector.canBeCompared(control, noText) == false
        assert selector.canBeCompared(control, differentText) == false
    }

    def "by name selectors should be able to recognize merged text child nodes"() {
        given:
        def control = document.createElement(FOO)
        control.appendChild(document.createTextNode(FOO))
        control.appendChild(document.createTextNode(BAR))

        def equal = document.createElement(FOO)
        equal.appendChild(document.createTextNode(FOO + BAR))

        expect:
        selector.canBeCompared(control, equal) == true

        where:
        selector << [
                ElementSelectors.byNameAndText,
                ElementSelectors.byNameAndTextRec
        ]
    }

    def "should check if can compare by nameAndTextRec"() {
        given:
        def selector = ElementSelectors.byNameAndTextRec

        def control = document.createElement(FOO)
        def controlChild = document.createElement(BAR)
        control.appendChild(controlChild)
        controlChild.appendChild(document.createTextNode(BAR))

        def equal = document.createElement(FOO)
        def equalChild = document.createElement(BAR)
        equal.appendChild(equalChild)
        equalChild.appendChild(document.createTextNode(BAR))

        def equalCDATA = document.createElement(FOO)
        def equalChildWithCDATA = document.createElement(BAR)
        equalCDATA.appendChild(equalChildWithCDATA)
        equalChildWithCDATA.appendChild(document.createCDATASection(BAR))

        def noText = document.createElement(FOO)

        def differentLevel = document.createElement(FOO)
        differentLevel.appendChild(document.createTextNode(BAR))

        def differentElement = document.createElement(FOO)
        def differentChild = document.createElement(FOO)
        differentElement.appendChild(differentChild)
        differentChild.appendChild(document.createTextNode(BAR))

        def differentText = document.createElement(FOO)
        def childWithDifferentText = document.createElement(BAR)
        differentText.appendChild(childWithDifferentText)
        childWithDifferentText.appendChild(document.createTextNode(FOO))

        expect:
        selector.canBeCompared(control, equal) == true
        selector.canBeCompared(control, equalCDATA) == true
        selector.canBeCompared(control, noText) == false
        selector.canBeCompared(control, differentLevel) == false
        selector.canBeCompared(control, differentElement) == false
        selector.canBeCompared(control, differentText) == false
    }

    def "should check if can compare by nameAndAllAttributes"() {
        given:
        def selector = ElementSelectors.byNameAndAllAttributes

        def control = document.createElement(FOO)
        control.setAttribute(BAR, BAR)

        def equal = document.createElement(FOO)
        equal.setAttribute(BAR, BAR)

        def noAttributes = document.createElement(FOO)

        def differentValue = document.createElement(FOO)
        differentValue.setAttribute(BAR, FOO)
        def differentName = document.createElement(FOO)
        differentName.setAttribute(FOO, FOO)
        def differentNS = document.createElement(FOO)
        differentNS.setAttributeNS(SOME_URI, BAR, BAR)

        expect:
        selector.canBeCompared(control, equal) == true
        selector.canBeCompared(control, noAttributes) == false
        selector.canBeCompared(noAttributes, control) == false
        selector.canBeCompared(control, differentValue) == false
        selector.canBeCompared(control, differentName) == false
        selector.canBeCompared(control, differentNS) == false
    }

    def "should check if can compare by nameAndAllAttributes Qname and String"() {
        given:
        def control = document.createElement(FOO)
        control.setAttribute(BAR, BAR)

        def equal = document.createElement(FOO)
        equal.setAttribute(BAR, BAR)

        def noAttributes = document.createElement(FOO)

        def differentValue = document.createElement(FOO)
        differentValue.setAttribute(BAR, FOO)

        def differentName = document.createElement(FOO)
        differentName.setAttribute(FOO, FOO)

        def differentNS = document.createElement(FOO)
        differentNS.setAttributeNS(SOME_URI, BAR, BAR)

        expect:
        ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, equal) == true
        ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, noAttributes) == false
        ElementSelectors.byNameAndAttributes(new QName(FOO)).canBeCompared(control, noAttributes) == true
        ElementSelectors.byNameAndAttributes([] as QName[]).canBeCompared(control, noAttributes) == true
        ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(noAttributes, control) == false
        ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, differentValue) == false
        ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, differentName) == false
        ElementSelectors.byNameAndAttributes(new QName(BAR)).canBeCompared(control, differentNS) == false

        ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, equal) == true
        ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, noAttributes) == false
        ElementSelectors.byNameAndAttributes(FOO).canBeCompared(control, noAttributes) == true
        ElementSelectors.byNameAndAttributes([] as String[]).canBeCompared(control, noAttributes) == true
        ElementSelectors.byNameAndAttributes(BAR).canBeCompared(noAttributes, control) == false
        ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, differentValue) == false
        ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, differentName) == false
        ElementSelectors.byNameAndAttributes(BAR).canBeCompared(control, differentNS) == false
    }

    def "should check if can compare by nameAndAttributesControlNS"() {
        given:
        def control = document.createElement(FOO)
        control.setAttributeNS(SOME_URI, BAR, BAR)

        def equal = document.createElement(FOO)
        equal.setAttributeNS(SOME_URI, BAR, BAR)

        def noAttributes = document.createElement(FOO)

        def differentValue = document.createElement(FOO)
        differentValue.setAttributeNS(SOME_URI, BAR, FOO)

        def differentName = document.createElement(FOO)
        differentName.setAttributeNS(SOME_URI, FOO, FOO)

        def differentNS = document.createElement(FOO)
        differentNS.setAttributeNS(SOME_URI + "2", BAR, BAR)

        def noNS = document.createElement(FOO)
        noNS.setAttribute(BAR, BAR)

        expect:
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, equal) == true
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, noAttributes) == false
        ElementSelectors.byNameAndAttributesControlNS(FOO).canBeCompared(control, noAttributes) == true
        ElementSelectors.byNameAndAttributesControlNS([] as String[]).canBeCompared(control, noAttributes) == true
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(noAttributes, control) == true
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(noAttributes, noNS) == false
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, differentValue) == false
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, differentName) == false
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, differentNS) == false
        ElementSelectors.byNameAndAttributesControlNS(BAR).canBeCompared(control, noNS) == false
    }

    def "should check if byName selector can compare elements with namespace"() {
        given:
        def selector = ElementSelectors.byName
        def anURI = "gopher://example.com"
        String anotherURI = "ftp://example.com"
        def qnameQualifierA = "qnq:"
        def qnameQualifierB = "pgp:"

        def control = document.createElementNS(anURI, qnameQualifierA + FOO)
        def equal = document.createElementNS(anURI, qnameQualifierA + FOO)
        def differentName = document.createElementNS(anURI, qnameQualifierA + BAR)
        def differentQualifier = document.createElementNS(anURI, qnameQualifierB + FOO)
        def differentQualifierAndName = document.createElementNS(anURI, qnameQualifierB + BAR)
        def differentUri = document.createElementNS(anotherURI, qnameQualifierA + FOO)
        def differentUriAndQualifier = document.createElementNS(anotherURI, qnameQualifierB + BAR)
        def different = document.createElementNS(anotherURI, qnameQualifierB + BAR)

        expect:
        selector.canBeCompared(control, equal) == true
        selector.canBeCompared(control, differentName) == false
        selector.canBeCompared(control, differentQualifier) == true
        selector.canBeCompared(control, differentQualifierAndName) == false
        selector.canBeCompared(control, differentUri) == false
        selector.canBeCompared(control, differentUriAndQualifier) == false
        selector.canBeCompared(control, different) == false
    }
}
