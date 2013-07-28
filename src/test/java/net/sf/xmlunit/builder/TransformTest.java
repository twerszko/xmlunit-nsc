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
package net.sf.xmlunit.builder;

import static com.googlecode.catchexception.CatchException.caughtException;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.builder.Transform.TransformationResult;
import net.sf.xmlunit.util.XsltUtils;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.io.Closeables;
import com.googlecode.catchexception.apis.CatchExceptionBdd;

public class TransformTest {
    private static final String FLEABALL = "<fleaball><animal><shaggy>dog</shaggy></animal></fleaball>";

    private final static String DOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>";

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private File animalXsl;

    private XmlUnitProperties properties;

    @Before
    public void setUp() throws Exception {
        animalXsl = TestResources.ANIMAL_XSL.getFile();
        properties = new XmlUnitProperties();
    }

    @Test
    public void should_transform_animal_to_string() throws Exception {
        // given
        Source source = Input.fromFile(TestResources.DOG_FILE.getFile()).build();
        Source stylesheet = Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build();

        // when
        String result = Transform.source(source).withStylesheet(stylesheet).build().toString();

        // then
        assertThat(result).isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>");
    }

    @Test
    public void should_transform_animal_to_document() throws Exception {
        // given
        Source source = Input.fromFile(TestResources.DOG_FILE.getFile()).build();
        Source stylesheet = Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build();

        // when
        Document doc = Transform.source(source).withStylesheet(stylesheet).build().toDocument();

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("dog");
    }

    @Test
    public void should_transform_animal_to_html() throws Exception {
        // given
        Source source = Input.fromFile(TestResources.DOG_FILE.getFile()).build();
        Source stylesheet = Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build();

        // when
        String result = Transform.source(source).withStylesheet(stylesheet)
                .withOutputProperty(OutputKeys.METHOD, "html").build().toString();

        // then
        assertThat(result).isNotEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>");
    }

    @Test
    public void should_transform_simple_xml() throws Exception {
        // given
        Source source = Input.fromMemory(FLEABALL).build();
        Source stylesheet = Input.fromFile(animalXsl).build();

        // when
        TransformationResult result = Transform.source(source)
                .withStylesheet(stylesheet)
                .build();
        String resultString = result.toString();

        // then
        assertThat(stripLineSeparators(resultString)).isEqualTo(DOG);
    }

    @Test
    public void should_be_identical() throws Exception {
        // given
        Source source = Input.fromMemory(FLEABALL).build();
        Source stylesheet = Input.fromFile(animalXsl).build();

        // when
        TransformationResult result = Transform.source(source)
                .withStylesheet(stylesheet)
                .build();
        Document resultDocument = result.toDocument();

        Diff diff = Diff.newDiff(properties)
                .betweenControlDocument(DOG)
                .andTestDocument(resultDocument)
                .build();

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_be_identical_when_identity_transform() throws Exception {
        // given
        Document controlDoc = new DocumentUtils(properties).buildControlDocument(FLEABALL);
        Source source = Input.fromNode(controlDoc).build();

        // when
        Document testDoc = Transform.source(source).build().toDocument();
        Diff diff = Diff.newDiff(null)
                .betweenControlDocument(controlDoc)
                .andTestDocument(testDoc)
                .build();

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_transform_to_html() throws Exception {
        // given
        Source source = Input.fromMemory(FLEABALL).build();
        Source stylesheet = Input.fromFile(animalXsl).build();

        // when
        String resultString = Transform.source(source)
                .withStylesheet(stylesheet)
                .withOutputProperty(OutputKeys.METHOD, "html")
                .build()
                .toString();

        // then
        assertThat(resultString).isNotEqualTo(DOG);
    }

    @Test
    public void should_transform_from_document() throws Exception {
        // given
        Document controlDoc = new DocumentUtils(properties).buildControlDocument(FLEABALL);
        Source source = Input.fromDocument(controlDoc).build();
        Source stylesheet = Input.fromFile(animalXsl).build();

        // when
        String resultString = Transform.source(source)
                .withStylesheet(stylesheet)
                .build()
                .toString();

        // then
        assertThat(stripLineSeparators(resultString)).isEqualTo(DOG);
    }

    @Test
    public void should_transform_from_file_reader_string() throws Exception {
        // given
        Document controlDoc = new DocumentUtils(properties).buildControlDocument(FLEABALL);
        FileReader reader = new FileReader(animalXsl);
        try {
            Source source = Input.fromDocument(controlDoc).build();
            Source stylesheet = Input.fromReader(reader).build();

            // when
            String resultString = Transform.source(source)
                    .withStylesheet(stylesheet)
                    .build()
                    .toString();

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
    public void should_transform_with_xsl_with_include() throws Exception {
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
        Source source = Input.fromMemory(input).build();
        Source stylesheet = Input.fromMemory(xslWithInclude).build();

        String resultString = Transform.source(source)
                .withStylesheet(stylesheet)
                .withOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                .build()
                .toString();

        // then
        assertThat(resultString).isEqualTo("<creepycrawly/>");
    }

    /**
     * Issue 1742826
     */
    @Test
    public void should_get_exception_when_incorrect_include_uri() throws Exception {
        // given
        String xsl =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">" +
                        "<xsl:include href=\"urn:bar\"/>" +
                        "</xsl:stylesheet>";

        String inputXml = "<foo/>";

        String systemId = new File(".").toURI().toString();
        Source source = Input.fromMemory(inputXml).build();
        Source stylesheet = Input.fromMemory(xsl).build();
        stylesheet.setSystemId(systemId);
        source.setSystemId(systemId);

        TransformerFactory factory = new XsltUtils().newTransformerFactory();

        // when
        URIResolver mockedResolver = mock(URIResolver.class);
        when(mockedResolver.resolve("urn:bar", systemId)).thenReturn(null);
        ErrorListener mockedErrorListener = mock(ErrorListener.class);

        factory.setURIResolver(mockedResolver);
        factory.setErrorListener(mockedErrorListener);

        TransformationResult result = Transform.source(source)
                .withStylesheet(stylesheet)
                .usingFactory(factory)
                .withURIResolver(mockedResolver)
                .build();
        CatchExceptionBdd.when(result).toString();

        // then
        assertThat(caughtException()).isInstanceOf(ConfigurationException.class);
        verify(mockedResolver, times(1)).resolve("urn:bar", systemId);
    }

    private String stripLineSeparators(String text) {
        return text.replace(LINE_SEPARATOR, "");
    }

}
