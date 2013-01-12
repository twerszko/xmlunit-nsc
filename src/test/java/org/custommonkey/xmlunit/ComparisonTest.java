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

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.Comparison.Detail;
import net.sf.xmlunit.diff.ComparisonType;

import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author TimBacon
 */
public class ComparisonTest {
    private final Comparison ORIGINAL = createCompariosn(ComparisonType.ATTR_NAME_LOOKUP);

    @Test
    public void should_check_defensive_constructor() {
        // given
        Comparison difference = createCompariosn(ComparisonType.ATTR_NAME_LOOKUP);

        // then
        assertThat(difference.getType()).isEqualTo(ORIGINAL.getType());
        assertThat(difference.isRecoverable()).isEqualTo(ORIGINAL.isRecoverable());

        assertThat(ORIGINAL.isRecoverable()).isFalse();
        difference.setRecoverable(true);
        assertThat(ORIGINAL.isRecoverable()).isNotEqualTo(difference.isRecoverable());
    }

    @Test
    public void should_check_equals() {
        // given
        Comparison difference = createCompariosn(ORIGINAL.getType());

        // then
        assertThat(difference).isNotEqualTo(null);
        assertFalse(difference.equals("aString"));
        assertThat(difference).isEqualTo(ORIGINAL);
        assertThat(difference).isEqualTo(difference);
    }

    @Test
    public void should_check_toString() {
        // TODO toString should be elsewere
        // given
        Comparison attrNameNotFound = createCompariosn(ComparisonType.ATTR_NAME_LOOKUP);

        String originalAsString =
                "Difference (#" + attrNameNotFound.getType() + ") " +
                        attrNameNotFound.getType().getDescription();

        // then
        assertThat(attrNameNotFound.toString()).isEqualTo(originalAsString);
    }

    @Test
    public void should_check_toString2() {
        // given
        Document document = new DocumentUtils(new XmlUnitProperties()).newControlDocumentBuilder().newDocument();
        Node controlNode = document.createComment("control");
        Detail controlNodeDetail = new Detail(
                controlNode,
                "/testToString/comment()",
                controlNode.getNodeValue());
        Node testNode = document.createComment("test");
        Detail testNodeDetail = new Detail(
                testNode,
                "/testToString/comment()",
                testNode.getNodeValue());

        Comparison difference = new Comparison(
                ComparisonType.COMMENT_VALUE,
                controlNode, "/testToString/comment()", controlNode.getNodeValue(),
                testNode, "/testToString/comment()", testNode.getNodeValue());

        StringBuilder buf = new StringBuilder("Expected ")
                .append(ComparisonType.COMMENT_VALUE.getDescription())
                .append(" 'control' but was 'test' - comparing ");

        NodeDescriptor.appendDetail(buf, controlNodeDetail);
        buf.append(" to ");

        NodeDescriptor.appendDetail(buf, testNodeDetail);

        // then
        assertThat(difference.toString()).isEqualTo(buf.toString());
    }

    // bug 2386807
    @Test
    public void should_check_xpath_of_missing_attribute() throws SAXException, IOException, BuilderException {
        // given
        String control = "<foo><bar a=\"x\" y=\"z\"/></foo>";
        String test = "<foo><bar a=\"x\"/></foo>";
        Diff diff = Diff.newDiff(null)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        DetailedDiff detailedDiff = new DetailedDiff(diff);

        // when
        List<Comparison> diffs = detailedDiff.getAllDifferences();

        Comparison difference1 = diffs.get(0);
        String controlXpathLocation1 = difference1.getControlDetails().getXpath();
        String testXpathLocation1 = difference1.getTestDetails().getXpath();

        // then
        assertThat(diffs).hasSize(2);

        assertThat(difference1.getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
        assertThat(controlXpathLocation1).isEqualTo("/foo[1]/bar[1]");
        assertThat(testXpathLocation1).isEqualTo("/foo[1]/bar[1]");

        Comparison difference2 = diffs.get(1);
        String controlXpathLocation2 = difference2.getControlDetails().getXpath();
        String testXpathLocation2 = difference2.getTestDetails().getXpath();

        assertThat(difference2.getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
        assertThat(controlXpathLocation2).isEqualTo("/foo[1]/bar[1]/@y");
        assertThat(testXpathLocation2).isEqualTo("/foo[1]/bar[1]");
    }

    private Comparison createCompariosn(ComparisonType type) {
        return Comparison.ofType(type)
                .between(null, null)
                .and(null, null);
    }
}
