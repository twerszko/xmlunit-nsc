/*
 ******************************************************************
Copyright (c) 2001-2008,2010 Jeff Martin, Tim Bacon
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;

import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.examples.MultiLevelElementNameAndTextSelector;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test a DetailedDiff. Extend the test case class for Diff so we can rerun
 * those tests with a DetailedDiff and assert that behaviour has not changed.
 */
public class DetailedDiffTest extends DiffTest {

	@Override
	protected Diff prepareDiff(XmlUnitProperties properties, Document control, Document test) {
		return new DetailedDiff(super.prepareDiff(properties, control, test));
	}

	@Override
	protected Diff prepareDiff(XmlUnitProperties properties, String control, String test) throws Exception {
		return new DetailedDiff(super.prepareDiff(properties, control, test));
	}

	@Override
	protected Diff prepareDiff(XmlUnitProperties properties, Reader control, Reader test) throws Exception {
		return new DetailedDiff(super.prepareDiff(properties, control, test));
	}

	@Override
	protected Diff prepareDiff(
	        XmlUnitProperties properties,
	        String control,
	        String test,
	        DifferenceEngineContract engine) throws Exception {

		return new DetailedDiff(super.prepareDiff(properties, control, test, engine));
	}

	private XmlUnitProperties properties;

	@Before
	public void before() {
		properties = new XmlUnitProperties();
	}

	@Test
	public void should_check_all_differences_in_first_forecast() throws BuilderException {
		// given
		String firstForecast =
		        "<weather>" +
		                "<today icon=\"clouds\" temp=\"17\">" +
		                "<outlook>unsettled</outlook></today>" +
		                "</weather>";
		String secondForecast = "<weather><today temp=\"20\"/></weather>";

		// when
		Diff multipleDifferences = Diff.newDiff(properties)
		        .betweenControlDocument(firstForecast)
		        .andTestDocument(secondForecast)
		        .build();
		DetailedDiff detailedDiff = new DetailedDiff(multipleDifferences);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		// then
		assertThat(differences).hasSize(5);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(differences.get(2).getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(differences.get(3).getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(differences.get(4).getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
	}

	@Test
	public void should_check_all_differences_in_second_forecast() throws SAXException, IOException, BuilderException {
		// given
		String firstForecast =
		        "<weather>" +
		                "<today icon=\"clouds\" temp=\"17\">" +
		                "<outlook>unsettled</outlook></today>" +
		                "</weather>";
		String secondForecast = "<weather><today temp=\"20\"/></weather>";

		// when
		Diff multipleDifferences = Diff.newDiff(properties)
		        .betweenControlDocument(secondForecast)
		        .andTestDocument(firstForecast)
		        .build();
		DetailedDiff detailedDiff = new DetailedDiff(multipleDifferences);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		// then
		assertThat(differences).hasSize(5);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(differences.get(2).getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(differences.get(3).getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(differences.get(4).getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
	}

	@Test
	public void should_pass_when_DetailedDiff_decorated_with_DetailedDiff() throws SAXException, IOException,
	        BuilderException {
		// given
		String firstForecast =
		        "<weather>" +
		                "<today icon=\"clouds\" temp=\"17\">" +
		                "<outlook>unsettled</outlook></today>" +
		                "</weather>";
		String secondForecast = "<weather><today temp=\"20\"/></weather>";

		// when
		Diff multipleDifferences = Diff.newDiff(properties)
		        .betweenControlDocument(firstForecast)
		        .andTestDocument(secondForecast)
		        .build();
		DetailedDiff detailedDiff = new DetailedDiff(new DetailedDiff(multipleDifferences));
		List<Comparison> differences = detailedDiff.getAllDifferences();

		// then
		assertThat(differences).hasSize(5);
		assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.HAS_CHILD_NODES);
		assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
		assertThat(differences.get(2).getType()).isEqualTo(ComparisonType.ATTR_NAME_LOOKUP);
		assertThat(differences.get(3).getType()).isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(differences.get(4).getType()).isEqualTo(ComparisonType.CHILD_LOOKUP);
	}

	@Test
	public void should_check_large_files() throws FileNotFoundException, IOException, SAXException,
	        ConfigurationException, XpathException, BuilderException {
		String expr = null;
		File test, control;
		control = TestResources.DETAIL_CONTROL.getFile();
		test = TestResources.DETAIL_TEST.getFile();

		properties.setIgnoreWhitespace(true);
		Diff prototype = Diff.newDiff(properties)
		        .betweenControlDocument(new FileReader(control))
		        .andTestDocument(new FileReader(test))
		        .build();
		DetailedDiff detailedDiff = new DetailedDiff(prototype);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		SimpleXpathEngine xpathEngine = new SimpleXpathEngine(properties);

		DocumentUtils documentUtils = new DocumentUtils(properties);

		Document controlDoc =
		        documentUtils.buildControlDocument(
		                new InputSource(new FileReader(control)));
		Document testDoc =
		        documentUtils.buildTestDocument(
		                new InputSource(new FileReader(test)));

		// TODO: reduce this mess
		String value;
		for (Comparison difference : differences) {

			if (difference.equals(createCompariosn(ComparisonType.ATTR_VALUE))
			        || difference.equals(createCompariosn(ComparisonType.CDATA_VALUE))
			        || difference.equals(createCompariosn(ComparisonType.COMMENT_VALUE))
			        || difference.equals(createCompariosn(ComparisonType.ELEMENT_TAG_NAME))
			        || difference.equals(createCompariosn(ComparisonType.TEXT_VALUE))) {

				expr = difference.getControlDetails().getXpath();
				if (expr != null && expr.length() > 0) {
					value = xpathEngine.evaluate(expr, new DOMSource(controlDoc));
					assertThat(difference.getControlDetails().getValue()).isEqualTo(value);
				}

				expr = difference.getTestDetails().getXpath();
				if (expr != null && expr.length() > 0) {
					value = xpathEngine.evaluate(expr, new DOMSource(testDoc));
					assertThat(difference.getTestDetails().getValue()).isEqualTo(value);
				}
			}
		}
	}

	private Comparison createCompariosn(ComparisonType type) {
		return new Comparison(type, null, null, null, null, null, null);
	}

	@Test
	public void should_see_all_differences_even_if_diff_would_say_halt_comparison()
	        throws SAXException, IOException, BuilderException {

		// given
		String control = "<a><b/><c/></a>";
		String test = "<a><c/></a>";

		// when
		Diff d = Diff.newDiff(properties)
		        .betweenControlDocument(control)
		        .andTestDocument(test)
		        .build();
		DetailedDiff dd = new DetailedDiff(d);

		List<Comparison> differences = dd.getAllDifferences();

		// then
		// number of children is different, didn't find <b/>,
		// wrong sequence of nodes
		assertThat(differences).hasSize(3);
	}

	@Test
	public void should_see_all_differences_even_if_diff_says_halt_comparison()
	        throws SAXException, IOException, BuilderException {

		// given
		String control = "<a><b/><c/></a>";
		String test = "<a><c/></a>";

		// when
		Diff diff = Diff.newDiff(properties)
		        .betweenControlDocument(control)
		        .andTestDocument(test)
		        .build();

		diff.similar();
		DetailedDiff detailedDiff = new DetailedDiff(diff);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		// then
		// number of children is different, didn't find <b/>,
		// wrong sequence of nodes
		assertThat(differences).hasSize(3);
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws BuilderException
	 * @see http 
	 *      ://sourceforge.net/forum/forum.php?thread_id=1691528&forum_id=73274
	 */
	@Test
	public void should_check_issue_from_forum_thread_1691528() throws SAXException, IOException, BuilderException {
		// given
		String control =
		        "<table border=\"1\">" +
		                "<tr>" +
		                "<th>News</th>" +
		                "</tr>" +
		                "<tr>" +
		                "<td>Newsitem 1</td>" +
		                "</tr>" +
		                "</table>";
		String test =
		        "<table border=\"1\">" +
		                "<tr>" +
		                "<th>News</th>" +
		                "</tr>" +
		                "<tr>" +
		                "<td>Newsitem 2</td>" +
		                "<td>Newsitem 1</td>" +
		                "</tr>" +
		                "</table>";

		// when
		DetailedDiff diff = new DetailedDiff(
		        Diff.newDiff(properties)
		                .betweenControlDocument(control)
		                .andTestDocument(test)
		                .build());
		List<Comparison> differences = diff.getAllDifferences();

		// then
		// number of children, text of first child,
		// unexpected second test child
		assertThat(differences).hasSize(3);
	}

	/**
	 * Bug 1860681
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws BuilderException
	 * 
	 * @see https
	 *      ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1860681
	 *      &amp;group_id=23187&amp;atid=377768
	 */
	@Test
	public void should_check_xpath_of_missing_node() throws SAXException, IOException, BuilderException {
		// given
		String control =
		        "<books>" +
		                "  <book>" +
		                "    <title>Kabale und Liebe</title>" +
		                "  </book>" +
		                "  <book>" +
		                "    <title>Schuld und Suehne</title>" +
		                "  </book>" +
		                "</books>";
		String test =
		        "<books>" +
		                "  <book>" +
		                "    <title>Schuld und Suehne</title>" +
		                "  </book>" +
		                "</books>";

		// when
		properties.setIgnoreWhitespace(true);
		Diff diff = Diff.newDiff(properties)
		        .betweenControlDocument(control)
		        .andTestDocument(test)
		        .build();
		diff.overrideElementSelector(new MultiLevelElementNameAndTextSelector(2));
		DetailedDiff detailedDiff = new DetailedDiff(diff);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		// (0) number of children, (1) order different, (2) node not found
		Comparison difference = differences.get(2);
		String controlXpathLocation = difference.getControlDetails().getXpath();
		String testXpathLocation = difference.getTestDetails().getXpath();

		// then
		assertThat(differences).hasSize(3);
		assertThat(difference.getType())
		        .isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(controlXpathLocation).isEqualTo("/books[1]/book[1]");
		assertThat(testXpathLocation).isNull();
	}

	@Test
	public void should_check_xpath_of_missing_node_reverse() throws SAXException, IOException, BuilderException {
		// given
		String control =
		        "<books>" +
		                "  <book>" +
		                "    <title>Kabale und Liebe</title>" +
		                "  </book>" +
		                "  <book>" +
		                "    <title>Schuld und Suehne</title>" +
		                "  </book>" +
		                "</books>";
		String test =
		        "<books>" +
		                "  <book>" +
		                "    <title>Schuld und Suehne</title>" +
		                "  </book>" +
		                "</books>";

		// when
		properties.setIgnoreWhitespace(true);
		Diff diff = Diff.newDiff(properties)
		        .betweenControlDocument(test)
		        .andTestDocument(control)
		        .build();
		diff.overrideElementSelector(new MultiLevelElementNameAndTextSelector(2));
		DetailedDiff detailedDiff = new DetailedDiff(diff);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		// (0) number of children, (1) order different, (2) node not found
		Comparison difference = differences.get(2);
		String reverseControlXpathLocation = difference.getControlDetails().getXpath();
		String reverseTestXpathLocation = difference.getTestDetails().getXpath();

		// then
		assertThat(differences).hasSize(3);
		assertThat(difference.getType())
		        .isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(reverseTestXpathLocation).isEqualTo("/books[1]/book[1]");
		assertThat(reverseControlXpathLocation).isNull();
	}

	/**
	 * https://sourceforge.net/tracker/?func=detail&aid=2758280&group_id=23187&
	 * atid=377768
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void should_compare_unmatched() throws Exception {
		// given
		String control =
		        "<root><a>1</a>" +
		                "<b>1</b>" +
		                "<c>1</c>" +
		                "<d>1</d>" +
		                "<e>1</e></root>";
		String test =
		        "<root><a>1</a>" +
		                "<b>1</b>" +
		                "<z>1</z>" +
		                "<d>1</d>" +
		                "<e>1</e></root>";

		// when
		DetailedDiff detailedDiff = (DetailedDiff) prepareDiff(properties, control, test);
		List<Comparison> differences = detailedDiff.getAllDifferences();
		Comparison difference = differences.get(0);

		// then
		assertThat(differences).hasSize(1);
		assertThat(difference.getType())
		        .isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
	}

	/**
	 * https://sourceforge.net/tracker/?func=detail&aid=2758280&group_id=23187&
	 * atid=377768
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void should_not_compare_unmatched() throws Exception {
		// given
		String control = "<root><a>1</a>" +
		        "<b>1</b>" +
		        "<c>1</c>" +
		        "<d>1</d>" +
		        "<e>1</e></root>";
		String test = "<root><a>1</a>" +
		        "<b>1</b>" +
		        "<z>1</z>" +
		        "<d>1</d>" +
		        "<e>1</e></root>";

		// when
		properties.setCompareUnmatched(false);

		DetailedDiff detailedDiff = (DetailedDiff) prepareDiff(properties, control, test);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		Comparison difference = differences.get(0);
		Node controlNode = difference.getControlDetails().getTarget();
		Node testNode = difference.getTestDetails().getTarget();

		Comparison difference1 = differences.get(1);
		Node controlNode1 = difference1.getControlDetails().getTarget();
		Node testNode1 = difference1.getTestDetails().getTarget();

		// then
		assertThat(differences).hasSize(2);
		assertThat(difference.getType())
		        .isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(controlNode).isNotNull();
		assertThat(testNode).isNull();

		assertThat(difference1.getType())
		        .isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(controlNode1).isNull();
		assertThat(testNode1).isNotNull();
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @see https
	 *      ://sourceforge.net/tracker/index.php?func=detail&amp;aid=3062518
	 *      &amp;group_id=23187&amp;atid=377768
	 */
	@Test
	public void should_check_issue_3062518() throws Exception {
		// given
		String control =
		        "<Fruits>" +
		                "<Apple size=\"11\" color=\"green\"/>" +
		                "<Apple size=\"15\" color=\"green\"/>" +
		                "<Banana size=\"10\"/>" +
		                "</Fruits>";
		String test =
		        "<Fruits>" +
		                "<Apple size=\"11\" color=\"green\"/>" +
		                "<Banana size=\"11\"/>" +
		                "</Fruits>";

		// when
		properties.setCompareUnmatched(false);
		DetailedDiff detailedDiff = (DetailedDiff) prepareDiff(properties, control, test);
		List<Comparison> differences = detailedDiff.getAllDifferences();

		Comparison difference = differences.get(0);
		String controlValue = String.valueOf(difference.getControlDetails().getValue());
		String testValue = String.valueOf(difference.getTestDetails().getValue());
		String controlXpathLocation = difference.getControlDetails().getXpath();
		String testXpathLocation = difference.getTestDetails().getXpath();

		Comparison difference1 = differences.get(1);
		String controlValue1 = String.valueOf(difference1.getControlDetails().getValue());
		String testValue1 = String.valueOf(difference1.getTestDetails().getValue());
		String controlXpathLocation1 = difference1.getControlDetails().getXpath();
		String testXpathLocation1 = difference1.getTestDetails().getXpath();

		Comparison difference2 = differences.get(2);
		String controlValue2 = String.valueOf(difference2.getControlDetails().getValue());
		String testValue2 = String.valueOf(difference2.getTestDetails().getValue());
		String controlXpathLocation2 = difference2.getControlDetails().getXpath();
		String testXpathLocation2 = difference2.getTestDetails().getXpath();

		// didn't find the second Apple element
		Comparison difference3 = differences.get(3);
		String controlValue3 = String.valueOf(difference3.getControlDetails().getValue());
		String testValue3 = String.valueOf(difference3.getTestDetails().getValue());
		String controlXpathLocation3 = difference3.getControlDetails().getXpath();
		String testXpathLocation3 = difference3.getTestDetails().getXpath();

		// then
		assertThat(differences).hasSize(4);
		assertThat(difference.getType())
		        .isEqualTo(ComparisonType.CHILD_NODELIST_LENGTH);
		// expected 3 children is 2
		assertThat(controlValue).isEqualTo("3");
		assertThat(testValue).isEqualTo("2");
		assertThat(controlXpathLocation).isEqualTo("/Fruits[1]");
		assertThat(testXpathLocation).isEqualTo("/Fruits[1]");

		// Banana is the third child in control but the second one in test
		assertThat(controlValue1).isEqualTo("2");
		assertThat(testValue1).isEqualTo("1");
		assertThat(controlXpathLocation1).isEqualTo("/Fruits[1]/Banana[1]");
		assertThat(testXpathLocation1).isEqualTo("/Fruits[1]/Banana[1]");

		// Banana's size attribute doesn't match
		assertThat(difference2.getType())
		        .isEqualTo(ComparisonType.ATTR_VALUE);
		assertThat(controlValue2).isEqualTo("10");
		assertThat(testValue2).isEqualTo("11");
		assertThat(controlXpathLocation2).isEqualTo("/Fruits[1]/Banana[1]/@size");
		assertThat(testXpathLocation2).isEqualTo("/Fruits[1]/Banana[1]/@size");

		assertThat(difference3.getType())
		        .isEqualTo(ComparisonType.CHILD_LOOKUP);
		assertThat(controlValue3).isEqualTo("Apple");
		assertThat(testValue3).isEqualTo("null");
		assertThat(controlXpathLocation3).isEqualTo("/Fruits[1]/Apple[2]");
		assertThat(testXpathLocation3).isNull();
	}
}
