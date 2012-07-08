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
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class DoctypeReaderTest extends AbstractDoctypeTests {
	private static final String NEWLINE = System.getProperty("line.separator");

	@Test
	public void should_get_content() throws IOException {
		// given
		String source = "WooPDeDoO!" + NEWLINE + "GooRanga!" + NEWLINE + " plIng! ";
		StringReader sourceReader = new StringReader(source);

		// when
		DoctypeReader doctypeReader = new DoctypeReader(sourceReader, "nonsense", "words");

		// then
		assertThat(doctypeReader.getContent()).isEqualTo(source);
		// can get content indefinitely from this reader
		assertThat(doctypeReader.getContent()).isEqualTo(source);
	}

	private DoctypeReader initDummyDoctypeReader() {
		StringReader sourceReader = new StringReader("yabba");
		DoctypeReader doctypeReader = new DoctypeReader(sourceReader, "yabba", "don\'t");
		return doctypeReader;
	}

	@SuppressWarnings("deprecation")
	@Test
	public void should_replace_doctype_internal_DTD() {
		// given
		DoctypeReader doctypeReader = initDummyDoctypeReader();
		String input =
		        "<!DOCTYPE cartoons [" +
		                "<!ELEMENT name (#PCDATA)> \n" +
		                "<!ELEMENT toon (name)> \n" +
		                "<!ELEMENT cartoons (toon*)> \n" + "\n]>";
		StringBuffer buf = new StringBuffer(input);
		String expected = "<!DOCTYPE ni SYSTEM \"shrubbery\">";

		// when
		String replacedDoctype = doctypeReader.replaceDoctype(buf, "ni", "shrubbery");

		// then
		assertThat(replacedDoctype).isEqualTo(expected);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void should_replace_doctype_external_DTD() {
		// given
		DoctypeReader doctypeReader = initDummyDoctypeReader();
		String expected = "<! DOCTYPE ni SYSTEM \"shrubbery\">";
		String input = "<! DOCTYPE PUBLIC \"yak\" SYSTEM \"llama\">";
		StringBuffer buf = new StringBuffer(input);

		// when
		String replacedDoctype = doctypeReader.replaceDoctype(buf, "ni", "shrubbery");

		// then
		assertThat(replacedDoctype).isEqualTo(expected);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void should_replace_doctype_no_DTD() {
		// given
		DoctypeReader doctypeReader = initDummyDoctypeReader();
		String input = "<!DOCTYPE ni SYSTEM \"shrubbery\">";
		StringBuffer buf = new StringBuffer(NO_DTD_XML);

		// when
		String replacedDoctype = doctypeReader.replaceDoctype(buf, "ni", "shrubbery");

		// then
		assertThat(replacedDoctype).isEqualTo(input + NO_DTD_XML);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void should_replace_doctype_no_DTD_but_XML_decl() {
		// given
		DoctypeReader doctypeReader = initDummyDoctypeReader();
		String input = XMLConstants.XML_DECLARATION + NO_DTD_XML;
		String expected =
		        XMLConstants.XML_DECLARATION +
		                "<!DOCTYPE ni SYSTEM \"shrubbery\">" +
		                NO_DTD_XML;
		StringBuffer buf = new StringBuffer(input);

		// when
		String replacedDoctype = doctypeReader.replaceDoctype(buf, "ni", "shrubbery");

		// then
		assertThat(replacedDoctype).isEqualTo(expected);
	}

	@Override
	protected String getDoctyped(String expected, String input, String docType, String systemId)
	        throws IOException {

		DoctypeReader doctypeReader =
		        new DoctypeReader(new StringReader(expected), docType, systemId);

		return IOUtils.toString(doctypeReader);
	}
}
