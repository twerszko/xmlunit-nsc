/*
 ******************************************************************
Copyright (c) 200, Jeff Martin, Tim Bacon
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;

import javax.xml.transform.OutputKeys;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class SimpleSerializerTest {
    private SimpleSerializer serializer;
    private DocumentUtils documentUtils;

    @Before
    public void setUp() {
        serializer = new SimpleSerializer();
        documentUtils = new DocumentUtils(new XmlUnitProperties());
    }

    @Test
    public void should_serialize() throws SAXException, IOException {
        // given
        String simpleXML = "<season><spring id=\"1\"><eg>daffodils</eg></spring></season>";
        Document doc = documentUtils.buildControlDocument(simpleXML);

        // when
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        String serializedWithXmlDecl = serializer.serialize(doc);
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        String serializedWithoutXmlDecl = serializer.serialize(doc);

        // then
        assertThat(serializedWithXmlDecl).isNotEqualTo(simpleXML);
        assertThat(serializedWithoutXmlDecl).isEqualTo(simpleXML);
    }

    @Test
    public void should_serialize2() throws SAXException, IOException {
        // given
        Document doc = documentUtils.buildControlDocument("<test/>");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // when
        Element testElem = doc.createElement("eg");
        Text lamb = doc.createTextNode("lamb");
        testElem.appendChild(lamb);
        String serializedTestElem = serializer.serialize(testElem);

        // then
        assertThat(serializedTestElem).isEqualTo("<eg>lamb</eg>");
    }

}
