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

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.xpath.XpathEngine;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;

/**
 * Assert that a specific XPath does not exist in some given XML document *
 */
public class XpathNotExistsInDocumentMatcher extends AbstractXmlUnitMatcher<Document> {

    private final String expectedXpath;
    private Document actualDocument;

    public XpathNotExistsInDocumentMatcher(String expectedXpath) {
        if (expectedXpath == null || expectedXpath.isEmpty()) {
            throw new IllegalArgumentException("Expected document cannot be null or empty!");
        }
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        String documentString;
        try {
            documentString = DocumentUtils.documentToString(actualDocument);
        } catch (TransformerException e) {
            documentString = "";
        }
        description.appendText("xpath " + quote(expectedXpath) +
                " does not exist in document " + quote(documentString));

    }

    @Override
    protected void describeMismatchSafely(Document item, Description description) {
        String documentString;
        try {
            documentString = DocumentUtils.documentToString(actualDocument);
        } catch (TransformerException e) {
            documentString = "";
        }
        description.appendText("xpath " + quote(expectedXpath) +
                " exists in document " + quote(documentString));
    }

    @Override
    public boolean matchesSafely(Document actualXmlDoc) {
        if (actualXmlDoc == null) {
            throw new IllegalArgumentException("Actual document cannot be null!");
        }

        this.actualDocument = actualXmlDoc;

        IterableNodeList nodeList;
        try {
            XpathEngine xpathEngine = getXmlUnit().newDocumentUtils().newXpathEngine();
            nodeList = xpathEngine.selectNodes(expectedXpath, new DOMSource(actualXmlDoc));
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }
        int matches = nodeList.getLength();

        return !(matches > 0);
    }

    @Factory
    public static XpathNotExistsInDocumentMatcher documentNotContainsXpath(String expectedXpath) {
        return new XpathNotExistsInDocumentMatcher(expectedXpath);
    }

}
