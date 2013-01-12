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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class AbstractDifferenceEngineTest {

    protected abstract AbstractDifferenceEngine getDifferenceEngine();

    private static class ResultGrabber implements DifferenceEvaluator {
        private ComparisonResult outcome = ComparisonResult.CRITICAL;

        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            this.outcome = outcome;
            return outcome;
        }
    }

    @Test
    public void should_compare_two_nulls() {
        // given
        ResultGrabber grabber = new ResultGrabber();
        AbstractDifferenceEngine diffEngine = getDifferenceEngine();
        diffEngine.setDifferenceEvaluator(grabber);

        Comparison comparison =
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(null, null)
                        .and(null, null);

        // then
        assertEquals(ComparisonResult.EQUAL, diffEngine.compare(comparison));
        assertEquals(ComparisonResult.EQUAL, grabber.outcome);
    }

    @Test
    public void should_compare_control_null_and_test_non_null() {
        // given
        ResultGrabber grabber = new ResultGrabber();
        AbstractDifferenceEngine diffEngine = getDifferenceEngine();
        diffEngine.setDifferenceEvaluator(grabber);
        Comparison comparison =
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(null, null)
                        .and(null, "");

        // then
        assertEquals(ComparisonResult.DIFFERENT, diffEngine.compare(comparison));
        assertEquals(ComparisonResult.DIFFERENT, grabber.outcome);
    }

    @Test
    public void should_compare_control_non_null_and_test_null() {
        // given
        ResultGrabber grabber = new ResultGrabber();
        AbstractDifferenceEngine diffEngine = getDifferenceEngine();
        diffEngine.setDifferenceEvaluator(grabber);
        Comparison comparison =
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(null, "")
                        .and(null, null);

        // then
        assertEquals(ComparisonResult.DIFFERENT, diffEngine.compare(comparison));
        assertEquals(ComparisonResult.DIFFERENT, grabber.outcome);
    }

    @Test
    public void compareTwoDifferentNonNulls() {
        ResultGrabber g = new ResultGrabber();
        AbstractDifferenceEngine d = getDifferenceEngine();
        d.setDifferenceEvaluator(g);
        assertEquals(ComparisonResult.DIFFERENT,
                d.compare(
                        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                                .between(null, new Short("1"))
                                .and(null, new Short("2"))));
        assertEquals(ComparisonResult.DIFFERENT, g.outcome);
    }

    @Test
    public void compareTwoEqualNonNulls() {
        ResultGrabber g = new ResultGrabber();
        AbstractDifferenceEngine d = getDifferenceEngine();
        d.setDifferenceEvaluator(g);
        assertEquals(ComparisonResult.EQUAL,
                d.compare(
                        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                                .between(null, new Short("2"))
                                .and(null, new Short("2"))));
        assertEquals(ComparisonResult.EQUAL, g.outcome);
    }

    @Test
    public void compareNotifiesListener() {
        AbstractDifferenceEngine d = getDifferenceEngine();
        ComparisonListenerSupportTest.Listener l =
                new ComparisonListenerSupportTest.Listener(ComparisonResult.EQUAL);
        d.addComparisonListener(l);
        assertEquals(ComparisonResult.EQUAL,
                d.compare(
                        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                                .between(null, new Short("2"))
                                .and(null, new Short("2"))));
        assertEquals(1, l.getInvocations());
    }

    @Test
    public void compareUsesResultOfEvaluator() {
        AbstractDifferenceEngine d = getDifferenceEngine();
        ComparisonListenerSupportTest.Listener l =
                new ComparisonListenerSupportTest.Listener(ComparisonResult.SIMILAR);
        d.addComparisonListener(l);
        d.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison,
                    ComparisonResult outcome) {
                return ComparisonResult.SIMILAR;
            }
        });
        assertEquals(ComparisonResult.SIMILAR,
                d.compare(
                        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                                .between(null, new Short("2"))
                                .and(null, new Short("2"))));
        assertEquals(1, l.getInvocations());
    }
}
