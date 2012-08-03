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

import net.sf.xmlunit.TestResources;

import org.custommonkey.xmlunit.builders.BuilderException;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.diff.DiffBuilder;
import org.custommonkey.xmlunit.diff.DifferenceType;
import org.custommonkey.xmlunit.examples.MultiLevelElementNameAndTextQualifier;
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
    protected Diff prepareDiff(XmlUnitProperties properties, String control, String test) throws SAXException,
            IOException {
        return new DetailedDiff(super.prepareDiff(properties, control, test));
    }

    @Override
    protected Diff prepareDiff(XmlUnitProperties properties, Reader control, Reader test) throws SAXException,
            IOException {
        return new DetailedDiff(super.prepareDiff(properties, control, test));
    }

    @Override
    protected Diff prepareDiff(
            XmlUnitProperties properties,
            String control,
            String test,
            DifferenceEngineContract engine)
            throws SAXException, IOException {

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
        Diff multipleDifferences = new DiffBuilder(properties)
                .withControlDocument(firstForecast)
                .withTestDocument(secondForecast)
                .build();
        DetailedDiff detailedDiff = new DetailedDiff(multipleDifferences);
        List<Difference> differences = detailedDiff.getAllDifferences();

        // then
        assertThat(differences).hasSize(5);
        assertThat(differences.get(0)).isEqualTo(new Difference(DifferenceType.HAS_CHILD_NODES));
        assertThat(differences.get(1)).isEqualTo(new Difference(DifferenceType.ELEMENT_NUM_ATTRIBUTES));
        assertThat(differences.get(2)).isEqualTo(new Difference(DifferenceType.ATTR_NAME_NOT_FOUND));
        assertThat(differences.get(3)).isEqualTo(new Difference(DifferenceType.ATTR_VALUE));
        assertThat(differences.get(4)).isEqualTo(new Difference(DifferenceType.CHILD_NODE_NOT_FOUND));
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
        Diff multipleDifferences = new DiffBuilder(properties)
                .withControlDocument(secondForecast)
                .withTestDocument(firstForecast)
                .build();
        DetailedDiff detailedDiff = new DetailedDiff(multipleDifferences);
        List<Difference> differences = detailedDiff.getAllDifferences();

        // then
        assertThat(differences).hasSize(5);
        assertThat(differences.get(0)).isEqualTo(new Difference(DifferenceType.HAS_CHILD_NODES));
        assertThat(differences.get(1)).isEqualTo(new Difference(DifferenceType.ELEMENT_NUM_ATTRIBUTES));
        assertThat(differences.get(2)).isEqualTo(new Difference(DifferenceType.ATTR_VALUE));
        assertThat(differences.get(3)).isEqualTo(new Difference(DifferenceType.ATTR_NAME_NOT_FOUND));
        assertThat(differences.get(4)).isEqualTo(new Difference(DifferenceType.CHILD_NODE_NOT_FOUND));
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
        Diff multipleDifferences = new DiffBuilder(properties)
                .withControlDocument(firstForecast)
                .withTestDocument(secondForecast)
                .build();
        DetailedDiff detailedDiff = new DetailedDiff(new DetailedDiff(multipleDifferences));
        List<Difference> differences = detailedDiff.getAllDifferences();

        // then
        assertThat(differences).hasSize(5);
        assertThat(differences.get(0)).isEqualTo(new Difference(DifferenceType.HAS_CHILD_NODES));
        assertThat(differences.get(1)).isEqualTo(new Difference(DifferenceType.ELEMENT_NUM_ATTRIBUTES));
        assertThat(differences.get(2)).isEqualTo(new Difference(DifferenceType.ATTR_NAME_NOT_FOUND));
        assertThat(differences.get(3)).isEqualTo(new Difference(DifferenceType.ATTR_VALUE));
        assertThat(differences.get(4)).isEqualTo(new Difference(DifferenceType.CHILD_NODE_NOT_FOUND));
    }

    @Test
    public void should_check_large_files() throws FileNotFoundException, IOException, SAXException,
            ConfigurationException, XpathException {
        String expr = null;
        File test, control;
        control = TestResources.DETAIL_CONTROL.getFile();
        test = TestResources.DETAIL_TEST.getFile();

        properties.setIgnoreWhitespace(true);
        Diff prototype =
                new Diff(properties, new FileReader(control), new FileReader(test));
        DetailedDiff detailedDiff = new DetailedDiff(prototype);
        List<Difference> differences = detailedDiff.getAllDifferences();

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
        for (Difference difference : differences) {

            if (difference.equals(new Difference(DifferenceType.ATTR_VALUE))
                    || difference.equals(new Difference(DifferenceType.CDATA_VALUE))
                    || difference.equals(new Difference(DifferenceType.COMMENT_VALUE))
                    || difference.equals(new Difference(DifferenceType.ELEMENT_TAG_NAME))
                    || difference.equals(new Difference(DifferenceType.TEXT_VALUE))) {

                expr = difference.getControlNodeDetail().getXpathLocation();
                if (expr != null && expr.length() > 0) {
                    value = xpathEngine.evaluate(expr, controlDoc);
                    assertThat(difference.getControlNodeDetail().getValue()).isEqualTo(value);
                }

                expr = difference.getTestNodeDetail().getXpathLocation();
                if (expr != null && expr.length() > 0) {
                    value = xpathEngine.evaluate(expr, testDoc);
                    assertThat(difference.getTestNodeDetail().getValue()).isEqualTo(value);
                }
            }
        }
    }

    @Test
    public void should_see_all_differences_even_if_diff_would_say_halt_comparison()
            throws SAXException, IOException, BuilderException {

        // given
        String control = "<a><b/><c/></a>";
        String test = "<a><c/></a>";

        // when
        Diff d = new DiffBuilder(properties)
                .withControlDocument(control)
                .withTestDocument(test)
                .build();
        DetailedDiff dd = new DetailedDiff(d);

        List<Difference> differences = dd.getAllDifferences();

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
        Diff diff = new DiffBuilder(properties)
                .withControlDocument(control)
                .withTestDocument(test)
                .build();
        diff.similar();
        DetailedDiff detailedDiff = new DetailedDiff(diff);
        List<Difference> differences = detailedDiff.getAllDifferences();

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
        DetailedDiff diff = new DetailedDiff(new DiffBuilder(properties)
                .withControlDocument(control)
                .withTestDocument(test)
                .build());
        List<Difference> differences = diff.getAllDifferences();

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
        Diff diff = new DiffBuilder(properties)
                .withControlDocument(control)
                .withTestDocument(test)
                .build();
        diff.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(2));
        DetailedDiff detailedDiff = new DetailedDiff(diff);
        List<Difference> differences = detailedDiff.getAllDifferences();

        // (0) number of children, (1) order different, (2) node not found
        Difference difference = differences.get(2);
        String controlXpathLocation = difference.getControlNodeDetail().getXpathLocation();
        String testXpathLocation = difference.getTestNodeDetail().getXpathLocation();

        // then
        assertThat(differences).hasSize(3);
        assertThat(difference.getType())
                .isEqualTo(DifferenceType.CHILD_NODE_NOT_FOUND);
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
        Diff diff = new DiffBuilder(properties)
                .withControlDocument(test)
                .withTestDocument(control)
                .build();
        diff.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(2));
        DetailedDiff detailedDiff = new DetailedDiff(diff);
        List<Difference> differences = detailedDiff.getAllDifferences();

        // (0) number of children, (1) order different, (2) node not found
        Difference difference = differences.get(2);
        String reverseControlXpathLocation = difference.getControlNodeDetail().getXpathLocation();
        String reverseTestXpathLocation = difference.getTestNodeDetail().getXpathLocation();

        // then
        assertThat(differences).hasSize(3);
        assertThat(difference.getType())
                .isEqualTo(DifferenceType.CHILD_NODE_NOT_FOUND);
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
    public void should_compare_unmatched() throws SAXException, IOException {
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
        List<Difference> differences = detailedDiff.getAllDifferences();
        Difference difference = differences.get(0);

        // then
        assertThat(differences).hasSize(1);
        assertThat(difference.getType())
                .isEqualTo(DifferenceType.ELEMENT_TAG_NAME);
    }

    /**
     * https://sourceforge.net/tracker/?func=detail&aid=2758280&group_id=23187&
     * atid=377768
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void should_not_compare_unmatched() throws SAXException, IOException {
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
        List<Difference> differences = detailedDiff.getAllDifferences();

        Difference difference = differences.get(0);
        Node controlNode = difference.getControlNodeDetail().getNode();
        Node testNode = difference.getTestNodeDetail().getNode();

        Difference difference1 = differences.get(1);
        Node controlNode1 = difference1.getControlNodeDetail().getNode();
        Node testNode1 = difference1.getTestNodeDetail().getNode();

        // then
        assertThat(differences).hasSize(2);
        assertThat(difference.getType())
                .isEqualTo(DifferenceType.CHILD_NODE_NOT_FOUND);
        assertThat(controlNode).isNotNull();
        assertThat(testNode).isNull();

        assertThat(difference1.getType())
                .isEqualTo(DifferenceType.CHILD_NODE_NOT_FOUND);
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
    public void should_check_issue_3062518() throws SAXException, IOException {
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
        List<Difference> differences = detailedDiff.getAllDifferences();

        Difference difference = differences.get(0);
        String controlValue = difference.getControlNodeDetail().getValue();
        String testValue = difference.getTestNodeDetail().getValue();
        String controlXpathLocation = difference.getControlNodeDetail().getXpathLocation();
        String testXpathLocation = difference.getTestNodeDetail().getXpathLocation();

        Difference difference1 = differences.get(1);
        String controlValue1 = difference1.getControlNodeDetail().getValue();
        String testValue1 = difference1.getTestNodeDetail().getValue();
        String controlXpathLocation1 = difference1.getControlNodeDetail().getXpathLocation();
        String testXpathLocation1 = difference1.getTestNodeDetail().getXpathLocation();

        Difference difference2 = differences.get(2);
        String controlValue2 = difference2.getControlNodeDetail().getValue();
        String testValue2 = difference2.getTestNodeDetail().getValue();
        String controlXpathLocation2 = difference2.getControlNodeDetail().getXpathLocation();
        String testXpathLocation2 = difference2.getTestNodeDetail().getXpathLocation();

        // didn't find the second Apple element
        Difference difference3 = differences.get(3);
        String controlValue3 = difference3.getControlNodeDetail().getValue();
        String testValue3 = difference3.getTestNodeDetail().getValue();
        String controlXpathLocation3 = difference3.getControlNodeDetail().getXpathLocation();
        String testXpathLocation3 = difference3.getTestNodeDetail().getXpathLocation();

        // then
        assertThat(differences).hasSize(4);
        assertThat(difference.getType())
                .isEqualTo(DifferenceType.CHILD_NODELIST_LENGTH);
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
                .isEqualTo(DifferenceType.ATTR_VALUE);
        assertThat(controlValue2).isEqualTo("10");
        assertThat(testValue2).isEqualTo("11");
        assertThat(controlXpathLocation2).isEqualTo("/Fruits[1]/Banana[1]/@size");
        assertThat(testXpathLocation2).isEqualTo("/Fruits[1]/Banana[1]/@size");

        assertThat(difference3.getType())
                .isEqualTo(DifferenceType.CHILD_NODE_NOT_FOUND);
        assertThat(controlValue3).isEqualTo("Apple");
        assertThat(testValue3).isEqualTo("null");
        assertThat(controlXpathLocation3).isEqualTo("/Fruits[1]/Apple[2]");
        assertThat(testXpathLocation3).isNull();
    }
}
