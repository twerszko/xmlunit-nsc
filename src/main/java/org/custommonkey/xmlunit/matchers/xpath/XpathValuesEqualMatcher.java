/*      
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.custommonkey.xmlunit.matchers.xpath;

import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.xpath.XpathEngine;
import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert that the evaluation of two Xpaths in two documents are equal
 */
public class XpathValuesEqualMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    private final XpathWrapper expectedXpath;
    private String expectedXpathValue;
    private String actualXpathValue;

    public XpathValuesEqualMatcher(XpathWrapper expectedXpath) {
        if (expectedXpath == null) {
            throw new IllegalArgumentException("Expected xpath cannot be null!");
        }
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        description.appendText("xpath with value equal to " + quote(expectedXpathValue));
    }

    @Override
    protected void describeMismatchSafely(XpathWrapper item, Description mismatchDescription) {
        super.describeMismatchSafely(item, mismatchDescription);
        mismatchDescription.appendText("xpath with value " + quote(actualXpathValue));
    }

    @Override
    public boolean matchesSafely(XpathWrapper actualXpath) {
        if (actualXpath == null) {
            throw new IllegalArgumentException("Actual xpath cannot be null!");
        }
        XpathEngine xpath = getXmlUnit().newDocumentUtils().newXpathEngine();
        try {
            expectedXpathValue = xpath.evaluate(
                    expectedXpath.getXpath(), new DOMSource(expectedXpath.getDocument()));
            actualXpathValue = xpath.evaluate(
                    actualXpath.getXpath(), new DOMSource(actualXpath.getDocument()));
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }

        return actualXpathValue.equals(expectedXpathValue);
    }

    @Factory
    public static XpathValuesEqualMatcher equalToXpathValueOf(XpathWrapper expectedXpath) {
        return new XpathValuesEqualMatcher(expectedXpath);
    }

}
