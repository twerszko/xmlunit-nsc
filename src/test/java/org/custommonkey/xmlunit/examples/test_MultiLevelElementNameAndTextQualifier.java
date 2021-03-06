/*
 ******************************************************************
Copyright (c) 2007, Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit.examples;

import junit.framework.TestCase;
import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEngineFactory;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

/**
 * JUnit testcase for MultiLevelElementNameAndTextQualifier
 * 
 * @see test_Diff#testRepeatedElementNamesWithTextQualification()
 */
public class test_MultiLevelElementNameAndTextQualifier extends TestCase {
    private static final String TAG_NAME = "tagYoureIt";
    private static final String TAG_NAME2 = "tagYoureIt2";
    private static final String TEXT_A = "textA";
    private static final String TEXT_B = "textB";

    private Document document;
    private XmlUnitProperties properties;
    private DifferenceEngineFactory engineFactory;

    @Override
    public void setUp() throws Exception {
        properties = new XmlUnitProperties();
        document = new DocumentUtils(properties).newControlDocumentBuilder().newDocument();
        engineFactory = new DefaultDifferenceEngineFactory(properties);
    }

    // copy of ElementNameAndTextQualifier test
    public void testSingleTextValue() throws Exception {
        ElementSelector qualifier =
                new MultiLevelElementNameAndTextSelector(1);

        Element control = document.createElement(TAG_NAME);
        control.appendChild(document.createTextNode(TEXT_A));

        Element test = document.createElement(TAG_NAME);

        assertFalse("control text not comparable to empty text",
                qualifier.canBeCompared(control, test));

        test.appendChild(document.createTextNode(TEXT_A));
        assertTrue("control textA comparable to test textA",
                qualifier.canBeCompared(control, test));

        test = document.createElement(TAG_NAME);

        test.appendChild(document.createTextNode(TEXT_B));
        assertFalse("control textA not comparable to test textB",
                qualifier.canBeCompared(control, test));
    }

    // copy of ElementNameAndTextQualifier test
    public void testMultipleTextValues() throws Exception {
        ElementSelector qualifier =
                new MultiLevelElementNameAndTextSelector(1);

        Element control = document.createElement(TAG_NAME);
        control.appendChild(document.createTextNode(TEXT_A));
        control.appendChild(document.createTextNode(TEXT_B));

        Element test = document.createElement(TAG_NAME);
        test.appendChild(document.createTextNode(TEXT_A + TEXT_B));
        assertTrue("denormalised control text comparable to normalised test text",
                qualifier.canBeCompared(control, test));
    }

    // three levels
    public void testThreeLevels() throws Exception {
        ElementSelector qualifier =
                new MultiLevelElementNameAndTextSelector(3);

        Element control = document.createElement(TAG_NAME);
        Element child = document.createElement(TAG_NAME2);
        control.appendChild(child);
        Element child2 = document.createElement(TAG_NAME);
        child.appendChild(child2);
        child2.appendChild(document.createTextNode(TEXT_B));

        Element test = document.createElement(TAG_NAME);
        child = document.createElement(TAG_NAME2);
        test.appendChild(child);
        child2 = document.createElement(TAG_NAME);
        child.appendChild(child2);
        child2.appendChild(document.createTextNode(TEXT_B));

        assertTrue(qualifier.canBeCompared(control, test));
    }

    /**
     * @see https 
     *      ://sourceforge.net/forum/forum.php?thread_id=1440169&forum_id=73274
     */
    public void testThread1440169() throws Exception {
        String s1 = "<a><b><c>foo</c></b><b><c>bar</c></b></a>";
        String s2 = "<a><b><c>bar</c></b><b><c>foo</c></b></a>";
        Diff d = Diff.newDiff(properties)
                .betweenControlDocument(s1)
                .andTestDocument(s2)
                .build();
        assertFalse(d.similar());

        // reset
        engineFactory.useSelector(ElementSelectors.byNameAndText);
        d = Diff.newDiff(properties)
                .betweenControlDocument(s1)
                .andTestDocument(s2)
                .usingDifferenceEngineFactory(engineFactory)
                .build();
        assertFalse(d.similar());

        // reset once again
        engineFactory.useSelector(new MultiLevelElementNameAndTextSelector(2));
        d = Diff.newDiff(properties)
                .betweenControlDocument(s1)
                .andTestDocument(s2)
                .usingDifferenceEngineFactory(engineFactory)
                .build();
        assertTrue(d.similar());

    }

    public void testUserGuideExample() throws Exception {
        String control =
                "<table>\n"
                        + "  <tr>\n"
                        + "    <td>foo</td>\n"
                        + "  </tr>\n"
                        + "  <tr>\n"
                        + "    <td>bar</td>\n"
                        + "  </tr>\n"
                        + "</table>\n";
        String test =
                "<table>\n"
                        + "  <tr>\n"
                        + "    <td>bar</td>\n"
                        + "  </tr>\n"
                        + "  <tr>\n"
                        + "    <td>foo</td>\n"
                        + "  </tr>\n"
                        + "</table>\n";

        engineFactory.useSelector(new MultiLevelElementNameAndTextSelector(2));
        Diff d = Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();
        assertFalse(d.toString(), d.similar());

        // TODO Diff builder knows about properties
        engineFactory.useSelector(new MultiLevelElementNameAndTextSelector(2));
        properties.setIgnoreWhitespace(true);
        d = Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();
        assertTrue(d.toString(), d.similar());
        properties.setIgnoreWhitespace(false);

        engineFactory.useSelector(new MultiLevelElementNameAndTextSelector(2, true));
        d = Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();
        assertTrue(d.toString(), d.similar());
    }
}
