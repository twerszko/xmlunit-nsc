package org.custommonkey.xmlunit.matchers.xpath;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert that the evaluation of two Xpaths in two documents are equal
 */
public class XpathValuesEqualMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    private XpathWrapper expectedXpath;

    public XpathValuesEqualMatcher(XpathWrapper expectedXpath) {
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        // TODO
    }

    @Override
    public boolean matchesSafely(XpathWrapper actualXpath) {
        XpathEngine xpath = XMLUnit.newXpathEngine();
        String expectedXpathValue;
        String actualXpathValue;
        try {
            expectedXpathValue = xpath.evaluate(expectedXpath.getXpath(), expectedXpath.getDocument());
            actualXpathValue = xpath.evaluate(actualXpath.getXpath(), actualXpath.getDocument());
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }

        return actualXpathValue.equals(expectedXpathValue);
    }

    @Factory
    public static XpathValuesEqualMatcher valueIsEqualToXpathValue(XpathWrapper expectedXpath) {
        return new XpathValuesEqualMatcher(expectedXpath);
    }

}
