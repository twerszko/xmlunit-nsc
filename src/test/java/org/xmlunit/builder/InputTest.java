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
package org.xmlunit.builder;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.util.Convert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xmlunit.builder.Input;

import com.google.common.io.Closeables;

public class InputTest {

    @Test
    public void should_parse_document() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_FILE.getFile();
        Document doc = parseDocument(Input.fromFile(testFile).build());

        // when
        Source source = Input.fromDocument(doc).build();
        doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_parse_existing_file_by_name() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_FILE.getFile();

        // when
        Source source = Input.fromFile(testFile.getAbsolutePath()).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
        assertThat(TestResources.ANIMAL_FILE.getUri().toString()).isEqualTo(source.getSystemId());
    }

    @Test
    public void should_parse_existing_file_by_file() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_FILE.getFile();

        // when
        Source source = Input.fromFile(testFile).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
        assertThat(TestResources.ANIMAL_FILE.getUri().toString()).isEqualTo(source.getSystemId());
    }

    @Test
    public void should_parse_existing_file_from_stream() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_FILE.getFile();
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);

            // when
            Source source = Input.fromStream(is).build();
            Document doc = parseDocument(source);

            // then
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    @Test
    public void should_parse_existing_file_from_reader() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_FILE.getFile();
        FileReader reader = null;
        try {
            reader = new FileReader(testFile);

            // when
            Source source = Input.fromReader(reader).build();
            Document doc = parseDocument(source);

            // then
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    @Test
    public void should_parse_string() throws Exception {
        // given
        String fileContent = FileUtils.readFileToString(TestResources.ANIMAL_FILE.getFile(), "UTF-8");

        // when
        Source source = Input.fromMemory(fileContent).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_parse_bytes() throws Exception {
        // given
        byte[] byteArray = FileUtils.readFileToByteArray(TestResources.ANIMAL_FILE.getFile());

        // when
        Source source = Input.fromMemory(byteArray).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_parse_file_from_URI_string() throws Exception {
        // given
        String uriString = TestResources.ANIMAL_FILE.getUri().toString();

        // when
        Source source = Input.fromURI(uriString).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_parse_file_from_URI() throws Exception {
        // given
        URI uri = TestResources.ANIMAL_FILE.getUri();

        // when
        Source source = Input.fromURI(uri).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_parse_file_from_URL() throws Exception {
        // given
        URL url = TestResources.ANIMAL_FILE.getUrl();

        // when
        Source source = Input.fromURL(url).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("animal");
    }

    @Test
    public void should_parse_transformation_from_source() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_XSL.getFile();

        // when
        Source input = Input.fromMemory("<animal>furry</animal>").build();
        Source source = Input.byTransforming(input).withStylesheet(Input.fromFile(testFile).build()).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("furry");
    }

    @Test
    public void should_parse_transformation_from_builder() throws Exception {
        // given
        File testFile = TestResources.ANIMAL_XSL.getFile();

        // when
        Input.Builder input = Input.fromMemory("<animal>furry</animal>");
        Source source = Input.byTransforming(input).withStylesheet(Input.fromFile(testFile)).build();
        Document doc = parseDocument(source);

        // then
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("furry");
    }

    private static Document parseDocument(Source s) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(Convert.toInputSource(s));
        return document;
    }
}
