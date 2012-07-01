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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DifferenceEvaluatorsTest {

    private static class Evaluator implements DifferenceEvaluator {
        private boolean called = false;
        private final ComparisonResult ret;

        private Evaluator(ComparisonResult ret) {
            this.ret = ret;
        }

        public ComparisonResult evaluate(Comparison comparison, ComparisonResult orig) {
            called = true;
            return ret;
        }
    }

    @Test
    public void empty_first_just_works() {
        // given
        DifferenceEvaluator d = DifferenceEvaluators.first();
        // when
        ComparisonResult evaluationResult = d.evaluate(null, ComparisonResult.CRITICAL);
        // then
        assertThat(evaluationResult, is(equalTo(ComparisonResult.CRITICAL)));
    }

    @Test
    public void first_change_wins_in_first() {
        // given
        Evaluator e1 = new Evaluator(ComparisonResult.CRITICAL);
        Evaluator e2 = new Evaluator(ComparisonResult.EQUAL);
        DifferenceEvaluator d = DifferenceEvaluators.first(e1, e2);

        // when
        ComparisonResult evaluationResult1 = d.evaluate(null, ComparisonResult.DIFFERENT);

        // then
        assertThat(evaluationResult1, is(equalTo(ComparisonResult.CRITICAL)));
        assertTrue(e1.called);
        assertFalse(e2.called);
        e1.called = false;
        assertEquals(ComparisonResult.EQUAL, d.evaluate(null, ComparisonResult.CRITICAL));
        assertTrue(e1.called);
        assertTrue(e2.called);
    }

}
