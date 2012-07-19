package org.custommonkey.xmlunit.matchers.xpath;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert that the node lists of two Xpaths in two XML documents are equal
 */
public class XpathsEqualMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    private XpathWrapper expectedXpath;

    public XpathsEqualMatcher(XpathWrapper expectedXpath) {
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        description.appendText("Xpath equal to given xpath");

    }

    @Override
    public boolean matchesSafely(XpathWrapper actualXpath) {
        boolean equalXpaths;
        try {
            equalXpaths = equalXpaths(
                    expectedXpath.getXpath(),
                    expectedXpath.getDocument(),
                    actualXpath.getXpath(),
                    actualXpath.getDocument());
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Couldn't check xpath", e);
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }
        return equalXpaths;
    }

    @Factory
    public static XpathsEqualMatcher equalToXpath(XpathWrapper expectedXpath) {
        return new XpathsEqualMatcher(expectedXpath);
    }

}
