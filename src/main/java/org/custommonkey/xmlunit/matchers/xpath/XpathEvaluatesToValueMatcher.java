//  Copyright 2012 Tomasz Werszko
//      
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.custommonkey.xmlunit.matchers.xpath;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert the value of an Xpath expression in an XML document.
 */
public class XpathEvaluatesToValueMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    private final String expectedXpathValue;
    private String actualEvalueatedXpathValue;

    public XpathEvaluatesToValueMatcher(String expectedXpathValue) {
        if (expectedXpathValue == null || expectedXpathValue.isEmpty()) {
            throw new IllegalArgumentException("Expected value cannot be null or empty!");
        }
        this.expectedXpathValue = expectedXpathValue;
    }

    public void describeTo(Description description) {
        description.appendText(quote(expectedXpathValue));
    }

    @Override
    protected void describeMismatchSafely(XpathWrapper item, Description description) {
        description.appendText(quote(actualEvalueatedXpathValue));
    }

    @Override
    public boolean matchesSafely(XpathWrapper actualXpath) {
        if (actualXpath == null) {
            throw new IllegalArgumentException("Actual xpath cannot be null or empty!");
        }
        XpathEngine simpleXpathEngine = getXmlUnit().newXpathEngine();
        try {
            actualEvalueatedXpathValue = simpleXpathEngine.evaluate(actualXpath.getXpath(), actualXpath.getDocument());
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }
        return actualEvalueatedXpathValue.equals(expectedXpathValue);
    }

    @Factory
    public static XpathEvaluatesToValueMatcher xpathEvaluatesTo(String expectedXpathValue) {
        return new XpathEvaluatesToValueMatcher(expectedXpathValue);
    }

}
