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
package org.xmlunit.input;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.util.Convert;

public class CommentLessSourceTest {

    @Test
    public void should_strip_comments_at_different_levels() {
        // given
        Source s = new StreamSource(new StringReader("<?xml version='1.0'?>"
                + "<!-- comment 1 -->"
                + "<foo>"
                + "<!-- comment 2 -->"
                + "</foo>"));

        // when
        CommentLessSource cls = new CommentLessSource(s);
        Document doc = Convert.toDocument(cls);

        // then
        assertThat(doc.getChildNodes().getLength()).isEqualTo(1);
        Node child = doc.getChildNodes().item(0);
        assertThat(child).isInstanceOf(Element.class);
        assertThat(child.getChildNodes().getLength()).isEqualTo(0);
    }

}
