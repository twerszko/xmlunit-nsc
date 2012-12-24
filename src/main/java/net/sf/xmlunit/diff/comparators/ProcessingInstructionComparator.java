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
package net.sf.xmlunit.diff.comparators;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionComparator extends NodeComparator<ProcessingInstruction> {

    public ProcessingInstructionComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    @Override
    public ComparisonResult compare(
            NodeAndXpathCtx<ProcessingInstruction> control,
            NodeAndXpathCtx<ProcessingInstruction> test) {

        Queue<ComparisonStrategy<?>> strategies = new LinkedList<ComparisonStrategy<?>>();
        strategies.add(new CompareProcInstrTargetStrategy(control, test));
        strategies.add(new CompareProcInstrDataStrategy(control, test));
        return compare(strategies);
    }

    protected class CompareProcInstrTargetStrategy extends ComparisonStrategy<ProcessingInstruction> {

        public CompareProcInstrTargetStrategy(
                NodeAndXpathCtx<ProcessingInstruction> control,
                NodeAndXpathCtx<ProcessingInstruction> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult performComparison() {
            ProcessingInstruction controlInstr = getControl().getNode();
            ProcessingInstruction testInstr = getTest().getNode();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.PROCESSING_INSTRUCTION_TARGET,
                            getControl(), controlInstr.getTarget(),
                            getTest(), testInstr.getTarget()));
        }
    }

    protected class CompareProcInstrDataStrategy extends ComparisonStrategy<ProcessingInstruction> {

        public CompareProcInstrDataStrategy(
                NodeAndXpathCtx<ProcessingInstruction> control,
                NodeAndXpathCtx<ProcessingInstruction> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult performComparison() {
            ProcessingInstruction controlInstr = getControl().getNode();
            ProcessingInstruction testInstr = getTest().getNode();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.PROCESSING_INSTRUCTION_DATA,
                            getControl(), controlInstr.getData(),
                            getTest(), testInstr.getData()));
        }

    }

}
