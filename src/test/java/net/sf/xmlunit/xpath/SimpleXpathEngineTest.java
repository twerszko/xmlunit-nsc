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
package net.sf.xmlunit.xpath;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;

import org.custommonkey.xmlunit.SimpleSerializer;
import org.custommonkey.xmlunit.XSLTConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.w3c.dom.Node;

public class SimpleXpathEngineTest extends AbstractXpathEngineTest {

    @Override
    protected SimpleXpathEngine newXpathEngine() {
        return new SimpleXpathEngine(null);
    }

    @Test
    public void should_get_xpath_result_node() throws Exception {
        // given
        SimpleXpathEngine engine = new SimpleXpathEngine(null);
        DOMSource source = new DOMSource(testDocument);

        // when
        Node result = engine.getXPathResultNode("test", source);
        SimpleSerializer serializer = new SimpleSerializer(new XmlUnitProperties());
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // then
        assertThat(serializer.serialize(result.getFirstChild())).isEqualTo(testString);
    }

    @Override
    @Test(expected = XpathException.class)
    public void should_get_matching_node_with_default_ns_and_empty_prefix() throws Exception {
        // This cannot work with xsl. Xsl wont parse with such XPath
        // so for this engine exception is expected
        super.should_get_matching_node_with_default_ns_and_empty_prefix();
    }

    @Test
    public void should_use_default_xslt_version() {
        // given
        SimpleXpathEngine engine = newXpathEngine();

        // when
        String tag = engine.createXsltStartTag();

        // then
        assertThat(tag).isEqualTo(XSLTConstants.XSLT_START);
    }

    @Test
    public void should_use_other_xslt_version() {
        // given
        String xsltVersion = "2.0";
        SimpleXpathEngine engine = newXpathEngine();
        engine.setXsltVersion(xsltVersion);

        // when
        String tag = engine.createXsltStartTag();

        // then
        assertThat(tag).startsWith(XSLTConstants.XSLT_START_NO_VERSION);
        assertThat(tag).endsWith("\"" + xsltVersion + "\">");
    }
}
