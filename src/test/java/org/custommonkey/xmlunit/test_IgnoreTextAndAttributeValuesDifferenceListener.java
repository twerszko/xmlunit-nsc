/*
 ******************************************************************
Copyright (c) 200, Jeff Martin, Tim Bacon
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

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.DifferenceListener.ReturnType;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.diff.DifferenceType;

/**
 * @author TimBacon
 */
public class test_IgnoreTextAndAttributeValuesDifferenceListener
        extends TestCase {
    private DifferenceListener listener;

    public void testDifferenceFound() {
        assertThat(listener.differenceFound(new Difference(DifferenceType.ATTR_NAME_NOT_FOUND)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.ATTR_SEQUENCE)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.ATTR_VALUE)))
                .isEqualTo(ReturnType.DIFFERENT_NODES_SIMILAR);
        assertThat(listener.differenceFound(new Difference(DifferenceType.ATTR_VALUE_EXPLICITLY_SPECIFIED)))
                .isEqualTo(ReturnType.DIFFERENT_NODES_SIMILAR);
        assertThat(listener.differenceFound(new Difference(DifferenceType.CDATA_VALUE)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.CHILD_NODELIST_SEQUENCE)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.COMMENT_VALUE)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.DOCTYPE_NAME)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.DOCTYPE_PUBLIC_ID)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.DOCTYPE_SYSTEM_ID)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.ELEMENT_NUM_ATTRIBUTES)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.ELEMENT_TAG_NAME)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.HAS_CHILD_NODES)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.HAS_DOCTYPE_DECLARATION)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.NAMESPACE_PREFIX)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.NAMESPACE_URI)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.NODE_TYPE)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.PROCESSING_INSTRUCTION_DATA)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.PROCESSING_INSTRUCTION_TARGET)))
                .isEqualTo(ReturnType.ACCEPT_DIFFERENCE);
        assertThat(listener.differenceFound(new Difference(DifferenceType.TEXT_VALUE)))
                .isEqualTo(ReturnType.DIFFERENT_NODES_SIMILAR);
    }

    public void testClassInUse() throws Exception {
        String control = "<clouds><cloud name=\"cumulus\" rain=\"maybe\">fluffy</cloud></clouds>";
        String similarTest = "<clouds><cloud name=\"cirrus\" rain=\"no\">wispy</cloud></clouds>";

        Diff diff = Diff.newDiff(null)
                .betweenControlDocument(control)
                .andTestDocument(similarTest)
                .build();
        diff.overrideDifferenceListener(listener);
        assertTrue("similar " + diff.toString(),
                diff.similar());
        assertTrue("but not identical " + diff.toString(),
                !diff.identical());

        DetailedDiff detailedDiff = new DetailedDiff(
                Diff.newDiff(null)
                        .betweenControlDocument(control)
                        .andTestDocument(similarTest)
                        .build());
        assertEquals("2 attribute and 1 text values",
                3, detailedDiff.getAllDifferences().size());

        String dissimilarTest = "<clouds><cloud name=\"nimbus\"/></clouds>";
        Diff dissimilarDiff = Diff.newDiff(null)
                .betweenControlDocument(control)
                .andTestDocument(dissimilarTest)
                .build();
        dissimilarDiff.overrideDifferenceListener(listener);
        assertTrue("not similar " + dissimilarDiff.toString(),
                !dissimilarDiff.similar());

        DetailedDiff dissimilarDetailedDiff = new DetailedDiff(
                Diff.newDiff(null)
                        .betweenControlDocument(control)
                        .andTestDocument(dissimilarTest)
                        .build());
        dissimilarDetailedDiff.overrideDifferenceListener(listener);
        List differences = dissimilarDetailedDiff.getAllDifferences();
        assertEquals(
                "has children, wrong number of attributes, missing attribute, different attribute value, and missing text node. "
                        + dissimilarDetailedDiff.toString(),
                5, differences.size());
        int recoverable = 0;
        for (Iterator iter = differences.iterator(); iter.hasNext();) {
            Difference aDifference = (Difference) iter.next();
            if (aDifference.isRecoverable()) {
                recoverable++;
            }
        }
        assertEquals("attribute value difference has been overridden as similar",
                1, recoverable);
    }

    public void testIssue771839() throws Exception {
        String xmlString1 = "<location>"
                + "<street-address>22 any street</street-address>"
                + "<postcode id='3'>XY0099Z</postcode>"
                + "</location>";
        String xmlString2 = "<location>"
                + "<postcode1 id='1'>EC3M 1EB</postcode1>"
                + "<street-address>20 east cheap</street-address>"
                + "</location>";

        Diff d = Diff.newDiff(null)
                .betweenControlDocument(xmlString1)
                .andTestDocument(xmlString2)
                .build();
        d.overrideDifferenceListener(listener);
        assertFalse(d.similar());
        assertTrue("postcode was matched against postcode1",
                d.toString().indexOf("Expected element tag name 'postcode'"
                        + " but was 'postcode1'") > -1);
    }

    @Override
    public void setUp() {
        listener =
                new IgnoreTextAndAttributeValuesDifferenceListener();
    }

    /**
     * Constructor for test_IgnoreTextAndAttributeValuesDifferenceListener.
     * 
     * @param name
     */
    public test_IgnoreTextAndAttributeValuesDifferenceListener(String name) {
        super(name);
    }
}
