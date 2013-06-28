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

import static junitparams.JUnitParamsRunner.$;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEngineFactory;

import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(JUnitParamsRunner.class)
public class ElementNameAndAttributeSelectorTest {
    private Document document;
    private ElementNameAndAttributeSelector elementNameAndAttributeQualifier;
    private static final String TAG_NAME = "qwerty";

    @Before
    public void setUp() throws Exception {
        document = new DocumentUtils(new XmlUnitProperties()).newControlDocumentBuilder().newDocument();
    }

    @Test
    @Parameters(method = "provideSelectors")
    public void should_cannot_compare_when_additional_attr(String attrName, ElementNameAndAttributeSelector selector) {
        // given
        Element control = document.createElement(TAG_NAME);
        control.setAttribute(attrName, "1");
        Element test = document.createElement(TAG_NAME);

        // when - then
        assertThat(selector.canBeCompared(control, test)).isFalse();
    }

    @Test
    @Parameters(method = "provideSelectors")
    public void should_can_compare_when_equal_attrs(String attrName, ElementNameAndAttributeSelector selector) {
        // given
        Element control = document.createElement(TAG_NAME);
        control.setAttribute(attrName, "1");
        Element test = document.createElement(TAG_NAME);
        test.setAttribute(attrName, "1");

        // when - then
        assertThat(selector.canBeCompared(control, test)).isTrue();
    }

    @Test
    @Parameters(method = "provideSelectors")
    public void should_cannot_compare_attrs_have_different_values(
            String attrName, ElementNameAndAttributeSelector selector) {
        // given
        Element control = document.createElement(TAG_NAME);
        control.setAttribute(attrName, "1");
        Element test = document.createElement(TAG_NAME);
        test.setAttribute(attrName, "2");

        // when - then
        assertThat(selector.canBeCompared(control, test)).isFalse();
    }

    @Test
    @Parameters(method = "provideSelectorsWithResult")
    public void should_return_correct_result_when_additional_attr_in_control(
            String attrName, ElementNameAndAttributeSelector selector, boolean expected) {
        // given
        Element control = document.createElement(TAG_NAME);
        control.setAttribute(attrName, "1");
        control.setAttribute("uiop", "true");
        Element test = document.createElement(TAG_NAME);
        test.setAttribute(attrName, "1");

        // when - then
        assertThat(selector.canBeCompared(control, test)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "provideSelectorsWithResult")
    public void should_return_correct_result_when_additional_attr_with_different_value_in_both_documents(
            String attrName, ElementNameAndAttributeSelector selector, boolean expected) {
        // given
        Element control = document.createElement(TAG_NAME);
        control.setAttribute(attrName, "1");
        control.setAttribute("uiop", "true");
        Element test = document.createElement(TAG_NAME);
        test.setAttribute(attrName, "1");
        test.setAttribute("uiop", "false");

        // when - then
        assertThat(selector.canBeCompared(control, test)).isEqualTo(expected);
    }

    Object[] provideSelectors() {
        String attrName = "id";
        return $(
                $(attrName, new ElementNameAndAttributeSelector()),
                $(attrName, new ElementNameAndAttributeSelector(attrName)));
    }

    Object[] provideSelectorsWithResult() {
        String attrName = "id";
        return $(
                $(attrName, new ElementNameAndAttributeSelector(), false),
                $(attrName, new ElementNameAndAttributeSelector(attrName), true));
    }

    @Test
    @Parameters(method = "provideSelectorsWithTwoAttrs")
    public void should_cannot_compare_when_missing_attrs_in_test(String[] attrNames,
            ElementNameAndAttributeSelector selector) {
        // given
        Element control = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            control.setAttribute(attrNames[i], "1");
        }
        Element test = document.createElement(TAG_NAME);

        // when - then
        assertThat(selector.canBeCompared(control, test)).isFalse();
    }

    @Test
    @Parameters(method = "provideSelectorsWithTwoAttrs")
    public void should_cannot_compare_when_different_values_of_on_of_attrs(String[] attrNames,
            ElementNameAndAttributeSelector selector) {
        // given
        Element control = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            control.setAttribute(attrNames[i], "1");
        }
        control.setAttribute(attrNames[0], "2");

        Element test = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            test.setAttribute(attrNames[i], "1");
        }

        // when - then
        assertThat(selector.canBeCompared(control, test)).isFalse();
    }

    @Test
    @Parameters(method = "provideSelectorsWithTwoAttrs")
    public void should_can_compare_when_equal_multiple_attrs(String[] attrNames,
            ElementNameAndAttributeSelector selector) {
        // given
        Element control = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            control.setAttribute(attrNames[i], "1");
        }
        Element test = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            test.setAttribute(attrNames[i], "1");
        }

        // when - then
        assertThat(selector.canBeCompared(control, test)).isTrue();
    }

    @Test
    @Parameters(method = "provideSelectorsWithTwoAttrsAndResult")
    public void should_return_correct_result_when_multiple_attrs_and_additional_attr_in_control(
            String[] attrNames, ElementNameAndAttributeSelector selector, boolean expected) {
        // given
        Element control = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            control.setAttribute(attrNames[i], "1");
        }
        control.setAttribute("oid", "0x2394b3456df");

        Element test = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            test.setAttribute(attrNames[i], "1");
        }

        // when - then
        assertThat(selector.canBeCompared(control, test)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "provideSelectorsWithTwoAttrsAndResult")
    public void should_return_correct_result_when_multiple_attrs_and_additional_attr_with_different_value_in_both_documents(
            String[] attrNames, ElementNameAndAttributeSelector selector, boolean expected) {
        // given
        Element control = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            control.setAttribute(attrNames[i], "1");
        }
        control.setAttribute("oid", "0x2394b3456df");

        Element test = document.createElement(TAG_NAME);
        for (int i = 0; i < attrNames.length; ++i) {
            test.setAttribute(attrNames[i], "1");
        }
        test.setAttribute("oid", "0xfd6543b4932");

        // when - then
        assertThat(selector.canBeCompared(control, test)).isEqualTo(expected);
    }

    Object[] provideSelectorsWithTwoAttrs() {
        String[] attrNames = { "id", "uid" };
        return $(
                $(attrNames, new ElementNameAndAttributeSelector()),
                $(attrNames, new ElementNameAndAttributeSelector(attrNames)));
    }

    Object[] provideSelectorsWithTwoAttrsAndResult() {
        String[] attrNames = { "id", "uid" };
        return $(
                $(attrNames, new ElementNameAndAttributeSelector(), false),
                $(attrNames, new ElementNameAndAttributeSelector(attrNames), true));
    }

    @Test
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
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isFalse();

        test.setAttributeNS(nsURI, attrName, "1");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isTrue();

        if (!matchesAllAttributes) {
            String otherNsURI = nsURI + "/2";
            test.setAttributeNS(otherNsURI, attrName, "2");
            assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isTrue();
        }

        control.setAttributeNS(nsURI, "uiop", "true");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isEqualTo(expectedValues[0]);

        test.setAttributeNS(nsURI, "uiop", "false");
        assertEquals("qwerty id 1 && uiop comparable to qwerty id 1 && !uiop", expectedValues[1],
                elementNameAndAttributeQualifier.canBeCompared(control, test));

        test.setAttributeNS(nsURI, attrName, "2");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isFalse();
    }

    // Bug 952920
    @Test
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
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isTrue();

        test.setAttribute("id", "1");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isTrue();

        control.setAttribute("id", "2");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isTrue();

        control.setAttribute("uid", "1");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isTrue();

        test.setAttribute("foo", "1");
        assertThat(elementNameAndAttributeQualifier.canBeCompared(control, test)).isFalse();
    }

    /**
     * @see https 
     *      ://sourceforge.net/forum/forum.php?thread_id=1135716&forum_id=73274l
     */
    @Test
    public void should_be_similar_when_element_order_different() throws Exception {
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
        DifferenceEngineFactory engineFactory = new DefaultDifferenceEngineFactory(new XmlUnitProperties());
        engineFactory.useSelector(new ElementNameAndAttributeSelector());

        Diff diff = Diff.newDiff(null)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .usingDifferenceEngineFactory(engineFactory)
                .build();

        // then
        assertThat(diff.identical()).isFalse();
        assertThat(diff.similar()).isTrue();
    }
}
