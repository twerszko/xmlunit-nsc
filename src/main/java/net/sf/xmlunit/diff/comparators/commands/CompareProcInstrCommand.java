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
package net.sf.xmlunit.diff.comparators.commands;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.ProcessingInstruction;

public class CompareProcInstrCommand extends ComparisonCommandBase<ProcessingInstruction> {

	public CompareProcInstrCommand(ComparisonPerformer compPerformer,
	        NodeAndXpathCtx<ProcessingInstruction> control, NodeAndXpathCtx<ProcessingInstruction> test) {
		super(compPerformer, control, test);
	}

	@Override
	public Queue<Comparison> provideComparisons() {
		ProcessingInstruction controlInstr = getControl().getNode();
		ProcessingInstruction testInstr = getTest().getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(new Comparison(
		        ComparisonType.PROCESSING_INSTRUCTION_TARGET,
		        getControl(), controlInstr.getTarget(),
		        getTest(), testInstr.getTarget()));

		comparisons.add(new Comparison(
		        ComparisonType.PROCESSING_INSTRUCTION_DATA,
		        getControl(), controlInstr.getData(),
		        getTest(), testInstr.getData()));

		return comparisons;
	}

}
