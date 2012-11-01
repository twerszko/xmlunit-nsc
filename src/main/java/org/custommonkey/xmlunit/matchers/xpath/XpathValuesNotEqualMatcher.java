package org.custommonkey.xmlunit.matchers.xpath;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert that the evaluation of two Xpaths in two documents are not equal
 */
public class XpathValuesNotEqualMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    private final XpathWrapper expectedXpath;
    private String expectedXpathValue;
    private String actualXpathValue;

    public XpathValuesNotEqualMatcher(XpathWrapper expectedXpath) {
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        description.appendText("xpath with value not equal to " + quote(expectedXpathValue));
    }

    @Override
    protected void describeMismatchSafely(XpathWrapper item, Description mismatchDescription) {
        super.describeMismatchSafely(item, mismatchDescription);
        mismatchDescription.appendText("xpath with value " + quote(actualXpathValue));
    }

    @Override
    public boolean matchesSafely(XpathWrapper actualXpath) {
        XpathEngine xpath = getXmlUnit().newDocumentUtils().newXpathEngine();
        try {
            expectedXpathValue = xpath.evaluate(expectedXpath.getXpath(), expectedXpath.getDocument());
            actualXpathValue = xpath.evaluate(actualXpath.getXpath(), actualXpath.getDocument());
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }

        return !actualXpathValue.equals(expectedXpathValue);
    }

    @Factory
    public static XpathValuesNotEqualMatcher notEqualToXpathValue(XpathWrapper expectedXpath) {
        return new XpathValuesNotEqualMatcher(expectedXpath);
    }

}
