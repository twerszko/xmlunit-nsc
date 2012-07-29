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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

import org.custommonkey.xmlunit.diff.DifferenceType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

@RunWith(JUnitParamsRunner.class)
public class DiffTest {
    private Document aDocument;

    protected XMLUnitProperties properties;

    @Before
    public void setUp() throws Exception {
        aDocument = XMLUnit.newControlParser().newDocument();
        properties = new XMLUnitProperties();
    }

    protected Diff prepareDiff(XMLUnitProperties properties, Document control, Document test) {
        return new Diff(properties, control, test);
    }

    protected Diff prepareDiff(XMLUnitProperties properties, String control, String test) throws SAXException,
            IOException {
        return new Diff(properties, control, test);
    }

    protected Diff prepareDiff(XMLUnitProperties properties, Reader control, Reader test) throws SAXException,
            IOException {
        return new Diff(properties, control, test);
    }

    protected Diff prepareDiff(XMLUnitProperties properties, String control, String test,
            DifferenceEngineContract engine)
            throws SAXException, IOException {

        Document controlDocument = XMLUnit.buildControlDocument(control);
        Document testDocument = XMLUnit.buildTestDocument(test);
        return new Diff(properties, controlDocument, testDocument, engine);
    }

    @Test
    public void should_create_valid_toString() {
        // given
        Diff diff = prepareDiff(properties, aDocument, aDocument);

        Element elemA = aDocument.createElement("tag");
        Text textA = aDocument.createTextNode("Monkey");
        elemA.appendChild(textA);

        Element elemB = aDocument.createElement("tag");

        // when
        NodeDetail controlNodeDetail = new NodeDetail(TRUE.toString(), elemA, "/tag");
        NodeDetail testNodeDetail = new NodeDetail(FALSE.toString(), elemB, "/tag");

        Difference difference = new Difference(new Difference(DifferenceType.HAS_CHILD_NODES), controlNodeDetail,
                testNodeDetail);
        diff.differenceFound(difference);

        String toStringResult = diff.toString();
        String expectedToString = diff.getClass().getName() + "\n[different] Expected "
                + DifferenceType.HAS_CHILD_NODES.getDescription()
                + " 'true' but was 'false' - comparing <tag...> at /tag to <tag...> at /tag\n";

        // then
        assertThat(toStringResult).isEqualTo(expectedToString);
    }

    public void should_create_valid_toString_2() {
        // given
        Diff diff = prepareDiff(properties, aDocument, aDocument);

        // when
        Text textA = aDocument.createTextNode("Monkey");
        Text textB = aDocument.createTextNode("Chicken");

        NodeDetail controlNodeDetail = new NodeDetail("Monkey", textA, "/tag/text()");
        NodeDetail testNodeDetail = new NodeDetail("Chicken", textB, "/tag/text()");

        Difference difference = new Difference(new Difference(DifferenceType.TEXT_VALUE), controlNodeDetail,
                testNodeDetail);
        diff.differenceFound(difference);

        String toStringResult = diff.toString();
        String expectedToString = diff.getClass().getName() + "\n[different] Expected "
                + DifferenceType.TEXT_VALUE.getDescription()
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
        Diff diff = prepareDiff(properties, fruitBat, longEaredBat);
        String toStringResult = diff.toString();

        String expectedString = diff.getClass().getName() + "\n[different] Expected "
                + DifferenceType.ATTR_VALUE.getDescription()
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
        Diff diffControl = prepareDiff(properties, control, control);
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_pass_when_string_is_identical_with_itself() throws SAXException, IOException {
        // given
        String control = "<same>pass</same>";

        // when
        Diff diff = prepareDiff(properties, control, control);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, control, test);

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
        Diff diff = prepareDiff(properties, xmlWithDTD, xmlWithoutDTD);

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
            Diff diff = prepareDiff(properties, xmlWithDTD, xmlWithExternalDTD);

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

        Diff diff = prepareDiff(properties, xmlWithDTD, xmlWithAnotherDTD);

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
        boolean whitespaceAwareDiffSimilar = true;
        boolean whitespaceIgnoredDiffSimilar = false;

        XMLUnitProperties properties1 = new XMLUnitProperties();
        properties1.setIgnoreWhitespace(false);

        XMLUnitProperties properties2 = new XMLUnitProperties();
        properties2.setIgnoreWhitespace(true);

        // given
        String control = "<aakture><node>text</node><node>text2</node></aakture>";
        String test = "<aakture>  <node>text</node>\t<node>text2</node> \n </aakture>";

        // when
        Diff whitespaceAwareDiff = prepareDiff(properties1, control, test);
        whitespaceAwareDiffSimilar = whitespaceAwareDiff.similar();

        Diff whitespaceIgnoredDiff = prepareDiff(properties2, control, test);
        whitespaceIgnoredDiffSimilar = whitespaceIgnoredDiff.similar();

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
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void should_be_similar_with_different_namespaces() throws SAXException, IOException {
        // given
        String control =
                "<control:abc xmlns:control=\"http://yada.com\">" +
                        "<control:xyz>text</control:xyz></control:abc>";

        String test =
                "<test:abc xmlns:test=\"http://yada.com\">" +
                        "<test:xyz>text</test:xyz></test:abc>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        Diff reverseDiff = prepareDiff(properties, test, control);

        // then
        assertThat(diff.similar()).isTrue();
        assertThat(diff.identical()).isFalse();

        assertThat(reverseDiff.similar()).isTrue();
        assertThat(reverseDiff.identical()).isFalse();
    }

    /**
     * Raised 16.05.2002
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void should_be_similar_with_default_namespace() throws SAXException, IOException {
        // given
        String control =
                "<control:abc xmlns:control=\"http://yada.com\">" +
                        "<control:xyz>text</control:xyz></control:abc>";

        String test =
                "<abc xmlns=\"http://yada.com\">" +
                        "<xyz>text</xyz></abc>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        Diff reverseDiff = prepareDiff(properties, test, control);

        // then
        assertThat(diff.similar()).isTrue();
        assertThat(diff.identical()).isFalse();

        assertThat(reverseDiff.similar()).isTrue();
        assertThat(reverseDiff.identical()).isFalse();
    }

    @Test
    public void should_be_similar_with_the_same_name_and_different_QName() throws SAXException, IOException {
        // given
        String control =
                "<ns1:root xmlns:ns1=\"http://example.org/ns1\" xmlns:ns2=\"http://example.org/ns2\">" +
                        "<ns1:branch>In namespace 1</ns1:branch>" +
                        "<ns2:branch>In namespace 2</ns2:branch>" +
                        "</ns1:root>";

        String test = "<ns1:root xmlns:ns1=\"http://example.org/ns1\" xmlns:ns2=\"http://example.org/ns2\">" +
                "<ns2:branch>In namespace 2</ns2:branch>" +
                "<ns1:branch>In namespace 1</ns1:branch>" +
                "</ns1:root>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        Diff reverseDiff = prepareDiff(properties, test, control);

        // then
        assertThat(diff.similar()).isTrue();
        assertThat(diff.identical()).isFalse();

        assertThat(reverseDiff.similar()).isTrue();
        assertThat(reverseDiff.identical()).isFalse();
    }

    @Test
    public void should_be_different_when_different_nodes() throws SAXException, IOException {
        // given
        String control =
                "<vehicles>" +
                        "<car colour=\"white\">ford fiesta</car>" +
                        "<car colour=\"red\">citroen xsara</car>" +
                        "</vehicles>";

        String test =
                "<vehicles>" +
                        "<car colour=\"white\">nissan primera</car>" +
                        "<car colour=\"blue\">peugot 206</car></vehicles>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.similar()).isFalse();

    }

    @Test
    public void should_check_diffs_with_overriden_DifferenceListener() throws SAXException, IOException {
        // given
        String control =
                "<vehicles>" +
                        "<car colour=\"white\">ford fiesta</car>" +
                        "<car colour=\"red\">citroen xsara</car>" +
                        "</vehicles>";

        String test =
                "<vehicles>" +
                        "<car colour=\"white\">nissan primera</car>" +
                        "<car colour=\"blue\">peugot 206</car></vehicles>";

        // when
        Diff diffWithIdenticalOverride = prepareDiff(properties, control, test);
        diffWithIdenticalOverride.overrideDifferenceListener(
                new OverrideDifferenceListener(DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL)
                );

        Diff diffWithSimilarOverride = prepareDiff(properties, control, test);
        diffWithSimilarOverride.overrideDifferenceListener(
                new OverrideDifferenceListener(DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR)
                );

        Diff diffWithOverride = prepareDiff(properties, control, test);
        diffWithOverride.overrideDifferenceListener(new OverrideDifferenceListener(
                DifferenceListener.RETURN_ACCEPT_DIFFERENCE));

        // then
        assertThat(diffWithIdenticalOverride.identical()).isTrue();
        assertThat(diffWithIdenticalOverride.similar()).isTrue();

        assertThat(diffWithSimilarOverride.identical()).isFalse();
        assertThat(diffWithSimilarOverride.similar()).isTrue();

        assertThat(diffWithOverride.identical()).isFalse();
        assertThat(diffWithOverride.similar()).isFalse();
    }

    @Test
    public void should_check_files_with_namespaced_attributes() throws SAXException, IOException {
        // given
        File controlFile = TestResources.NAMESPACES_CONTROL.getFile();
        File testFile = TestResources.NAMESPACES_TEST.getFile();
        FileReader control = new FileReader(controlFile);
        FileReader test = new FileReader(testFile);

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideDifferenceListener(
                new ExpectedDifferenceListener(DifferenceType.NAMESPACE_PREFIX)
                );

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_different_structures() throws SAXException, IOException {
        // given
        String control = "<root><node>text</node></root>";
        String test = "<root><node><inner-node>text</inner-node></node></root>";

        // when
        Diff myDiff = prepareDiff(properties, control, test);

        // then
        assertThat(myDiff.similar()).isFalse();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification()
            throws SAXException, IOException {

        // given
        String control = "<root><node id=\"1\"/><node id=\"2\"/></root>";
        String test = "<root><node id=\"2\"/><node id=\"1\"/></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification2()
            throws SAXException, IOException {

        // given
        String control = "<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>";
        String test = "<root><node id=\"2\" val=\"4\"/><node id=\"1\" val=\"3\"/></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification3()
            throws SAXException, IOException {

        // given
        String control = "<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>";
        String test = "<root><node id=\"2\" val=\"3\"/><node id=\"1\" val=\"4\"/></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification4()
            throws SAXException, IOException {

        // given
        String control = "<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>";
        String test = "<root><node id=\"2\" val=\"4\"/><node id=\"1\" val=\"3\"/></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_check_repeated_element_names_with_namespaced_attribute_qualification()
            throws SAXException, IOException {

        // given
        String control =
                "<root xmlns:a=\"http://a.com\" xmlns:b=\"http://b.com\">" +
                        "<node id=\"1\" a:val=\"a\" b:val=\"b\"/>" +
                        "<node id=\"2\" a:val=\"a2\" b:val=\"b2\"/>" +
                        "</root>";
        String test =
                "<root xmlns:c=\"http://a.com\" xmlns:d=\"http://b.com\">" +
                        "<node id=\"2\" c:val=\"a2\" d:val=\"b2\"/>" +
                        "<node id=\"1\" c:val=\"a\" d:val=\"b\"/>" +
                        "</root>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        diff.overrideDifferenceListener(
                new ExpectedDifferenceListener(
                        new DifferenceType[] {
                                DifferenceType.NAMESPACE_PREFIX,
                                DifferenceType.CHILD_NODELIST_SEQUENCE
                        })
                );

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_repeated_element_names_with_text_qualification()
            throws SAXException, IOException {

        // given
        String control = "<root><node>1</node><node>2</node></root>";
        String test = "<root><node>2</node><node>1</node></root>";

        ExaminingExpectedDifferenceListener delegate =
                new ExaminingExpectedDifferenceListener(DifferenceType.CHILD_NODELIST_SEQUENCE) {
                    private int i = 0;

                    protected void examineDifferenceContents(Difference difference) {
                        ++i;
                        String differenceXpathLocation = difference.getControlNodeDetail().getXpathLocation();
                        assertThat(differenceXpathLocation).isEqualTo("/root[1]/node[" + i + "]");
                    }
                };

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(delegate);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    private abstract class ExaminingExpectedDifferenceListener extends ExpectedDifferenceListener {
        private ExaminingExpectedDifferenceListener(DifferenceType expectedType) {
            super(expectedType);
        }
    }

    // defect raised by Kevin Krouse Jan 2003
    @Test
    public void should_check_XMLNS_number_of_attributes() throws SAXException, IOException {
        // given
        String control = "<root xmlns=\"qwerty\"><node/></root>";
        String test = "<root xmlns=\"qwerty\" xmlns:qwerty=\"qwerty\"><qwerty:node/></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
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
        private final Set<DifferenceType> expectedIds;

        private ExpectedDifferenceListener(DifferenceType expectedType) {
            this(new DifferenceType[] { expectedType });
        }

        private ExpectedDifferenceListener(DifferenceType[] expectedIdValues) {
            this.expectedIds = new HashSet<DifferenceType>(expectedIdValues.length);
            for (int i = 0; i < expectedIdValues.length; ++i) {
                expectedIds.add(expectedIdValues[i]);
            }
        }

        public int differenceFound(Difference difference) {
            assertTrue(difference.toString(), expectedIds.contains(difference.getType()));
            examineDifferenceContents(difference);
            return RETURN_ACCEPT_DIFFERENCE;
        }

        public void skippedComparison(Node control, Node test) {
        }

        protected void examineDifferenceContents(Difference difference) {
        }
    }

    @Test
    public void should_check_issue_1189681() throws SAXException, IOException {
        // given
        String control =
                "<farm>\n" +
                        "<size>100</size>\n" +
                        "<animal>\n" +
                        "<name>Cow</name>\n" +
                        " </animal>\n" +
                        " <animal>\n" +
                        "<name>Sheep</name>\n" +
                        " </animal>\n" +
                        "</farm>";
        String test =
                "<farm>\n" +
                        " <animal>\n" +
                        "<name>Sheep</name>\n" +
                        " </animal>\n" +
                        " <size>100</size>\n" +
                        " <animal>\n" +
                        " <name>Cow</name>\n" +
                        " </animal>\n" +
                        "</farm>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_be_different_when_CDATA_not_ignored() throws SAXException, IOException {
        // given
        String expected = "<a>Hello</a>";
        String actual = "<a><![CDATA[Hello]]></a>";

        // when
        Diff diff = prepareDiff(properties, expected, actual);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_be_identical_when_CDATA_ignored() throws SAXException, IOException {
        try {
            // given
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);

            String control = "<a>Hello</a>";
            String test = "<a><![CDATA[Hello]]></a>";

            // when
            Diff diff = prepareDiff(properties, control, test);

            // then
            assertThat(diff.identical()).isTrue();
            assertThat(diff.similar()).isTrue();
        } finally {
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);
        }
    }

    @Test
    public void should_be_identical_when_comments_ignored() throws SAXException, IOException {
        // given
        String control = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String test = "<foo><bar a=\"b\"><!-- test --></bar> </foo>";
        try {
            // when
            Diff diff = prepareDiff(properties, control, test);
            assertThat(diff.identical()).isFalse();
            assertThat(diff.similar()).isFalse();

            XMLUnit.setIgnoreComments(true);

            diff = prepareDiff(properties, control, test);

            // then
            assertThat(diff.identical()).isTrue();
            assertThat(diff.similar()).isTrue();
        } finally {
            XMLUnit.setIgnoreComments(false);
        }
    }

    @Test
    public void should_pass_when_whitespaces_ignored() throws SAXException, IOException {
        properties.setIgnoreWhitespace(true);
        should_be_identical_when_comments_ignored();
    }

    @Test
    public void should_pass_when_normalization() throws SAXException, IOException {
        // TODO clean it up;
        try {
            XMLUnit.setNormalize(true);
            should_be_identical_when_comments_ignored();
        } finally {
            XMLUnit.setNormalize(false);
        }
    }

    @Test
    public void should_check_normalization() {
        // given
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

        // when - then
        // TODO This is ugly, test flow is disturbed - must be changed
        Diff diff = prepareDiff(properties, control, test);
        assertThat(diff.identical()).isFalse();
        try {
            XMLUnit.setNormalize(true);
            diff = prepareDiff(properties, control, test);
            assertTrue(diff.identical());
            assertTrue(diff.similar());
        } finally {
            XMLUnit.setNormalize(false);
        }
        diff = prepareDiff(properties, control, test);
        assertThat(diff.similar()).isFalse();
    }

    // fails with Java 5 and later
    @Test
    @Ignore
    public void should_make_sure_ignoring_whitespace_doesnt_affect_normalization() {
        properties.setIgnoreWhitespace(true);
        should_check_normalization();
    }

    // fails with Java 5 and later
    @Test
    @Ignore
    public void should_make_sure_ignoring_comments_doesnt_affect_normalization() {
        try {
            XMLUnit.setIgnoreComments(true);
            should_check_normalization();
        } finally {
            XMLUnit.setIgnoreComments(false);
        }
    }

    @Test
    public void should_neither_be_identical_nor_similar_when_different_whitespaces() throws SAXException, IOException {
        // given
        String control = "<foo>a = b;</foo>";
        String test = "<foo>\r\n\ta =\tb; \r\n</foo>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_be_identical_and_similar_when_different_whitespaces() throws SAXException, IOException {
        // given
        properties.setNormalizeWhitespace(true);
        String control = "<foo>a = b;</foo>";
        String test = "<foo>\r\n\ta =\tb; \r\n</foo>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(diff.similar()).isTrue();
    }

    /**
     * inspired by {@link http 
     * ://day-to-day-stuff.blogspot.com/2007/05/comparing-xml-in-junit-test.html
     * Erik von Oosten's Weblog}, made us implement special handling of
     * schemaLocation.
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void should_pass_when_differently_specified_namespace() throws SAXException, IOException {
        // given
        String control =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Message xmlns=\"http://www.a.nl/a10.xsd\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"C:/longpath/a10.xsd\">" +
                        "<MessageHeader/>" +
                        "</Message>";
        String test =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<a:Message xmlns:a=\"http://www.a.nl/a10.xsd\">" +
                        "<a:MessageHeader/>" +
                        "</a:Message>";

        // when
        Diff d = prepareDiff(properties, control, test);

        // then
        assertThat(d.identical()).isFalse();
        assertThat(d.similar()).isTrue();
    }

    /**
     * Bug Report 1779701
     * 
     * @throws IOException
     * @throws SAXException
     * 
     * @see http 
     *      ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1779701&
     *      amp;group_id=23187&amp;atid=377768
     */
    @Test
    public void should_be_identical_when_namespaces_present_and_whitespaces_ignored()
            throws SAXException, IOException {

        // given
        String control =
                "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>\r\n " +
                        "<env:Header/>\r\n " +
                        "</env:Envelope>";
        String test =
                "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<env:Header/>" +
                        "</env:Envelope>";

        // when
        properties.setIgnoreWhitespace(true);
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isTrue();
    }

    /**
     * Bug Report 1863632
     * 
     * @throws IOException
     * @throws SAXException
     * 
     * @see http 
     *      ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1863632&
     *      amp;group_id=23187&amp;atid=377768
     */
    @Test
    public void should_be_identical_when_whitespaces_ignored() throws SAXException, IOException {
        // given
        String control = "<a><b/></a>";
        String test = "<a>\r\n  <b/>\r\n</a>";

        properties.setIgnoreWhitespace(true);

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isTrue();
    }

    @Test
    public void should_check_upgrading_of_recoverable_difference() throws SAXException, IOException {
        // given
        String control = "<foo:bar xmlns:foo='urn:foo'/>";
        String test = "<bar xmlns='urn:foo'/>";

        // when
        Diff controlDiff = prepareDiff(properties, control, test);

        Diff diff = prepareDiff(properties, control, test);
        diff.overrideDifferenceListener(
                new DifferenceListener() {
                    public int differenceFound(Difference d) {
                        return RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT;
                    }

                    public void skippedComparison(Node c, Node t) {
                        fail("skippedComparison shouldn't get invoked");
                    }
                });

        // then
        assertThat(controlDiff.identical()).isFalse();
        assertThat(controlDiff.similar()).isTrue();

        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_verify_calls_on_overriden_MatchTracker() throws SAXException, IOException {
        // given
        MatchTracker mockedTracker = mock(MatchTracker.class);
        doNothing().when(mockedTracker).matchFound(any(Difference.class));

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>");
        diff.overrideMatchTracker(mockedTracker);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedTracker, times(14)).matchFound(any(Difference.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_engine() throws SAXException, IOException {
        // given
        MatchTracker mockedTracker = mock(MatchTracker.class);
        doNothing().when(mockedTracker).matchFound(any(Difference.class));

        ComparisonController mockedController = mock(ComparisonController.class);
        Mockito.doReturn(false).when(mockedController).haltComparison(any(Difference.class));

        DifferenceEngineContract engine = new DifferenceEngine(properties, mockedController, mockedTracker);

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", engine);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedTracker, times(12)).matchFound(any(Difference.class));
        verify(mockedController, times(0)).haltComparison(any(Difference.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_diff() throws SAXException, IOException {
        // given
        MatchTracker mockedTracker = mock(MatchTracker.class);
        doNothing().when(mockedTracker).matchFound(any(Difference.class));

        ComparisonController mockedController = mock(ComparisonController.class);
        Mockito.doReturn(false).when(mockedController).haltComparison(any(Difference.class));

        DifferenceEngineContract engine = new DifferenceEngine(properties, mockedController);

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", engine);
        diff.overrideMatchTracker(mockedTracker);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedTracker, times(12)).matchFound(any(Difference.class));
        verify(mockedController, times(0)).haltComparison(any(Difference.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_newEngine()
            throws SAXException, IOException {

        // given
        MatchTracker mockedTracker = mock(MatchTracker.class);
        doNothing().when(mockedTracker).matchFound(any(Difference.class));

        ComparisonController mockedController = mock(ComparisonController.class);
        Mockito.doReturn(false).when(mockedController).haltComparison(any(Difference.class));

        DifferenceEngineContract engine = new NewDifferenceEngine(properties, mockedController, mockedTracker);
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", engine);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedTracker, times(14)).matchFound(any(Difference.class));
        verify(mockedController, times(0)).haltComparison(any(Difference.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_diff2()
            throws SAXException, IOException {

        // given
        MatchTracker mockedTracker = mock(MatchTracker.class);
        doNothing().when(mockedTracker).matchFound(any(Difference.class));

        ComparisonController mockedController = mock(ComparisonController.class);
        Mockito.doReturn(false).when(mockedController).haltComparison(any(Difference.class));

        DifferenceEngineContract engine = new NewDifferenceEngine(properties, mockedController);

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", engine);
        diff.overrideMatchTracker(mockedTracker);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedTracker, times(14)).matchFound(any(Difference.class));
        verify(mockedController, times(0)).haltComparison(any(Difference.class));
    }

    @Test
    public void should_be_similar_when_CDATA_and_ignored_whitespaces()
            throws SAXException, IOException {

        // given
        String control =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Data>" +
                        "<Person><Name><![CDATA[JOE]]></Name></Person>" +
                        "</Data>";

        String test =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Data> " +
                        "<Person> <Name> <![CDATA[JOE]]> </Name> </Person></Data>";

        properties.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        try {
            // when
            Diff diff = prepareDiff(properties, control, test);

            // then
            assertThat(diff.similar()).isTrue();
        } finally {
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);
        }
    }

    /**
     * Not a real test. Need something that actually fails unless I set the
     * flag.
     * 
     * @throws IOException
     * @throws SAXException
     */
    // TODO: investigate wtf.
    @Ignore
    @Test
    public void testEntityExpansion() throws SAXException, IOException {
        String control = "<root>bla&#13;bla</root>";
        String test = "<root>bla&#xD;bla</root>";
        // XMLUnit.setExpandEntityReferences(true);
        try {
            Diff diff = prepareDiff(properties, control, test);
            assertThat(diff.similar()).isTrue();
        } finally {
            XMLUnit.setExpandEntityReferences(false);
        }
    }

    /**
     * @throws IOException
     * @throws SAXException
     * @see https 
     *      ://sourceforge.net/tracker/?func=detail&aid=2807167&group_id=23187
     *      &atid=377768
     */
    @Test
    public void should_check_issue_2807167() throws SAXException, IOException {
        // given
        String test =
                "<tag>" +
                        "<child amount=\"100\" />" +
                        "<child amount=\"100\" />" +
                        "<child amount=\"100\" />" +
                        "<child amount=\"250\" />" +
                        "<child amount=\"100\" />" +
                        "</tag>";

        String control =
                "<tag>" +
                        "<child amount=\"100\" />" +
                        "<child amount=\"100\" />" +
                        "<child amount=\"250\" />" +
                        "<child amount=\"100\" />" +
                        "<child amount=\"100\" />" +
                        "</tag>";

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

        // then
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_cocoon_xml_test_case() throws SAXException, IOException {
        // given
        String control = "<!DOCTYPE skinconfig []>" + "<!--abcd--><root></root>";
        String test = "<!DOCTYPE skinconfig [<!--abcd-->]>" + "<root></root>";

        Document controlDoc = XMLUnit.buildTestDocument(control);
        Document testDoc = XMLUnit.buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        Diff reverseDiff = prepareDiff(properties, testDoc, controlDoc);

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(reverseDiff.identical()).isTrue();
    }
}
