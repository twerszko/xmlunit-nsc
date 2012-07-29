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

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.custommonkey.xmlunit.util.DomUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Assert that a specific XPath exists in some given XML document *
 */
public class XpathExistsInDocumentMatcher extends AbstractXmlUnitMatcher<Document> {

    private final String expectedXpath;
    private Document actualDocument;

    public XpathExistsInDocumentMatcher(String expectedXpath) {
        if (expectedXpath == null || expectedXpath.isEmpty()) {
            throw new IllegalArgumentException("Expected document cannot be null or empty!");
        }
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        String documentString;
        try {
            documentString = DomUtils.documentToString(actualDocument);
        } catch (TransformerException e) {
            documentString = "";
        }
        description.appendText("xpath " + quote(expectedXpath) +
                " exists in document " + quote(documentString));

    }

    @Override
    protected void describeMismatchSafely(Document item, Description description) {
        String documentString;
        try {
            documentString = DomUtils.documentToString(actualDocument);
        } catch (TransformerException e) {
            documentString = "";
        }
        description.appendText("xpath " + quote(expectedXpath) +
                " does not exist in document " + quote(documentString));
    }

    @Override
    public boolean matchesSafely(Document actualXmlDoc) {
        if (actualXmlDoc == null) {
            throw new IllegalArgumentException("Actual document cannot be null!");
        }
        this.actualDocument = actualXmlDoc;

        NodeList nodeList;
        try {
            XpathEngine simpleXpathEngine = getXmlUnit().newXpathEngine();
            nodeList = simpleXpathEngine.getMatchingNodes(expectedXpath, actualXmlDoc);
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }
        int matches = nodeList.getLength();

        return matches > 0;
    }

    @Factory
    public static XpathExistsInDocumentMatcher documentContainsXpath(String expectedXpath) {
        return new XpathExistsInDocumentMatcher(expectedXpath);
    }

}
