package org.custommonkey.xmlunit;

import junit.framework.*;
import junit.textui.TestRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import java.io.FileReader;
import java.io.Reader;

/**
 * Test a Diff
 */
public class test_Diff extends TestCase{
    private static final String[] control = new String[]{
        "<test/>",
        "<test></test>",
        "<test>test</test>",
        "<test test=\"test\">test</test>",
        "<test/>",
        "<test>test</test>",
        "<test test=\"test\"/>",
        "<test><test><test></test></test></test>",
        "<test test=\"test\"><test>test<test>test</test></test></test>",
        "<test test=\"test\"><test>test<test>test</test></test></test>",
        "<html>Yo this is a test!</html>",
        "<java></java>"
        };
    private static final String[] test = new String[]{
        "<fail/>",
        "<fail/>",
        "<fail>test</fail>",
        "<test>test</test>",
        "<fail/>",
        "<test>fail</test>",
        "<test test=\"fail\"/>",
        "<test><test><test>test</test></test></test>",
        "<test test=\"test\"><test>fail<test>test</test></test></test>",
        "<test test=\"fail\"><test>test<test>test</test></test></test>",
        "<html>Yo this isn't a test!</html>",
        "<java><package-def><ident>org</ident><dot/><ident>apache</ident><dot/><ident>test</ident></package-def></java>"
        };
    private Document aDocument;

    public void setUp() throws Exception {
        aDocument = XMLUnit.getControlParser().newDocument();
    }

    public void testToString(){
        Diff diff = buildDiff(aDocument, aDocument);
        String[] animals = {"Monkey", "Chicken"};
        String tag = "tag";
        Element elemA = aDocument.createElement(tag);
        elemA.appendChild(aDocument.createTextNode(animals[0]));

        Element elemB = aDocument.createElement(tag);
        diff.differenceFound(Boolean.TRUE.toString(), Boolean.FALSE.toString(),
            elemA, elemB, DifferenceConstants.HAS_CHILD_NODES);

        assertEquals(diff.getClass().getName() +"\n[different] Expected "
            + DifferenceConstants.HAS_CHILD_NODES.getDescription()
            + " 'true' but was 'false' - comparing <tag...> to <tag...>",
            diff.toString());

        diff = buildDiff(aDocument, aDocument);
        elemB.appendChild(aDocument.createTextNode(animals[1]));
        diff.differenceFound(animals[0], animals[1], elemA, elemB,
            DifferenceConstants.TEXT_VALUE);

        assertEquals(diff.getClass().getName() +"\n[different] Expected "
            + DifferenceConstants.TEXT_VALUE.getDescription()
            + " '" + animals[0] + "' but was '" + animals[1]
            + "' - comparing <tag...> to <tag...>",
            diff.toString());

    }

    /**
     * Tests the compare method
     */
    public void testSimilar() throws Exception {
        for(int i=0;i<control.length;i++){
            assertEquals("XMLUnit.compare().similar() test case "+i+" failed",
                true, buildDiff(control[i], control[i]).similar());
            assertEquals("!XMLUnit.compare().similar() test case "+i+" failed",
                false, (buildDiff(control[i], test[i])).similar());
        }
    }

    /**
     * Tests the compare method
     */
    public void testIdentical() throws Exception {
        String control="<control><test>test1</test><test>test2</test></control>";
        String test="<control><test>test2</test><test>test1</test></control>";

        assertEquals("Documents are identical, when they are not", false,
            buildDiff(control, test).identical());
    }

    /**
     * Tests the compare method
     */
    public void testFiles() throws Exception {
        FileReader control = new FileReader(test_Constants.BASEDIR
            + "/tests/etc/test.blame.html");
        FileReader test = new FileReader(test_Constants.BASEDIR
            + "/tests/etc/test.blame.html");
        Diff diff = buildDiff(control, test);
        assertEquals(diff.toString(), true, diff.identical());
    }

    public void testSameTwoStrings() throws Exception {
        Diff diff = buildDiff("<same>pass</same>", "<same>pass</same>");
        assertEquals("same should be identical", true, diff.identical());
        assertEquals("same should be similar", true, diff.similar());
    }

    public void testMissingElement() throws Exception {
        Diff diff = buildDiff("<root></root>", "<root><node/></root>");
        assertEquals("should not be identical", false, diff.identical());
        assertEquals("and should not be similar", false, diff.similar());
    }

    public void testExtraElement() throws Exception {
        Diff diff = buildDiff("<root><node/></root>", "<root></root>");
        assertEquals("should not be identical", false, diff.identical());
        assertEquals("and should not be similar", false, diff.similar());
    }

    public void testElementsInReverseOrder() throws Exception {
        Diff diff = buildDiff("<root><same/><pass/></root>", "<root><pass/><same/></root>");
        assertEquals("should not be identical", false, diff.identical());
        assertEquals("but should be similar", true, diff.similar());
    }

    public void testMissingAttribute() throws Exception {
        Diff diff = buildDiff("<same>pass</same>", "<same except=\"this\">pass</same>");
        assertEquals("should not be identical", false, diff.identical());
        assertEquals("and should not be similar", false, diff.similar());
    }

    public void testExtraAttribute() throws Exception {
        Diff diff = buildDiff("<same except=\"this\">pass</same>", "<same>pass</same>");
        assertEquals("should not be identical", false, diff.identical());
        assertEquals("and should not be similar", false, diff.similar());
    }

    public void testAttributesInReverseOrder() throws Exception {
        Diff diff = buildDiff("<same zzz=\"qwerty\" aaa=\"uiop\">pass</same>",
            "<same aaa=\"uiop\" zzz=\"qwerty\">pass</same>" );
        if (diff.identical()) {
            System.out.println(getName() + " - should not ideally be identical "
                + "but JAXP implementations can reorder attributes inside NamedNodeMap");
        }
        assertEquals(diff.toString() + ": but should be similar",
            true, diff.similar());
    }

    public void testDiffStringWithAttributes() throws Exception {
        final String fruitBat = "<bat type=\"fruit\"/>",
            longEaredBat = "<bat type=\"longeared\"/>";
        Diff diff = buildDiff(fruitBat, longEaredBat);
        assertEquals(diff.getClass().getName() +"\n[different] Expected "
            + DifferenceConstants.ATTR_VALUE.getDescription()
            + " 'fruit' but was 'longeared' - comparing <bat type=\"fruit\"...> to <bat type=\"longeared\"...>",
            diff.toString());
    }

    /**
     * Raised by aakture 25.04.2002
     * Despite the name under which this defect was raised the issue is really
     * about managing redundant whitespace
     */
    public void testXMLUnitDoesNotWorkWellWithFiles() throws Exception {
        // to avoid test sequencing issues we need to restore whitespace setting
        boolean startValueIgnoreWhitespace = XMLUnit.getIgnoreWhitespace();
        try {
            XMLUnit.setIgnoreWhitespace(false);
            Diff whitespaceAwareDiff = buildDiff(test_Constants.XML_WITHOUT_WHITESPACE,
                test_Constants.XML_WITH_WHITESPACE);
            assertTrue(whitespaceAwareDiff.toString(),
                !whitespaceAwareDiff.similar());

            XMLUnit.setIgnoreWhitespace(true);
            Diff whitespaceIgnoredDiff = buildDiff(test_Constants.XML_WITHOUT_WHITESPACE,
                test_Constants.XML_WITH_WHITESPACE);
            assertTrue(whitespaceIgnoredDiff.toString(),
                whitespaceIgnoredDiff.similar());
        } finally {
            XMLUnit.setIgnoreWhitespace(startValueIgnoreWhitespace);
        }
    }

    /**
     * Raised 15.05.2002
     */
    public void testNamespaceIssues() throws Exception {
        String control = "<control:abc xmlns:control=\"http://yada.com\">"
            + "<control:xyz>text</control:xyz></control:abc>";
        Replacement replace = new Replacement("control", "test");
        String test = replace.replace(control);

        Diff diff = buildDiff(control, test);
        assertTrue("a-"+diff.toString(), diff.similar());
        assertTrue("b-"+diff.toString(), !diff.identical());

        Diff reverseDiff = buildDiff(test, control);
        assertTrue("c-"+reverseDiff.toString(), reverseDiff.similar());
        assertTrue("d-"+reverseDiff.toString(), !reverseDiff.identical());
    }

    /**
     * Raised 16.05.2002
     */
    public void testDefaultNamespace() throws Exception {
        String control = "<control:abc xmlns:control=\"http://yada.com\">"
            + "<control:xyz>text</control:xyz></control:abc>";
        Replacement replace = new Replacement("control:", "");
        Replacement trim = new Replacement("xmlns:control", "xmlns");
        String test = trim.replace(replace.replace(control));

        Diff diff = buildDiff(control, test);
        assertTrue("a-"+diff.toString(), diff.similar());
        assertTrue("b-"+diff.toString(), !diff.identical());

        Diff reverseDiff = buildDiff(test, control);
        assertTrue("c-"+reverseDiff.toString(), reverseDiff.similar());
        assertTrue("d-"+reverseDiff.toString(), !reverseDiff.identical());
    }

    public void testSameNameDifferentQName() throws Exception {
        String control = "<ns1:root xmlns:ns1=\"http://example.org/ns1\" xmlns:ns2=\"http://example.org/ns2\">"
            + "<ns1:branch>In namespace 1</ns1:branch>"
            + "<ns2:branch>In namespace 2</ns2:branch>"
            + "</ns1:root>";

        String test = "<ns1:root xmlns:ns1=\"http://example.org/ns1\" xmlns:ns2=\"http://example.org/ns2\">"
            + "<ns2:branch>In namespace 2</ns2:branch>"
            + "<ns1:branch>In namespace 1</ns1:branch>"
            + "</ns1:root>";

        Diff diff = buildDiff(control, test);
        assertTrue("a-"+diff.toString(), diff.similar());
        assertTrue("b-"+diff.toString(), !diff.identical());

        Diff reverseDiff = buildDiff(test, control);
        assertTrue("c-"+reverseDiff.toString(), reverseDiff.similar());
        assertTrue("d-"+reverseDiff.toString(), !reverseDiff.identical());
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

    /**
     * Construct a test
     * @param name Test name
     */
    public test_Diff(String name){
        super(name);
    }


    /**
     * Handy dandy main method to run this suite with text-based TestRunner
     */
    public static void main(String[] args) {
        new TestRunner().run(suite());
    }

    /**
     * Return the test suite containing this test
     */
    public static TestSuite suite(){
        return new TestSuite(test_Diff.class);
    }
}
