package org.custommonkey.xmlunit;

import junit.framework.*;
import junit.textui.TestRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import org.w3c.dom.Document;

/**
 * JUnit test for Validator
 * Also includes tests for XMLTestCase <code>assertValidXML</code> methods
 * because test values etc are here
 */
public class test_Validator extends XMLTestCase {
    private Validator validator;
    private File tempDTDFile;

    public void testIsValidGood() throws Exception {
        String toonXML = test_Constants.XML_DECLARATION
             + test_Constants.CHUCK_JONES_RIP_DTD_DECL
             + test_Constants.CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(toonXML));
        assertEquals("toonXML " + validator.toString(),
            true, validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(toonXML);
        passXMLTestCaseTest(validator);

        String noXMLDeclaration = test_Constants.CHUCK_JONES_RIP_DTD_DECL
             + test_Constants.CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(noXMLDeclaration));
        assertEquals("noXMLDeclaration " + validator.toString(),
            true, validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(noXMLDeclaration);
        passXMLTestCaseTest(validator);
    }

    public void testIsValidExternalSystemId() throws Exception {
        writeTempDTDFile();
        assertEquals(tempDTDFile.getAbsolutePath(), true, tempDTDFile.exists());

        String externalDTD = test_Constants.XML_DECLARATION
             + test_Constants.DOCUMENT_WITH_GOOD_EXTERNAL_DTD;
        validator = new Validator(new StringReader(externalDTD),
            tempDTDFile.toURL().toExternalForm());

        assertEquals("externalDTD " + validator.toString(),
            true, validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(externalDTD, tempDTDFile.toURL().toExternalForm());
        passXMLTestCaseTest(validator);

        String noDTD = test_Constants.XML_DECLARATION
             + test_Constants.DOCUMENT_WITH_NO_EXTERNAL_DTD;
        validator = new Validator(new StringReader(noDTD),
            tempDTDFile.toURL().toExternalForm());

        assertEquals("noDTD " + validator.toString(),
            false, validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(noDTD, tempDTDFile.toURL().toExternalForm());
        failXMLTestCaseTest(validator);
    }

    public void testIsValidNoDTD() throws Exception {
        writeTempDTDFile();
        assertEquals(tempDTDFile.getAbsolutePath(), true, tempDTDFile.exists());

        String noDTD = test_Constants.CHUCK_JONES_RIP_XML;
        String systemid = tempDTDFile.toURL().toExternalForm();
        String doctype = "cartoons";
        String notDoctype = "anima";
        validator = new Validator(new StringReader(noDTD),
            systemid, doctype);
        assertEquals(validator.toString(), true, validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(noDTD, systemid, doctype);
        passXMLTestCaseTest(validator);

        validator = new Validator(new StringReader(noDTD),
            systemid, notDoctype);
        assertEquals(validator.toString(), false, validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(noDTD, systemid, notDoctype);
        failXMLTestCaseTest(validator);

        Document document = getDocument(noDTD);
        validator = new Validator(document, systemid, doctype);
        assertEquals("Document " + validator.toString(),
            true, validator.isValid());
        // test XMLTestCase
        passXMLTestCaseTest(validator);

        validator = new Validator(document, systemid, notDoctype);
        assertEquals("Document " + validator.toString(),
            false, validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(validator);
    }

    public void testIsValidBad() throws Exception {
        String noDTD = test_Constants.XML_DECLARATION
             + test_Constants.CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(noDTD));
        assertEquals("noDTD " + validator.toString(),
            false, validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(validator);

        String dtdTwice = test_Constants.XML_DECLARATION
             + test_Constants.CHUCK_JONES_RIP_DTD_DECL
             + test_Constants.CHUCK_JONES_RIP_DTD_DECL
             + test_Constants.CHUCK_JONES_RIP_XML;
        validator = new Validator(new StringReader(dtdTwice));
        assertEquals("dtdTwice " + validator.toString(),
            false, validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(validator);

        String invalidXML = test_Constants.XML_DECLARATION
             + test_Constants.CHUCK_JONES_RIP_DTD_DECL
             + test_Constants.CHUCK_JONES_SPINNING_IN_HIS_GRAVE_XML;
        validator = new Validator(new StringReader(invalidXML));
        assertEquals("invalidXML " + validator.toString(),
            false, validator.isValid());
        // test XMLTestCase
        failXMLTestCaseTest(validator);
    }

    private Document getDocument(String fromXML) throws Exception {
        return XMLUnit.buildControlDocument(fromXML);
    }

    private void removeTempDTDFile() throws Exception {
        if (tempDTDFile.exists()) {
            tempDTDFile.delete();
        }
    }

    private void writeTempDTDFile() throws Exception {
        FileWriter writer = new FileWriter(tempDTDFile);
        writer.write(test_Constants.CHUCK_JONES_RIP_DTD);
        writer.close();
    }

    public void setUp() throws Exception {
        tempDTDFile = new File(test_Constants.EXTERNAL_DTD);
        removeTempDTDFile();
    }

    public void tearDown() throws Exception {
        removeTempDTDFile();
    }

    // ---- XMLTestCase methods ----
    private void passXMLTestCaseTest(String xml) throws Exception {
        assertXMLValid(xml);
    }
    private void passXMLTestCaseTest(String xml, String systemId) throws Exception {
        assertXMLValid(xml, systemId);
    }
    private void passXMLTestCaseTest(String xml, String systemId, String doctype)
    throws Exception {
        assertXMLValid(xml, systemId, doctype);
    }
    private void passXMLTestCaseTest(Validator validator) throws Exception {
        assertXMLValid(validator);
    }
    private void failXMLTestCaseTest(String xml, String systemId) throws Exception {
        try {
            assertXMLValid(xml, systemId);
            fail("Expected assertion to fail!");
        } catch (AssertionFailedError e) {
            // expecting this
        }
    }
    private void failXMLTestCaseTest(String xml, String systemId, String doctype)
    throws Exception {
        try {
            assertXMLValid(xml, systemId, doctype);
            fail("Expected assertion to fail!");
        } catch (AssertionFailedError e) {
            // expecting this
        }
    }
    private void failXMLTestCaseTest(Validator validator) throws Exception {
        try {
            assertXMLValid(validator);
            fail("Expected assertion to fail!");
        } catch (AssertionFailedError e) {
            // expecting this
        }
    }

    public test_Validator(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(test_Validator.class);
    }

    public static void main(String[] args) {
        new TestRunner().run(suite());
    }
}
