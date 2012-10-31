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

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.XmlUnitException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;

/**
 * Compares two XML documents end checks if these are not similar.
 * 
 */
public class NotEqualToDocumentMatcher extends AbstractXmlUnitMatcher<Document> {

    private final Document expectedDoc;
    private Document actualDoc;

    public NotEqualToDocumentMatcher(Document expectedDoc) {
        this.expectedDoc = expectedDoc;
    }

    public void describeTo(Description description) {
        String documentString;
        try {
            documentString = DocumentUtils.documentToString(expectedDoc);
        } catch (TransformerException e) {
            documentString = "";
        }

        description.appendText("Xml document not equal to " + quote(documentString));
    }

    @Override
    protected void describeMismatchSafely(Document item, Description mismatchDescription) {
        super.describeMismatchSafely(item, mismatchDescription);
        String documentString;
        try {
            documentString = DocumentUtils.documentToString(actualDoc);
        } catch (TransformerException e) {
            documentString = "";
        }
        mismatchDescription.appendText(quote(documentString));
    }

    @Override
    public boolean matchesSafely(Document actualDoc) {
        XmlUnit xmlUnit = getXmlUnit();
        this.actualDoc = actualDoc;

        Diff diff;
        try {
            diff = xmlUnit.compareXML(expectedDoc, actualDoc);
        } catch (XmlUnitException e) {
            throw new IllegalArgumentException("Failed to compare documents.", e);
        }
        return !diff.similar();
    }

    @Factory
    public static NotEqualToDocumentMatcher notEqualToXmlDocument(Document expectedDoc) {
        return new NotEqualToDocumentMatcher(expectedDoc);
    }

}
