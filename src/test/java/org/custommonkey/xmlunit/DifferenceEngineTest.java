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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.Comparison;

import org.custommonkey.xmlunit.diff.DifferenceType;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class DifferenceEngineTest {
    private CollectingDifferenceListener listener;
    private DifferenceEngine engine;
    private Document document;

    private final ComparisonController PSEUDO_DIFF = new SimpleComparisonController();
    private final ComparisonController PSEUDO_DETAILED_DIFF = new NeverHaltingComparisonController();

    private final static ElementQualifier DEFAULT_ELEMENT_QUALIFIER = new ElementNameQualifier();
    private final static String TEXT_A = "the pack on my back is aching";
    private final static String TEXT_B = "the straps seem to cut me like a knife";
    private final static String COMMENT_A = "Im no clown I wont back down";
    private final static String COMMENT_B = "dont need you to tell me whats going down";
    private final static String[] PROC_A = { "down", "down down" };
    private final static String[] PROC_B = { "dadada", "down" };
    private final static String CDATA_A = "I'm standing alone, you're weighing the gold";
    private final static String CDATA_B = "I'm watching you sinking... Fools Gold";
    private final static String ATTR_A = "These boots were made for walking";
    private final static String ATTR_B = "The marquis de sade never wore no boots like these";

    protected XmlUnitProperties properties;
    private DocumentUtils documentUtils;

    @Before
    public void setUp() throws Exception {
        properties = new XmlUnitProperties();

        resetListener();
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);
        documentUtils = new DocumentUtils(properties);
        DocumentBuilder documentBuilder = documentUtils.newControlDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    private class SimpleComparisonController implements ComparisonController {
        public boolean haltComparison(Difference afterDifference) {
            return !afterDifference.isRecoverable();
        }
    }

    private class NeverHaltingComparisonController implements ComparisonController {
        public boolean haltComparison(Difference afterDifference) {
            return false;
        }
    }

    private void assertDifferentText(Text control, Text test,
            Difference difference) {
        try {
            engine.compareText(control, test, listener);
        } catch (DifferenceEngine.DifferenceFoundException e) {
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(true, listener.different);
        resetListener();
    }

    private class TextMatcher extends TypeSafeMatcher<Text> {
        public void describeTo(Description description) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean matchesSafely(Text arg0) {
            // TODO Auto-generated method stub
            return false;
        }
    }

    @Test
    public void testCompareText() throws Exception {
        String expected = TEXT_A;
        String actual = TEXT_B;
        Text control = document.createTextNode(expected);
        Text test = document.createTextNode(actual);

        assertDifferentText(control, test, new Difference(DifferenceType.TEXT_VALUE));
    }

    private void assertDifferentProcessingInstructions(
            ProcessingInstruction control, ProcessingInstruction test,
            Difference difference) {
        try {
            engine.compareProcessingInstruction(control, test, listener);
        } catch (DifferenceEngine.DifferenceFoundException e) {
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(true, listener.different);
        resetListener();
    }

    @Test
    public void testCompareProcessingInstruction() throws Exception {
        String[] expected = PROC_A;
        String[] actual = PROC_B;
        ProcessingInstruction control = document.createProcessingInstruction(
                expected[0], expected[1]);
        ProcessingInstruction test = document.createProcessingInstruction(
                actual[0], actual[1]);

        assertDifferentProcessingInstructions(control, test,
                new Difference(DifferenceType.PROCESSING_INSTRUCTION_TARGET));

        ProcessingInstruction control2 = document.createProcessingInstruction(
                expected[0], expected[1]);
        ProcessingInstruction test2 = document.createProcessingInstruction(
                expected[0], actual[1]);
        assertDifferentProcessingInstructions(control2, test2,
                new Difference(DifferenceType.PROCESSING_INSTRUCTION_DATA));
    }

    private void assertDifferentComments(Comment control, Comment test,
            Difference difference) {
        try {
            engine.compareComment(control, test, listener);
        } catch (DifferenceEngine.DifferenceFoundException e) {
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(true, listener.different);
        resetListener();
    }

    @Test
    public void testCompareComment() throws Exception {
        String expected = COMMENT_A;
        String actual = COMMENT_B;
        Comment control = document.createComment(expected);
        Comment test = document.createComment(actual);

        assertDifferentComments(control, test, new Difference(DifferenceType.COMMENT_VALUE));
    }

    private void assertDifferentCDATA(CDATASection control, CDATASection test,
            Difference difference) {
        try {
            engine.compareCDataSection(control, test, listener);
        } catch (DifferenceEngine.DifferenceFoundException e) {
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(true, listener.different);
        resetListener();
    }

    @Test
    public void testCompareCDATA() throws Exception {
        String expected = CDATA_A;
        String actual = CDATA_B;
        CDATASection control = document.createCDATASection(expected);
        CDATASection test = document.createCDATASection(actual);

        assertDifferentCDATA(control, test, new Difference(DifferenceType.CDATA_VALUE));
    }

    private void assertDifferentDocumentTypes(DocumentType control,
            DocumentType test, Difference difference, boolean fatal) {
        try {
            engine.compareDocumentType(control, test, listener);
            if (fatal) {
                fail("Expected fatal difference!");
            }
        } catch (DifferenceEngine.DifferenceFoundException e) {
            if (!fatal) {
                fail("Expected similarity not fatal difference!");
            }
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(fatal, listener.different);
        resetListener();
    }

    @Test
    public void testCompareDocumentType() throws Exception {
        File tmpFile = File.createTempFile("Roses", "dtd");
        tmpFile.deleteOnExit();
        String tmpDTD = "<!ELEMENT leaf (#PCDATA)><!ELEMENT root (leaf)>";
        new FileWriter(tmpFile).write(tmpDTD);
        String rosesDTD = tmpFile.toURI().toURL().toExternalForm();

        File altTmpFile = File.createTempFile("TheCrows", "dtd");
        altTmpFile.deleteOnExit();
        new FileWriter(altTmpFile).write(tmpDTD);
        String theCrowsDTD = altTmpFile.toURI().toURL().toExternalForm();

        Document controlDoc = documentUtils.buildControlDocument(
                "<!DOCTYPE root PUBLIC 'Stone' '" + rosesDTD + "'>"
                        + "<root><leaf/></root>");
        Document testDoc = documentUtils.buildTestDocument(
                "<!DOCTYPE tree PUBLIC 'Stone' '" + rosesDTD + "'>"
                        + "<tree><leaf/></tree>");

        DocumentType control = controlDoc.getDoctype();
        DocumentType test = testDoc.getDoctype();

        assertDifferentDocumentTypes(control, test, new Difference(DifferenceType.DOCTYPE_NAME), true);

        test = documentUtils.buildTestDocument("<!DOCTYPE root PUBLIC 'id' '" + rosesDTD + "'>"
                + "<root><leaf/></root>").getDoctype();
        assertDifferentDocumentTypes(control, test, new Difference(DifferenceType.DOCTYPE_PUBLIC_ID), true);

        test = documentUtils.buildTestDocument("<!DOCTYPE root SYSTEM '" + rosesDTD + "'>"
                + "<root><leaf/></root>").getDoctype();
        assertDifferentDocumentTypes(control, test, new Difference(DifferenceType.DOCTYPE_PUBLIC_ID), true);

        test = documentUtils.buildTestDocument("<!DOCTYPE root PUBLIC 'Stone' '" + theCrowsDTD + "'>"
                + "<root><leaf/></root>").getDoctype();
        assertDifferentDocumentTypes(control, test, new Difference(DifferenceType.DOCTYPE_SYSTEM_ID), false);

        test = documentUtils.buildTestDocument("<!DOCTYPE root SYSTEM '" + theCrowsDTD + "'>"
                + "<root><leaf/></root>").getDoctype();
        assertDifferentDocumentTypes(control, test, new Difference(DifferenceType.DOCTYPE_PUBLIC_ID), true);

        control = documentUtils.buildTestDocument("<!DOCTYPE root SYSTEM '" + rosesDTD + "'>"
                + "<root><leaf/></root>").getDoctype();
        assertDifferentDocumentTypes(control, test, new Difference(DifferenceType.DOCTYPE_SYSTEM_ID), false);
    }

    private void assertDifferentAttributes(Attr control, Attr test,
            Difference difference, boolean fatal) {
        try {
            engine.compareAttribute(control, test, listener);
            if (fatal) {
                fail("Expecting fatal difference!");
            }
        } catch (DifferenceEngine.DifferenceFoundException e) {
            if (!fatal) {
                fail("Expecting similarity not fatal difference!");
            }
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(fatal, listener.different);
        resetListener();
    }

    @Test
    public void testCompareAttribute() throws Exception {
        String expected = ATTR_A;
        String actual = ATTR_B;
        Attr control = document.createAttribute("testAttr");
        control.setValue(expected);
        Attr test = document.createAttribute("testAttr");
        test.setValue(actual);

        assertDifferentAttributes(control, test, new Difference(DifferenceType.ATTR_VALUE), true);

        String doctypeDeclaration = "<!DOCTYPE manchester [" +
                "<!ELEMENT sound EMPTY><!ATTLIST sound sorted (true|false) \"true\">" +
                "<!ELEMENT manchester (sound)>]>";
        Document controlDoc = documentUtils.buildControlDocument(doctypeDeclaration +
                "<manchester><sound sorted=\"true\"/></manchester>");
        control = (Attr) controlDoc.getDocumentElement().getFirstChild()
                .getAttributes().getNamedItem("sorted");

        Document testDoc = documentUtils.buildTestDocument(doctypeDeclaration +
                "<manchester><sound/></manchester>");
        test = (Attr) testDoc.getDocumentElement().getFirstChild()
                .getAttributes().getNamedItem("sorted");

        assertDifferentAttributes(control, test,
                new Difference(DifferenceType.ATTR_VALUE_EXPLICITLY_SPECIFIED), false);
    }

    private void assertDifferentElements(Element control, Element test,
            Difference difference) {
        try {
            engine.compareElement(control, test, listener);
        } catch (DifferenceEngine.DifferenceFoundException e) {
        }
        assertEquals(difference.getType(), listener.comparingWhat);
        assertEquals(true, listener.different);
        resetListener();
    }

    @Test
    public void testCompareElements() throws Exception {
        Document document = documentUtils.buildControlDocument(
                "<down><im standing=\"alone\"/><im watching=\"you\" all=\"\"/>"
                        + "<im watching=\"you all\"/><im watching=\"you sinking\"/></down>");
        Element control = document.getDocumentElement();
        Element test = (Element) control.getFirstChild();

        assertDifferentElements(control, test, new Difference(DifferenceType.ELEMENT_TAG_NAME));

        // compare im#1 to im#2
        control = test;
        test = (Element) control.getNextSibling();
        assertDifferentElements(control, test, new Difference(DifferenceType.ELEMENT_NUM_ATTRIBUTES));

        // compare im#1 to im#3
        test = (Element) test.getNextSibling();
        assertDifferentElements(control, test, new Difference(DifferenceType.ATTR_NAME_NOT_FOUND));

        // compare im#3 to im#4
        control = test;
        test = (Element) control.getNextSibling();
        assertDifferentElements(control, test, new Difference(DifferenceType.ATTR_VALUE));
    }

    @Test
    public void testCompareNode() throws Exception {
        Document controlDocument = documentUtils.buildControlDocument("<root>"
                + "<!-- " + COMMENT_A + " -->"
                + "<?" + PROC_A[0] + " " + PROC_A[1] + " ?>"
                + "<elem attr=\"" + ATTR_A + "\">" + TEXT_A + "</elem></root>");
        Document testDocument = documentUtils.buildTestDocument("<root>"
                + "<!-- " + COMMENT_B + " -->"
                + "<?" + PROC_B[0] + " " + PROC_B[1] + " ?>"
                + "<elem attr=\"" + ATTR_B + "\">" + TEXT_B + "</elem></root>");

        engine.compare(controlDocument, testDocument, listener, null);

        Node control = controlDocument.getDocumentElement().getFirstChild();
        Node test = testDocument.getDocumentElement().getFirstChild();

        do {
            resetListener();
            engine.compare(control, test, listener, null);
            assertEquals(true, null != listener.comparingWhat);
            assertEquals(false, listener.nodesSkipped);

            resetListener();
            engine.compare(control, control, listener, null);
            assertEquals(null, listener.comparingWhat);

            control = control.getNextSibling();
            test = test.getNextSibling();
        } while (control != null);
    }

    private void assertDifferentNamespaceDetails(Node control, Node test,
            Difference expectedDifference, boolean fatal) {
        try {
            engine.compareNodeBasics(control, test, listener);
            if (fatal) {
                fail("Expected fatal difference");
            }
        } catch (DifferenceEngine.DifferenceFoundException e) {
            if (!fatal) {
                fail("Not expecting fatal difference!");
            }
        }
        assertEquals(expectedDifference.getType(), listener.comparingWhat);
        assertEquals(fatal, listener.different);
        resetListener();
    }

    @Test
    public void testCompareNodeBasics() throws Exception {
        String namespaceA = "http://example.org/StoneRoses";
        String namespaceB = "http://example.org/Stone/Roses";
        String prefixA = "music";
        String prefixB = "cd";
        String elemName = "nowPlaying";
        Element control = document.createElementNS(namespaceA,
                prefixA + ':' + elemName);
        engine.compareNodeBasics(control, control, listener);

        Element test = document.createElementNS(namespaceB,
                prefixA + ':' + elemName);
        assertDifferentNamespaceDetails(control, test, new Difference(DifferenceType.NAMESPACE_URI),
                true);

        test = document.createElementNS(namespaceA,
                prefixB + ':' + elemName);
        assertDifferentNamespaceDetails(control, test,
                new Difference(DifferenceType.NAMESPACE_PREFIX), false);
    }

    private void assertDifferentChildren(Node control, Node test,
            Difference expectedDifference, boolean fatal) {
        try {
            engine.compareHasChildNodes(control, test, listener);
            engine.compareNodeChildren(control, test, listener, DEFAULT_ELEMENT_QUALIFIER);
            if (fatal) {
                fail("Expected fatal difference");
            }
        } catch (DifferenceEngine.DifferenceFoundException e) {
            if (!fatal) {
                fail("Not expecting fatal difference " +
                        listener.comparingWhat
                        + ": expected " + listener.expected
                        + " but was " + listener.actual);
            }
        }
        assertEquals(expectedDifference == null ? null : expectedDifference.getType(),
                listener.comparingWhat);
        assertEquals(fatal, listener.different);
        resetListener();
    }

    @Test
    public void testCompareNodeChildren() throws Exception {
        document = documentUtils.buildControlDocument(
                "<down><im standing=\"alone\"/><im><watching/>you all</im>"
                        + "<im watching=\"you\">sinking</im></down>");
        // compare im #1 to itself
        Node control = document.getDocumentElement().getFirstChild();
        Node test = control;
        assertDifferentChildren(control, control, null, false);

        // compare im #1 to im #2
        test = control.getNextSibling();
        assertDifferentChildren(control, test, new Difference(DifferenceType.HAS_CHILD_NODES), true);

        // compare im #2 to im #3
        control = test;
        test = control.getNextSibling();
        assertDifferentChildren(control, test, new Difference(DifferenceType.CHILD_NODELIST_LENGTH),
                true);
    }

    private void assertDifferentNodeLists(Node control, Node test,
            Difference expectedDifference, boolean fatal) {
        try {
            engine.compareNodeList(control.getChildNodes(), test.getChildNodes(),
                    control.getChildNodes().getLength(), listener, DEFAULT_ELEMENT_QUALIFIER);
            if (fatal) {
                fail("Expected fatal difference");
            }
        } catch (DifferenceEngine.DifferenceFoundException e) {
            if (!fatal) {
                fail("Not expecting fatal difference!");
            }
        }
        assertEquals(expectedDifference == null ? null : expectedDifference.getType(),
                listener.comparingWhat);
        assertEquals(fatal, listener.different);
        resetListener();
    }

    @Test
    public void testCompareNodeList() throws Exception {
        document = documentUtils.buildControlDocument(
                "<down><im><standing/>alone</im><im><watching/>you all</im>"
                        + "<im><watching/>you sinking</im></down>");
        // compare im #1 to itself
        Node control = document.getDocumentElement().getFirstChild();
        Node test = control;
        assertDifferentNodeLists(control, test, null, false);

        // compare im #1 to im #2
        test = control.getNextSibling();
        assertDifferentChildren(control, test, new Difference(DifferenceType.ELEMENT_TAG_NAME), true);

        // compare im #2 to im #3
        control = test;
        test = control.getNextSibling();
        assertDifferentChildren(control, test, new Difference(DifferenceType.TEXT_VALUE), true);
    }

    @Test
    public void testCompareNodeListElements() throws Exception {
        Element control = document.createElement("root");
        control.appendChild(document.createElement("leafElemA"));
        control.appendChild(document.createElement("leafElemB"));

        Element test = document.createElement("root");
        test.appendChild(document.createElement("leafElemB"));
        test.appendChild(document.createElement("leafElemA"));

        assertDifferentChildren(control, test, new Difference(DifferenceType.CHILD_NODELIST_SEQUENCE), false);
        assertDifferentChildren(test, control, new Difference(DifferenceType.CHILD_NODELIST_SEQUENCE), false);
    }

    @Test
    public void testCompareNodeListMixedContent() throws Exception {
        Element control = document.createElement("root");
        control.appendChild(document.createTextNode("text leaf"));
        control.appendChild(document.createElement("leafElem"));

        Element test = document.createElement("root");
        test.appendChild(document.createElement("leafElem"));
        test.appendChild(document.createTextNode("text leaf"));

        assertDifferentChildren(control, test, new Difference(DifferenceType.CHILD_NODELIST_SEQUENCE), false);
        assertDifferentChildren(test, control, new Difference(DifferenceType.CHILD_NODELIST_SEQUENCE), false);
    }

    @Test
    public void testBasicCompare() throws Exception {
        try {
            Comparison comparison = new Comparison(null,
                    null, null, "black",
                    null, null, "white");
            engine.createValueComparator(new Difference(DifferenceType.ATTR_NAME_NOT_FOUND), listener)
                    .compare(comparison);
            fail("Expected difference found exception");
        } catch (DifferenceEngine.DifferenceFoundException e) {
            assertEquals(true, listener.different);
            assertEquals(DifferenceType.ATTR_NAME_NOT_FOUND, listener.comparingWhat);
        }
        resetListener();

        try {
            Comparison comparison = new Comparison(null,
                    null, null, "black",
                    null, null, "white");
            engine.createValueComparator(new Difference(DifferenceType.NAMESPACE_PREFIX), listener)
                    .compare(comparison);

            assertEquals(false, listener.different);
            assertEquals(DifferenceType.NAMESPACE_PREFIX, listener.comparingWhat);
        } catch (Exception e) {
            fail("Not expecting difference found exception");
        }
    }

    @Test
    public void testXpathLocation1() throws Exception {
        String control = "<dvorak><keyboard/><composer/></dvorak>";
        String test = "<qwerty><keyboard/></qwerty>";
        listenToDifferences(control, test);
        assertEquals("1st control xpath", "/dvorak[1]", listener.controlXpath);
        assertEquals("1st test xpath", "/qwerty[1]", listener.testXpath);
    }

    @Test
    public void testXpathLocation2() throws Exception {
        String control = "<dvorak><keyboard/><composer/></dvorak>";
        String test = "<qwerty><keyboard/></qwerty>";
        String start = "<a>", end = "</a>";
        listenToDifferences(start + control + end, start + test + end);
        assertEquals("2nd control xpath", "/a[1]/dvorak[1]", listener.controlXpath);
        assertEquals("2nd test xpath", "/a[1]/qwerty[1]", listener.testXpath);
    }

    @Test
    public void testXpathLocation3() throws Exception {
        String control = "<stuff><wood type=\"rough\"/></stuff>";
        String test = "<stuff><wood type=\"smooth\"/></stuff>";
        listenToDifferences(control, test);
        assertEquals("3rd control xpath", "/stuff[1]/wood[1]/@type", listener.controlXpath);
        assertEquals("3rd test xpath", "/stuff[1]/wood[1]/@type", listener.testXpath);
    }

    @Test
    public void testXpathLocation4() throws Exception {
        String control = "<stuff><glass colour=\"clear\"/><glass colour=\"green\"/></stuff>";
        String test = "<stuff><glass colour=\"clear\"/><glass colour=\"blue\"/></stuff>";
        ;
        listenToDifferences(control, test);
        assertEquals("4th control xpath", "/stuff[1]/glass[2]/@colour", listener.controlXpath);
        assertEquals("4th test xpath", "/stuff[1]/glass[2]/@colour", listener.testXpath);
    }

    @Test
    public void testXpathLocation5() throws Exception {
        String control = "<stuff><wood>maple</wood><wood>oak</wood></stuff>";
        String test = "<stuff><wood>maple</wood><wood>ash</wood></stuff>";
        listenToDifferences(control, test);
        assertEquals("5th control xpath", "/stuff[1]/wood[2]/text()[1]", listener.controlXpath);
        assertEquals("5th test xpath", "/stuff[1]/wood[2]/text()[1]", listener.testXpath);
    }

    @Test
    public void testXpathLocation6() throws Exception {
        String control = "<stuff><list><wood/><glass/></list><item/></stuff>";
        String test = "<stuff><list><wood/><glass/></list><item>description</item></stuff>";
        listenToDifferences(control, test);
        assertEquals("6th control xpath", "/stuff[1]/item[1]", listener.controlXpath);
        assertEquals("6th test xpath", "/stuff[1]/item[1]", listener.testXpath);
    }

    @Test
    public void testXpathLocation7() throws Exception {
        String control = "<stuff><list><wood/></list></stuff>";
        String test = "<stuff><list><glass/></list></stuff>";
        listenToDifferences(control, test);
        assertEquals("7th control xpath", "/stuff[1]/list[1]/wood[1]", listener.controlXpath);
        assertEquals("7th test xpath", "/stuff[1]/list[1]/glass[1]", listener.testXpath);
    }

    @Test
    public void testXpathLocation8() throws Exception {
        String control = "<stuff><list><!--wood--></list></stuff>";
        String test = "<stuff><list><!--glass--></list></stuff>";
        listenToDifferences(control, test);
        assertEquals("8th control xpath", "/stuff[1]/list[1]/comment()[1]", listener.controlXpath);
        assertEquals("8th test xpath", "/stuff[1]/list[1]/comment()[1]", listener.testXpath);
    }

    @Test
    public void testXpathLocation9() throws Exception {
        String control = "<stuff><list/><?wood rough?><list/></stuff>";
        String test = "<stuff><list/><?glass clear?><list/></stuff>";
        listenToDifferences(control, test);
        assertEquals("9th control xpath", "/stuff[1]/processing-instruction()[1]",
                listener.controlXpath);
        assertEquals("9th test xpath", "/stuff[1]/processing-instruction()[1]",
                listener.testXpath);
    }

    @Test
    public void testXpathLocation10() throws Exception {
        String control = "<stuff><list/>list<![CDATA[wood]]></stuff>";
        String test = "<stuff><list/>list<![CDATA[glass]]></stuff>";
        listenToDifferences(control, test);
        assertEquals("10th control xpath", "/stuff[1]/text()[2]",
                listener.controlXpath);
        assertEquals("10th test xpath", "/stuff[1]/text()[2]",
                listener.testXpath);
    }

    @Test
    public void testXpathLocation11() throws Exception {
        String control = "<stuff><list><item/></list></stuff>";
        String test = "<stuff><list>item text</list></stuff>";
        listenToDifferences(control, test);
        assertEquals("11th control xpath", "/stuff[1]/list[1]/item[1]",
                listener.controlXpath);
        assertEquals("11th test xpath", "/stuff[1]/list[1]/text()[1]",
                listener.testXpath);
    }

    @Test
    public void testXpathLocation12() throws Exception {
        engine = new DifferenceEngine(null, PSEUDO_DETAILED_DIFF);
        String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
        String test = "<stuff><item id=\"1\"/></stuff>";
        listenToDifferences(control, test);
        assertEquals("12th control xpath", "/stuff[1]/item[2]",
                listener.controlXpath);
        assertEquals("12th test xpath", "/stuff[1]/item[1]",
                listener.testXpath);
    }

    @Test
    public void testXpathLocation13() throws Exception {
        engine = new DifferenceEngine(null, PSEUDO_DETAILED_DIFF);
        String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
        String test = "<stuff><?item data?></stuff>";
        listenToDifferences(control, test);
        // mutiple Differences, we only see the last one, missing second element
        assertEquals("13 difference type",
                DifferenceType.CHILD_NODE_NOT_FOUND,
                listener.comparingWhat);
        assertEquals("13th control xpath", "/stuff[1]/item[2]",
                listener.controlXpath);
        assertNull("13th test xpath", listener.testXpath);
    }

    @Test
    public void testXpathLocation14() throws Exception {
        engine = new DifferenceEngine(null, PSEUDO_DETAILED_DIFF);
        String control = "<stuff><thing id=\"1\"/><item id=\"2\"/></stuff>";
        String test = "<stuff><item id=\"2\"/><item id=\"1\"/></stuff>";
        listenToDifferences(control, test);
        assertEquals("14th control xpath", "/stuff[1]/item[1]/@id",
                listener.controlXpath);
        assertEquals("14th test xpath", "/stuff[1]/item[2]/@id",
                listener.testXpath);
    }

    @Test
    public void testIssue1027863() throws Exception {
        engine = new DifferenceEngine(null, PSEUDO_DETAILED_DIFF);
        String control = "<stuff><item id=\"1\"><thing/></item></stuff>";
        String test = "<stuff><item id=\"2\"/></stuff>";
        listenToDifferences(control, test);
        assertEquals("15th difference type",
                DifferenceType.HAS_CHILD_NODES,
                listener.comparingWhat);
        assertEquals("15th difference control value", "true",
                listener.expected);
        assertEquals("15th difference test value", "false",
                listener.actual);
        assertEquals("15th control xpath", "/stuff[1]/item[1]",
                listener.controlXpath);
        assertEquals("15th test xpath", "/stuff[1]/item[1]",
                listener.testXpath);
    }

    @Test
    public void testAttributeSequence() throws Exception {
        properties.setIgnoreAttributeOrder(false);
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);

        testAttributeSequence(DifferenceType.ATTR_SEQUENCE);

        properties.setIgnoreAttributeOrder(true);
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);

        resetListener();
        testAttributeSequence(null);
    }

    private void testAttributeSequence(DifferenceType expected) throws Exception {
        Element control = document.createElement("foo");
        Element test = document.createElement("foo");
        OrderPreservingNamedNodeMap controlMap =
                new OrderPreservingNamedNodeMap();
        OrderPreservingNamedNodeMap testMap = new OrderPreservingNamedNodeMap();
        for (int i = 0; i < 2; i++) {
            int j = 1 - i;
            Attr attrI = document.createAttribute("attr" + i);
            attrI.setValue(String.valueOf(i));
            Attr attrJ = document.createAttribute("attr" + j);
            attrJ.setValue(String.valueOf(j));

            control.setAttributeNode(attrI);
            controlMap.add(attrI);
            test.setAttributeNode(attrJ);
            testMap.add(attrJ);
        }
        engine.compareElementAttributes(control, test, controlMap, testMap,
                listener);
        assertEquals(expected, listener.comparingWhat);
    }

    @Test
    public void testAttributeSequenceNS() throws Exception {
        properties.setIgnoreAttributeOrder(false);
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);

        testAttributeSequenceNS(DifferenceType.ATTR_SEQUENCE);

        properties.setIgnoreAttributeOrder(true);
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);

        resetListener();
        testAttributeSequenceNS(null);
    }

    private void testAttributeSequenceNS(DifferenceType expected) throws Exception {
        Element control = document.createElementNS("ns", "foo");
        Element test = document.createElementNS("ns", "foo");
        OrderPreservingNamedNodeMap controlMap =
                new OrderPreservingNamedNodeMap();
        OrderPreservingNamedNodeMap testMap = new OrderPreservingNamedNodeMap();
        for (int i = 0; i < 2; i++) {
            int j = 1 - i;
            Attr attrI = document.createAttributeNS("ns", "attr" + i);
            attrI.setValue(String.valueOf(i));
            Attr attrJ = document.createAttributeNS("ns", "attr" + j);
            attrJ.setValue(String.valueOf(j));

            control.setAttributeNode(attrI);
            controlMap.add(attrI);
            test.setAttributeNode(attrJ);
            testMap.add(attrJ);
        }
        engine.compareElementAttributes(control, test, controlMap, testMap,
                listener);
        assertEquals(expected, listener.comparingWhat);
    }

    @Test
    public void testExtraComment() {
        testExtraComment(true);
        resetListener();

        properties.setIgnoreComments(true);
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);

        testExtraComment(false);
    }

    private void testExtraComment(boolean expectDifference) {
        Element control = document.createElement("foo");
        Element test = document.createElement("foo");
        Comment c = document.createComment("bar");
        control.appendChild(c);
        Element cChild = document.createElement("baz");
        control.appendChild(cChild);
        Element tChild = document.createElement("baz");
        test.appendChild(tChild);
        engine.compare(control, test, listener, null);
        assertEquals(expectDifference, listener.different);
        resetListener();
        engine.compare(test, control, listener, null);
        assertEquals(expectDifference, listener.different);
    }

    @Test
    public void testCommentContent() {
        testCommentContent(true);
        resetListener();

        properties.setIgnoreComments(true);
        engine = new DifferenceEngine(properties, PSEUDO_DIFF);

        testCommentContent(false);
    }

    private void testCommentContent(boolean expectDifference) {
        Element control = document.createElement("foo");
        Element test = document.createElement("foo");
        Comment c = document.createComment("bar");
        control.appendChild(c);
        Comment c2 = document.createComment("baz");
        test.appendChild(c2);
        engine.compare(control, test, listener, null);
        assertEquals(expectDifference, listener.different);
    }

    @Test
    public void testMissingSchemaLocation() throws Exception {
        testMissingXSIAttribute(XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR,
                DifferenceType.SCHEMA_LOCATION);
    }

    @Test
    public void testMissingNoNamespaceSchemaLocation() throws Exception {
        testMissingXSIAttribute(XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR,
                DifferenceType.NO_NAMESPACE_SCHEMA_LOCATION);
    }

    private void testMissingXSIAttribute(String attrName,
            DifferenceType expectedDifference)
            throws Exception {
        Element control = document.createElement("foo");
        control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                attrName, "bar");
        Element test = document.createElement("foo");
        engine.compare(control, test, listener, null);
        assertEquals(expectedDifference, listener.comparingWhat);
        resetListener();
        engine.compare(test, control, listener, null);
        assertEquals(expectedDifference, listener.comparingWhat);
    }

    @Test
    public void testDifferentSchemaLocation() throws Exception {
        testDifferentXSIAttribute(XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR,
                DifferenceType.SCHEMA_LOCATION);
    }

    @Test
    public void testDifferentNoNamespaceSchemaLocation() throws Exception {
        testDifferentXSIAttribute(XMLConstants
                .W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR,
                DifferenceType.NO_NAMESPACE_SCHEMA_LOCATION);
    }

    private void testDifferentXSIAttribute(String attrName,
            DifferenceType expectedDifference)
            throws Exception {
        Element control = document.createElement("foo");
        control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                attrName, "bar");
        Element test = document.createElement("foo");
        test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                attrName, "baz");
        engine.compare(control, test, listener, null);
        assertEquals(expectedDifference, listener.comparingWhat);
    }

    @Test
    public void testMissingAttribute() throws Exception {
        Element control = document.createElement("foo");
        control.setAttribute("bar", "baz");
        Element test = document.createElement("foo");
        test.setAttribute("baz", "bar");
        engine.compare(control, test, listener, null);
        assertEquals(DifferenceType.ATTR_NAME_NOT_FOUND, listener.comparingWhat);
    }

    @Test
    public void testMatchTrackerSetViaConstructor() throws Exception {
        Element control = document.createElement("foo");
        Element test = document.createElement("foo");
        final int[] count = new int[1];
        DifferenceEngine d =
                new DifferenceEngine(null, new SimpleComparisonController(),
                        new MatchTracker() {
                            public void matchFound(Difference d) {
                                count[0]++;
                            }
                        });
        d.compare(control, test, listener, null);
        // NODE_TYPE (not null), NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), ELEMENT_TAG_NAME(foo),
        // ELEMENT_NUM_ATTRIBUTE(none), HAS_CHILD_NODES(false)
        assertEquals(7, count[0]);
    }

    @Test
    public void testMatchTrackerSetViaSetter() throws Exception {
        Element control = document.createElement("foo");
        Element test = document.createElement("foo");
        final int[] count = new int[1];
        engine.setMatchTracker(new MatchTracker() {
            public void matchFound(Difference d) {
                count[0]++;
            }
        });
        engine.compare(control, test, listener, null);
        // NODE_TYPE (not null), NODE_TYPE(Element), NAMESPACE_URI(none),
        // NAMESPACE_PREFIX(none), ELEMENT_TAG_NAME(foo),
        // ELEMENT_NUM_ATTRIBUTE(none), HAS_CHILD_NODES(false)
        assertEquals(7, count[0]);
    }

    /**
     * @see http 
     *      ://sourceforge.net/forum/forum.php?thread_id=3284504&forum_id=73274
     */
    @Test
    public void testNamespaceAttributeDifferences() throws Exception {
        String control = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
                + "<ns0:Message xmlns:ns0 = \"http://mynamespace\">"
                + "<ns0:EventHeader>"
                + "<ns0:EventID>9999</ns0:EventID>"
                + "<ns0:MessageID>1243409665297</ns0:MessageID>"
                + "<ns0:MessageVersionID>1.0</ns0:MessageVersionID>"
                + "<ns0:EventName>TEST-EVENT</ns0:EventName>"
                + "<ns0:BWDomain>TEST</ns0:BWDomain>"
                + "<ns0:DateTimeStamp>2009-01-01T12:00:00</ns0:DateTimeStamp>"
                + "<ns0:SchemaPayloadRef>anything</ns0:SchemaPayloadRef>"
                + "<ns0:MessageURI>anything</ns0:MessageURI>"
                + "<ns0:ResendFlag>F</ns0:ResendFlag>"
                + "</ns0:EventHeader>"
                + "<ns0:EventBody>"
                + "<ns0:XMLContent>"
                + "<xyz:root xmlns:xyz=\"http://test.com/xyz\">"
                + "<xyz:test1>A</xyz:test1>"
                + "<xyz:test2>B</xyz:test2>"
                + "</xyz:root>"
                + "</ns0:XMLContent>"
                + "</ns0:EventBody>"
                + "</ns0:Message>";
        String test =
                "<abc:Message xmlns:abc=\"http://mynamespace\" xmlns:xyz=\"http://test.com/xyz\">"
                        + "<abc:EventHeader>"
                        + "<abc:EventID>9999</abc:EventID>"
                        + "<abc:MessageID>1243409665297</abc:MessageID>"
                        + "<abc:MessageVersionID>1.0</abc:MessageVersionID>"
                        + "<abc:EventName>TEST-EVENT</abc:EventName>"
                        + "<abc:BWDomain>TEST</abc:BWDomain>"
                        + "<abc:DateTimeStamp>2009-01-01T12:00:00</abc:DateTimeStamp>"
                        + "<abc:SchemaPayloadRef>anything</abc:SchemaPayloadRef>"
                        + "<abc:MessageURI>anything</abc:MessageURI>"
                        + "<abc:ResendFlag>F</abc:ResendFlag>"
                        + "</abc:EventHeader>"
                        + "<abc:EventBody>"
                        + "<abc:XMLContent>"
                        + "<xyz:root>"
                        + "<xyz:test1>A</xyz:test1>"
                        + "<xyz:test2>B</xyz:test2>"
                        + "</xyz:root>"
                        + "</abc:XMLContent>"
                        + "</abc:EventBody>"
                        + "</abc:Message>";
        listenToDifferences(control, test);
        assertFalse(listener.different);
    }

    /**
     * XMLUnit 1.3 jumps from the document node straight to the root element,
     * ignoring any other children the document might have. Some people consider
     * this a bug (Issue 2770386) others rely on it.
     * 
     * <p>
     * XMLUnit 2.x doesn't ignore differences in the prelude but we want to keep
     * the behavior for the legacy code base.
     * </p>
     */
    @Test
    public void testIgnoresDifferencesBetweenDocAndRootElement()
            throws Throwable {
        String control =
                "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
                        + "<!-- some comment -->"
                        + "<?foo some PI ?>"
                        + "<bar/>";
        String test = "<bar/>";
        listenToDifferences(control, test);
        assertFalse("unexpected difference: " + listener.comparingWhat,
                listener.different);
        resetListener();
        control =
                "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
                        + "<!-- some comment -->"
                        + "<?foo some PI ?>"
                        + "<bar/>";
        test =
                "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
                        + "<?foo some other PI ?>"
                        + "<!-- some other comment -->"
                        + "<bar/>";
        listenToDifferences(control, test);
        assertFalse("unexpected difference: " + listener.comparingWhat,
                listener.different);
    }

    private void listenToDifferences(String control, String test)
            throws SAXException, IOException {
        Document controlDoc = documentUtils.buildControlDocument(control);
        Document testDoc = documentUtils.buildTestDocument(test);
        engine.compare(controlDoc, testDoc, listener, DEFAULT_ELEMENT_QUALIFIER);
    }

    private void resetListener() {
        listener = new CollectingDifferenceListener();
    }

    private class CollectingDifferenceListener implements DifferenceListener {
        public String expected;
        public String actual;
        public Node control;
        public Node test;
        public DifferenceType comparingWhat = null;
        public boolean different = false;
        public boolean nodesSkipped = false;
        public String controlXpath;
        public String testXpath;
        private boolean tracing = false;

        public ReturnType differenceFound(Difference difference) {
            if (tracing) {
                System.out.println(difference.toString());
            }
            assertNotNull("difference not null", difference);
            assertNotNull("control node detail not null", difference.getControlNodeDetail());
            assertNotNull("test node detail not null", difference.getTestNodeDetail());
            this.expected = String.valueOf(difference.getControlNodeDetail().getValue());
            this.actual = String.valueOf(difference.getTestNodeDetail().getValue());
            this.control = difference.getControlNodeDetail().getTarget();
            this.test = difference.getTestNodeDetail().getTarget();
            this.comparingWhat = difference.getType();
            this.different = !difference.isRecoverable();
            this.controlXpath = difference.getControlNodeDetail().getXpath();
            this.testXpath = difference.getTestNodeDetail().getXpath();
            return ReturnType.ACCEPT_DIFFERENCE;
        }

        public void skippedComparison(Node control, Node test) {
            nodesSkipped = true;
        }

        public void setTrace(boolean active) {
            tracing = active;
        }
    }

    private class OrderPreservingNamedNodeMap implements NamedNodeMap {
        private final ArrayList/* Attr */nodes = new ArrayList();

        void add(Attr attr) {
            nodes.add(attr);
        }

        public int getLength() {
            return nodes.size();
        }

        public Node item(int index) {
            return (Node) nodes.get(index);
        }

        public Node getNamedItem(String name) {
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Attr a = (Attr) iter.next();
                if (a.getName().equals(name)) {
                    return a;
                }
            }
            return null;
        }

        public Node getNamedItemNS(String ns, String localName) {
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Attr a = (Attr) iter.next();
                if (a.getLocalName().equals(localName)
                        && a.getNamespaceURI().equals(ns)) {
                    return a;
                }
            }
            return null;
        }

        // not implemented, not needed in our case
        public Node removeNamedItem(String n) {
            return fail();
        }

        public Node removeNamedItemNS(String n1, String n2) {
            return fail();
        }

        public Node setNamedItem(Node n) {
            return fail();
        }

        public Node setNamedItemNS(Node n) {
            return fail();
        }

        private Node fail() {
            throw new RuntimeException("not implemented");
        }
    }
}
