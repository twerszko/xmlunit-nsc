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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class ComparisonListenerSupportTest {

    @Test
    public void shoud_count_invocations() {
        // given
        ComparisonListenerSupport support = new ComparisonListenerSupport();
        Listener comparisonListener = new Listener(
                ComparisonResult.EQUAL,
                ComparisonResult.SIMILAR,
                ComparisonResult.DIFFERENT,
                ComparisonResult.CRITICAL);
        Listener matchListener = new Listener(ComparisonResult.EQUAL);
        Listener diffListener = new Listener(ComparisonResult.SIMILAR,
                ComparisonResult.DIFFERENT,
                ComparisonResult.CRITICAL);

        ComparisonResult[] results = new ComparisonResult[] {
                ComparisonResult.EQUAL,
                ComparisonResult.SIMILAR,
                ComparisonResult.DIFFERENT,
                ComparisonResult.CRITICAL
        };

        // when
        support.addComparisonListener(comparisonListener);
        support.addMatchListener(matchListener);
        support.addDifferenceListener(diffListener);
        for (ComparisonResult r : results) {
            support.fireComparisonPerformed(null, r);
        }

        // then
        assertEquals(4, comparisonListener.invocations);
        assertEquals(1, matchListener.invocations);
        assertEquals(3, diffListener.invocations);
    }

    @Test
    public void should_do_nothing_when_no_listeners() {
        // given
        ComparisonListenerSupport support = new ComparisonListenerSupport();

        // when
        support.fireComparisonPerformed(null, ComparisonResult.EQUAL);
    }

    static class Listener implements ComparisonListener {
        private final HashSet<ComparisonResult> acceptable = new HashSet<ComparisonResult>();
        private int invocations = 0;

        Listener(ComparisonResult... accept) {
            acceptable.addAll(Arrays.asList(accept));
        }

        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
            invocations++;
            if (!acceptable.contains(outcome)) {
                fail("unexpected outcome: " + outcome);
            }
        }

        int getInvocations() {
            return invocations;
        }
    }
}
