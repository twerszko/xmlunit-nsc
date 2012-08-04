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

package org.custommonkey.xmlunit.matchers.comparison;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.XmlUnitException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Compares two XML documents end checks if these are not similar.
 */
public class NotEqualToXmlStringMatcher extends AbstractXmlUnitMatcher<String> {

	private final String expectedString;

	public NotEqualToXmlStringMatcher(String expectedString) {
		this.expectedString = expectedString;
	}

	public void describeTo(Description description) {
		description.appendText("Xml string not equal to " + quote(expectedString));
	}

	@Override
	protected void describeMismatchSafely(String item, Description mismatchDescription) {
		super.describeMismatchSafely(item, mismatchDescription);
	}

	@Override
	public boolean matchesSafely(String actualDoc) {
		XMLUnit xmlUnit = getXmlUnit();

		Diff diff;
		try {
			diff = xmlUnit.compareXML(expectedString, actualDoc);
		} catch (XmlUnitException e) {
			throw new IllegalArgumentException("Failed to compare documents.", e);
		}
		return !diff.similar();
	}

	@Factory
	public static NotEqualToXmlStringMatcher notEqualToXmlString(String expectedString) {
		return new NotEqualToXmlStringMatcher(expectedString);
	}

}
