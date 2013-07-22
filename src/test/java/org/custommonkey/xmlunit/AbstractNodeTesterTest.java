/*
 ******************************************************************
Copyright (c) 2007, Jeff Martin, Tim Bacon
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class AbstractNodeTesterTest {

    @Test
    public void testExactlyOncePerMethod() throws Exception {
        // given
        XmlUnitProperties properties = new XmlUnitProperties();
        properties.setExpandEntityReferences(true);
        DocumentUtils documentUtils = new DocumentUtils(properties);

        String testXml = "<!DOCTYPE foo ["
                + "<!ELEMENT foo (#PCDATA)>"
                + "<!ATTLIST foo  attr CDATA #IMPLIED>"
                + "<!ENTITY my \"hello\">"
                + "<!NOTATION notation PUBLIC \"pub\">"
                + "]>"
                + "<foo attr=\"value\">"
                + "<!--comment-->"
                + "<?target processing-instruction?>"
                + "bar"
                + "&my;"
                + "<![CDATA[baz]]>"
                + "</foo>";

        NodeTest nt = new NodeTest(documentUtils, testXml);
        ExactlyOncePerMethod tester = Mockito.spy(new ExactlyOncePerMethod());

        // when
        nt.performTest(tester, new short[] {
                Node.ATTRIBUTE_NODE,
                Node.CDATA_SECTION_NODE,
                Node.COMMENT_NODE,
                Node.DOCUMENT_FRAGMENT_NODE,
                Node.DOCUMENT_NODE,
                Node.DOCUMENT_TYPE_NODE,
                Node.ELEMENT_NODE,
                Node.ENTITY_NODE,
                Node.ENTITY_REFERENCE_NODE,
                Node.NOTATION_NODE,
                Node.PROCESSING_INSTRUCTION_NODE,
                Node.TEXT_NODE,
        });

        // then
        InOrder inOrder = Mockito.inOrder(tester);
        inOrder.verify(tester, times(1)).testElement(any(Element.class));
        inOrder.verify(tester, times(1)).testComment(any(Comment.class));
        inOrder.verify(tester, times(1)).testProcessingInstruction(any(ProcessingInstruction.class));
        inOrder.verify(tester, times(1)).testText(any(Text.class));
        inOrder.verify(tester, times(1)).testCDATASection(any(CDATASection.class));
        inOrder.verify(tester, times(1)).noMoreNodes(any(NodeTest.class));
    }

    private class ExactlyOncePerMethod extends AbstractNodeTester {

        public void testCDATASection(CDATASection cdata) {
            assertThat(cdata.getNodeValue()).isEqualTo("baz");
        }

        public void testComment(Comment comment) {
            assertThat(comment.getNodeValue()).isEqualTo("comment");
        }

        public void testElement(Element element) {
            assertThat(element.getNodeName()).isEqualTo("foo");
            assertThat(element.getAttribute("attr")).isEqualTo("value");
        }

        public void testProcessingInstruction(ProcessingInstruction instr) {
            assertThat(instr.getTarget()).isEqualTo("target");
            assertThat(instr.getData()).isEqualTo("processing-instruction");
        }

        public void testText(Text text) {
            assertThat(text.getNodeValue()).isEqualTo("barhello");
        }

        public void noMoreNodes(NodeTest t) {
        }
    }
}
