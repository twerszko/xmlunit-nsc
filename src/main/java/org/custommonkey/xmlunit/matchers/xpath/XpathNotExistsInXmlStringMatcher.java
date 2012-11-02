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

import java.io.IOException;

import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.xpath.XpathEngine;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Assert that a specific XPath does not exists in some given XML *
 */
public class XpathNotExistsInXmlStringMatcher extends AbstractXmlUnitMatcher<String> {

    private final String expectedXpath;
    private String actualXml;

    public XpathNotExistsInXmlStringMatcher(String expectedXpath) {
        if (expectedXpath == null || expectedXpath.isEmpty()) {
            throw new IllegalArgumentException("Expected document cannot be null or empty!");
        }
        this.expectedXpath = expectedXpath;
    }

    public void describeTo(Description description) {
        description.appendText("xpath " + quote(expectedXpath) +
                " does not exist in document " + quote(actualXml));

    }

    @Override
    protected void describeMismatchSafely(String item, Description description) {
        description.appendText("xpath " + quote(expectedXpath) +
                " exists in document " + quote(actualXml));
    }

    @Override
    public boolean matchesSafely(String actualXmlDoc) {
        if (actualXmlDoc == null || actualXmlDoc.isEmpty()) {
            throw new IllegalArgumentException("Actual document cannot be null or empty!");
        }

        this.actualXml = actualXmlDoc;

        Document inDocument;
        int matches;

        try {
            DocumentUtils documentUtils = new DocumentUtils(getXmlUnit().getProperties());
            inDocument = documentUtils.buildControlDocument(actualXmlDoc);

            XpathEngine simpleXpathEngine = getXmlUnit().newDocumentUtils().newXpathEngine();
            IterableNodeList nodeList = simpleXpathEngine.selectNodes(expectedXpath, new DOMSource(inDocument));
            matches = nodeList.getLength();
        } catch (SAXException e) {
            throw new IllegalStateException("Couldn't check xpath", e);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't check xpath", e);
        } catch (XpathException e) {
            throw new IllegalArgumentException("Invalid xpath!", e);
        }

        return !(matches > 0);
    }

    @Factory
    public static XpathNotExistsInXmlStringMatcher notContainsXpath(String expectedXpath) {
        return new XpathNotExistsInXmlStringMatcher(expectedXpath);
    }

}
