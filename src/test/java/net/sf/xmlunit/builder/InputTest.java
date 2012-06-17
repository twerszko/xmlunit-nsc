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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.util.Convert;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.io.Closeables;

public class InputTest {

	private static Document parse(Source s) throws Exception {
		DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return b.parse(Convert.toInputSource(s));
	}

	@Test
	public void shouldParseADocument() throws Exception {
		File testFile = TestResources.ANIMAL_FILE.getFile();
		Document d = parse(Input.fromFile(testFile).build());
		Source s = Input.fromDocument(d).build();
		allIsWellFor(s);
	}

	@Test
	public void shouldParseAnExistingFileByName() throws Exception {
		File testFile = TestResources.ANIMAL_FILE.getFile();
		Source s = Input.fromFile(testFile.getAbsolutePath()).build();
		allIsWellFor(s);
		// TODO check that toFileUri
		// assertEquals(toFileUri(TestResources.ANIMAL_FILE), s.getSystemId());
		Assert.assertThat(TestResources.ANIMAL_FILE.getUri().toString(), is(equalTo(s.getSystemId())));
	}

	@Test
	public void shouldParseAnExistingFileByFile() throws Exception {
		File testFile = TestResources.ANIMAL_FILE.getFile();
		Source s = Input.fromFile(testFile).build();
		allIsWellFor(s);
		assertEquals(TestResources.ANIMAL_FILE.getUri().toString(), s.getSystemId());
		// TODO check that toFileUri
		// assertEquals(toFileUri(TestResources.ANIMAL_FILE), s.getSystemId());
	}

	@Test
	public void shouldParseAnExistingFileFromStream() throws Exception {
		File testFile = TestResources.ANIMAL_FILE.getFile();
		FileInputStream is = null;
		try {
			is = new FileInputStream(testFile);
			allIsWellFor(Input.fromStream(is).build());
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	@Test
	public void shouldParseAnExistingFileFromReader() throws Exception {
		File testFile = TestResources.ANIMAL_FILE.getFile();
		FileReader r = null;
		try {
			r = new FileReader(testFile);
			allIsWellFor(Input.fromReader(r).build());
		} finally {
			Closeables.closeQuietly(r);
		}
	}

	@Test
	public void shouldParseString() throws Exception {
		allIsWellFor(Input.fromMemory(new String(readTestFile(), "UTF-8")).build());
	}

	@Test
	public void shouldParseBytes() throws Exception {
		allIsWellFor(Input.fromMemory(readTestFile()).build());
	}

	@Test
	public void shouldParseFileFromURIString() throws Exception {
		String uriString = TestResources.ANIMAL_FILE.getUri().toString();
		allIsWellFor(Input.fromURI(uriString).build());
	}

	@Test
	public void shouldParseFileFromURI() throws Exception {
		URI uri = TestResources.ANIMAL_FILE.getUri();
		allIsWellFor(Input.fromURI(uri).build());
	}

	@Test
	public void shouldParseFileFromURL() throws Exception {
		URL url = TestResources.ANIMAL_FILE.getUrl();
		allIsWellFor(Input.fromURL(url).build());
	}

	@Test
	public void shouldParseATransformationFromSource() throws Exception {
		// given
		File testFile = TestResources.ANIMAL_XSL.getFile();
		Source input = Input.fromMemory("<animal>furry</animal>").build();
		Source s = Input.byTransforming(input).withStylesheet(Input.fromFile(testFile).build()).build();

		// when
		// then
		allIsWellFor(s, "furry");
	}

	@Test
	public void shouldParseATransformationFromBuilder() throws Exception {
		// given
		File testFile = TestResources.ANIMAL_XSL.getFile();
		Input.Builder input = Input.fromMemory("<animal>furry</animal>");
		Source s = Input.byTransforming(input).withStylesheet(Input.fromFile(testFile)).build();

		// when
		// then
		allIsWellFor(s, "furry");
	}

	private static void allIsWellFor(Source s) throws Exception {
		allIsWellFor(s, "animal");
	}

	private static void allIsWellFor(Source s, String rootElementName) throws Exception {
		assertThat(s, notNullValue());
		Document d = parse(s);
		assertThat(d, notNullValue());
		assertThat(d.getDocumentElement().getTagName(), is(rootElementName));
	}

	private static byte[] readTestFile() throws Exception {
		File testFile = TestResources.ANIMAL_FILE.getFile();
		FileInputStream is = new FileInputStream(testFile);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read = -1;
		while ((read = is.read(buffer)) >= 0) {
			if (read > 0) {
				bos.write(buffer, 0, read);
			}
		}
		is.close();
		return bos.toByteArray();
	}

	// private static String toFileUri(String fileName) throws
	// URISyntaxException {
	// String url = TestUtils.getFile(InputTest.class,
	// fileName).toURI().toString();
	// if (url.startsWith("file:/") && !url.startsWith("file:///")
	// && ("1.5".equals(System.getProperty("java.specification.version")) ||
	// transformerIsApacheXalan())) {
	// // Java5's StreamSource as well as the one used by apache
	// // Xalan create a triple slash URLs,
	// // Java6's sticks with only one - toURI uses only one
	// // slash in either version
	// url = "file:///" + url.substring(6);
	// }
	// return url;
	// }

	// private static boolean transformerIsApacheXalan() {
	// try {
	// TransformerFactory fac = TransformerFactory.newInstance();
	// return
	// fac.getClass().getName().equals("org.apache.xalan.processor.TransformerFactoryImpl");
	// } catch (Exception ex) {
	// return false;
	// }
	// }
}
