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

package org.custommonkey.xmlunit.util;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Class containing convenience methods related to DOM
 * 
 */
public class DomUtils {

	/**
	 * Converts document into string
	 * 
	 * @param document
	 * @return
	 * @throws TransformerException
	 *             when transformation fails
	 * @throws IllegalArgumentException
	 *             when document is null
	 */
	public static String documentToString(Document document) throws TransformerException {
		if (document == null) {
			throw new IllegalArgumentException("Document cannot be null!");
		}

		DOMSource domSource = new DOMSource(document);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.transform(domSource, result);
		return writer.toString();
	}
}
