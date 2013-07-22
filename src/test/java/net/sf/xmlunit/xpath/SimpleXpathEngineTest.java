/*
 ******************************************************************
Copyright (c) 200, Jeff Martin, Tim Bacon
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package net.sf.xmlunit.xpath;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;

import org.custommonkey.xmlunit.SimpleSerializer;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.w3c.dom.Node;

public class SimpleXpathEngineTest extends AbstractXpathEngineTest {

    @Override
    protected XpathEngine newXpathEngine() {
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
}
