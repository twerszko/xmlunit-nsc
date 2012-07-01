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
package net.sf.xmlunit.transform;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.xml.transform.OutputKeys;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.builder.Input;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class TransformationTest {
	private Transformation transformation;

	@Before
	public void createTransformation() throws IOException {
		transformation = new Transformation(Input.fromFile(TestResources.DOG_FILE.getFile()).build());
		transformation.setStylesheet(Input.fromFile(TestResources.ANIMAL_XSL.getFile()).build());
	}

	@Test
	public void transformAnimalToString() {
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>", transformation.transformToString());
	}

	@Test
	public void transformAnimalToDocument() {
		Document doc = transformation.transformToDocument();
		assertEquals("dog", doc.getDocumentElement().getTagName());
	}

	@Test
	public void transformAnimalToHtml() {
		transformation.addOutputProperty(OutputKeys.METHOD, "html");
		assertThat(transformation.transformToString(), not("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dog/>"));
	}

}
