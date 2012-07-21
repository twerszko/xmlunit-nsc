//  Copyright 2012 Tomasz Werszko
//		
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//	
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.

package org.custommonkey.xmlunit.matchers.xpath;

import javax.xml.transform.TransformerException;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.util.DomUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert that the node lists of two Xpaths in two XML documents are equal
 */
public class XpathsEqualMatcher extends AbstractXpathEqualMatcher {

	private final XpathWrapper expectedXpath;

	public XpathsEqualMatcher(XpathWrapper expectedXpath) {
		this.expectedXpath = expectedXpath;
	}

	public void describeTo(Description description) {
		String actual;
		try {
			actual = DomUtils.documentToString(actualXpathDocument);
		} catch (TransformerException e) {
			actual = "";
		}
		description.appendText("something equal to " + quote(actual));
	}

	@Override
	protected void describeMismatchSafely(XpathWrapper item, Description description) {
		String expected;
		try {
			expected = DomUtils.documentToString(expectedXpathDocument);
		} catch (TransformerException e) {
			expected = "";
		}
		description.appendText("got" + quote(expected));
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
