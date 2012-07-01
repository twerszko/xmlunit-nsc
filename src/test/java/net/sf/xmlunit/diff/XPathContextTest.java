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
package net.sf.xmlunit.diff;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import net.sf.xmlunit.util.Linqy;

import org.junit.Test;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class XPathContextTest {
    @Test
    public void empty() {
        assertThat(new XPathContext().getXPath(), is(equalTo("/")));
    }

    @Test
    public void should_check_one_level_of_elements() {
        // given
        List<Element> elements = Lists.newArrayList();
        elements.add(new Element("foo"));
        elements.add(new Element("foo"));
        elements.add(new Element("bar"));
        elements.add(new Element("foo"));

        // when
        XPathContext ctx = new XPathContext();
        ctx.setChildren(elements);

        // then
        ctx.navigateToChild(0);
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(1);
        assertThat(ctx.getXPath(), is(equalTo("/foo[2]")));
        ctx.navigateToParent();
        ctx.navigateToChild(2);
        assertThat(ctx.getXPath(), is(equalTo("/bar[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(3);
        assertThat(ctx.getXPath(), is(equalTo("/foo[3]")));
    }

    @Test
    public void should_check_context_with_appended_children() {
        // given
        List<Element> initialElements = Lists.newArrayList();
        initialElements.add(new Element("foo"));
        initialElements.add(new Element("foo"));

        List<Element> additionaElements = Lists.newArrayList();
        additionaElements.add(new Element("bar"));
        additionaElements.add(new Element("foo"));

        // when
        XPathContext ctx = new XPathContext();
        ctx.setChildren(initialElements);
        ctx.appendChildren(additionaElements);

        // then
        ctx.navigateToChild(0);
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(1);
        assertThat(ctx.getXPath(), is(equalTo("/foo[2]")));
        ctx.navigateToParent();
        ctx.navigateToChild(2);
        assertThat(ctx.getXPath(), is(equalTo("/bar[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(3);
        assertThat(ctx.getXPath(), is(equalTo("/foo[3]")));
    }

    @Test
    public void should_check_two_levels_of_elements() {
        // given
        List<Element> elements = Lists.newArrayList();
        elements.add(new Element("foo"));
        elements.add(new Element("foo"));
        elements.add(new Element("bar"));
        elements.add(new Element("foo"));

        // when
        XPathContext ctx = new XPathContext();
        ctx.setChildren(elements);

        // then
        ctx.navigateToChild(0);
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]")));
        ctx.setChildren(elements);
        ctx.navigateToChild(3);
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]/foo[3]")));
        ctx.navigateToParent();
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(2);
        assertThat(ctx.getXPath(), is(equalTo("/bar[1]")));
    }

    @Test
    public void should_check_attributes() {
        // given
        List<QName> attributes = Lists.newArrayList();
        attributes.add(new QName("bar"));

        // when
        XPathContext ctx = new XPathContext();
        ctx.setChildren(Linqy.singleton(new Element("foo")));
        ctx.navigateToChild(0);
        ctx.addAttributes(attributes);
        ctx.navigateToAttribute(new QName("bar"));

        // then
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]/@bar")));
    }

    @Test
    public void should_check_mixed_elements() {
        // given
        List<XPathContext.NodeInfo> elements = Lists.newArrayList();
        elements.add(new Text());
        elements.add(new Comment());
        elements.add(new CDATA());
        elements.add(new PI());
        elements.add(new CDATA());
        elements.add(new Comment());
        elements.add(new PI());
        elements.add(new Text());

        // when
        XPathContext ctx = new XPathContext();
        ctx.setChildren(elements);

        // then
        ctx.navigateToChild(0);
        assertThat(ctx.getXPath(), is(equalTo("/text()[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(1);
        assertThat(ctx.getXPath(), is(equalTo("/comment()[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(2);
        assertThat(ctx.getXPath(), is(equalTo("/text()[2]")));
        ctx.navigateToParent();
        ctx.navigateToChild(3);
        assertThat(ctx.getXPath(), is(equalTo("/processing-instruction()[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(4);
        assertThat(ctx.getXPath(), is(equalTo("/text()[3]")));
        ctx.navigateToParent();
        ctx.navigateToChild(5);
        assertThat(ctx.getXPath(), is(equalTo("/comment()[2]")));
        ctx.navigateToParent();
        ctx.navigateToChild(6);
        assertThat(ctx.getXPath(), is(equalTo("/processing-instruction()[2]")));
        ctx.navigateToParent();
        ctx.navigateToChild(7);
        assertThat(ctx.getXPath(), is(equalTo("/text()[4]")));
    }

    @Test
    public void should_check_elements_and_namespaces() {
        // given
        List<Element> list = Lists.newArrayList();
        list.add(new Element("foo", "urn:foo:foo"));
        list.add(new Element("foo"));
        list.add(new Element("foo", "urn:foo:bar"));
        HashMap<String, String> uri2Prefix = Maps.newHashMap();
        uri2Prefix.put("urn:foo:bar", "bar");

        // when
        XPathContext ctx = new XPathContext(uri2Prefix);
        ctx.setChildren(list);

        // then
        ctx.navigateToChild(0);
        assertThat(ctx.getXPath(), is(equalTo("/foo[1]")));
        ctx.navigateToParent();
        ctx.navigateToChild(1);
        assertThat(ctx.getXPath(), is(equalTo("/foo[2]")));
        ctx.navigateToParent();
        ctx.navigateToChild(2);
        assertThat(ctx.getXPath(), is(equalTo("/bar:foo[1]")));
    }

    @Test
    public void should_check_attributes_and_namespaces() {
        // given
        HashMap<String, String> uri2Prefix = new HashMap<String, String>();
        uri2Prefix.put("urn:foo:bar", "bar");

        List<QName> list = Lists.newArrayList();
        list.add(new QName("baz"));
        list.add(new QName("urn:foo:bar", "baz"));

        // when
        XPathContext ctx = new XPathContext(uri2Prefix);
        ctx.setChildren(Linqy.singleton(new Element("foo", "urn:foo:bar")));
        ctx.navigateToChild(0);
        ctx.addAttributes(list);

        // then
        ctx.navigateToAttribute(new QName("baz"));
        assertEquals("/bar:foo[1]/@baz", ctx.getXPath());
        ctx.navigateToParent();
        ctx.navigateToAttribute(new QName("urn:foo:bar", "baz"));
        assertEquals("/bar:foo[1]/@bar:baz", ctx.getXPath());
        ctx.navigateToParent();
    }

    private static class Element implements XPathContext.NodeInfo {
        private final QName name;

        private Element(String name) {
            this.name = new QName(name);
        }

        private Element(String name, String ns) {
            this.name = new QName(ns, name);
        }

        public QName getName() {
            return name;
        }

        public short getType() {
            return Node.ELEMENT_NODE;
        }
    }

    private static abstract class NonElement implements XPathContext.NodeInfo {
        public QName getName() {
            return null;
        }
    }

    private static class Text extends NonElement {
        public short getType() {
            return Node.TEXT_NODE;
        }
    }

    private static class Comment extends NonElement {
        public short getType() {
            return Node.COMMENT_NODE;
        }
    }

    private static class PI extends NonElement {
        public short getType() {
            return Node.PROCESSING_INSTRUCTION_NODE;
        }
    }

    private static class CDATA extends NonElement {
        public short getType() {
            return Node.CDATA_SECTION_NODE;
        }
    }
}