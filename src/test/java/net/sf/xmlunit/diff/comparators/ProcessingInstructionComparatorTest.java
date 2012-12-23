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

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DOMDifferenceEngine;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionComparatorTest {
    private final DocumentUtils documentUtils = new DocumentUtils();

    @Test
    public void should_detect_different_target_of_processing_instructions() throws Exception {
        // given
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);
        ComparisonPerformer performer = engine.getComparisonPerformer();

        String expectedTarget = "down";
        String expectedData = "down down";
        String actualTarget = "dadada";
        String actualData = "down";

        // when
        ProcessingInstruction controlInstr = document.createProcessingInstruction(expectedTarget, expectedData);
        ProcessingInstruction testInstr = document.createProcessingInstruction(actualTarget, actualData);

        NodeAndXpathCtx<ProcessingInstruction> control =
                new NodeAndXpathCtx<ProcessingInstruction>(controlInstr, new XPathContext());
        NodeAndXpathCtx<ProcessingInstruction> test =
                new NodeAndXpathCtx<ProcessingInstruction>(testInstr, new XPathContext());

        new ProcessingInstructionComparator(performer).compare(control, test);
        List<Comparison> differences = evaluator.getDifferences();

        // then
        assertThat(differences).hasSize(2);
        Comparison first = differences.get(0);
        Comparison last = differences.get(1);
        assertThat(first.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_TARGET);
        assertThat(last.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_DATA);
    }
}
