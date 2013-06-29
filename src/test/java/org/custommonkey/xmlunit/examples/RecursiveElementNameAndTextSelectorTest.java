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

package org.custommonkey.xmlunit.examples;

import static org.fest.assertions.api.Assertions.assertThat;
import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEngineFactory;
import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ElementSelectors;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RecursiveElementNameAndTextSelectorTest {
    private Document document;
    private XmlUnitProperties properties;
    private DifferenceEngineFactory engineFactory;
    private final ElementSelector selectorUnderTest = ElementSelectors.byNameAndTextRec;

    @Before
    public void setUp() throws Exception {
        properties = new XmlUnitProperties();
        document = new DocumentUtils(properties).newControlDocumentBuilder().newDocument();
        engineFactory = new DefaultDifferenceEngineFactory(properties);
    }

    @Test
    public void should_cannot_compare_when_additional_text_in_control() throws Exception {
        // given
        String tag = "tag";

        Element control = document.createElement(tag);
        control.appendChild(document.createTextNode("something"));
        Element test = document.createElement(tag);

        // when
        boolean canBeCompared = selectorUnderTest.canBeCompared(control, test);

        // then
        assertThat(canBeCompared).isFalse();
    }

    @Test
    public void should_can_compare_when_identical_children() throws Exception {
        // given
        String tag = "tag";
        String text = "some_text";

        Element control = document.createElement(tag);
        control.appendChild(document.createTextNode(text));
        Element test = document.createElement(tag);
        test.appendChild(document.createTextNode(text));

        // when
        boolean canBeCompared = selectorUnderTest.canBeCompared(control, test);

        // then
        assertThat(canBeCompared).isTrue();
    }

    @Test
    public void should_cannot_compare_when_different_children() throws Exception {
        // given
        String tag = "tag";

        Element control = document.createElement(tag);
        control.appendChild(document.createTextNode("something"));
        Element test = document.createElement(tag);
        test.appendChild(document.createTextNode("something else"));

        // when
        boolean canBeCompared = selectorUnderTest.canBeCompared(control, test);

        // then
        assertThat(canBeCompared).isFalse();
    }

    @Test
    public void should_can_compare_when_multiple_text_child_nodes() throws Exception {
        // given
        String tag = "tag";
        String textA = "simple ";
        String textB = "example";

        Element control = document.createElement(tag);
        control.appendChild(document.createTextNode(textA));
        control.appendChild(document.createTextNode(textB));
        Element test = document.createElement(tag);
        test.appendChild(document.createTextNode(textA + textB));

        // when
        boolean canBeCompared = selectorUnderTest.canBeCompared(control, test);

        // then
        assertThat(canBeCompared).isTrue();
    }

    @Test
    public void should_can_compare_when_tree_level_of_identical_nodes() throws Exception {
        // given
        String tag = "tag";
        String childTag = "child";
        String text = "content";

        Element control = document.createElement(tag);
        Element child = document.createElement(childTag);
        control.appendChild(child);
        Element child2 = document.createElement(tag);
        child.appendChild(child2);
        child2.appendChild(document.createTextNode(text));

        Element test = document.createElement(tag);
        child = document.createElement(childTag);
        test.appendChild(child);
        child2 = document.createElement(tag);
        child.appendChild(child2);
        child2.appendChild(document.createTextNode(text));

        // when
        boolean canBeCompared = selectorUnderTest.canBeCompared(control, test);

        // then
        assertThat(canBeCompared).isTrue();
    }

    /**
     * @see http://sourceforge.net/p/xmlunit/discussion/73274/thread/0d0cca54
     */
    @Test
    public void should_can_compare_when_different_order_deep_in_the_tree() throws Exception {
        String s1 = "<a><b><c>foo</c></b><b><c>bar</c></b></a>";
        String s2 = "<a><b><c>bar</c></b><b><c>foo</c></b></a>";

        Diff d1 = Diff.newDiff(properties)
                .betweenControlDocument(s1)
                .andTestDocument(s2)
                .build();

        engineFactory.useSelector(selectorUnderTest);
        Diff d2 = Diff.newDiff(properties)
                .betweenControlDocument(s1)
                .andTestDocument(s2)
                .usingDifferenceEngineFactory(engineFactory)
                .build();

        // when
        boolean similarWhenDefaultSelector = d1.similar();
        boolean similarWhenSUT = d2.similar();

        // then
        assertThat(similarWhenDefaultSelector).isFalse();
        assertThat(similarWhenSUT).isTrue();

    }

    @Test
    public void diff_should_be_similar_when_different_order_of_children() throws Exception {
        // given
        engineFactory.useSelector(selectorUnderTest);

        String control =
                "<table>\n"
                        + "  <tr>\n"
                        + "    <td>foo</td>\n"
                        + "  </tr>\n"
                        + "  <tr>\n"
                        + "    <td>bar</td>\n"
                        + "  </tr>\n"
                        + "</table>\n";
        String test =
                "<table>\n"
                        + "  <tr>\n"
                        + "    <td>bar</td>\n"
                        + "  </tr>\n"
                        + "  <tr>\n"
                        + "    <td>foo</td>\n"
                        + "  </tr>\n"
                        + "</table>\n";

        Diff d = Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();

        // when
        boolean similar = d.similar();

        // then
        assertThat(similar).isTrue();
    }

    /**
     * @see http://sourceforge.net/p/xmlunit/discussion/73273/thread/396f3cdc
     */
    @Test
    public void should_be_similar_when_different_order_of_children_deep_in_the_tree() throws Exception {
        // given
        engineFactory.useSelector(selectorUnderTest);
        String control = "<root>"
                + "  <ent>"
                + "    <value>"
                + "      <int>1</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>2</int>"
                + "    </value>"
                + "  </ent>"
                + "  <ent>"
                + "    <value>"
                + "      <int>3</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>4</int>"
                + "    </value>"
                + "  </ent>"
                + "</root>";
        String test = "<root>"
                + "  <ent>"
                + "    <value>"
                + "      <int>2</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>1</int>"
                + "    </value>"
                + "  </ent>"
                + "  <ent>"
                + "    <value>"
                + "      <int>3</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>4</int>"
                + "    </value>"
                + "  </ent>"
                + "</root>";

        Diff myDiff = Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();

        // when
        boolean similar = myDiff.similar();

        // then
        assertThat(similar).isTrue();
    }

    /**
     * @see https 
     *      ://sourceforge.net/forum/forum.php?thread_id=2948005&amp;forum_id
     *      =73273
     */
    @Test
    public void should_be_similar_when_different_order_of_children_deep_in_the_tree_2() throws Exception {
        engineFactory.useSelector(selectorUnderTest);
        String control = "<root>"
                + "  <ent>"
                + "    <value>"
                + "      <int>1</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>2</int>"
                + "    </value>"
                + "  </ent>"
                + "  <ent>"
                + "    <value>"
                + "      <int>3</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>4</int>"
                + "    </value>"
                + "  </ent>"
                + "</root>";
        String test = "<root>"
                + "  <ent>"
                + "    <value>"
                + "      <int>1</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>2</int>"
                + "    </value>"
                + "  </ent>"
                + "  <ent>"
                + "    <value>"
                + "      <int>4</int>"
                + "    </value>"
                + "    <value>"
                + "      <int>3</int>"
                + "    </value>"
                + "  </ent>"
                + "</root>";

        Diff myDiff = Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();

        // when
        boolean similar = myDiff.similar();

        // then
        assertThat(similar).isTrue();
    }
}
