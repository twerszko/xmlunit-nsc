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
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.ProcessingInstruction;

public class CompareProcInstrStrategy extends ComparisonStrategyBase<ProcessingInstruction> {

	public CompareProcInstrStrategy(ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	@Override
	public Comparisons provideComparisons(
	        NodeAndXpath<ProcessingInstruction> control, NodeAndXpath<ProcessingInstruction> test) {
		ProcessingInstruction controlInstr = control.getNode();
		ProcessingInstruction testInstr = test.getNode();

		Comparisons comparisons = new Comparisons();

		comparisons.add(
		        Comparison.ofType(ComparisonType.PROCESSING_INSTRUCTION_TARGET)
		                .between(control, controlInstr.getTarget())
		                .and(test, testInstr.getTarget()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.PROCESSING_INSTRUCTION_DATA)
		                .between(control, controlInstr.getData())
		                .and(test, testInstr.getData()));

		return comparisons;
	}

}
