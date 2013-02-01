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

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

public class CompareProcInstrStrategyTest {
	private final DocumentUtils documentUtils = new DocumentUtils();

	@Test
	public void should_detect_different_target_of_processing_instructions() throws Exception {
		// given
		DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

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
		DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
		Document document = documentBuilder.newDocument();

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

	private List<Comparison> findProcessingInstrDifferences(
	        ProcessingInstruction controlInstr, ProcessingInstruction testInstr) {

		ListingComparisonPerformer performer = new ListingComparisonPerformer();

		NodeAndXpath<ProcessingInstruction> control = NodeAndXpath.from(controlInstr, new XPathContext());
		NodeAndXpath<ProcessingInstruction> test = NodeAndXpath.from(testInstr, new XPathContext());

		new CompareProcInstrStrategy(performer).execute(control, test);
		return performer.getDifferences();
	}
}
