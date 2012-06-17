/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package net.sf.xmlunit.builder;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.xmlunit.TestResources;

import org.junit.Test;
import org.w3c.dom.Document;

public class TransformTest {

	public String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Test
	public void transformAnimalToString() throws IOException {
		String result = Transform.source(
		        Input.fromFile(TestResources.DOG_FILE.getFile()).build())
		        .withStylesheet(
		                Input.fromFile(TestResources.ANIMAL_XSL.getFile())
		                        .build()).build().toString();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>", result);
	}

	@Test
	public void transformAnimalToDocument() throws IOException {
		Document doc = Transform.source(
		        Input.fromFile(TestResources.DOG_FILE.getFile()).build())
		        .withStylesheet(Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build()).build().toDocument();

		assertEquals("dog", doc.getDocumentElement().getTagName());
	}

	@Test
	public void transformAnimalToHtml() throws IOException {
		assertThat(
		        Transform.source(Input.fromFile(TestResources.DOG_FILE.getFile()).build())
		                .withStylesheet(Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build())
		                .withOutputProperty(OutputKeys.METHOD, "html").build().toString(),
		        not("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>"));
	}

}
