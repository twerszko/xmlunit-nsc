/*
 ******************************************************************
Copyright (c) 2001, Jeff Martin, Tim Bacon
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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JUnit testcase for ElementNameAndAttributeQualifier
 * 
 * @see test_Diff#testRepeatedElementNamesWithAttributeQualification()
 */
public class test_ElementNameAndAttributeQualifier extends TestCase {
    private Document document;
    private ElementNameAndAttributeSelector elementNameAndAttributeQualifier;
    private static final String TAG_NAME = "qwerty";

    public void testSingleQualifyingAttribute() throws Exception {
        final String attrName = "id";

        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector();
        testAssertionsFor(attrName, new boolean[] { false, false });

        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector(attrName);
        testAssertionsFor(attrName, new boolean[] { true, true });
    }

    private void testAssertionsFor(String attrName, boolean[] expectedValues) throws Exception {
        Element control = document.createElement(TAG_NAME);
        control.setAttribute(attrName, "1");

        Element test = document.createElement(TAG_NAME);
        assertFalse("qwerty id 1 not comparable to qwerty with no attributes",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute(attrName, "1");
        assertTrue("qwerty id 1 comparable to qwerty id 1",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        control.setAttribute("uiop", "true");
        assertEquals("qwerty id 1 && uiop comparable to qwerty id 1", expectedValues[0],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute("uiop", "false");
        assertEquals("qwerty id 1 && uiop comparable to qwerty id 1 && !uiop", expectedValues[1],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute(attrName, "2");
        assertFalse("qwerty id 1 && uiop NOT comparable to qwerty id 2 && !uiop",
                elementNameAndAttributeQualifier.canBeCompared(control, test));
    }

    public void testMultipleQualifyingAttributes() throws Exception {
        final String[] attrNames = { "id", "uid" };

        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector();
        testAssertionsFor(attrNames, new boolean[] { false, false });

        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector(attrNames);
        testAssertionsFor(attrNames, new boolean[] { true, true });
    }

    private void testAssertionsFor(String[] attrNames, boolean[] expectedValues) throws Exception {
        Element control = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            control.setAttribute(attrNames[i], "1");
        }

        Element test = document.createElement(TAG_NAME);
        assertFalse("qwerty id/uid 1 not comparable to qwerty with no attributes",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        for (int i = 0; i < attrNames.length; ++i) {
            test.setAttribute(attrNames[i], "1");
        }

        assertTrue("qwerty id/uid 1 comparable to qwerty id/uid 1",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        control.setAttribute("oid", "0x2394b3456df");
        assertEquals("qwerty id/uid 1 with oid comparable to qwerty id/uid 1",
                expectedValues[0],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute("oid", "0xfd6543b4932");
        assertEquals("qwerty id/uid 1 with oid comparable to qwerty id/uid 1 with different oid",
                expectedValues[1],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute(attrNames[0], "2");
        assertFalse("qwerty id/uid 1 not comparable to qwerty id 2 /uid 1",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute(attrNames[0], "1");
        test.setAttribute(attrNames[1], "2");
        assertFalse("qwerty id/uid 1 not comparable to qwerty id 1 /uid 2",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute(attrNames[0], "2");
        assertFalse("qwerty id/uid 1 not comparable to qwerty id/uid 2",
                elementNameAndAttributeQualifier.canBeCompared(control, test));
    }

    public void testNamespacedQualifyingAttribute() throws Exception {
        final String attrName = "id";
        final String nsURI = "http://xmlunit.sourceforge.net/tests";

        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector();
        testAssertionsFor(attrName, nsURI, new boolean[] { false, false }, true);

        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector(attrName);
        testAssertionsFor(attrName, nsURI, new boolean[] { true, true }, false);
    }

    private void testAssertionsFor(String attrName, String nsURI,
            boolean[] expectedValues,
            boolean matchesAllAttributes)
            throws Exception {
        Element control = document.createElement(TAG_NAME);
        control.setAttributeNS(nsURI, attrName, "1");

        Element test = document.createElement(TAG_NAME);
        assertFalse("qwerty id 1 not comparable to qwerty with no attributes",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttributeNS(nsURI, attrName, "1");
        assertTrue("qwerty id 1 comparable to qwerty id 1",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        if (!matchesAllAttributes) {
            String otherNsURI = nsURI + "/2";
            test.setAttributeNS(otherNsURI, attrName, "2");
            assertTrue("qwerty id 1 comparable to qwerty id 1 and other-NS id 2",
                    elementNameAndAttributeQualifier.canBeCompared(control, test));
        }

        control.setAttributeNS(nsURI, "uiop", "true");
        assertEquals("qwerty id 1 && uiop comparable to qwerty id 1", expectedValues[0],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttributeNS(nsURI, "uiop", "false");
        assertEquals("qwerty id 1 && uiop comparable to qwerty id 1 && !uiop", expectedValues[1],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttributeNS(nsURI, attrName, "2");
        assertFalse("qwerty id 1 && uiop NOT comparable to qwerty id 2 && !uiop",
                elementNameAndAttributeQualifier.canBeCompared(control, test));
    }

    // Bug 952920
    public void testQualifyingAttributeMissingInControl() throws Exception {
        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector("foo");
        assertQualifyingAttributeMissingInControl();
        elementNameAndAttributeQualifier = new ElementNameAndAttributeSelector(new String[] { "foo", "bar" });
        assertQualifyingAttributeMissingInControl();
    }

    private void assertQualifyingAttributeMissingInControl()
            throws Exception {
        Element control = document.createElement(TAG_NAME);

        Element test = document.createElement(TAG_NAME);
        assertTrue("empty elements match",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute("id", "1");
        assertTrue("extra attribute on test matches",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        control.setAttribute("id", "2");
        assertTrue("differerent values for extra attribute still matches",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        control.setAttribute("uid", "1");
        assertTrue("extra attribute on control matches",
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttribute("foo", "1");
        assertFalse("no match if attribute is present in test",
                elementNameAndAttributeQualifier.canBeCompared(control, test));
    }

    /**
     * @see https 
     *      ://sourceforge.net/forum/forum.php?thread_id=1135716&forum_id=73274l
     */
    public void testHelpForumThread1135716() throws Exception {
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
        Diff d = Diff.newDiff(null)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
        assertFalse(d.similar());

        // reset
        d = Diff.newDiff(null)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingElementSelector(new ElementNameAndAttributeSelector())
                .build();
        assertTrue(d.similar());
    }

    @Override
    public void setUp() throws Exception {
        document = new DocumentUtils(new XmlUnitProperties()).newControlDocumentBuilder().newDocument();
    }

    public static TestSuite suite() {
        return new TestSuite(test_ElementNameAndAttributeQualifier.class);
    }

    /**
     * Constructor for test_ElementNameAndAttributeQualifier.
     * 
     * @param name
     */
    public test_ElementNameAndAttributeQualifier(String name) {
        super(name);
    }

}
