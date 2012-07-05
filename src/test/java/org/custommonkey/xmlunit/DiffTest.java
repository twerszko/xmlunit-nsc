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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static junitparams.JUnitParamsRunner.$;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.xmlunit.TestResources;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

@RunWith(JUnitParamsRunner.class)
public class DiffTest {
    private Document aDocument;

    @Before
    public void setUp() throws Exception {
        aDocument = XMLUnit.newControlParser().newDocument();
    }

    @Test
    public void should_create_valid_toString() {
        // given
        Diff diff = new Diff(aDocument, aDocument);

        Element elemA = aDocument.createElement("tag");
        Text textA = aDocument.createTextNode("Monkey");
        elemA.appendChild(textA);

        Element elemB = aDocument.createElement("tag");

        // when
        NodeDetail controlNodeDetail = new NodeDetail(TRUE.toString(), elemA, "/tag");
        NodeDetail testNodeDetail = new NodeDetail(FALSE.toString(), elemB, "/tag");

        Difference difference = new Difference(DifferenceConstants.HAS_CHILD_NODES, controlNodeDetail, testNodeDetail);
        diff.differenceFound(difference);

        String toStringResult = diff.toString();
        String expectedToString = diff.getClass().getName() + "\n[different] Expected "
                + DifferenceConstants.HAS_CHILD_NODES.getDescription()
                + " 'true' but was 'false' - comparing <tag...> at /tag to <tag...> at /tag\n";

        // then
        assertThat(toStringResult).isEqualTo(expectedToString);
    }

    public void should_create_valid_toString_2() {
        // given
        Diff diff = new Diff(aDocument, aDocument);

        // when
        Text textA = aDocument.createTextNode("Monkey");
        Text textB = aDocument.createTextNode("Chicken");

        NodeDetail controlNodeDetail = new NodeDetail("Monkey", textA, "/tag/text()");
        NodeDetail testNodeDetail = new NodeDetail("Chicken", textB, "/tag/text()");

        Difference difference = new Difference(DifferenceConstants.TEXT_VALUE, controlNodeDetail, testNodeDetail);
        diff.differenceFound(difference);

        String toStringResult = diff.toString();
        String expectedToString = diff.getClass().getName() + "\n[different] Expected "
                + DifferenceConstants.TEXT_VALUE.getDescription()
                + " 'Monkey' but was 'Chicken' - comparing <tag ...>Monkey</tag> "
                + "at /tag/text() to <tag ...>Chicken</tag> at /tag/text()\n";

        // then
        assertThat(toStringResult).isEqualTo(expectedToString);
    }

    @Test
    public void should_create_valid_toString_when_different_attributes() throws SAXException, IOException {
        // given
        String fruitBat = "<bat type=\"fruit\"/>";
        String longEaredBat = "<bat type=\"longeared\"/>";

        // when
        Diff diff = new Diff(fruitBat, longEaredBat);
        String toStringResult = diff.toString();

        String expectedString = diff.getClass().getName() + "\n[different] Expected "
                + DifferenceConstants.ATTR_VALUE.getDescription()
                + " 'fruit' but was 'longeared' - comparing "
                + "<bat type=\"fruit\"...> at /bat[1]/@type to <bat type=\"longeared\"...> at /bat[1]/@type\n";

        // then
        assertThat(toStringResult).isEqualTo(expectedString);
    }

    @Test
    @Parameters(method = "provideXmlPairs")
    public void should_check_if_two_xmls_arent_similar(String control, String test)
            throws SAXException, IOException {

        // given
        // when
        Diff diffControl = new Diff(control, control);
        Diff diff = new Diff(control, test);

        // then
        assertThat(diffControl.similar()).isTrue();
        assertThat(diff.similar()).isFalse();
    }

    @SuppressWarnings("unused")
    private Object[] provideXmlPairs() {
        return $(
                $(
                        "<test/>",
                        "<fail/>"),
                $(
                        "<test></test>",
                        "<fail/>"),
                $(
                        "<test>test</test>",
                        "<fail>test</fail>"),
                $(
                        "<test test=\"test\">test</test>",
                        "<test>test</test>"),
                $(
                        "<test/>",
                        "<fail/>"),
                $(
                        "<test>test</test>",
                        "<test>fail</test>"),
                $(
                        "<test test=\"test\"/>",
                        "<test test=\"fail\"/>"),
                $(
                        "<test><test><test></test></test></test>",
                        "<test><test><test>test</test></test></test>"),
                $(
                        "<test test=\"test\"><test>test<test>test</test></test></test>",
                        "<test test=\"test\"><test>fail<test>test</test></test></test>"),
                $(
                        "<test test=\"test\"><test>test<test>test</test></test></test>",
                        "<test test=\"fail\"><test>test<test>test</test></test></test>"),
                $(
                        "<html>Yo this is a test!</html>",
                        "<html>Yo this isn't a test!</html>"),
                $(
                        "<java></java>",
                        "<java><package-def><ident>org</ident><dot/><ident>apache</ident><dot/><ident>test</ident></package-def></java>"));
    }

    @Test
    public void should_pass_when_two_strings_arent_identical() throws SAXException, IOException {
        // given
        String control = "<control><test>test1</test><test>test2</test></control>";
        String test = "<control><test>test2</test><test>test1</test></control>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isFalse();
    }

    @Test
    public void should_pass_when_two_strings_are_identical() throws SAXException, IOException {
        // given
        File controlFile = TestResources.BLAME_FILE.getFile();
        File testFile = TestResources.BLAME_FILE.getFile();
        FileReader control = new FileReader(controlFile);
        FileReader test = new FileReader(testFile);

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_pass_when_string_is_identical_with_itself() throws SAXException, IOException {
        // given
        String control = "<same>pass</same>";

        // when
        Diff diff = new Diff(control, control);

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_pass_when_one_node_missing_in_control_string() throws SAXException, IOException {
        // given
        String control = "<root></root>";
        String test = "<root><node/></root>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_pass_when_one_extra_node_in_control_string() throws SAXException, IOException {
        // given
        String control = "<root><node/></root>";
        String test = "<root></root>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_be_similar_when_nodes_in_reverse_order() throws SAXException, IOException {
        // given
        String control = "<root><same/><pass/></root>";
        String test = "<root><pass/><same/></root>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_pass_when_extra_attribute_in_test_string() throws SAXException, IOException {
        // given
        String control = "<same>pass</same>";
        String test = "<same except=\"this\">pass</same>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_pass_when_extra_attribute_in_control_string() throws SAXException, IOException {
        // given
        String control = "<same except=\"this\">pass</same>";
        String test = "<same>pass</same>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_pass_when_strings_have_attributes_in_reverse_order() throws SAXException, IOException {
        // given
        String control = "<same zzz=\"qwerty\" aaa=\"uiop\">pass</same>";
        String test = "<same aaa=\"uiop\" zzz=\"qwerty\">pass</same>";

        // when
        Diff diff = new Diff(control, test);

        // then
        assertThat(diff.similar()).isTrue();
        if (diff.identical()) {
            // TODO
            // should not ideally be identical
            // but JAXP implementations can reorder attributes
            // inside NamedNodeMap
        }
    }

    @Test
    public void should_pass_when_xml_and_xml_with_dtd_are_similar() throws SAXException, IOException {
        // given
        String xmlWithoutDTD = "<test>" +
                "<assertion result=\"pass\"/>" +
                "<assertion result=\"fail\"/>" +
                "</test>";

        String xmlWithDTD =
                "<!DOCTYPE test [" + "<!ELEMENT assertion EMPTY>" +
                        "<!ATTLIST assertion result (pass|fail) \"fail\">" +
                        "<!ELEMENT test (assertion)*>" +
                        "]>" +
                        xmlWithoutDTD;

        // when
        Diff diff = new Diff(xmlWithDTD, xmlWithoutDTD);

        // then
        assertThat(diff.similar()).isTrue();
        assertThat(diff.identical()).isFalse();
    }

    @Test
    public void should_pass_when_xml_and_xml_with_external_dtd_are_similar() throws SAXException, IOException {
        // given
        String xmlWithoutDTD = "<test>" +
                "<assertion result=\"pass\"/>" +
                "<assertion result=\"fail\"/>" +
                "</test>";

        String aDTD =
                "<!DOCTYPE test [" + "<!ELEMENT assertion EMPTY>" +
                        "<!ATTLIST assertion result (pass|fail) \"fail\">" +
                        "<!ELEMENT test (assertion)*>" +
                        "]>";

        String xmlWithDTD = aDTD + xmlWithoutDTD;

        // TODO: remove writing to file
        File tempDtdFile = File.createTempFile(this.getClass().toString(), "dtd");
        tempDtdFile.deleteOnExit();
        FileWriter dtdWriter = new FileWriter(tempDtdFile);
        dtdWriter.write(aDTD);

        String xmlWithExternalDTD =
                "<!DOCTYPE test SYSTEM \"" +
                        tempDtdFile.toURI().toURL().toExternalForm()
                        + "\">" +
                        xmlWithoutDTD;
        try {
            // when
            Diff diff = new Diff(xmlWithDTD, xmlWithExternalDTD);

            // then
            assertThat(diff.similar()).isTrue();
            assertThat(diff.identical()).isFalse();
        } finally {
            tempDtdFile.delete();
        }
    }

    @Test
    public void should_pass_when_two_xmls_with_dtd_are_identical() throws SAXException, IOException {
        String xmlWithoutDTD = "<test>" +
                "<assertion result=\"pass\"/>" +
                "<assertion result=\"fail\"/>" +
                "</test>";

        String aDTDpart =
                "<!DOCTYPE test [" + "<!ELEMENT assertion EMPTY>" +
                        "<!ATTLIST assertion result (pass|fail) \"fail\">" +
                        "<!ELEMENT test (assertion)*>";

        String xmlWithDTD = aDTDpart + "]>" + xmlWithoutDTD;
        String xmlWithAnotherDTD = aDTDpart + "<!ELEMENT comment (ANY)>" + "]>" + xmlWithoutDTD;

        Diff diff = new Diff(xmlWithDTD, xmlWithAnotherDTD);

        assertThat(diff.similar()).isTrue();
        assertThat(diff.identical()).isTrue();
    }

    /**
     * Raised by aakture 25.04.2002 Despite the name under which this defect was
     * raised the issue is really about managing redundant whitespace
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void should_check_whitespace_awareness() throws SAXException, IOException {
        // to avoid test sequencing issues we need to restore whitespace setting
        boolean startValueIgnoreWhitespace = XMLUnit.getIgnoreWhitespace();
        boolean whitespaceAwareDiffSimilar = true;
        boolean whitespaceIgnoredDiffSimilar = false;

        try {
            XMLUnit.setIgnoreWhitespace(false);

            // given
            String control = "<aakture><node>text</node><node>text2</node></aakture>";
            String test = "<aakture>  <node>text</node>\t<node>text2</node> \n </aakture>";

            // when
            Diff whitespaceAwareDiff = new Diff(control, test);
            whitespaceAwareDiffSimilar = whitespaceAwareDiff.similar();

            XMLUnit.setIgnoreWhitespace(true);
            Diff whitespaceIgnoredDiff = new Diff(control, test);
            whitespaceIgnoredDiffSimilar = whitespaceIgnoredDiff.similar();
        } finally {
            XMLUnit.setIgnoreWhitespace(startValueIgnoreWhitespace);
        }

        // then
        assertThat(whitespaceAwareDiffSimilar).isFalse();
        assertThat(whitespaceIgnoredDiffSimilar).isTrue();
    }

    @Test
    public void should_check_whitespace_awareness_with_comment_handling() throws SAXException, IOException {
        try {
            XMLUnit.setIgnoreComments(true);
            should_check_whitespace_awareness();
        } finally {
            XMLUnit.setIgnoreComments(false);
        }
    }

    @Test
    public void should_check_whitespace_awareness_with_normalization() throws SAXException, IOException {
        try {
            XMLUnit.setNormalize(true);
            should_check_whitespace_awareness();
        } finally {
            XMLUnit.setNormalize(false);
        }
    }

    // TODO
    /**
     * Raised 15.05.2002
     */
    @Test
    public void testNamespaceIssues() throws Exception {
        String control = "<control:abc xmlns:control=\"http://yada.com\">"
                + "<control:xyz>text</control:xyz></control:abc>";
        Replacement replace = new Replacement("control", "test");
        String test = replace.replace(control);

        Diff diff = buildDiff(control, test);
        assertTrue("a-" + diff.toString(), diff.similar());
        assertTrue("b-" + diff.toString(), !diff.identical());

        Diff reverseDiff = buildDiff(test, control);
        assertTrue("c-" + reverseDiff.toString(), reverseDiff.similar());
        assertTrue("d-" + reverseDiff.toString(), !reverseDiff.identical());
    }

    /**
     * Raised 16.05.2002
     */
    @Test
    public void testDefaultNamespace() throws Exception {
        String control = "<control:abc xmlns:control=\"http://yada.com\">"
                + "<control:xyz>text</control:xyz></control:abc>";
        Replacement replace = new Replacement("control:", "");
        Replacement trim = new Replacement("xmlns:control", "xmlns");
        String test = trim.replace(replace.replace(control));

        Diff diff = buildDiff(control, test);
        assertTrue("a-" + diff.toString(), diff.similar());
        assertTrue("b-" + diff.toString(), !diff.identical());

        Diff reverseDiff = buildDiff(test, control);
        assertTrue("c-" + reverseDiff.toString(), reverseDiff.similar());
        assertTrue("d-" + reverseDiff.toString(), !reverseDiff.identical());
    }

    @Test
    public void testSameNameDifferentQName() throws Exception {
        String control = "<ns1:root xmlns:ns1=\"http://example.org/ns1\" xmlns:ns2=\"http://example.org/ns2\">"
                + "<ns1:branch>In namespace 1</ns1:branch>" + "<ns2:branch>In namespace 2</ns2:branch>" + "</ns1:root>";

        String test = "<ns1:root xmlns:ns1=\"http://example.org/ns1\" xmlns:ns2=\"http://example.org/ns2\">"
                + "<ns2:branch>In namespace 2</ns2:branch>" + "<ns1:branch>In namespace 1</ns1:branch>" + "</ns1:root>";

        Diff diff = buildDiff(control, test);
        assertTrue("a-" + diff.toString(), diff.similar());
        assertTrue("b-" + diff.toString(), !diff.identical());

        Diff reverseDiff = buildDiff(test, control);
        assertTrue("c-" + reverseDiff.toString(), reverseDiff.similar());
        assertTrue("d-" + reverseDiff.toString(), !reverseDiff.identical());
    }

    @Test
    public void testOverrideDifferenceListener() throws Exception {
        String control = "<vehicles><car colour=\"white\">ford fiesta</car>"
                + "<car colour=\"red\">citroen xsara</car></vehicles>";
        String test = "<vehicles><car colour=\"white\">nissan primera</car>"
                + "<car colour=\"blue\">peugot 206</car></vehicles>";
        Diff diff = buildDiff(control, test);
        assertTrue("initially " + diff.toString(), !diff.similar());

        Diff diffWithIdenticalOverride = buildDiff(control, test);
        diffWithIdenticalOverride.overrideDifferenceListener(new OverrideDifferenceListener(
                DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL));
        assertTrue("now identical" + diffWithIdenticalOverride.toString(), diffWithIdenticalOverride.identical());

        Diff diffWithSimilarOverride = buildDiff(control, test);
        diffWithSimilarOverride.overrideDifferenceListener(new OverrideDifferenceListener(
                DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR));
        assertTrue("no longer identical" + diffWithSimilarOverride.toString(), !diffWithSimilarOverride.identical());
        assertTrue("but still similar" + diffWithSimilarOverride.toString(), diffWithSimilarOverride.similar());

        Diff diffWithOverride = buildDiff(control, test);
        diffWithOverride.overrideDifferenceListener(new OverrideDifferenceListener(
                DifferenceListener.RETURN_ACCEPT_DIFFERENCE));
        assertTrue("default behaviour" + diffWithOverride.toString(), !diffWithOverride.similar());
    }

    @Test
    public void testNamespacedAttributes() throws Exception {
        File controlFile = TestResources.NAMESPACES_CONTROL.getFile();
        File testFile = TestResources.NAMESPACES_TEST.getFile();
        FileReader control = new FileReader(controlFile);
        FileReader test = new FileReader(testFile);
        Diff diff = buildDiff(control, test);
        diff.overrideDifferenceListener(new ExpectedDifferenceListener(DifferenceConstants.NAMESPACE_PREFIX_ID));
        assertEquals(diff.toString(), false, diff.identical());
        assertEquals(diff.toString(), true, diff.similar());
    }

    @Test
    public void testDifferentStructure() throws Exception {
        String control = "<root><node>text</node></root>";
        String test = "<root><node><inner-node>text</inner-node></node></root>";
        Diff myDiff = buildDiff(control, test);
        assertEquals(myDiff.toString(), false, myDiff.similar());
    }

    @Test
    public void testRepeatedElementNamesWithAttributeQualification1() throws Exception {
        Diff diff = buildDiff("<root><node id=\"1\"/><node id=\"2\"/></root>",
                "<root><node id=\"2\"/><node id=\"1\"/></root>");
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));
        assertFalse("should not be identical: " + diff.toString(), diff.identical());
        assertTrue("should be similar: " + diff.toString(), diff.similar());
    }

    @Test
    public void testRepeatedElementNamesWithAttributeQualification2() throws Exception {
        Diff diff = buildDiff("<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>",
                "<root><node id=\"2\" val=\"4\"/><node id=\"1\" val=\"3\"/></root>");
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));
        assertFalse("should not be identical: " + diff.toString(), diff.identical());
        assertFalse("should not be similar: " + diff.toString(), diff.similar());
    }

    @Test
    public void testRepeatedElementNamesWithAttributeQualification3() throws Exception {
        Diff diff = buildDiff("<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>",
                "<root><node id=\"2\" val=\"3\"/><node id=\"1\" val=\"4\"/></root>");
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        assertFalse("should not be identical: " + diff.toString(), diff.identical());
        assertTrue("should be similar: " + diff.toString(), diff.similar());
    }

    @Test
    public void testRepeatedElementNamesWithAttributeQualification4() throws Exception {
        Diff diff = buildDiff("<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>",
                "<root><node id=\"2\" val=\"4\"/><node id=\"1\" val=\"3\"/></root>");
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        assertFalse("should not be identical: " + diff.toString(), diff.identical());
        assertFalse("should not be similar: " + diff.toString(), diff.similar());
    }

    @Test
    public void testRepeatedElementNamesWithNamespacedAttributeQualification() throws Exception {
        Diff diff = buildDiff("<root xmlns:a=\"http://a.com\" xmlns:b=\"http://b.com\">"
                + "<node id=\"1\" a:val=\"a\" b:val=\"b\"/><node id=\"2\" a:val=\"a2\" b:val=\"b2\"/></root>",
                "<root xmlns:c=\"http://a.com\" xmlns:d=\"http://b.com\">"
                        + "<node id=\"2\" c:val=\"a2\" d:val=\"b2\"/><node id=\"1\" c:val=\"a\" d:val=\"b\"/></root>");
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        diff.overrideDifferenceListener(new ExpectedDifferenceListener(new int[] {
                DifferenceConstants.NAMESPACE_PREFIX_ID, DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID }));
        assertFalse("should not be identical: " + diff.toString(), diff.identical());
        assertTrue("should be similar: " + diff.toString(), diff.similar());
    }

    @Test
    public void testRepeatedElementNamesWithTextQualification() throws Exception {
        Diff diff = buildDiff("<root><node>1</node><node>2</node></root>", "<root><node>2</node><node>1</node></root>");
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new ExaminingExpectedDifferenceListener(
                DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID) {
            private int i = 0;

            protected void examineDifferenceContents(Difference difference) {
                ++i;
                assertEquals("/root[1]/node[" + i + "]", difference.getControlNodeDetail().getXpathLocation());
            }
        });
        assertFalse("should not be identical: " + diff.toString(), diff.identical());
        assertTrue("should be similar: " + diff.toString(), diff.similar());
    }

    // defect raised by Kevin Krouse Jan 2003
    @Test
    public void testXMLNSNumberOfAttributes() throws Exception {
        Diff diff = buildDiff("<root xmlns=\"qwerty\"><node/></root>",
                "<root xmlns=\"qwerty\" xmlns:qwerty=\"qwerty\"><qwerty:node/></root>");
        assertTrue(diff.toString(), diff.similar());
        assertFalse(diff.toString(), diff.identical());
    }

    protected Diff buildDiff(Document control, Document test) {
        return new Diff(control, test);
    }

    protected Diff buildDiff(String control, String test) throws Exception {
        return new Diff(control, test);
    }

    protected Diff buildDiff(Reader control, Reader test) throws Exception {
        return new Diff(control, test);
    }

    protected Diff buildDiff(String control, String test, DifferenceEngineContract engine) throws Exception {
        return new Diff(XMLUnit.buildControlDocument(control), XMLUnit.buildTestDocument(test), engine);
    }

    private class OverrideDifferenceListener implements DifferenceListener {
        private final int overrideValue;

        private OverrideDifferenceListener(int overrideValue) {
            this.overrideValue = overrideValue;
        }

        public int differenceFound(Difference difference) {
            return overrideValue;
        }

        public void skippedComparison(Node control, Node test) {
        }
    }

    private class ExpectedDifferenceListener implements DifferenceListener {
        private final Set<Integer> expectedIds;

        private ExpectedDifferenceListener(int expectedIdValue) {
            this(new int[] { expectedIdValue });
        }

        private ExpectedDifferenceListener(int[] expectedIdValues) {
            this.expectedIds = new HashSet<Integer>(expectedIdValues.length);
            for (int i = 0; i < expectedIdValues.length; ++i) {
                expectedIds.add(expectedIdValues[i]);
            }
        }

        public int differenceFound(Difference difference) {
            assertTrue(difference.toString(), expectedIds.contains(new Integer(difference.getId())));
            examineDifferenceContents(difference);
            return RETURN_ACCEPT_DIFFERENCE;
        }

        public void skippedComparison(Node control, Node test) {
        }

        protected void examineDifferenceContents(Difference difference) {
        }
    }

    private abstract class ExaminingExpectedDifferenceListener extends ExpectedDifferenceListener {
        private ExaminingExpectedDifferenceListener(int expectedIdValue) {
            super(expectedIdValue);
        }

        protected abstract void examineDifferenceContents(Difference difference);
    }

    @Test
    public void testIssue1189681() throws Exception {
        String left = "" + "<farm>\n" + "<size>100</size>\n" + " <animal>\n" + "<name>Cow</name>\n" + " </animal>\n"
                + " <animal>\n" + "<name>Sheep</name>\n" + " </animal>\n" + "</farm>";
        String right = "" + "<farm>\n" + " <animal>\n" + "<name>Sheep</name>\n" + " </animal>\n"
                + " <size>100</size>\n" + " <animal>\n" + " <name>Cow</name>\n" + " </animal>\n" + "</farm>";
        assertFalse(buildDiff(left, right).similar());
    }

    @Test
    public void testCDATANoIgnore() throws Exception {
        String expected = "<a>Hello</a>";
        String actual = "<a><![CDATA[Hello]]></a>";
        assertFalse(buildDiff(expected, actual).similar());
        assertFalse(buildDiff(expected, actual).identical());
    }

    @Test
    public void testCDATAIgnore() throws Exception {
        try {
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
            String expected = "<a>Hello</a>";
            String actual = "<a><![CDATA[Hello]]></a>";
            assertTrue(buildDiff(expected, actual).similar());
            assertTrue(buildDiff(expected, actual).identical());
        } finally {
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);
        }
    }

    @Test
    public void testCommentHandling() throws Exception {
        String xml1 = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String xml2 = "<foo><bar a=\"b\"><!-- test --></bar> </foo>";
        try {
            assertFalse(buildDiff(xml1, xml2).identical());
            assertFalse(buildDiff(xml1, xml2).similar());
            XMLUnit.setIgnoreComments(true);
            assertTrue(buildDiff(xml1, xml2).identical());
            assertTrue(buildDiff(xml1, xml2).similar());
        } finally {
            XMLUnit.setIgnoreComments(false);
        }
    }

    @Test
    public void testWhitespaceHandlingDoesntAffectCommentHandling() throws Exception {
        try {
            XMLUnit.setIgnoreWhitespace(true);
            testCommentHandling();
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    @Test
    public void testNormalizationDoesntAffectCommentHandling() throws Exception {
        try {
            XMLUnit.setNormalize(true);
            testCommentHandling();
        } finally {
            XMLUnit.setNormalize(false);
        }
    }

    @Test
    public void testNormalization() throws Exception {
        Document control = XMLUnit.newControlParser().newDocument();
        Element root = control.createElement("root");
        control.appendChild(root);
        root.appendChild(control.createTextNode("Text 1"));
        root.appendChild(control.createTextNode(" and 2"));
        Element inner = control.createElement("inner");
        root.appendChild(inner);
        inner.appendChild(control.createTextNode("Text 3 and 4"));

        Document test = XMLUnit.newTestParser().newDocument();
        root = test.createElement("root");
        test.appendChild(root);
        root.appendChild(test.createTextNode("Text 1 and 2"));
        inner = test.createElement("inner");
        root.appendChild(inner);
        inner.appendChild(test.createTextNode("Text 3"));
        inner.appendChild(test.createTextNode(" and 4"));

        assertFalse(buildDiff(control, test).identical());
        try {
            XMLUnit.setNormalize(true);
            assertTrue(buildDiff(control, test).identical());
            assertTrue(buildDiff(control, test).similar());
        } finally {
            XMLUnit.setNormalize(false);
        }
        assertFalse(buildDiff(control, test).similar());
    }

    // fails with Java 5 and later
    @Test
    @Ignore
    public void XtestWhitespaceHandlingDoesntAffectNormalization() throws Exception {
        try {
            XMLUnit.setIgnoreWhitespace(true);
            testNormalization();
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    // fails with Java 5 and later
    @Test
    @Ignore
    public void XtestCommentHandlingDoesntAffectNormalization() throws Exception {
        try {
            XMLUnit.setIgnoreComments(true);
            testNormalization();
        } finally {
            XMLUnit.setIgnoreComments(false);
        }
    }

    @Test
    public void testNormalizedWhitespace() throws Exception {
        String xml1 = "<foo>a = b;</foo>";
        String xml2 = "<foo>\r\n\ta =\tb; \r\n</foo>";
        try {
            assertFalse(buildDiff(xml1, xml2).identical());
            assertFalse(buildDiff(xml1, xml2).similar());
            XMLUnit.setNormalizeWhitespace(true);
            assertTrue(buildDiff(xml1, xml2).identical());
            assertTrue(buildDiff(xml1, xml2).similar());
        } finally {
            XMLUnit.setNormalizeWhitespace(false);
        }
    }

    /**
     * inspired by {@link http 
     * ://day-to-day-stuff.blogspot.com/2007/05/comparing-xml-in-junit-test.html
     * Erik von Oosten's Weblog}, made us implement special handling of
     * schemaLocation.
     */
    @Test
    public void testNamespacePrefixDiff() throws Exception {
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Message xmlns=\"http://www.a.nl/a10.xsd\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"C:/longpath/a10.xsd\"" + ">" + "<MessageHeader/>" + "</Message>";
        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<a:Message xmlns:a=\"http://www.a.nl/a10.xsd\">"
                + "<a:MessageHeader/>" + "</a:Message>";
        Diff d = buildDiff(xml1, xml2);
        assertFalse(d.toString(), d.identical());
        assertTrue(d.toString(), d.similar());
    }

    /**
     * Bug Report 1779701
     * 
     * @see http 
     *      ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1779701&
     *      amp;group_id=23187&amp;atid=377768
     */
    @Test
    public void testWhitespaceAndNamespaces() throws Exception {
        String control = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + "\r\n <env:Header/>"
                + "\r\n </env:Envelope>";
        String test = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + "<env:Header/>"
                + "</env:Envelope>";
        XMLUnit.setIgnoreWhitespace(true);
        try {
            Diff diff = buildDiff(control, test);
            assertTrue(diff.toString(), diff.identical());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    /**
     * Bug Report 1863632
     * 
     * @see http 
     *      ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1863632&
     *      amp;group_id=23187&amp;atid=377768
     */
    @Test
    public void testBasicWhitespaceHandling() throws Exception {
        String control = "<a><b/></a>";
        String test = "<a>\r\n  <b/>\r\n</a>";
        XMLUnit.setIgnoreWhitespace(true);
        try {
            Diff diff = buildDiff(control, test);
            assertTrue(diff.toString(), diff.identical());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    @Test
    public void testUpgradingOfRecoverableDifference() throws Exception {
        String control = "<foo:bar xmlns:foo='urn:foo'/>";
        String test = "<bar xmlns='urn:foo'/>";
        Diff diff = buildDiff(control, test);
        assertFalse(diff.toString(), diff.identical());
        assertTrue(diff.toString(), diff.similar());

        diff = buildDiff(control, test);
        diff.overrideDifferenceListener(new DifferenceListener() {
            public int differenceFound(Difference d) {
                return RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
            }

            public void skippedComparison(Node c, Node t) {
                fail("skippedComparison shouldn't get invoked");
            }
        });

        assertFalse(diff.toString(), diff.identical());
        assertFalse(diff.toString(), diff.similar());
    }

    @Test
    public void testMatchTrackerSetViaOverride() throws Exception {
        Diff diff = buildDiff("<foo/>", "<foo/>");
        final int[] count = new int[1];
        diff.overrideMatchTracker(new MatchTracker() {
            public void matchFound(Difference d) {
                count[0]++;
            }
        });
        assertTrue(diff.identical());

        // uses new engine

        // NODE_TYPE(Document), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), NUMBER_OF_CHILDREN(1)
        // HAS_DOCTYPE_DECLARATION(no), CHILD_NODE_SEQUENCE(0)
        //
        // NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
        // ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
        // SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
        assertEquals(14, count[0]);
    }

    @Test
    public void testMatchTrackerSetViaEngine() throws Exception {
        final int[] count = new int[1];
        DifferenceEngineContract engine = new DifferenceEngine(new ComparisonController() {
            public boolean haltComparison(Difference afterDifference) {
                fail("haltComparison invoked");
                // NOTREACHED
                return false;
            }
        }, new MatchTracker() {
            public void matchFound(Difference d) {
                count[0]++;
            }
        });
        Diff diff = buildDiff("<foo/>", "<foo/>", engine);
        assertTrue(diff.identical());
        // NODE_TYPE (not null), NODE_TYPE(Document), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), HAS_DOCTYPE_DECLARATION(no),
        // HAS_CHILD_NODES(true)
        //
        // NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), ELEMENT_TAG_NAME(foo),
        // ELEMENT_NUM_ATTRIBUTE(none), HAS_CHILD_NODES(false)
        assertEquals(12, count[0]);
    }

    @Test
    public void testMatchTrackerSetViaOverrideOnEngine() throws Exception {
        DifferenceEngineContract engine = new DifferenceEngine(new ComparisonController() {
            public boolean haltComparison(Difference afterDifference) {
                fail("haltComparison invoked");
                // NOTREACHED
                return false;
            }
        });
        Diff diff = buildDiff("<foo/>", "<foo/>", engine);
        final int[] count = new int[1];
        diff.overrideMatchTracker(new MatchTracker() {
            public void matchFound(Difference d) {
                count[0]++;
            }
        });
        assertTrue(diff.identical());
        // NODE_TYPE (not null), NODE_TYPE(Document), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), HAS_DOCTYPE_DECLARATION(no),
        // HAS_CHILD_NODES(true)
        //
        // NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), ELEMENT_TAG_NAME(foo),
        // ELEMENT_NUM_ATTRIBUTE(none), HAS_CHILD_NODES(false)
        assertEquals(12, count[0]);
    }

    @Test
    public void testMatchTrackerSetViaNewEngine() throws Exception {
        final int[] count = new int[1];
        DifferenceEngineContract engine = new NewDifferenceEngine(new ComparisonController() {
            public boolean haltComparison(Difference afterDifference) {
                fail("haltComparison invoked");
                // NOTREACHED
                return false;
            }
        }, new MatchTracker() {
            public void matchFound(Difference d) {
                count[0]++;
            }
        });
        Diff diff = buildDiff("<foo/>", "<foo/>", engine);
        assertTrue(diff.identical());
        // NODE_TYPE(Document), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), NUMBER_OF_CHILDREN(1)
        // HAS_DOCTYPE_DECLARATION(no), CHILD_NODE_SEQUENCE(0)
        //
        // NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
        // ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
        // SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
        assertEquals(14, count[0]);
    }

    @Test
    public void testMatchTrackerSetViaOverrideOnNewEngine() throws Exception {
        DifferenceEngineContract engine = new NewDifferenceEngine(new ComparisonController() {
            public boolean haltComparison(Difference afterDifference) {
                fail("haltComparison invoked");
                // NOTREACHED
                return false;
            }
        });
        Diff diff = buildDiff("<foo/>", "<foo/>", engine);
        final int[] count = new int[1];
        diff.overrideMatchTracker(new MatchTracker() {
            public void matchFound(Difference d) {
                count[0]++;
            }
        });
        assertTrue(diff.identical());
        // NODE_TYPE(Document), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), NUMBER_OF_CHILDREN(1)
        // HAS_DOCTYPE_DECLARATION(no), CHILD_NODE_SEQUENCE(0)
        //
        // NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
        // ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
        // SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
        assertEquals(14, count[0]);
    }

    @Test
    public void testCDATAAndIgnoreWhitespace() throws Exception {
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Data><Person><Name><![CDATA[JOE]]></Name></Person></Data>";

        String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Data>" + " <Person>" + " <Name>"
                + " <![CDATA[JOE]]>" + " </Name>" + " </Person>" + "</Data>";

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        try {
            Diff diff = buildDiff(control, test);
            assertTrue(diff.toString(), diff.similar());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);
        }
    }

    /**
     * Not a real test. Need something that actually fails unless I set the
     * flag.
     */
    @Test
    public void testEntityExpansion() throws Exception {
        String control = "<root>bla&#13;bla</root>";
        String test = "<root>bla&#xD;bla</root>";
        // XMLUnit.setExpandEntityReferences(true);
        try {
            Diff diff = buildDiff(control, test);
            assertTrue(diff.toString(), diff.similar());
        } finally {
            XMLUnit.setExpandEntityReferences(false);
        }
    }

    /**
     * @see https 
     *      ://sourceforge.net/tracker/?func=detail&aid=2807167&group_id=23187
     *      &atid=377768
     */
    @Test
    public void testIssue2807167() throws Exception {
        String test = "<tag>" + "<child amount=\"100\" />" + "<child amount=\"100\" />" + "<child amount=\"100\" />"
                + "<child amount=\"250\" />" + "<child amount=\"100\" />" + "</tag>";

        String control = "<tag>" + "<child amount=\"100\" />" + "<child amount=\"100\" />" + "<child amount=\"250\" />"
                + "<child amount=\"100\" />" + "<child amount=\"100\" />" + "</tag>";

        Diff diff = new Diff(control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testCocoonXmlTestCase() throws Exception {
        Document control = XMLUnit.buildTestDocument("<!DOCTYPE skinconfig []>" + "<!--abcd--><root></root>");
        Document test = XMLUnit.buildControlDocument("<!DOCTYPE skinconfig [<!--abcd-->]>" + "<root></root>");
        Diff diff = new Diff(control, test);
        assertTrue(diff.toString(), diff.identical());

        diff = new Diff(test, control);
        assertTrue(diff.toString(), diff.identical());
    }
}
