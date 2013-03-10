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

import static org.fest.assertions.api.Assertions.assertThat;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.DifferenceEvaluator;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.diff.Diff;
import org.junit.Test;

public class TextDifferenceListenerBaseTest {
    private static final String C_ATTR = "controlAttr";
    private static final String T_ATTR = "testAttr";
    private static final String C_CDATA = "controlCdata";
    private static final String T_CDATA = "testCdata";
    private static final String C_CMMT = "controlComment";
    private static final String T_CMMT = "testComment";
    private static final String C_TEXT = "controlText";
    private static final String T_TEXT = "testText";

    private static class TestEvaluator extends TextDifferenceEvaluatorBase {
        public int invocationCounter = 0;
        public Comparison difference;

        protected TestEvaluator(DifferenceEvaluator delegateTo) {
            super(delegateTo);
        }

        @Override
        protected ComparisonResult attributeDifference(Comparison d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

        @Override
        protected ComparisonResult cdataDifference(Comparison d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

        @Override
        protected ComparisonResult commentDifference(Comparison d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

        @Override
        protected ComparisonResult textDifference(Comparison d, ComparisonResult outcome) {
            return ComparisonResult.EQUAL;
        }

    }

    @Test
    public void should_delegate_attribute_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestEvaluator listener = new TestEvaluator(null) {
            @Override
            protected ComparisonResult attributeDifference(Comparison difference, ComparisonResult outcome) {
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
        diff.overrideDifferenceEvaluator(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlDetails().getValue())
                .isEqualTo(C_ATTR);
        assertThat(listener.difference.getTestDetails().getValue())
                .isEqualTo(T_ATTR);

    }

    @Test
    public void should_delegate_cdata_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestEvaluator listener = new TestEvaluator(null) {
            @Override
            protected ComparisonResult cdataDifference(Comparison difference, ComparisonResult outcome) {
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
        diff.overrideDifferenceEvaluator(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlDetails().getValue())
                .isEqualTo(C_CDATA);
        assertThat(listener.difference.getTestDetails().getValue())
                .isEqualTo(T_CDATA);
    }

    @Test
    public void should_delegate_comment_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestEvaluator listener = new TestEvaluator(null) {
            @Override
            protected ComparisonResult commentDifference(Comparison difference, ComparisonResult outcome) {
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
        diff.overrideDifferenceEvaluator(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlDetails().getValue())
                .isEqualTo(C_CMMT);
        assertThat(listener.difference.getTestDetails().getValue())
                .isEqualTo(T_CMMT);
    }

    @Test
    public void should_delegate_text_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        TestEvaluator listener = new TestEvaluator(null) {
            @Override
            protected ComparisonResult textDifference(Comparison difference, ComparisonResult outcome) {
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
        diff.overrideDifferenceEvaluator(listener);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(listener.invocationCounter).isEqualTo(1);
        assertThat(listener.difference.getControlDetails().getValue())
                .isEqualTo(C_TEXT);
        assertThat(listener.difference.getTestDetails().getValue())
                .isEqualTo(T_TEXT);
    }

    @Test
    public void should_delegate_textual_difference() throws Exception {
        // given
        String control = getDoc(C_ATTR, C_CDATA, C_CMMT, C_TEXT);
        String test = getDoc(T_ATTR, T_CDATA, T_CMMT, T_TEXT);

        final int[] invocationCounter = new int[1];

        TextDifferenceEvaluatorBase evaluator = new TextDifferenceEvaluatorBase(null) {
            @Override
            protected ComparisonResult textualDifference(Comparison d, ComparisonResult outcome) {
                invocationCounter[0]++;
                return ComparisonResult.EQUAL;
            }
        };

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceEvaluator(evaluator);
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

        TextDifferenceEvaluatorBase evaluator = new TextDifferenceEvaluatorBase(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison d, ComparisonResult outcome) {
                invocationCounter[0]++;
                return ComparisonResult.EQUAL;
            }
        }) {};

        // when
        Diff diff = Diff.newDiff(new XmlUnitProperties())
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        diff.overrideDifferenceEvaluator(evaluator);
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
