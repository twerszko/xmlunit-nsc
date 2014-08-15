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

import junitparams.JUnitParamsRunner;
import net.sf.xmlunit.diff.*;
import org.custommonkey.xmlunit.ElementNameAndAttributeSelector;
import org.custommonkey.xmlunit.XMLConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.TestResources;
import org.xmlunit.diff.ElementSelectors;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.custommonkey.xmlunit.diff.Diff.newDiff;
import static org.custommonkey.xmlunit.diff.Diffs.prepareDiff;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(JUnitParamsRunner.class)
public class AbstractDiffTest {

    protected XmlUnitProperties properties;
    protected DefaultDifferenceEngineFactory engineFactory;
    private DocumentUtils documentUtils;

    @Before
    public void setUp() throws Exception {
        properties = new XmlUnitProperties();
        documentUtils = new DocumentUtils(properties);
        engineFactory = new DefaultDifferenceEngineFactory(properties);
    }

    @Test
    public void should_be_different_when_text_node_values_differ() throws Exception {
        String ctrl = "<root><test>text1</test></root>";
        String test = "<root><test>text2</test></root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_different_when_text_node_order_differs() throws Exception {
        String ctrl = "<control><test>test1</test><test>test2</test></control>";
        String test = "<control><test>test2</test><test>text1</test></control>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_different_when_text_node_is_added() throws Exception {
        String ctrl = "<control><test></test></control>";
        String test = "<control><test>text</test></control>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_identical_when_docs_are_identical() throws Exception {
        String ctrl = TestResources.BLAME_FILE.getContents();
        String test = TestResources.BLAME_FILE.getContents();

        assertIdentical(ctrl, test);
    }

    @Test
    public void should_be_identical_when_doc_is_compared_to_itself() throws Exception {
        String doc = "<root a=\"b\">text</root>";

        assertIdentical(doc, doc);
    }

    @Test
    public void should_be_different_when_element_is_added() throws Exception {
        String ctrl = "<root></root>";
        String test = "<root><node/></root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_different_when_element_is_removed() throws Exception {
        String ctrl = "<root><node/></root>";
        String test = "<root></root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_similar_when_element_order_differs() throws Exception {
        String ctrl = "<root><child1/><child2/></root>";
        String test = "<root><child2/><child1/></root>";

        assertSimilar(ctrl, test);
    }

    @Test
    public void should_be_different_when_attribute_is_added() throws Exception {
        String ctrl = "<root>text</root>";
        String test = "<root a=\"b\">text</root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_different_when_attribute_is_removed() throws Exception {
        String ctrl = "<root a=\"b\">text</root>";
        String test = "<root>text</root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_identical_when_attribute_order_differs() throws Exception {
        String ctrl = "<root a=\"b\" c=\"d\">text</root>";
        String test = "<root c=\"d\" a=\"b\">text</root>";

        assertIdentical(ctrl, test);
    }

    @Test
    public void should_be_different_when_attribute_value_differs() throws Exception {
        String ctrl = "<root a=\"b\">text</root>";
        String test = "<root a=\"c\">text</root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_be_similar_when_dtd_is_added() throws Exception {
        String ctrl = TestResources.SIMPLE_XML.getContents();
        String test = TestResources.SIMPLE_XML_WITH_DTD.getContents();

        assertSimilar(ctrl, test);
    }

    @Test
    public void should_be_similar_when_dtd_is_externalized() throws Exception {
        String ctrl = TestResources.SIMPLE_XML_WITH_DTD.getContents();
        String test = TestResources.SIMPLE_XML_WITH_EXT_DTD.getContents();

        assertSimilar(ctrl, test);
    }

    @Test
    public void should_be_identical_when_dtds_are_equivalent() throws Exception {
        String ctrl = TestResources.SIMPLE_XML_WITH_DTD.getContents();
        String test = TestResources.SIMPLE_XML_WITH_LONGER_DTD.getContents();

        assertIdentical(ctrl, test);
    }

    @Test
    public void should_not_ignore_whitespace_by_default() throws Exception {
        String ctrl = "<root><child>text</child></root>";
        String test = "<root>  \n<child> \ntext </child>\t </root>";

        assertDifferent(ctrl, test);
    }

    @Test
    public void should_ignore_whitespace_if_configured_so() throws Exception {
        String ctrl = "<root><child>text</child></root>";
        String test = "<root>  \n<child> \ntext </child>\t </root>";

        properties.setIgnoreWhitespace(true);

        assertIdentical(ctrl, test);
    }

    @Test
    public void normalizing_whitespace_should_have_same_effect_as_ignoring_whitespace() throws Exception {
        String ctrl = "<root><child>text</child></root>";
        String test = "<root>  \n<child> \ntext </child>\t </root>";

        properties.setNormalizeWhitespace(true);

        assertIdentical(ctrl, test);
    }

    @Test
    public void ignoring_comments_should_not_affect_ignoring_whitespace() throws Exception {
        properties.setIgnoreComments(true);

        should_not_ignore_whitespace_by_default();
        should_ignore_whitespace_if_configured_so();
    }

    @Test
    public void should_be_similar_when_only_namespace_name_differs() throws Exception {
        String ctrl = "<ns1:root xmlns:ns1=\"http://yada.com\"><ns1:child/></ns1:root>";
        String test = "<ns2:root xmlns:ns2=\"http://yada.com\"><ns2:child/></ns2:root>";

        assertSimilar(ctrl, test);
    }

    @Test
    public void should_be_similar_when_namespace_prefix_is_omitted() throws Exception {
        String ctrl = "<ns1:root xmlns:ns1=\"http://yada.com\"><ns1:child/></ns1:root>";
        String test = "<root xmlns=\"http://yada.com\"><child/></root>";

        assertSimilar(ctrl, test);
    }

    @Test
    public void should_be_similar_when_fully_named_element_order_differs() throws Exception {
        String ctrl = "<ns1:root xmlns:ns1=\"foo\" xmlns:ns2=\"bar\"><ns1:child/><ns2:child/></ns1:root>";
        String test = "<ns1:root xmlns:ns1=\"foo\" xmlns:ns2=\"bar\"><ns2:child/><ns1:child/></ns1:root>";

        assertSimilar(ctrl, test);
    }

    @Test
    public void should_use_provided_evaluator() throws Exception {
        String ctrl = "<root></root>";
        String test = "<root><node/></root>";

        engineFactory.useEvaluator(new OverridingDifferenceEvaluator(ComparisonResult.EQUAL));

        assertIdentical(ctrl, test);
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
        assertSimilar(diff);
    }

    @Test
    public void should_check_different_structures() throws Exception {
        // given
        String control = "<root><node>text</node></root>";
        String test = "<root><node><inner-node>text</inner-node></node></root>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertDifferent(diff);
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
        assertSimilar(diff);
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
        assertDifferent(diff);
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
        assertSimilar(diff);
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
        assertDifferent(diff);
    }

    @Test
    public void should_check_repeated_element_names_with_namespaced_attribute_qualification()
            throws Exception {
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
                new ComparisonType[]{
                        ComparisonType.NAMESPACE_PREFIX,
                        ComparisonType.CHILD_NODELIST_SEQUENCE
                });

        engineFactory.useEvaluator(evaluator);
        engineFactory.useSelector(new ElementNameAndAttributeSelector());

        // when
        Diff diff = prepareDiff(properties, control, test);
        diff.setEngineFactory(engineFactory);

        // then
        assertSimilar(diff);
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
        assertSimilar(diff);
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
        assertSimilar(diff);
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
        assertDifferent(diff);
    }

    @Test
    public void should_be_different_when_CDATA_not_ignored() throws Exception {
        // given
        String expected = "<a>Hello</a>";
        String actual = "<a><![CDATA[Hello]]></a>";

        // when
        Diff diff = prepareDiff(properties, expected, actual);

        // then
        assertSimilar(diff);
    }

    @Test
    public void should_neither_be_identical_nor_similar_when_different_commants() throws Exception {
        // given
        String control = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String test = "<foo><bar a=\"b\"><!-- test --></bar> </foo>";

        // when
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertDifferent(diff);
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
        assertIdentical(diff);
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
        assertDifferent(diff);
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
        assertDifferent(diff);
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
        assertIdentical(diff);
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
        Diff diff = prepareDiff(properties, control, test);

        // then
        assertSimilar(diff);
    }

    /**
     * Bug Report 1779701
     *
     * @throws IOException
     * @throws SAXException
     * @see http
     * ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1779701&
     * amp;group_id=23187&amp;atid=377768
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
        assertIdentical(diff);
    }

    /**
     * Bug Report 1863632
     *
     * @throws IOException
     * @throws SAXException <<<<<<< HEAD
     * @see http://sourceforge.net/tracker/index.php?func=detail&amp;aid=1863632&
     * amp;group_id=23187&amp;atid=377768
     * =======
     * @see http
     * ://sourceforge.net/tracker/index.php?func=detail&amp;aid=1863632&
     * amp;group_id=23187&amp;atid=377768
     * >>>>>>> 51b4600b7d345020c4f223daf545d80abc66829a
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
        assertIdentical(diff);
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
        assertSimilar(controlDiff);

        assertDifferent(diff);
    }

    @Test
    public void should_verify_calls_on_overriden_MatchTracker() throws Exception {
        // given
        final ComparisonListener mockedTracker = mock(ComparisonListener.class);
        doNothing().when(mockedTracker).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));

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
        assertIdentical(diff);
        verify(mockedTracker, times(13)).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_engine() throws Exception {
        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));

        DifferenceEngineFactory factory = new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addMatchListener(mockedListener);
                return engine;
            }

            ;
        };

        // when
        Diff diff = prepareDiff(properties, "<foo/>", "<foo/>", factory);

        // then
        assertIdentical(diff);
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_diff() throws Exception {
        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));

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
        assertIdentical(diff);
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_newEngine() throws Exception {

        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));

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
        assertIdentical(diff);
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));
    }

    @Test
    public void should_verify_calls_on_MatchTracker_overriden_in_diff2() throws Exception {

        // given
        final ComparisonListener mockedListener = mock(ComparisonListener.class);
        doNothing().when(mockedListener).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));

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
        assertIdentical(diff);
        verify(mockedListener, times(13)).comparisonPerformed(any(Comparison.class),
                any(ComparisonResult.class));
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
        assertIdentical(diff);
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
     * ://sourceforge.net/tracker/?func=detail&aid=2807167&group_id=23187
     * &atid=377768
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
        assertSimilar(diff);
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
        assertSimilar(diff);
        assertSimilar(reverseDiff);
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

        // then
        assertIdentical(diff);
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

        // then
        assertIdentical(diff);
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

        // then
        assertIdentical(diff);
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

        // then
        assertIdentical(diff);
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

        // then
        List<Comparison> differences = evaluator.getDifferences();
        assertDifferent(diff);
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

        // then
        assertSimilar(diff);
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

        // then
        assertSimilar(diff);
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

        List<Comparison> differences = evaluator.getDifferences();

        // then
        assertSimilar(diff);

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
        assertDifferent(diff);
    }

    private Diff createDiff(String ctrl, String test) throws BuilderException {
        return newDiff(properties).betweenControlDocument(ctrl).andTestDocument(test).build();
    }

    private void assertDifferent(String ctrl, String test) throws BuilderException {
        assertDifferent(createDiff(ctrl, test));
    }

    private void assertSimilar(String ctrl, String test) throws BuilderException {
        assertSimilar(createDiff(ctrl, test));
    }

    private void assertIdentical(String ctrl, String test) throws BuilderException {
        assertIdentical(createDiff(ctrl, test));
    }

    protected void assertDifferent(Diff diff) {
        assertThat(diff.similar()).as("Should not be similar").isFalse();
        assertThat(diff.identical()).as("Should not be identical").isFalse();
    }

    protected void assertSimilar(Diff diff) {
        assertThat(diff.similar()).as("Should be similar").isTrue();
        assertThat(diff.identical()).as("Should not be identical").isFalse();
    }

    protected void assertIdentical(Diff diff) {
        assertThat(diff.similar()).as("Should be similar").isTrue();
        assertThat(diff.identical()).as("Should be identical").isTrue();
    }

    private abstract class ExaminingExpectedDifferenceListener extends ExpectedDifferenceEvaluator {
        private ExaminingExpectedDifferenceListener(ComparisonType expectedType) {
            super(expectedType);
        }
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
            this(new ComparisonType[]{expectedType});
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
}
