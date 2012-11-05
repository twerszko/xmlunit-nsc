/*
 ******************************************************************
Copyright (c) 2008, Jeff Martin, Tim Bacon
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
package org.custommonkey.xmlunit.examples;

import static junit.framework.Assert.fail;
import static org.fest.assertions.api.Assertions.assertThat;
import net.sf.xmlunit.diff.ComparisonResult;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.diff.Diff;
import org.junit.Test;
import org.w3c.dom.Node;

public class TextDifferenceListenerBaseTest {
    private static final String C_ATTR = "controlAttr";
    private static final String T_ATTR = "testAttr";
    private static final String C_CDATA = "controlCdata";
    private static final String T_CDATA = "testCdata";
    private static final String C_CMMT = "controlComment";
    private static final String T_CMMT = "testComment";
    private static final String C_TEXT = "controlText";
    private static final String T_TEXT = "testText";

    private static class TestListener extends TextDifferenceListenerBase {
        public int invocationCounter = 0;
        public Difference difference;

        protected TestListener(DifferenceListener delegateTo) {
            super(delegateTo);
        }

        @Override
        protected ComparisonResult attributeDifference(Difference d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

        @Override
        protected ComparisonResult cdataDifference(Difference d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

        @Override
        protected ComparisonResult commentDifference(Difference d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

        @Override
        protected ComparisonResult textDifference(Difference d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

    }

    @Test
    public void should_delegate_attribute_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestListener listener = new TestListener(null) {
            @Override
            protected ComparisonResult attributeDifference(Difference difference, ComparisonResult outcome) {
                this.difference = difference;
                invocationCounter++;
                return ComparisonResult.EQUAL;
            }
        };

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceListener(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlNodeDetail().getValue())
                .isEqualTo(C_ATTR);
        assertThat(listener.difference.getTestNodeDetail().getValue())
                .isEqualTo(T_ATTR);

    }

    @Test
    public void should_delegate_cdata_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestListener listener = new TestListener(null) {
            @Override
            protected ComparisonResult cdataDifference(Difference difference, ComparisonResult outcome) {
                this.difference = difference;
                invocationCounter++;
                return ComparisonResult.EQUAL;
            }
        };

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceListener(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlNodeDetail().getValue())
                .isEqualTo(C_CDATA);
        assertThat(listener.difference.getTestNodeDetail().getValue())
                .isEqualTo(T_CDATA);
    }

    @Test
    public void should_delegate_comment_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestListener listener = new TestListener(null) {
            @Override
            protected ComparisonResult commentDifference(Difference difference, ComparisonResult outcome) {
                this.difference = difference;
                invocationCounter++;
                return ComparisonResult.EQUAL;
            }
        };

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceListener(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlNodeDetail().getValue())
                .isEqualTo(C_CMMT);
        assertThat(listener.difference.getTestNodeDetail().getValue())
                .isEqualTo(T_CMMT);
    }

    @Test
    public void should_delegate_text_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestListener listener = new TestListener(null) {
            @Override
            protected ComparisonResult textDifference(Difference difference, ComparisonResult outcome) {
                this.difference = difference;
                invocationCounter++;
                return ComparisonResult.EQUAL;
            }
        };

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceListener(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlNodeDetail().getValue())
                .isEqualTo(C_TEXT);
        assertThat(listener.difference.getTestNodeDetail().getValue())
                .isEqualTo(T_TEXT);
    }

    @Test
    public void should_delegate_textual_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        final int[] invocationCounter = new int[1];

        TextDifferenceListenerBase listener = new TextDifferenceListenerBase(null) {
            @Override
            protected ComparisonResult textualDifference(Difference d, ComparisonResult outcome) {
                invocationCounter[0]++;
                return ComparisonResult.EQUAL;
            }
        };

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceListener(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(invocationCounter[0]).isEqualTo(4);
    }

    @Test
    public void should_delegate_everything() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        final int[] invocationCounter = new int[1];

        TextDifferenceListenerBase listener = new TextDifferenceListenerBase(new DifferenceListener() {
            public ComparisonResult differenceFound(Difference d, ComparisonResult outcome) {
                invocationCounter[0]++;
                return ComparisonResult.EQUAL;
            }

            public void skippedComparison(Node c, Node t) {
                fail("skippedComparison shouldn't get invoked");
            }
        }) {};

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceListener(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(invocationCounter[0]).isEqualTo(4);
    }

    private String getDoc(String attr, String cdata, String comment,
            String text) {
        return "<root><first attr=\"" + attr + "\"/><!--" + comment + "-->"
                + "<second><![CDATA[" + cdata + "]]></second><third>" + text
                + "</third></root>";
    }
}
