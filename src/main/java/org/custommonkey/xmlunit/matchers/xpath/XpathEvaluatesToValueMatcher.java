package org.custommonkey.xmlunit.matchers.xpath;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert the value of an Xpath expression in an XML String
 */
// TODO: find better name
public class XpathEvaluatesToValueMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    private String expectedXpathValue;

    public XpathEvaluatesToValueMatcher(String expectedXpathValue) {
        this.expectedXpathValue = expectedXpathValue;
    }

    public void describeTo(Description description) {
        // TODO
    }

    @Override
    public boolean matchesSafely(XpathWrapper actualXpath) {
        XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
        String evaluatedValue;
        try {
            evaluatedValue = simpleXpathEngine.evaluate(actualXpath.getXpath(), actualXpath.getDocument());
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }
        return evaluatedValue.equals(expectedXpathValue);
    }

    @Factory
    public static XpathEvaluatesToValueMatcher xpathEvaluatesTo(String expectedXpathValue) {
        return new XpathEvaluatesToValueMatcher(expectedXpathValue);
    }

}
