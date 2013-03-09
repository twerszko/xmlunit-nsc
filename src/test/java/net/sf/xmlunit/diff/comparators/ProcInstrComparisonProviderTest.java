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

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.comparators.ProcInstrComparisonProvider;
import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

public class ProcInstrComparisonProviderTest {
	private final DocumentUtils documentUtils = new DocumentUtils();
	private final Document document = documentUtils.newControlDocumentBuilder().newDocument();

	@Test
	public void should_detect_different_target_of_processing_instructions() throws Exception {
		// given
		String expectedTarget = "down";
		String expectedData = "down down";
		String actualTarget = "dadada";
		String actualData = "down";

		// when
		ProcessingInstruction controlInstr = document.createProcessingInstruction(expectedTarget, expectedData);
		ProcessingInstruction testInstr = document.createProcessingInstruction(actualTarget, actualData);

		List<Comparison> differences = findProcessingInstrDifferences(controlInstr, testInstr);

		// then
		assertThat(differences).hasSize(2);
		Comparison first = differences.get(0);
		Comparison last = differences.get(1);
		assertThat(first.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_TARGET);
		assertThat(last.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_DATA);
	}

	@Test
	public void should_detect_different_target_of_processing_data() throws Exception {
		// given
		String target = "down";
		String expectedData = "down down";
		String actualData = "down";

		// when
		ProcessingInstruction controlInstr = document.createProcessingInstruction(target, expectedData);
		ProcessingInstruction testInstr = document.createProcessingInstruction(target, actualData);

		List<Comparison> differences = findProcessingInstrDifferences(controlInstr, testInstr);

		// then
		assertThat(differences).hasSize(1);
		Comparison first = differences.get(0);
		assertThat(first.getType()).isEqualTo(ComparisonType.PROCESSING_INSTRUCTION_DATA);
	}

	@Test
	public void should_detect_no_differences() throws Exception {
		// given
		String target = "down";
		String data = "up";

		// when
		ProcessingInstruction controlInstr = document.createProcessingInstruction(target, data);
		ProcessingInstruction testInstr = document.createProcessingInstruction(target, data);

		List<Comparison> differences = findProcessingInstrDifferences(controlInstr, testInstr);

		// then
		assertThat(differences).hasSize(0);
	}

	private List<Comparison> findProcessingInstrDifferences(
	        ProcessingInstruction controlInstr, ProcessingInstruction testInstr) {

		ListingComparator comparator = new ListingComparator();

		NodeAndXpath<ProcessingInstruction> control = NodeAndXpath.from(controlInstr, new XPathContext());
		NodeAndXpath<ProcessingInstruction> test = NodeAndXpath.from(testInstr, new XPathContext());

		Comparisons comparisons = new ProcInstrComparisonProvider().provideComparisons(control, test);
		comparator.executeComparisons(comparisons);
		return comparator.getDifferences();
	}
}
