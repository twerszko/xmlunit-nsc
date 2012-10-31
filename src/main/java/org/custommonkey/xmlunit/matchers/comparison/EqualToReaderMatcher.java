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

import java.io.Reader;

import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.XmlUnitException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Assert that two XML documents are similar.
 * 
 */
public class EqualToReaderMatcher extends AbstractXmlUnitMatcher<Reader> {

    private final Reader expectedReader;

    public EqualToReaderMatcher(Reader expectedReader) {
        this.expectedReader = expectedReader;
    }

    public void describeTo(Description description) {
        description.appendText("reader containing xml equal to xml contained in given reader");
    }

    @Override
    protected void describeMismatchSafely(Reader item, Description mismatchDescription) {
        mismatchDescription.appendText("was reader containing xml not equal to xml contained in given reader");
    }

    @Override
    public boolean matchesSafely(Reader actualReader) {
        XmlUnit xmlUnit = getXmlUnit();

        Diff diff;
        try {
            diff = xmlUnit.compareXML(expectedReader, actualReader);
        } catch (XmlUnitException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        }
        return diff.similar();
    }

    @Factory
    public static EqualToReaderMatcher equalToXmlReader(Reader expectedReader) {
        return new EqualToReaderMatcher(expectedReader);
    }

}
