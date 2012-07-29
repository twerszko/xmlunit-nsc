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

package org.custommonkey.xmlunit;

import static org.junit.Assert.assertEquals;

import javax.xml.transform.OutputKeys;

import org.junit.Test;
import org.w3c.dom.Node;

/**
 * JUnit test for SimpleXpathEngine
 */
public class test_SimpleXpathEngine extends AbstractXpathEngineTests {

    private SimpleXpathEngine simpleXpathEngine = new SimpleXpathEngine(null);

    protected XpathEngine newXpathEngine() {
        return simpleXpathEngine;
    }

    @Test
    public void testGetXPathResultNode() throws Exception {
        Node result = simpleXpathEngine.getXPathResultNode("test",
                testDocument);
        SimpleSerializer serializer = new SimpleSerializer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        assertEquals(testString, serializer.serialize(result.getFirstChild()));
    }

    @Test
    public void testGetMatchingNodesMatchText() throws Exception {
        if (isJava5OrNewer()) {
            // fails with "more recent" version of Xalan shipping with Java5
            return;
        }
        super.testGetMatchingNodesMatchText();
    }

    @Test
    public void testEvaluate() throws Exception {
        if (isJava5OrNewer()) {
            // fails with "more recent" version of Xalan shipping with Java5
            return;
        }
        super.testEvaluate();
    }

    private static boolean isJava5OrNewer() {
        try {
            Class.forName("java.net.Proxy");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
