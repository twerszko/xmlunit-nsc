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
package net.sf.xmlunit.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.xmlunit.TestResources;

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class ConvertTest {

    private static void convertToInputSourceAndAssert(Source s) throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.parse(Convert.toInputSource(s));
        documentAsserts(d);
    }

    private static void documentAsserts(Document d) {
        assertThat(d, IsNull.notNullValue());
        assertThat(d.getDocumentElement().getTagName(), is("animal"));
    }

    @Test
    public void streamSourceToInputSource() throws Exception {
        convertToInputSourceAndAssert(new StreamSource(TestResources.ANIMAL_FILE.getFile()));
    }

    @Test
    public void domSourceToInputSource() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.parse(TestResources.ANIMAL_FILE.getFile());
        convertToInputSourceAndAssert(new DOMSource(d));
    }

    @Test
    public void saxSourceToInputSource() throws Exception {
        InputSource s = new InputSource(new FileInputStream(TestResources.ANIMAL_FILE.getFile()));
        convertToInputSourceAndAssert(new SAXSource(s));
    }

    private static void convertToDocumentAndAssert(Source s) {
        documentAsserts(Convert.toDocument(s));
    }

    @Test
    public void streamSourceToDocument() throws Exception {
        convertToDocumentAndAssert(new StreamSource(TestResources.ANIMAL_FILE.getFile()));
    }

    @Test
    public void domSourceToDocument() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.parse(TestResources.ANIMAL_FILE.getFile());
        convertToDocumentAndAssert(new DOMSource(d));
        assertSame(d, Convert.toDocument(new DOMSource(d)));
    }

    @Test
    public void saxSourceToDocument() throws Exception {
        InputSource s = new InputSource(new FileInputStream(TestResources.ANIMAL_FILE.getFile()));
        convertToDocumentAndAssert(new SAXSource(s));
    }

    @Test
    public void domElementToDocument() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.parse(TestResources.ANIMAL_FILE.getFile());
        convertToDocumentAndAssert(new DOMSource(d.getDocumentElement()));
        assertNotSame(d, Convert.toDocument(new DOMSource(d.getDocumentElement())));
    }

    private static void convertToNodeAndAssert(Source s) {
        Node n = Convert.toNode(s);
        Document d = n instanceof Document ? (Document) n : n.getOwnerDocument();
        documentAsserts(d);
    }

    @Test
    public void streamSourceToNode() throws Exception {
        convertToNodeAndAssert(new StreamSource(TestResources.ANIMAL_FILE.getFile()));
    }

    @Test
    public void domSourceToNode() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.parse(TestResources.ANIMAL_FILE.getFile());
        convertToNodeAndAssert(new DOMSource(d));
        assertSame(d, Convert.toNode(new DOMSource(d)));
    }

    @Test
    public void saxSourceToNode() throws Exception {
        InputSource s = new InputSource(new FileInputStream(TestResources.ANIMAL_FILE.getFile()));
        convertToNodeAndAssert(new SAXSource(s));
    }

    @Test
    public void domElementToNode() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.parse(TestResources.ANIMAL_FILE.getFile());
        convertToNodeAndAssert(new DOMSource(d.getDocumentElement()));
        assertSame(d.getDocumentElement(), Convert.toNode(new DOMSource(d.getDocumentElement())));
    }

}
