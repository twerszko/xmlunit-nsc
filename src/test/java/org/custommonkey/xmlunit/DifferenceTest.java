/*
 ******************************************************************
Copyright (c) 2001-2010, Jeff Martin, Tim Bacon
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
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.diff.DifferenceType;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author TimBacon
 */
public class DifferenceTest {
    private final Difference ORIGINAL = new Difference(DifferenceType.ATTR_NAME_NOT_FOUND);

    @Test
    public void should_check_defensive_constructor() {
        // given
        Difference difference = new Difference(ORIGINAL, null, null);

        // then
        assertThat(difference.getType()).isEqualTo(ORIGINAL.getType());
        assertThat(difference.getDescription()).isEqualTo(ORIGINAL.getDescription());
        assertThat(difference.isRecoverable()).isEqualTo(ORIGINAL.isRecoverable());

        assertThat(ORIGINAL.isRecoverable()).isFalse();
        difference.setRecoverable(true);
        assertThat(ORIGINAL.isRecoverable()).isNotEqualTo(difference.isRecoverable());
    }

    @Test
    public void should_check_equals() {
        // given
        Difference difference = new Difference(ORIGINAL, null, null);

        // then
        assertThat(difference).isNotEqualTo(null);
        assertFalse(difference.equals("aString"));
        assertThat(difference).isEqualTo(ORIGINAL);
        assertThat(difference).isEqualTo(difference);
    }

    @Test
    public void should_check_toString() {
        // given
        Difference attrNameNotFound = new Difference(DifferenceType.ATTR_NAME_NOT_FOUND);

        String originalAsString =
                "Difference (#" + attrNameNotFound.getType() + ") " +
                        attrNameNotFound.getDescription();

        // then
        assertThat(attrNameNotFound.toString()).isEqualTo(originalAsString);
    }

    @Test
    public void should_check_toString2() {
        // given
        Document document = new DocumentUtils(new XMLUnitProperties()).newControlParser().newDocument();
        Node controlNode = document.createComment("control");
        NodeDetail controlNodeDetail = new NodeDetail(
                controlNode.getNodeValue(),
                controlNode,
                "/testToString/comment()");
        Node testNode = document.createComment("test");
        NodeDetail testNodeDetail = new NodeDetail(
                testNode.getNodeValue(),
                testNode,
                "/testToString/comment()");

        Difference difference = new Difference(
                new Difference(DifferenceType.COMMENT_VALUE),
                controlNodeDetail,
                testNodeDetail);
        StringBuffer buf = new StringBuffer("Expected ")
                .append(DifferenceType.COMMENT_VALUE.getDescription())
                .append(" 'control' but was 'test' - comparing ");

        NodeDescriptor.appendNodeDetail(buf, controlNodeDetail);
        buf.append(" to ");

        NodeDescriptor.appendNodeDetail(buf, testNodeDetail);

        // then
        assertThat(difference.toString()).isEqualTo(buf.toString());
    }

    // bug 2386807
    @Test
    public void should_check_xpath_of_missing_attribute() throws SAXException, IOException {
        // given
        String control = "<foo><bar a=\"x\" y=\"z\"/></foo>";
        String test = "<foo><bar a=\"x\"/></foo>";
        Diff diff = new Diff(new XMLUnitProperties(), control, test);
        DetailedDiff detailedDiff = new DetailedDiff(diff);

        // when
        List<Difference> diffs = detailedDiff.getAllDifferences();

        Difference difference1 = diffs.get(0);
        String controlXpathLocation1 = difference1.getControlNodeDetail().getXpathLocation();
        String testXpathLocation1 = difference1.getTestNodeDetail().getXpathLocation();

        Difference difference2 = diffs.get(1);
        String controlXpathLocation2 = difference2.getControlNodeDetail().getXpathLocation();
        String testXpathLocation2 = difference2.getTestNodeDetail().getXpathLocation();

        // then
        assertThat(diffs).hasSize(2);

        assertThat(difference1.getType())
                .isEqualTo(DifferenceType.ELEMENT_NUM_ATTRIBUTES);
        assertThat(controlXpathLocation1).isEqualTo("/foo[1]/bar[1]");
        assertThat(testXpathLocation1).isEqualTo("/foo[1]/bar[1]");

        assertThat(difference2.getType())
                .isEqualTo(DifferenceType.ATTR_NAME_NOT_FOUND);
        assertThat(controlXpathLocation2).isEqualTo("/foo[1]/bar[1]/@y");
        assertThat(testXpathLocation2).isEqualTo("/foo[1]/bar[1]");
    }
}
