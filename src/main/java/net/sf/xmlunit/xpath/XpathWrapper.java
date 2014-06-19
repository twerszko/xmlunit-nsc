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

package net.sf.xmlunit.xpath;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.xmlunit.util.Preconditions;

/**
 * This object is a simple wrapper that holds xpath string and a document
 * related with it
 * 
 */
public class XpathWrapper {
    private final String xpath;
    private final Document document;

    /**
     * @param xpath
     * @param document
     * 
     * @throws IllegalArgumentException
     *             when at least one parameter is null.
     */
    public XpathWrapper(String xpath, Document document) {
        Preconditions.checkArgument(xpath != null, "Xpath cannot be null!");
        Preconditions.checkArgument(document != null, "Document cannot be null");
        this.xpath = xpath;
        this.document = document;
    }

    /**
     * @return Xpath string
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * @return Document related with xpath string.
     */
    public Document getDocument() {
        return document;
    }

    @Override
    public String toString() {
        String xml;
        try {
            xml = DocumentUtils.documentToString(document);
        } catch (TransformerException e) {
            xml = "invalid xml";
        }
        return "XpathWrapper [xpath=" + xpath + ", document=" + xml + "]";
    }
}
