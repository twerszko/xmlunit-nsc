/*
 ******************************************************************
Copyright (c) 2001-2007, Jeff Martin, Tim Bacon
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import net.sf.xmlunit.util.XsltUtils;

import org.w3c.dom.Node;
import org.xmlunit.builder.Input;
import org.xmlunit.builder.Transform;
import org.xmlunit.builder.Transform.Builder;

/**
 * Adapter class to present the content of a DOM Node (e.g. a Document) as an
 * InputStream using a DOM to Stream transformation. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 */
public class NodeInputStream extends InputStream {
    private final Node rootNode;
    private final ByteArrayOutputStream nodeContentBytes;
    private final Properties outputProperties;
    private int atPos = 0;

    // TODO get rid of it
    private final XmlUnitProperties properties;

    /**
     * Simple constructor
     * 
     * @param rootNode
     *            the node to be presented as an input stream
     */
    public NodeInputStream(Node rootNode, XmlUnitProperties properties) {
        this(rootNode, null, properties);
    }

    /**
     * Simple constructor
     * 
     * @param rootNode
     *            the node to be presented as an input stream
     */
    public NodeInputStream(Node rootNode, Properties outputProperties, XmlUnitProperties properties) {
        this.properties = properties.clone();
        this.rootNode = rootNode;
        nodeContentBytes = new ByteArrayOutputStream();
        if (outputProperties == null) {
            this.outputProperties = new Properties();
        } else {
            this.outputProperties = outputProperties;
        }
    }

    /**
     * Do the actual work of serializing the node to bytes
     * 
     * @throws IOException
     *             if serialization goes awry
     */
    private void ensureContentAvailable() throws IOException {
        if (nodeContentBytes.size() > 0) {
            return;
        }
        try {
            Source source = Input.fromNode(rootNode).build();

            // TODO
            XsltUtils xsltUtils = new XsltUtils();
            xsltUtils.setUriResolver(properties.getUriResolver());
            xsltUtils.setTransformerFactoryClass(properties.getTransformerFactoryClass());

            Builder transformBuilder = Transform.source(source)
                    .usingFactory(xsltUtils.newTransformerFactory());
            Iterator<Entry<Object, Object>> it = outputProperties.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Object, Object> entry = it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key != null && value != null) {
                    transformBuilder.withOutputProperty(key, value);
                }
            }

            StreamResult byteResult = new StreamResult(nodeContentBytes);
            transformBuilder.build().to(byteResult);
        } catch (Exception e) {
            throw new IOException("Unable to serialize document to outputstream: "
                    + e.toString());
        }
    }

    /**
     * InputStream method
     * 
     * @return byte as read
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        ensureContentAvailable();
        if (reallyAvailable() == 0) {
            return -1;
        }
        int contentByte = nodeContentBytes.toByteArray()[atPos];
        atPos++;
        return contentByte;
    }

    /**
     * InputStream method Note that calling close allows a repeated read of the
     * content
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        atPos = 0;
    }

    /**
     * InputStream method
     * 
     * @return number of bytes available
     */
    @Override
    public int available() throws IOException {
        ensureContentAvailable();
        return reallyAvailable();
    }

    /**
     * @return really available
     */
    private int reallyAvailable() {
        return nodeContentBytes.size() - atPos;
    }
}
