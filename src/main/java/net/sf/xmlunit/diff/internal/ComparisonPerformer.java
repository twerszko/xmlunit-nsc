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
package net.sf.xmlunit.diff.internal;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;

public abstract class ComparisonPerformer {
	public final ComparisonResult performComparison(Comparison comparison) {
		Object controlValue = comparison.getControlDetails().getValue();
		Object testValue = comparison.getTestDetails().getValue();
		boolean equal = controlValue == null ? testValue == null : controlValue.equals(testValue);
		ComparisonResult initialResult = equal ? ComparisonResult.EQUAL : ComparisonResult.DIFFERENT;

		ComparisonResult alteredResult = evaluateResult(comparison, initialResult);
		comparisonPerformed(comparison, alteredResult);

		return alteredResult;
	}

	protected ComparisonResult evaluateResult(Comparison comparison, ComparisonResult result) {
		return result;
	}

	protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {

	}
}
