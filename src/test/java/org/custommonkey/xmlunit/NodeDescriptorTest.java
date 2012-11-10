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

import java.io.File;
import java.io.IOException;

import net.sf.xmlunit.diff.Comparison.Detail;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class NodeDescriptorTest {
	private Document aDocument;
	private DocumentUtils documentUtils;

	@Before
	public void setUp() {
		documentUtils = new DocumentUtils(new XmlUnitProperties());
		aDocument = documentUtils.newControlDocumentBuilder().newDocument();
	}

	@Test
	public void should_append_document_detail() {
		// given
		StringBuilder buffer = new StringBuilder();
		String textDetail =
		        "<" + NodeDescriptor.DOCUMENT_NODE_DESCRIPTION + "<...>> at /";

		// when
		Detail nodeDetail = new Detail(aDocument, "/", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_attribute_detail() {
		// given
		String attrName = "attrName";
		String attrValue = "attrValue";
		Attr attr = aDocument.createAttribute(attrName);
		attr.setValue(attrValue);

		String tagName = "elemTag";
		Element element = aDocument.createElement(tagName);
		element.setAttributeNode(attr);

		StringBuilder buffer = new StringBuilder();
		String textDetail =
		        "<" + tagName + " " + attrName + "=\"" + attrValue + "\"...> at /elemTag";

		// when
		Detail nodeDetail = new Detail(attr, "/elemTag", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_element_detail() {
		// given
		String tagName = "elemTag";
		Element element = aDocument.createElement(tagName);

		StringBuilder buffer = new StringBuilder();
		String textDetail = "<" + tagName + "...> at /elemTag";

		// when
		Detail nodeDetail = new Detail(element, "/elemTag", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_text_detail() {
		// given
		String textString = "some text";
		Text text = aDocument.createTextNode(textString);
		String tagName = "elemTag";
		Element element = aDocument.createElement(tagName);
		element.appendChild(text);

		StringBuilder buffer = new StringBuilder();
		String textDetail =
		        "<" + tagName + " ...>" +
		                textString +
		                "</" + tagName + "> at /elemTag/text()";

		// when
		Detail nodeDetail = new Detail(text, "/elemTag/text()", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_processing_instruction_detail() {
		// given
		String target = "PItarget";
		String data = "PIdata";
		Node processingInstruction = aDocument.createProcessingInstruction(target, data);

		StringBuilder buffer = new StringBuilder();
		String textDetail = "<?" + target + " " + data + "?> at /processing-instruction()";

		// when
		Detail nodeDetail = new Detail(processingInstruction, "/processing-instruction()", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_comment_detail() {
		// given
		String comments = "This is a comment";
		Node comment = aDocument.createComment(comments);

		StringBuilder buffer = new StringBuilder();
		String textDetail = "<!--" + comments + "--> at /comment()";

		// when
		Detail nodeDetail = new Detail(comment, "/comment()", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_CData_detail() {
		// given
		String cData = "<>& etc";
		Node cDataNote = aDocument.createCDATASection(cData);

		StringBuilder buffer = new StringBuilder();
		String textDetail = "<![CDATA[" + cData + "]]> at /text()";

		// when
		Detail nodeDetail = new Detail(cDataNote, "/text()", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(textDetail);
	}

	@Test
	public void should_append_doc_type_detail() throws SAXException, IOException {
		// given
		File dtdA = File.createTempFile(this.getClass() + "A", "dtd");
		dtdA.deleteOnExit();

		String systemOnlyDTD =
		        "<!DOCTYPE blah SYSTEM \"" + dtdA.toURI().toURL().toExternalForm() + "\">";
		String someContent = "<blah>ignored</blah>";
		String xmlWithExternalDTD = systemOnlyDTD + someContent;

		aDocument = documentUtils.buildControlDocument(xmlWithExternalDTD);
		Node doctypeA = aDocument.getDoctype();

		StringBuilder buffer = new StringBuilder();

		// when
		Detail nodeDetail = new Detail(doctypeA, "/", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(systemOnlyDTD + " at /");
	}

	@Test
	public void should_append_doc_type_detail2() throws SAXException, IOException {
		// given
		File dtdB = File.createTempFile(this.getClass() + "B", "dtd");
		dtdB.deleteOnExit();

		String publicDTD = "<!DOCTYPE web-app "
		        + "PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN\" "
		        + "\"" + dtdB.toURI().toURL().toExternalForm() + "\">";
		String someOtherContent = "<web-app><!--ignore me--></web-app>";
		String xmlWithPublicDTD = publicDTD + someOtherContent;

		aDocument = documentUtils.buildControlDocument(xmlWithPublicDTD);
		Node doctypeB = aDocument.getDoctype();

		StringBuilder buffer = new StringBuilder();

		// when
		Detail nodeDetail = new Detail(doctypeB, "/", "");
		NodeDescriptor.appendDetail(buffer, nodeDetail);

		// then
		assertThat(buffer.toString()).isEqualTo(publicDTD + " at /");
	}

}
