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

package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import net.sf.xmlunit.TestResources;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.io.Closeables;

public class TransformTest {
    private static final String FLEABALL = "<fleaball><animal><shaggy>dog</shaggy></animal></fleaball>";

    private final static String DOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>";

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private File animalXsl;

    @Before
    public void setUp() throws Exception {
        animalXsl = TestResources.ANIMAL_XSL.getFile();
    }

    @Test
    public void should_transform_simple_xml() throws TransformerException {
        // given
        Transform transform = new Transform(FLEABALL, animalXsl);

        // when
        String resultString = transform.getResultString();

        // then
        assertThat(stripLineSeparators(resultString)).isEqualTo(DOG);
    }

    @Test
    public void should_be_identical() throws IOException, TransformerException, SAXException {
        // given
        Transform transform = new Transform(FLEABALL, animalXsl);

        // when
        Diff diff = new Diff(new XMLUnitProperties(), DOG, transform);

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_be_identical_when_identity_transform() throws SAXException, IOException, TransformerException {
        // given
        Document controlDoc = XMLUnit.buildControlDocument(FLEABALL);
        Transform transform = new Transform(controlDoc);

        // when
        Document testDoc = transform.getResultDocument();
        Diff diff = new Diff(new XMLUnitProperties(), controlDoc, testDoc);

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_transform_to_html() throws TransformerException {
        // given
        Transform transform = new Transform(FLEABALL, animalXsl);

        // when
        transform.setOutputProperty(OutputKeys.METHOD, "html");
        String resultString = transform.getResultString();

        // then
        assertThat(resultString).isNotEqualTo(DOG);
    }

    @Test
    public void should_transform_from_document() throws SAXException, IOException, TransformerException {
        // given
        Document controlDoc = XMLUnit.buildControlDocument(FLEABALL);
        Transform transform = new Transform(controlDoc, animalXsl);

        // when
        String resultString = transform.getResultString();

        // then
        assertThat(stripLineSeparators(resultString)).isEqualTo(DOG);
    }

    @Test
    public void should_transform_from_file_reader_string() throws SAXException, IOException, TransformerException {
        // given
        Document controlDoc = XMLUnit.buildControlDocument(FLEABALL);
        FileReader reader = new FileReader(animalXsl);
        try {
            char[] animalXSL = new char[1024];
            int length = reader.read(animalXSL);
            String stringFromReader = new String(animalXSL, 0, length);

            // when
            Transform transform = new Transform(controlDoc, stringFromReader);
            String resultString = transform.getResultString();

            // then
            assertThat(stripLineSeparators(resultString)).isEqualTo(DOG);
        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    /**
     * Raised by Craig Strong 04.04.2002
     * 
     * @throws TransformerException
     */
    @Test
    public void should_transform_with_xsl_with_include() throws TransformerException {
        // given
        String input = "<bug><animal>creepycrawly</animal></bug>";

        String xslWithInclude =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">" +
                        "<xsl:output method=\"xml\" version=\"1.0\" indent=\"no\"/>" +
                        "<xsl:template match=\"bug\">" +
                        "<xsl:apply-templates select=\"animal\"/>" +
                        "</xsl:template>" +
                        "<xsl:include href=\"" + animalXsl.toURI() + "\"/>" +
                        "</xsl:stylesheet>";

        // when
        Transform transform = new Transform(input, xslWithInclude);
        transform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        String resultString = transform.getResultString();

        // then
        assertThat(resultString).isEqualTo("<creepycrawly/>");
    }

    /**
     * Issue 1742826
     * 
     * @throws TransformerException
     */
    @Test
    public void should_get_exception_when_incorrect_include_uri() throws TransformerException {
        // given
        String xsl =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">" +
                        "<xsl:include href=\"urn:bar\"/>" +
                        "</xsl:stylesheet>";

        String inputXml = "<foo/>";

        ErrorListener errorListener = XMLUnit.getTransformerFactory().getErrorListener();

        try {
            // when
            URIResolver mockedResolver = mock(URIResolver.class);
            when(mockedResolver.resolve("urn:bar", new File(".").toURI().toString())).thenReturn(null);
            ErrorListener mockedErrorListener = mock(ErrorListener.class);

            XMLUnit.setURIResolver(mockedResolver);
            XMLUnit.getTransformerFactory().setErrorListener(mockedErrorListener);

            Transform transform = new Transform(inputXml, xsl);

            try {
                transform.getResultString();

                fail("should fail because of unknown include URI");
            } catch (ConfigurationException tce) {
                // expected exception because of unknown protocol "urn"
            }

            // then
            verify(mockedResolver, times(1)).resolve("urn:bar", new File(".").toURI().toString());
            verify(mockedErrorListener, times(1)).fatalError(Mockito.any(TransformerException.class));
        } finally {
            XMLUnit.setURIResolver(null);
            XMLUnit.getTransformerFactory().setErrorListener(errorListener);
        }
    }

    private String stripLineSeparators(String text) {
        return text.replace(LINE_SEPARATOR, "");
    }
}
