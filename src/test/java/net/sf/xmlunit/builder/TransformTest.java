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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;

import net.sf.xmlunit.TestResources;

import org.junit.Test;
import org.w3c.dom.Document;

public class TransformTest {

    @Test
    public void should_transform_animal_to_string() throws IOException {
        // given
        Source source = Input.fromFile(TestResources.DOG_FILE.getFile()).build();
        Source stylesheet = Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build();

        // when
        String result = Transform.source(source).withStylesheet(stylesheet).build().toString();

        // then
        assertThat(result, is(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>")));
    }

    @Test
    public void should_transform_animal_to_document() throws IOException {
        // given
        Source source = Input.fromFile(TestResources.DOG_FILE.getFile()).build();
        Source stylesheet = Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build();

        // when
        Document doc = Transform.source(source).withStylesheet(stylesheet).build().toDocument();

        // then
        assertThat(doc.getDocumentElement().getTagName(), is(equalTo("dog")));
    }

    @Test
    public void should_transform_animal_to_html() throws IOException {
        // given
        Source source = Input.fromFile(TestResources.DOG_FILE.getFile()).build();
        Source stylesheet = Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build();

        // when
        String result = Transform.source(source).withStylesheet(stylesheet)
                .withOutputProperty(OutputKeys.METHOD, "html").build().toString();

        // then
        assertThat(result, is(not(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>"))));
    }

}
