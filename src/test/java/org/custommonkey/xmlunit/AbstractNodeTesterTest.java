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
    public void each_method_should_be_called_exactly_once() throws Exception {
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
