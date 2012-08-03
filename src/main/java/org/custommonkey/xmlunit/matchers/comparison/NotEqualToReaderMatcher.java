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

import java.io.IOException;
import java.io.Reader;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.xml.sax.SAXException;

/**
 * Assert that two XML documents are not similar.
 * 
 */
public class NotEqualToReaderMatcher extends AbstractXmlUnitMatcher<Reader> {

    private Reader expectedReader;

    public NotEqualToReaderMatcher(Reader expectedReader) {
        this.expectedReader = expectedReader;
    }

    public void describeTo(Description description) {
        description.appendText("reader containing xml not equal to xml contained in given reader");
    }

    @Override
    protected void describeMismatchSafely(Reader item, Description mismatchDescription) {
        mismatchDescription.appendText("was reader containing xml equal to xml contained in given reader");
    }

    @Override
    public boolean matchesSafely(Reader actualReader) {
        XMLUnit xmlUnit = getXmlUnit();

        Diff diff;
        try {
            diff = xmlUnit.compareXML(expectedReader, actualReader);
        } catch (SAXException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        }
        return !diff.similar();
    }

    @Factory
    public static NotEqualToReaderMatcher notEqualToXmlReader(Reader expectedReader) {
        return new NotEqualToReaderMatcher(expectedReader);
    }

}
