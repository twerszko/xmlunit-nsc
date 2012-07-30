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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.io.Closeables;

public class NodeInputStreamTest {
    private NodeInputStream nodeStream;
    private final String frog =
            "<frog>" +
                    "<!-- eats slugs and snails -->" +
                    "<frogspawn>fertilised egg</frogspawn>" +
                    "<tadpole juvenile=\"true\"/>" +
                    "</frog>";

    @Before
    public void setUp() throws SAXException, IOException {
        Document document = new DocumentUtils(new XMLUnitProperties()).buildControlDocument(frog);
        nodeStream = new NodeInputStream(document);
    }

    @After
    public void tearDown() {
        Closeables.closeQuietly(nodeStream);
    }

    @Test
    public void should_read() throws SAXException, IOException {
        // given
        Reader testReader = new InputStreamReader(nodeStream);
        StringReader controlReader = new StringReader(frog);

        // when
        Diff diff = new Diff(new XMLUnitProperties(), controlReader, testReader);

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_get_available() throws IOException {
        // when
        int availableAtStart = nodeStream.available();
        nodeStream.read();
        int availableAfterRead = nodeStream.available();

        // then
        assertThat(availableAtStart).isGreaterThan(0);
        assertThat(availableAfterRead).isEqualTo(availableAtStart - 1);
    }

}
