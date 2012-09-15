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

import org.junit.Test;

/**
 * Base for tests for DoctypeReader and DoctypeInputStream
 */
public abstract class AbstractDoctypeTests {

    private static final String COMMENT = "<!-- comment -->";
    protected static final String NO_DTD_XML = "<document><element>one</element></document>";

    protected abstract String getDoctyped(
            String expected,
            String input,
            String docType,
            String systemId)
            throws IOException;

    @Test
    public void should_read_with_doctype() throws IOException {
        // given
        String oz = "Chirurgische Verbesserungen sind g\u00fcnstig";
        String expected = "<!DOCTYPE Kylie SYSTEM \"bumJob\">" + oz;

        // when
        String doctyped = getDoctyped(expected, oz, "Kylie", "bumJob");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_internal_DTD() throws IOException {
        // given
        String input =
                "<!DOCTYPE cartoons [" +
                        "<!ELEMENT name (#PCDATA)> \n" +
                        "<!ELEMENT toon (name)> \n" +
                        "<!ELEMENT cartoons (toon*)> \n" + "\n]>";

        String expected = "<!DOCTYPE ni SYSTEM \"shrubbery\">";

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_external_DTD() throws IOException {
        // given
        String input = "<! DOCTYPE PUBLIC \"yak\" SYSTEM \"llama\">";
        String expected = "<!DOCTYPE ni SYSTEM \"shrubbery\">";

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_no_DTD() throws IOException {
        // given
        String expected = "<!DOCTYPE ni SYSTEM \"shrubbery\">" + NO_DTD_XML;

        // when
        String doctyped = getDoctyped(expected, NO_DTD_XML, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_no_DTD_but_XML_Declaration() throws IOException {
        // given
        String input = XMLConstants.XML_DECLARATION + NO_DTD_XML;
        String expected =
                XMLConstants.XML_DECLARATION +
                        "<!DOCTYPE ni SYSTEM \"shrubbery\">" +
                        NO_DTD_XML;

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_internal_DTD_with_comment() throws IOException {
        // given
        String input = XMLConstants.XML_DECLARATION +
                COMMENT +
                "<!DOCTYPE cartoons [" +
                "<!ELEMENT name (#PCDATA)> \n" +
                "<!ELEMENT toon (name)> \n" +
                "<!ELEMENT cartoons (toon*)> \n" + "\n]>";

        String expected =
                XMLConstants.XML_DECLARATION +
                        "<!DOCTYPE ni SYSTEM \"shrubbery\">" +
                        COMMENT;

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_external_DTD_with_comment() throws IOException {
        // given
        String input = COMMENT + "<! DOCTYPE PUBLIC \"yak\" SYSTEM \"llama\">";
        String expected = "<!DOCTYPE ni SYSTEM \"shrubbery\">" +
                COMMENT;

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_no_DTD_with_comment() throws IOException {
        // given
        String input = COMMENT + NO_DTD_XML;
        String expected = "<!DOCTYPE ni SYSTEM \"shrubbery\">" + input;

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }

    @Test
    public void should_check_no_DTD_but_XML_decl_with_comment() throws IOException {
        // given
        String expected = XMLConstants.XML_DECLARATION
                + "<!DOCTYPE ni SYSTEM \"shrubbery\">" + COMMENT + NO_DTD_XML;
        String input = XMLConstants.XML_DECLARATION + COMMENT + NO_DTD_XML;

        // when
        String doctyped = getDoctyped(expected, input, "ni", "shrubbery");

        // then
        assertThat(doctyped).isEqualTo(expected);
    }
}
