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

package org.custommonkey.xmlunit.diff;

import static junitparams.JUnitParamsRunner.$;
import static org.custommonkey.xmlunit.diff.Diffs.prepareDiff;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEngine;
import net.sf.xmlunit.diff.DifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEvaluator;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;

import org.custommonkey.xmlunit.ElementNameAndAttributeSelector;
import org.custommonkey.xmlunit.XMLConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.diff.ElementSelectors;

@RunWith(JUnitParamsRunner.class)
public abstract class DiffTestAbstract {

    protected XmlUnitProperties properties;
    private DocumentUtils documentUtils;
    protected DefaultDifferenceEngineFactory engineFactory;

    @Before
    public void setUp() throws Exception {
        properties = new XmlUnitProperties();
        documentUtils = new DocumentUtils(properties);
        engineFactory = new DefaultDifferenceEngineFactory(properties);
    }

    // TODO toString?
    // @Test
    // public void should_create_valid_toString_2() throws Exception {
    // // given
    // Diff diff = prepareDiff(properties, aDocument, aDocument);
    //
    // // when
    // Text textA = aDocument.createTextNode("Monkey");
    // Text textB = aDocument.createTextNode("Chicken");
    //
    // Comparison comparison = new Comparison(
    // ComparisonType.TEXT_VALUE,
    // new Detail(textA, "/tag/text()", "Monkey"),
    // new Detail(textB, "/tag/text()", "Chicken"));
    //
    // diff.evaluate(comparison, ComparisonResult.DIFFERENT);
    //
    // String toStringResult = diff.toString();
    // String expectedToString = diff.getClass().getName() +
    // "\n[different] Expected "
    // + ComparisonType.TEXT_VALUE.getDescription()
    // + " 'Monkey' but was 'Chicken' - comparing <tag ...>Monkey</tag> "
    // + "at /tag/text() to <tag ...>Chicken</tag> at /tag/text()\n";
    //
    // // then
    // assertThat(toStringResult).isEqualTo(expectedToString);
    // }

    @Test
    @Parameters(method = "provideXmlPairs")
    public void should_check_if_two_xmls_arent_similar(String control, String test)
            throws Exception {

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
    public void should_pass_when_two_strings_arent_identical() throws Exception {
        // given
        String control = "<control><test>test1</test><test>test2</test></control>";
        String test = "<control><test>test2</test><test>test1</test></control>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isFalse();
    }

    @Test
    public void should_pass_when_two_strings_are_identical() throws Exception {
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
    public void should_pass_when_string_is_identical_with_itself() throws Exception {
        // given
        String control = "<same>pass</same>";

        // when
        Diff diff = prepareDiff(properties, control, control);

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_pass_when_one_node_missing_in_control_string() throws Exception {
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
    public void should_pass_when_one_extra_node_in_control_string() throws Exception {
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
    public void should_be_similar_when_nodes_in_reverse_order() throws Exception {
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
    public void should_pass_when_extra_attribute_in_test_string() throws Exception {
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
    public void should_pass_when_extra_attribute_in_control_string() throws Exception {
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
    public void should_pass_when_strings_have_attributes_in_reverse_order() throws Exception {
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
    public void should_pass_when_xml_and_xml_with_dtd_are_similar() throws Exception {
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
    public void should_pass_when_xml_and_xml_with_external_dtd_are_similar() throws Exception {
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
    public void should_pass_when_two_xmls_with_dtd_are_identical() throws Exception {
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
    public void should_check_whitespace_awareness() throws Exception {
        // to avoid test sequencing issues we need to restore whitespace setting
        boolean whitespaceAwareDiffSimilar = true;
        boolean whitespaceIgnoredDiffSimilar = false;

        XmlUnitProperties properties1 = new XmlUnitProperties();
        properties1.setIgnoreWhitespace(false);

        XmlUnitProperties properties2 = new XmlUnitProperties();
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
    public void should_check_whitespace_awareness_with_comment_handling() throws Exception {
        properties.setIgnoreComments(true);
        should_check_whitespace_awareness();
    }

    @Test
    public void should_check_whitespace_awareness_with_normalization() throws Exception {
        properties.setNormalizeWhitespace(true);
        should_check_whitespace_awareness();
    }

    // TODO
    /**
     * Raised 15.05.2002
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void should_be_similar_with_different_namespaces() throws Exception {
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
    public void should_be_similar_with_default_namespace() throws Exception {
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
    public void should_be_similar_with_the_same_name_and_different_QName() throws Exception {
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
    public void should_be_different_when_different_nodes() throws Exception {
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
    public void should_always_be_equal_with_overriden_evaluator() throws Exception {
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

        engineFactory.useEvaluator(new OverridingDifferenceEvaluator(ComparisonResult.EQUAL));

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_always_be_similar_with_overriden_evaluator() throws Exception {
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

        engineFactory.useEvaluator(new OverridingDifferenceEvaluator(ComparisonResult.SIMILAR));

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_always_be_different_with_overriden_evaluator() throws Exception {
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

        engineFactory.useEvaluator(new OverridingDifferenceEvaluator(ComparisonResult.DIFFERENT));

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_check_files_with_namespaced_attributes() throws Exception {
        // given
        File controlFile = TestResources.NAMESPACES_CONTROL.getFile();
        File testFile = TestResources.NAMESPACES_TEST.getFile();
        FileReader control = new FileReader(controlFile);
        FileReader test = new FileReader(testFile);

        engineFactory.useEvaluator(new ExpectedDifferenceEvaluator(ComparisonType.NAMESPACE_PREFIX));

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_different_structures() throws Exception {
        // given
        String control = "<root><node>text</node></root>";
        String test = "<root><node><inner-node>text</inner-node></node></root>";

        // when
        Diff myDiff = prepareDiff(properties, control, test);

        // then
        assertThat(myDiff.similar()).isFalse();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification() throws Exception {
        // given
        String control = "<root><node id=\"1\"/><node id=\"2\"/></root>";
        String test = "<root><node id=\"2\"/><node id=\"1\"/></root>";

        // when
        engineFactory.useSelector(new ElementNameAndAttributeSelector("id"));
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification2() throws Exception {
        // given
        String control = "<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>";
        String test = "<root><node id=\"2\" val=\"4\"/><node id=\"1\" val=\"3\"/></root>";

        // when
        engineFactory.useSelector(new ElementNameAndAttributeSelector("id"));
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification3() throws Exception {
        // given
        String control = "<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>";
        String test = "<root><node id=\"2\" val=\"3\"/><node id=\"1\" val=\"4\"/></root>";

        // when
        engineFactory.useSelector(new ElementNameAndAttributeSelector());
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_repeated_element_names_with_attribute_qualification4()
            throws Exception {

        // given
        String control = "<root><node id=\"1\" val=\"4\"/><node id=\"2\" val=\"3\"/></root>";
        String test = "<root><node id=\"2\" val=\"4\"/><node id=\"1\" val=\"3\"/></root>";

        // when
        engineFactory.useSelector(new ElementNameAndAttributeSelector());
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_check_repeated_element_names_with_namespaced_attribute_qualification() throws Exception {
        // TODO
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

        ExpectedDifferenceEvaluator evaluator = new ExpectedDifferenceEvaluator(
                new ComparisonType[] {
                        ComparisonType.NAMESPACE_PREFIX,
                        ComparisonType.CHILD_NODELIST_SEQUENCE
                });

        engineFactory.useEvaluator(evaluator);
        engineFactory.useSelector(new ElementNameAndAttributeSelector());

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_repeated_element_names_with_text_qualification() throws Exception {
        // given
        String control = "<root><node>1</node><node>2</node></root>";
        String test = "<root><node>2</node><node>1</node></root>";

        ExaminingExpectedDifferenceListener delegate =
                new ExaminingExpectedDifferenceListener(ComparisonType.CHILD_NODELIST_SEQUENCE) {
                    private int i = 0;

                    @Override
                    protected void examineDifferenceContents(Comparison difference) {
                        ++i;
                        String differenceXpathLocation = difference.getControlDetails().getXpath();
                        assertThat(differenceXpathLocation).isEqualTo("/root[1]/node[" + i + "]");
                    }
                };

        engineFactory.useEvaluator(delegate);
        engineFactory.useSelector(ElementSelectors.byNameAndText);

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    private abstract class ExaminingExpectedDifferenceListener extends ExpectedDifferenceEvaluator {
        private ExaminingExpectedDifferenceListener(ComparisonType expectedType) {
            super(expectedType);
        }
    }

    // defect raised by Kevin Krouse Jan 2003
    @Test
    public void should_check_XMLNS_number_of_attributes() throws Exception {
        // given
        String control = "<root xmlns=\"qwerty\"><node/></root>";
        String test = "<root xmlns=\"qwerty\" xmlns:qwerty=\"qwerty\"><qwerty:node/></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    private class OverridingDifferenceEvaluator implements DifferenceEvaluator {
        private final ComparisonResult overrideValue;

        private OverridingDifferenceEvaluator(ComparisonResult overrideValue) {
            this.overrideValue = overrideValue;
        }

        @Override
        public ComparisonResult evaluate(Comparison difference, ComparisonResult outcome) {
            return overrideValue;
        }
    }

    private class ExpectedDifferenceEvaluator implements DifferenceEvaluator {
        private final Set<ComparisonType> expectedIds;

        private ExpectedDifferenceEvaluator(ComparisonType expectedType) {
            this(new ComparisonType[] { expectedType });
        }

        private ExpectedDifferenceEvaluator(ComparisonType[] expectedIdValues) {
            this.expectedIds = new HashSet<ComparisonType>(expectedIdValues.length);
            for (int i = 0; i < expectedIdValues.length; ++i) {
                expectedIds.add(expectedIdValues[i]);
            }
        }

        @Override
        public ComparisonResult evaluate(Comparison difference, ComparisonResult outcome) {
            if (outcome == ComparisonResult.EQUAL) {
                return outcome;
            }
            assertTrue(difference.toString(), expectedIds.contains(difference.getType()));
            examineDifferenceContents(difference);
            return ComparisonResult.DIFFERENT;
        }

        protected void examineDifferenceContents(Comparison comparison) {
        }
    }

    @Test
    public void should_check_issue_1189681() throws Exception {
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
    public void should_be_different_when_CDATA_not_ignored() throws Exception {
        // given
        String expected = "<a>Hello</a>";
        String actual = "<a><![CDATA[Hello]]></a>";

        // when
        Diff diff = prepareDiff(properties, expected, actual);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_neither_be_identical_nor_similar_when_different_commants() throws Exception {
        // given
        String control = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String test = "<foo><bar a=\"b\"><!-- test --></bar> </foo>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_be_identical_and_similar_when_different_commants() throws Exception {
        // given
        String control = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String test = "<foo><bar a=\"b\"><!-- test --></bar> </foo>";

        // when
        properties.setIgnoreComments(true);
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isTrue();
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_pass_when_whitespaces_ignored() throws Exception {
        properties.setIgnoreWhitespace(true);
        should_neither_be_identical_nor_similar_when_different_commants();
        should_be_identical_and_similar_when_different_commants();
    }

    @Test
    public void should_pass_when_normalization() throws Exception {
        properties.setNormalizeWhitespace(true);
        should_neither_be_identical_nor_similar_when_different_commants();
        should_be_identical_and_similar_when_different_commants();
    }

    @Test
    public void should_check_normalization() throws Exception {
        // given
        Document control = new DocumentUtils(properties).newControlDocumentBuilder().newDocument();
        Element root = control.createElement("root");
        control.appendChild(root);
        root.appendChild(control.createTextNode("Text 1"));
        root.appendChild(control.createTextNode(" and 2"));
        Element inner = control.createElement("inner");
        root.appendChild(inner);
        inner.appendChild(control.createTextNode("Text 3 and 4"));

        Document test = new DocumentUtils(properties).newTestDocumentBuilder().newDocument();
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

        properties.setNormalizeWhitespace(true);
        diff = prepareDiff(properties, control, test);
        assertTrue(diff.identical());
        assertTrue(diff.similar());
        properties.setNormalizeWhitespace(false);

        diff = prepareDiff(properties, control, test);
        assertThat(diff.similar()).isFalse();
    }

    // fails with Java 5 and later
    @Test
    @Ignore
    public void should_make_sure_ignoring_whitespace_doesnt_affect_normalization() throws Exception {
        properties.setIgnoreWhitespace(true);
        should_check_normalization();
    }

    // fails with Java 5 and later
    @Test
    @Ignore
    public void should_make_sure_ignoring_comments_doesnt_affect_normalization() throws Exception {
        properties.setIgnoreComments(true);
        should_check_normalization();
    }

    @Test
    public void should_neither_be_identical_nor_similar_when_different_whitespaces() throws Exception {
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
    public void should_be_identical_and_similar_when_different_whitespaces() throws Exception {
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
    public void should_pass_when_differently_specified_namespace() throws Exception {
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
    public void should_be_identical_when_namespaces_present_and_whitespaces_ignored() throws Exception {

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
    public void should_be_identical_when_whitespaces_ignored() throws Exception {
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
    public void should_check_upgrading_of_recoverable_difference() throws Exception {
        // given
        String control = "<foo:bar xmlns:foo='urn:foo'/>";
        String test = "<bar xmlns='urn:foo'/>";

        DifferenceEvaluator evaluator = new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                return ComparisonResult.CRITICAL;
            }
        };
        engineFactory.useEvaluator(evaluator);

        // when
        Diff controlDiff = prepareDiff(properties, control, test);

        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(controlDiff.identical()).isFalse();
        assertThat(controlDiff.similar()).isTrue();

        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();
    }

    @Test
    public void should_verify_calls_on_overriden_MatchTracker() throws Exception {
        // given
        final ComparisonListener mockedTracker = mock(ComparisonListener.class);
        doNothing().when(mockedTracker).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>");
        diff.setEngineFactory(new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addMatchListener(mockedTracker);
                return engine;
            }
        });

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedTracker, times(13)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_engine() throws Exception {
        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));

        DifferenceEngineFactory factory = new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addMatchListener(mockedListener);
                return engine;
            };
        };

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", factory);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_diff() throws Exception {
        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));

        DifferenceEngineFactory factory = new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addMatchListener(mockedListener);
                return engine;
            }
        };

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", factory);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_newEngine() throws Exception {

        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));

        DifferenceEngineFactory factory = new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addMatchListener(mockedListener);
                return engine;
            }
        };

        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", factory);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_diff2() throws Exception {

        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));

        DifferenceEngineFactory factory = new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addMatchListener(mockedListener);
                return engine;
            }
        };

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", factory);

        // then
        assertThat(diff.identical()).isTrue();
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
    }

    @Test
    public void should_be_similar_when_ignoring_whitespaces() throws Exception {

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

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.similar()).isTrue();
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
    public void testEntityExpansion() throws Exception {
        String control = "<root>bla&#13;bla</root>";
        String test = "<root>bla&#xD;bla</root>";
        // XMLUnit.setExpandEntityReferences(true);
        try {
            Diff diff = prepareDiff(properties, control, test);
            assertThat(diff.similar()).isTrue();
        } finally {
            properties.setExpandEntityReferences(false);
        }
    }

    /**
     * @see https 
     *      ://sourceforge.net/tracker/?func=detail&aid=2807167&group_id=23187
     *      &atid=377768
     */
    @Test
    public void should_check_issue_2807167() throws Exception {
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
        engineFactory.useSelector(new ElementNameAndAttributeSelector());
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertThat(diff.similar()).isTrue();
    }

    @Test
    public void should_check_cocoon_xml_test_case() throws Exception {
        // given
        String control = "<!DOCTYPE skinconfig []>" + "<!--abcd--><root></root>";
        String test = "<!DOCTYPE skinconfig [<!--abcd-->]>" + "<root></root>";

        Document controlDoc = documentUtils.buildTestDocument(control);
        Document testDoc = documentUtils.buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        Diff reverseDiff = prepareDiff(properties, testDoc, controlDoc);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();

        assertThat(reverseDiff.identical()).isFalse();
        assertThat(reverseDiff.similar()).isTrue();
    }

    @Test
    public void should_ignore_different_comments() throws Exception {
        // given
        properties.setIgnoreComments(true);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        String control = "<foo><!--bar--></foo>";
        String test = "<foo><!--baz--></foo>";

        Document controlDoc = new DocumentUtils(properties).buildTestDocument(control);
        Document testDoc = new DocumentUtils(properties).buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(evaluator.getDifferences()).isEmpty();
    }

    @Test
    public void should_ignore_extra_comment() throws Exception {
        // given
        properties.setIgnoreComments(true);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        String control = "<foo><!--bar--><baz/></foo>";
        String test = "<foo><baz/></foo>";

        Document controlDoc = new DocumentUtils(properties).buildTestDocument(control);
        Document testDoc = new DocumentUtils(properties).buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(evaluator.getDifferences()).isEmpty();
    }

    @Test
    public void should_ignore_different_text_when_whitespace_normalized() throws Exception {
        // given
        properties.setNormalizeWhitespace(true);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        String control = "<stuff>string</stuff>";
        String test = "<stuff>  string  </stuff>";

        Document controlDoc = new DocumentUtils(properties).buildTestDocument(control);
        Document testDoc = new DocumentUtils(properties).buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(evaluator.getDifferences()).isEmpty();
    }

    @Test
    public void should_ignore_different_text_when_whitespace_ignored() throws Exception {
        // given
        properties.setIgnoreWhitespace(true);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        String control = "<stuff>string</stuff>";
        String test = "<stuff>  string  </stuff>";

        Document controlDoc = new DocumentUtils(properties).buildTestDocument(control);
        Document testDoc = new DocumentUtils(properties).buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isTrue();
        assertThat(evaluator.getDifferences()).isEmpty();
    }

    @Test
    public void should_detect_difference_between_text_and_cdata() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        String control = "<stuff>string</stuff>";
        String test = "<stuff><![CDATA[string]]></stuff>";

        Document controlDoc = new DocumentUtils(properties).buildTestDocument(control);
        Document testDoc = new DocumentUtils(properties).buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        // then
        List<Comparison> differences = evaluator.getDifferences();
        assertThat(identical).isFalse();
        assertThat(differences).hasSize(1);

        Comparison difference = differences.get(0);
        assertThat(difference.getType()).isEqualTo(ComparisonType.NODE_TYPE);
        assertThat(difference.getControlDetails().getValue()).isEqualTo(Node.TEXT_NODE);
        assertThat(difference.getTestDetails().getValue()).isEqualTo(Node.CDATA_SECTION_NODE);
    }

    @Test
    public void should_detect_similarity_of_text_and_cdata() throws Exception {
        // given
        String control = "<stuff>string</stuff>";
        String test = "<stuff><![CDATA[string]]></stuff>";

        Document controlDoc = documentUtils.buildTestDocument(control);
        Document testDoc = documentUtils.buildControlDocument(test);

        // when
        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        boolean identical = diff.identical();
        boolean similar = diff.similar();

        // then
        assertThat(identical).isFalse();
        assertThat(similar).isTrue();
    }

    @Test
    public void should_detect_missing_elements_between_doc_and_root_element_in_test() throws Exception {
        // given
        String control = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
                + "<!-- some comment -->"
                + "<?foo some PI ?>"
                + "<bar/>";

        String test = "<bar/>";

        // when
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        // then
        assertThat(identical).isFalse();
        List<Comparison> differences = evaluator.getDifferences();
        assertThat(differences).hasSize(1);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
    }

    @Test
    public void should_detect_different_no_namespace_schema_location() throws Exception {
        // given
        DocumentUtils utils = new DocumentUtils(properties);

        String attrName = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR;

        Document controlDoc = utils.newControlDocumentBuilder().newDocument();
        Element control = controlDoc.createElement("foo");
        control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "bar");
        controlDoc.appendChild(control);

        Document testDoc = utils.newTestDocumentBuilder().newDocument();
        Element test = testDoc.createElement("foo");
        test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, attrName, "baz");
        testDoc.appendChild(test);

        // when
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engineFactory.useEvaluator(evaluator);

        Diff diff = prepareDiff(properties, controlDoc, testDoc);
        diff.setEngineFactory(engineFactory);
        boolean identical = diff.identical();

        List<Comparison> differences = evaluator.getDifferences();

        // then
        assertThat(identical).isFalse();
        assertThat(diff.similar()).isTrue();

        Comparison difference = differences.get(0);
        assertThat(difference.getType()).isEqualTo(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
        assertThat(difference.getControlDetails().getValue()).isEqualTo("bar");
        assertThat(difference.getTestDetails().getValue()).isEqualTo("baz");
    }

    @Test
    public void should_be_different_when_element_order_different() throws Exception {
        // given
        String control = "<class id=\"c0\"> "
                + "<method id=\"c0_m0\"> "
                + "<dependency_info stmtId=\"c0_m0_s4\"> "
                + "<dependent tid=\"c3_m1_s18\"/> "
                + "<dependent tid=\"c3_m1_s32\"/> "
                + "<dependent tid=\"c3_m1_s26\"/> "
                + "</dependency_info> "
                + "</method> "
                + "</class>";
        String test = "<class id=\"c0\"> "
                + "<method id=\"c0_m0\"> "
                + "<dependency_info stmtId=\"c0_m0_s4\"> "
                + "<dependent tid=\"c3_m1_s32\"/> "
                + "<dependent tid=\"c3_m1_s18\"/> "
                + "<dependent tid=\"c3_m1_s26\"/> "
                + "</dependency_info> "
                + "</method> "
                + "</class>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isFalse();

    }
}
