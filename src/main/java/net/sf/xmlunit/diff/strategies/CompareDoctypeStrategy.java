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
package net.sf.xmlunit.diff.strategies;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.DocumentType;

public class CompareDoctypeStrategy extends ComparisonStrategy<DocumentType> {

	public CompareDoctypeStrategy(
	        ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	@Override
	public Comparisons provideComparisons(NodeAndXpath<DocumentType> control, NodeAndXpath<DocumentType> test) {
		DocumentType controlDt = control.getNode();
		DocumentType testDt = test.getNode();

		Comparisons comparisons = new Comparisons();
		if (controlDt == null || testDt == null) {
			return comparisons;
		}

		comparisons.add(
		        Comparison.ofType(ComparisonType.DOCTYPE_NAME)
		                .between(control, controlDt.getName())
		                .and(test, testDt.getName()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.DOCTYPE_PUBLIC_ID)
		                .between(control, controlDt.getPublicId())
		                .and(test, testDt.getPublicId()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.DOCTYPE_SYSTEM_ID)
		                .between(control, controlDt.getSystemId())
		                .and(test, testDt.getSystemId()));

		return comparisons;
	}
}
