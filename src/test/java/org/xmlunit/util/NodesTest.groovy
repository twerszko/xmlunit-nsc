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
package org.xmlunit.util

import org.w3c.dom.*
import org.xmlunit.transform.Input
import spock.lang.Specification

import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX
import static javax.xml.XMLConstants.NULL_NS_URI

class NodesTest extends Specification {
    private static final String FOO = "foo"
    private static final String BAR = "bar"
    private static final String SOME_URI = "urn:some:uri"

    private Document doc

    def setup() {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    }

    def "should create qname with no ns"() {
        when:
        def q = Nodes.getQName(doc.createElement(FOO))

        then:
        q.getLocalPart() == FOO
        q.getNamespaceURI() == NULL_NS_URI
        q.getPrefix() == DEFAULT_NS_PREFIX
        q == new QName(FOO)
    }

    def "should create qname with ns but without prefix"() {
        when:
        QName q = Nodes.getQName(doc.createElementNS(SOME_URI, FOO))

        then:
        q.getLocalPart() == FOO
        q.getNamespaceURI() == SOME_URI
        q.getPrefix() == DEFAULT_NS_PREFIX
        q == new QName(SOME_URI, FOO)
    }

    def "should create qname with ns and prefix"() {
        given:
        Element e = doc.createElementNS(SOME_URI, FOO)
        e.setPrefix(BAR)

        when:
        def q = Nodes.getQName(e)

        then:
        q.getLocalPart() == FOO
        q.getNamespaceURI() == SOME_URI
        q.getPrefix() == BAR
        q == new QName(SOME_URI, FOO)
        q == new QName(SOME_URI, FOO, BAR)
    }

    def "should return empty string when no text to merge"() {
        expect:
        Nodes.getMergedNestedText(doc.createElement(FOO)) == ""
    }

    def "should merge single text node"() {
        given:
        Element e = doc.createElement(FOO)
        Text t = doc.createTextNode(BAR)
        e.appendChild(t);

        when:
        def text = Nodes.getMergedNestedText(e)

        then:
        text == BAR
    }

    def "should merge single CDATA section"() {
        given:
        Element e = doc.createElement(FOO)
        CDATASection t = doc.createCDATASection(BAR)
        e.appendChild(t)

        when:
        def text = Nodes.getMergedNestedText(e)

        then:
        text == BAR
    }

    def "should merge only direct text children ignoring grandchildren"() {
        given:
        Element e = doc.createElement(FOO)
        Element c = doc.createElement("child")
        Text t = doc.createTextNode(BAR)
        e.appendChild(c)
        c.appendChild(t)

        when:
        def text = Nodes.getMergedNestedText(e)

        then:
        text == ""
    }

    def "should remove comments while merging text"() {
        given:
        Element e = doc.createElement(FOO)
        Comment c = doc.createComment(BAR)
        e.appendChild(c);

        when:
        def text = Nodes.getMergedNestedText(e)

        then:
        text == ""
    }

    def "should merge multiple text children"() {
        given:
        Element e = doc.createElement(FOO)
        CDATASection c = doc.createCDATASection(BAR)
        e.appendChild(c)
        e.appendChild(doc.createElement("child"))
        Text t = doc.createTextNode(BAR)
        e.appendChild(t)

        when:
        def text = Nodes.getMergedNestedText(e)

        then:
        text == BAR + BAR
    }

    def "should return empty map when no attributes"() {
        given:
        Element e = doc.createElement(FOO);
        when:
        def m = Nodes.getAttributes(e);
        then:
        m.size() == 0
    }

    def "should put in map single attribute without ns"() {
        given:
        Element e = doc.createElement(FOO)
        e.setAttribute(FOO, BAR)

        when:
        def m = Nodes.getAttributes(e)

        then:
        m.size() == 1
        m.get(new QName(FOO)) == BAR
    }

    def "should put in map single attribute with ns"() {
        given:
        Element e = doc.createElement(FOO)
        e.setAttributeNS(SOME_URI, FOO, BAR)

        when:
        def m = Nodes.getAttributes(e)

        then:
        m.size() == 1
        m.get(new QName(SOME_URI, FOO)) == BAR
        m.get(new QName(SOME_URI, FOO, BAR)) == BAR
    }

    def "should normalize"() {
        expect:
        Nodes.normalize("foo") == "foo"
        Nodes.normalize("foo bar") == "foo bar"
        Nodes.normalize("foo\nbar") == "foo bar"
        Nodes.normalize("foo  \r\n\t bar") == "foo bar"
    }

    def "stripping whitespaces should not change original document"() {
        given:
        def sampleDoc = getSampleDocument()
        when:
        Nodes.stripWhitespace(sampleDoc);
        then:
        assertSampleDocStructure(sampleDoc);
    }

    def "normalizing whitespaces should not change original document"() {
        given:
        def sampleDoc = getSampleDocument()
        when:
        Nodes.normalizeWhitespace(sampleDoc)
        then:
        assertSampleDocStructure(sampleDoc)
    }

    private static final String SAMPLE_XML = "<root>\n" +
            "<!-- trim\tme -->\n" +
            "<child attr=\" trim me \" attr2=\"not me\">\n" +
            " trim me \n" +
            "</child><![CDATA[ trim me ]]>\n" +
            "<?target  trim me ?>\n" +
            "<![CDATA[          ]]>\n" +
            "</root>"

    private Document getSampleDocument() {
        return Convert.toDocument(Input.fromMemory(SAMPLE_XML).build());
    }

    private void assertSampleDocStructure(Document doc) {
        NodeList top = doc.getChildNodes();
        assert top.getLength() == 1
        assert top.item(0) instanceof Element
        assert top.item(0).getNodeName() == "root"

        NodeList rootsChildren = top.item(0).getChildNodes();
        assert rootsChildren.getLength() == 10
        assertIsNewlineTextNode(rootsChildren.item(0));
        assert rootsChildren.item(1) instanceof Comment
        assert ((Comment) rootsChildren.item(1)).getData() == " trim\tme "
        assertIsNewlineTextNode(rootsChildren.item(2));
        assert rootsChildren.item(3) instanceof Element
        assert rootsChildren.item(3).getNodeName() == "child"
        assert rootsChildren.item(4) instanceof CDATASection
        assert ((CDATASection) rootsChildren.item(4)).getData() == " trim me "
        assertIsNewlineTextNode(rootsChildren.item(5));
        assert rootsChildren.item(6) instanceof ProcessingInstruction
        assert ((ProcessingInstruction) rootsChildren.item(6)).getData() == "trim me "
        assertIsNewlineTextNode(rootsChildren.item(7));
        assert rootsChildren.item(8) instanceof CDATASection
        assert ((CDATASection) rootsChildren.item(8)).getData() == "          "
        assertIsNewlineTextNode(rootsChildren.item(9));

        Node child = rootsChildren.item(3);
        NodeList grandChildren = child.getChildNodes();
        assert grandChildren.getLength() == 1
        assert grandChildren.item(0) instanceof Text
        assert ((Text) grandChildren.item(0)).getData() == "\n trim me \n"

        NamedNodeMap attrs = child.getAttributes();
        assert attrs.getLength() == 2
        assert ((Attr) attrs.getNamedItem("attr")).getValue() == " trim me "
        assert ((Attr) attrs.getNamedItem("attr2")).getValue() == "not me"
    }

    private void assertIsNewlineTextNode(Node n) {
        assert n instanceof Text
        assert n.getData() == "\n"
    }

    def "should strip whitespace"() {
        given:
        def expectedComment = "trim\tme"
        when:
        def modifiedDoc = Nodes.stripWhitespace(getSampleDocument())
        then:
        assertSampleDocTrimmed(modifiedDoc, expectedComment)
    }

    def "should normalize whitespace"() {
        given:
        def expectedComment = "trim me"
        when:
        def modifiedDoc = Nodes.normalizeWhitespace(getSampleDocument())
        then:
        assertSampleDocTrimmed(modifiedDoc, expectedComment)
    }

    private void assertSampleDocTrimmed(Node n, String commentContent) {
        assert n instanceof Document
        NodeList top = n.getChildNodes()
        assert top.getLength() == 1
        assert top.item(0) instanceof Element
        assert top.item(0).getNodeName() == "root"
        NodeList rootsChildren = top.item(0).getChildNodes();
        assert rootsChildren.getLength() == 4
        assert rootsChildren.item(0) instanceof Comment
        assert ((Comment) rootsChildren.item(0)).getData() == commentContent
        assert rootsChildren.item(1) instanceof Element
        assert rootsChildren.item(1).getNodeName() == "child"
        assert rootsChildren.item(2) instanceof CDATASection
        assert ((CDATASection) rootsChildren.item(2)).getData() == "trim me"
        assert rootsChildren.item(3) instanceof ProcessingInstruction
        assert ((ProcessingInstruction) rootsChildren.item(3)).getData() == "trim me"

        Node child = rootsChildren.item(1);
        NodeList grandChildren = child.getChildNodes();
        assert grandChildren.getLength() == 1
        assert grandChildren.item(0) instanceof Text
        assert ((Text) grandChildren.item(0)).getData() == "trim me"

        NamedNodeMap attrs = child.getAttributes();
        assert attrs.getLength() == 2
        assert ((Attr) attrs.getNamedItem("attr")).getValue() == "trim me"
        assert ((Attr) attrs.getNamedItem("attr2")).getValue() == "not me"
    }
}
