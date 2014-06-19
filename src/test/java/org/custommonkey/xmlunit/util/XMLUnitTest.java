/*
 ******************************************************************
Copyright (c) 2001-2007, Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit.util;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.custommonkey.xmlunit.XmlUnitBuilder;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

public class XMLUnitTest {
    private XmlUnitProperties properties;

    @Before
    public void setUp() {
        properties = new XmlUnitProperties();
    }

    /**
     * Test overiding the SAX parser used to parse control documents
     */
    @Test
    public void testSetControlParser() throws Exception {
        // given
        DocumentBuilder builderBefore = new DocumentUtils(properties).newControlDocumentBuilder();
        DocumentBuilderFactory controlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

        // when
        properties.setControlDocumentBuilderFactoryClass(controlDocumentBuilderFactory.getClass());
        DocumentBuilder builderAfter = new DocumentUtils(properties).newControlDocumentBuilder();

        // then
        assertFalse(builderBefore == builderAfter);
    }

    @Test
    public void testSetDocumentBuilderFactory() throws Exception {
        // given
        DocumentBuilder builderBefore = new DocumentUtils(properties).newTestDocumentBuilder();
        DocumentBuilderFactory testDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

        // when
        properties.setTestDocumentBuilderFactoryClass(testDocumentBuilderFactory.getClass());
        DocumentBuilder builderAfter = new DocumentUtils(properties).newTestDocumentBuilder();

        // then
        assertThat(builderBefore).isNotSameAs(builderAfter);
    }

    // TODO more casses of loading different document internal factories

    @Test
    public void testIgnoreWhitespace() throws Exception {
        properties.setIgnoreWhitespace(true);
        String test = "<test>  monkey   </test>";
        String control = "<test>monkey</test>";
        assertThat(Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build()
                .similar())
                .isTrue();

        properties.setIgnoreWhitespace(false);
        assertThat(Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build()
                .similar())
                .isFalse();
    }

    @Test(expected = ConfigurationException.class)
    public void should_not_use_incorrect_xslt_version() {
        // given - when
        new XmlUnitBuilder().usingXsltVersion("foo").build();

        // then exception
    }

    @Test(expected = ConfigurationException.class)
    public void should_not_use_negative_xslt_version() {
        // given - when
        new XmlUnitBuilder().usingXsltVersion("-1.0").build();

        // then exception
    }
}
