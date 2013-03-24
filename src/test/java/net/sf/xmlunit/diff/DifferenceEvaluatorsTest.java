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

import static org.fest.assertions.api.Assertions.assertThat;

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
	public void should_return_orginal_result_when_empty_first() {
		// given
		ComparisonResult expectredResult = ComparisonResult.SIMILAR;
		DifferenceEvaluator d = DifferenceEvaluators.first();
		// when
		ComparisonResult result = d.evaluate(null, expectredResult);
		// then
		assertThat(result).isEqualTo(expectredResult);
	}

	@Test
	public void should_use_first_evaluator() {
		// given
		Evaluator e1 = new Evaluator(ComparisonResult.SIMILAR);
		Evaluator e2 = new Evaluator(ComparisonResult.EQUAL);
		DifferenceEvaluator d = DifferenceEvaluators.first(e1, e2);

		// when
		ComparisonResult result = d.evaluate(null, ComparisonResult.DIFFERENT);

		// then
		assertThat(result).isEqualTo(ComparisonResult.SIMILAR);
		assertThat(e1.called).isTrue();
		assertThat(e2.called).isFalse();
	}

	@Test
	public void should_ommit_first_evaluator() {
		// given
		Evaluator e1 = new Evaluator(ComparisonResult.SIMILAR);
		Evaluator e2 = new Evaluator(ComparisonResult.EQUAL);
		DifferenceEvaluator d = DifferenceEvaluators.first(e1, e2);

		// when
		ComparisonResult result = d.evaluate(null, ComparisonResult.SIMILAR);

		// then
		assertThat(result).isEqualTo(ComparisonResult.EQUAL);
		assertThat(e1.called).isTrue();
		assertThat(e2.called).isTrue();
	}

}
