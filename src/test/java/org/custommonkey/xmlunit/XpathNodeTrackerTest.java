/*
 ******************************************************************
Copyright (c) 2001, Jeff Martin, Tim Bacon
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

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class XpathNodeTrackerTest {
    private XpathNodeTracker xpathNodeTracker;
    private static final Node DUMMY_NODE = null;
    private DocumentUtils documentUtils = new DocumentUtils(new XmlUnitProperties());

    @Before
    public void setUp() {
        xpathNodeTracker = new XpathNodeTracker();
        xpathNodeTracker.reset();

    }

    @Test
    public void should_track_diary() {
        // when
        xpathNodeTracker.visitedNode(DUMMY_NODE, "diary");
        String xpathString = xpathNodeTracker.toXpathString();

        // then
        assertThat(xpathString).isEqualTo("/diary[1]");
    }

    @Test
    public void should_track_one_level_of_children() {
        xpathNodeTracker.visitedNode(DUMMY_NODE, "diary");

        xpathNodeTracker.indent();
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]");

        xpathNodeTracker.visitedNode(DUMMY_NODE, "event");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[1]");

        xpathNodeTracker.visitedNode(DUMMY_NODE, "event");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[2]");

        xpathNodeTracker.visitedNode(DUMMY_NODE, "event");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[3]");

        xpathNodeTracker.visitedNode(DUMMY_NODE, "reminder");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/reminder[1]");

        xpathNodeTracker.visitedNode(DUMMY_NODE, "event");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[4]");
    }

    @Test
    public void should_track_two_levels_of_children() {
        // then
        xpathNodeTracker.visitedNode(DUMMY_NODE, "diary");

        xpathNodeTracker.indent();
        xpathNodeTracker.visitedNode(DUMMY_NODE, "event");

        xpathNodeTracker.indent();
        xpathNodeTracker.visitedNode(DUMMY_NODE, "details");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[1]/details[1]");

        xpathNodeTracker.outdent();
        xpathNodeTracker.visitedNode(DUMMY_NODE, "event");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[2]");

        xpathNodeTracker.indent();
        xpathNodeTracker.visitedNode(DUMMY_NODE, "details");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[2]/details[1]");

        xpathNodeTracker.outdent();
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]/event[2]");

        xpathNodeTracker.outdent();
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]");
    }

    @Test
    public void should_check_nodes() {
        Document doc = documentUtils.newControlDocumentBuilder().newDocument();
        Element element = doc.createElementNS("http://example.com/xmlunit", "eg:root");
        xpathNodeTracker.visited(element);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/root[1]");

        Attr attr = doc.createAttributeNS("http://example.com/xmlunit", "eg:type");
        attr.setValue("qwerty");
        element.setAttributeNodeNS(attr);
        xpathNodeTracker.visited(attr);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/root[1]/@type");

        xpathNodeTracker.indent();

        Comment comment = doc.createComment("testing a comment");
        xpathNodeTracker.visited(comment);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/root[1]/comment()[1]");

        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        xpathNodeTracker.visited(pi);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/root[1]/processing-instruction()[1]");

        Text text = doc.createTextNode("some text");
        xpathNodeTracker.visited(text);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/root[1]/text()[1]");

        CDATASection cdata = doc.createCDATASection("some characters");
        xpathNodeTracker.visited(cdata);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/root[1]/text()[2]");
    }

    @Test
    public void should_check_repeat_nodes_for_test_tracker() {
        Document doc = documentUtils.newControlDocumentBuilder().newDocument();
        final Element element = doc.createElement("repeated");
        final Element copy = doc.createElement("repeated");

        NodeList nodeList = new NodeList() {
            public Node item(int index) {
                switch (index) {
                case 0:
                    return element;
                case 1:
                    return copy;
                default:
                    return null;
                }
            }

            public int getLength() {
                return 2;
            }
        };
        xpathNodeTracker.preloadNodeList(nodeList);

        xpathNodeTracker.visited(element);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/repeated[1]");

        xpathNodeTracker.visited(element);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/repeated[1]");

        xpathNodeTracker.visited(copy);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/repeated[2]");
    }

    @Test
    public void should_check_repeat_nodes_for_control_tracker() {
        Document doc = documentUtils.newControlDocumentBuilder().newDocument();
        Element element = doc.createElement("repeated");

        xpathNodeTracker.visited(element);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/repeated[1]");

        xpathNodeTracker.visited(element);
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/repeated[2]");
    }

    // bug 1047364
    @Test
    public void should_check_empty_indent_outdent_root_node() {
        xpathNodeTracker.indent();
        xpathNodeTracker.outdent();
        xpathNodeTracker.visitedNode(DUMMY_NODE, "diary");
        assertThat(xpathNodeTracker.toXpathString()).isEqualTo("/diary[1]");
    }

}
