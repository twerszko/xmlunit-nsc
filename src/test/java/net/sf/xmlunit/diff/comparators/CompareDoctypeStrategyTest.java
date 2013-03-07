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

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.comparators.DoctypeComparisonProvider;
import net.sf.xmlunit.diff.internal.Comparisons;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class CompareDoctypeStrategyTest {
	private final DocumentUtils documentUtils = new DocumentUtils();

	private static File rosesFile;
	private static String rosesDtd;

	private static String crowsDtd;
	private static File crowsFile;

	@BeforeClass
	public static void initFiles() throws Exception {
		String dtd = "<!ELEMENT leaf (#PCDATA)><!ELEMENT root (leaf)>";

		rosesFile = File.createTempFile("Roses", ".dtd");
		rosesFile.deleteOnExit();
		FileWriter rosesWriter = new FileWriter(rosesFile);
		try {
			rosesWriter.write(dtd);
		} finally {
			rosesWriter.close();
		}
		rosesDtd = rosesFile.toURI().toURL().toExternalForm();

		crowsFile = File.createTempFile("TheCrows", ".dtd");
		crowsFile.deleteOnExit();
		FileWriter crowsWriter = new FileWriter(crowsFile);
		try {
			crowsWriter.write(dtd);
		} finally {
			crowsWriter.close();
		}
		crowsDtd = crowsFile.toURI().toURL().toExternalForm();
	}

	@AfterClass
	public static void removeFiles() {
		rosesFile.delete();
		crowsFile.delete();
	}

	@Test
	public void should_detect_different_doctype_name() throws Exception {
		// given
		Document controlDoc = documentUtils.buildControlDocument(
		        "<!DOCTYPE root PUBLIC 'Stone' '" + rosesDtd + "'>"
		                + "<root><leaf/></root>");
		Document testDoc = documentUtils.buildTestDocument(
		        "<!DOCTYPE tree PUBLIC 'Stone' '" + rosesDtd + "'>"
		                + "<tree><leaf/></tree>");

		DocumentType control = controlDoc.getDoctype();
		DocumentType test = testDoc.getDoctype();

		// when
		List<Comparison> differences = findDoctypeDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.DOCTYPE_NAME);
	}

	@Test
	public void should_detect_different_doctype_public_id_1() throws Exception {
		// given
		Document controlDoc = documentUtils.buildControlDocument(
		        "<!DOCTYPE root PUBLIC 'Stone' '" + rosesDtd + "'>"
		                + "<root><leaf/></root>");
		Document testDoc = documentUtils.buildTestDocument(
		        "<!DOCTYPE root PUBLIC 'id' '" + rosesDtd + "'>"
		                + "<root><leaf/></root>");

		DocumentType control = controlDoc.getDoctype();
		DocumentType test = testDoc.getDoctype();

		// when
		List<Comparison> differences = findDoctypeDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.DOCTYPE_PUBLIC_ID);
	}

	@Test
	public void should_detect_different_doctype_public_id_2() throws Exception {
		// given
		Document controlDoc = documentUtils.buildControlDocument(
		        "<!DOCTYPE root PUBLIC 'Stone' '" + rosesDtd + "'>"
		                + "<root><leaf/></root>");
		Document testDoc = documentUtils.buildTestDocument(
		        "<!DOCTYPE root SYSTEM '" + rosesDtd + "'>"
		                + "<root><leaf/></root>");

		DocumentType control = controlDoc.getDoctype();
		DocumentType test = testDoc.getDoctype();

		// when
		List<Comparison> differences = findDoctypeDifferences(control, test);

		// then
		assertThat(differences).hasSize(1);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.DOCTYPE_PUBLIC_ID);
	}

	@Test
	public void should_detect_different_doctype_public_id_and_system_id() throws Exception {
		// given
		Document controlDoc = documentUtils.buildControlDocument(
		        "<!DOCTYPE root PUBLIC 'Stone' '" + rosesDtd + "'>"
		                + "<root><leaf/></root>");
		Document testDoc = documentUtils.buildTestDocument(
		        "<!DOCTYPE root SYSTEM '" + crowsDtd + "'>"
		                + "<root><leaf/></root>");

		DocumentType control = controlDoc.getDoctype();
		DocumentType test = testDoc.getDoctype();

		// when
		List<Comparison> differences = findDoctypeDifferences(control, test);

		// then
		assertThat(differences).hasSize(2);
		Comparison first = differences.get(0);
		assertThat(first.getType()).isEqualTo(ComparisonType.DOCTYPE_PUBLIC_ID);
		Comparison second = differences.get(1);
		assertThat(second.getType()).isEqualTo(ComparisonType.DOCTYPE_SYSTEM_ID);
	}

	private List<Comparison> findDoctypeDifferences(DocumentType controlType, DocumentType testType) {
		ListingComparator comparator = new ListingComparator();

		NodeAndXpath<DocumentType> control = new NodeAndXpath<DocumentType>(controlType, new XPathContext());
		NodeAndXpath<DocumentType> test = new NodeAndXpath<DocumentType>(testType, new XPathContext());

		Comparisons comparisons = new DoctypeComparisonProvider().provideComparisons(control, test);
		comparator.executeComparisons(comparisons);
		return comparator.getDifferences();
	}
}
