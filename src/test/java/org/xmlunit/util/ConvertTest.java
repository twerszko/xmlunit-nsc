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
package org.xmlunit.util;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xmlunit.TestResources;

public class ConvertTest {

    private File testFile;

    @Before
    public void setUp() throws Exception {
        testFile = TestResources.ANIMAL_FILE.getFile();
    }

    private Document parse(InputSource s) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(s);
    }

    private void assertIsCorrectDocument(Document d) {
        assertThat(d).isNotNull();
        assertThat(d.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_convert_StreamSource_to_InputSource() throws Exception {
        // given
        StreamSource input = new StreamSource(testFile);
        // when
        Document d = parse(Convert.toInputSource(input));
        // then
        assertIsCorrectDocument(d);
    }

    @Test
    public void should_convert_DomSource_to_InputSource() throws Exception {
        // given
        Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testFile);
        DOMSource input = new DOMSource(inputDoc);
        // when
        Document d = parse(Convert.toInputSource(input));
        // then
        assertIsCorrectDocument(d);
    }

    @Test
    public void should_convert_SaxSource_to_InputSource() throws Exception {
        // given
        InputSource input = new InputSource(new FileInputStream(testFile));
        // when
        Document d = parse(Convert.toInputSource(new SAXSource(input)));
        // then
        assertIsCorrectDocument(d);
    }

    @Test
    public void should_convert_StreamSource_to_Document() throws Exception {
        // given
        StreamSource input = new StreamSource(testFile);
        // when
        Document d = Convert.toDocument(input);
        // then
        assertIsCorrectDocument(d);
    }

    @Test
    public void should_convert_DomSource_to_Document() throws Exception {
        // given
        Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testFile);
        DOMSource input = new DOMSource(inputDoc);
        // when
        Document d = Convert.toDocument(input);
        // then
        assertIsCorrectDocument(d);
        assertThat(d).isSameAs(inputDoc);
    }

    @Test
    public void should_convert_SaxSource_to_Document() throws Exception {
        // given
        InputSource s = new InputSource(new FileInputStream(testFile));
        SAXSource input = new SAXSource(s);
        // when
        Document d = Convert.toDocument(input);
        // then
        assertIsCorrectDocument(d);
    }

    @Test
    public void should_convert_DomElement_to_Document() throws Exception {
        // given
        Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testFile);
        DOMSource input = new DOMSource(inputDoc.getDocumentElement());
        // when
        Document d = Convert.toDocument(input);
        // then
        assertIsCorrectDocument(d);
        assertThat(d).isNotSameAs(inputDoc);
    }

    private void assertIsCorrectNode(Node n) {
        Document d = n instanceof Document ? (Document) n : n.getOwnerDocument();
        assertIsCorrectDocument(d);
    }

    @Test
    public void should_convert_StreamSource_to_Node() throws Exception {
        // given
        StreamSource input = new StreamSource(testFile);
        // when
        Node n = Convert.toNode(input);
        // then
        assertIsCorrectNode(n);
    }

    @Test
    public void should_convert_DomSource_to_Node() throws Exception {
        // given
        Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testFile);
        DOMSource input = new DOMSource(inputDoc);
        // when
        Node n = Convert.toNode(input);
        // then
        assertIsCorrectNode(n);
        assertThat(n).isSameAs(inputDoc);
    }

    @Test
    public void should_convert_SaxSource_to_Node() throws Exception {
        // given
        SAXSource input = new SAXSource(new InputSource(new FileInputStream(testFile)));
        // when
        Node n = Convert.toNode(input);
        // then
        assertIsCorrectNode(n);
    }

    @Test
    public void should_convert_DomElement_to_Node() throws Exception {
        // given
        Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testFile);
        DOMSource input = new DOMSource(inputDoc.getDocumentElement());
        // when
        Node n = Convert.toNode(input);
        // then
        assertIsCorrectNode(n);
        assertThat(n).isSameAs(inputDoc.getDocumentElement());
    }

}
