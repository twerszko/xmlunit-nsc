/*
 ******************************************************************
Copyright (c) 2001-2008, Jeff Martin, Tim Bacon
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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XSLTConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.transform.TransformerFactoryUtils;
import org.xmlunit.util.IterableNodeList;

/**
 * Simple class for accessing the Nodes matched by an Xpath expression, or
 * evaluating the String value of an Xpath expression. Uses a
 * <code>copy-of</code> or <code>value-of</code> XSL template (as appropriate)
 * to execute the Xpath. This is not an efficient method for accessing XPaths
 * but it is portable across underlying transform implementations. (Yes I know
 * Jaxen is too, but this approach seemed to be the simplest thing that could
 * possibly work...)
 */
public class SimpleXpathEngine implements XpathEngine {
    private final static String DEFAULT_XSLT_VERSION = "1.0";

    private String xsltVersion = DEFAULT_XSLT_VERSION;
    private Map<String, String> ctx = Collections.emptyMap();
    private final XmlUnitProperties properties;

    public SimpleXpathEngine(@Nullable XmlUnitProperties properties) {
        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }
    }

    public void setXsltVersion(String xsltVersion) {
        this.xsltVersion = xsltVersion;
    }

    /**
     * What every XSL transform needs
     * 
     * @return
     */
    private StringBuffer getXSLTBase() {
        StringBuffer result = new StringBuffer()
                .append(XSLTConstants.XML_DECLARATION)
                .append(createXsltStartTag());
        String tmp = result.toString();
        int close = tmp.lastIndexOf('>');
        if (close == -1) {
            close = tmp.length();
        }
        result.insert(close, getNamespaceDeclarations());
        return result;
    }

    /**
     * @param select
     *            an xpath syntax <code>select</code> expression
     * @return the <code>copy-of</code> transformation
     */
    private String getCopyTransformation(String select) {
        return getXSLTBase()
                .append("<xsl:preserve-space elements=\"*\"/>")
                .append("<xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\"/>")
                .append("<xsl:template match=\"/\">")
                .append("<xpathResult>")
                .append("<xsl:apply-templates select=\"").append(select)
                .append("\" mode=\"result\"/>")
                .append("</xpathResult>")
                .append("</xsl:template>")
                .append("<xsl:template match=\"*\" mode=\"result\">")
                .append("  <xsl:copy-of select=\".\"/>")
                .append("</xsl:template>")
                .append("</xsl:stylesheet>")
                .toString();
    }

    /**
     * @param select
     *            an xpath syntax <code>select</code> expression
     * @return the <code>value-of</code> transformation
     */
    private String getValueTransformation(String select) {
        return getXSLTBase()
                .append("<xsl:output method=\"text\"/>")
                .append("<xsl:template match=\"/\">")
                .append("  <xsl:value-of select=\"").append(select).append("\"/>")
                .append("</xsl:template>")
                .append("</xsl:stylesheet>")
                .toString();
    }

    /**
     * Perform the actual transformation work required
     * 
     * @param xslt
     * @param document
     * @param result
     * @throws XpathException
     * @throws TransformerException
     * @throws ConfigurationException
     */
    private void performTransform(String xslt, Source source, Result result)
            throws TransformerException, ConfigurationException, XpathException {
        try {
            StreamSource xsltSource = new StreamSource(new StringReader(xslt));

            // TODO
            TransformerFactoryUtils transformerFactoryUtils = new TransformerFactoryUtils();
            transformerFactoryUtils.setUriResolver(properties.getUriResolver());
            transformerFactoryUtils.setTransformerFactoryClass(properties.getTransformerFactoryClass());

            TransformerFactory tf = transformerFactoryUtils.newTransformerFactory();
            ErrorListener el = new ErrorListener() {
                @Override
                public void error(TransformerException ex) throws TransformerException {
                    // any error in our simple stylesheet must be fatal
                    throw ex;
                }

                @Override
                public void fatalError(TransformerException ex) throws TransformerException {
                    throw ex;
                }

                @Override
                public void warning(TransformerException ex) {
                    // there shouldn't be any warning
                    // TODO logger
                    ex.printStackTrace();
                }
            };
            tf.setErrorListener(el);
            Transformer transformer = tf.newTransformer(xsltSource);
            // Issue 1985229 says Xalan-J 2.7.0 may return null for
            // illegal input
            if (transformer == null) {
                throw new XpathException("Failed to obtain an XSLT transformer"
                        + " for XPath expression.");
            }
            transformer.setErrorListener(el);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            throw new XpathException(ex);
        }
    }

    /**
     * Testable method to execute the copy-of transform and return the root node
     * of the resulting Document.
     * 
     * @param select
     * @param document
     * @throws ConfigurationException
     * @throws TransformerException
     * @return the root node of the Document created by the copy-of transform.
     */
    protected Node getXPathResultNode(String select, Source source)
            throws ConfigurationException, TransformerException, XpathException {
        return getXPathResultAsDocument(select, source).getDocumentElement();
    }

    /**
     * Execute the copy-of transform and return the resulting Document. Used for
     * XMLTestCase comparison
     * 
     * @param select
     * @param document
     * @throws ConfigurationException
     * @throws TransformerException
     * @return the Document created by the copy-of transform.
     */
    protected Document getXPathResultAsDocument(String select, Source source)
            throws ConfigurationException, TransformerException, XpathException {
        DOMResult result = new DOMResult();
        performTransform(getCopyTransformation(select), source, result);
        return (Document) result.getNode();
    }

    /**
     * Execute the specified xpath syntax <code>select</code> expression on the
     * specified document and return the list of nodes (could have length zero)
     * that match
     * 
     * @param select
     * @param document
     * @return list of matching nodes
     */
    @Override
    public IterableNodeList selectNodes(String select, Source source)
            throws ConfigurationException, XpathException {
        try {
            NodeList nodes = getXPathResultNode(select, source).getChildNodes();
            return new IterableNodeList(nodes);
        } catch (TransformerException ex) {
            throw new XpathException("Failed to apply stylesheet", ex);
        }
    }

    /**
     * Evaluate the result of executing the specified xpath syntax
     * <code>select</code> expression on the specified document
     * 
     * @param select
     * @param document
     * @return evaluated result
     */
    @Override
    public String evaluate(String xPath, Source s) throws XpathException, ConfigurationException {
        try {
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            performTransform(getValueTransformation(xPath), s, result);
            return writer.toString();
        } catch (TransformerException ex) {
            throw new XpathException("Failed to apply stylesheet", ex);
        }
    }

    @Override
    public void setNamespaceContext(@Nullable Map<String, String> prefix2Uri) {
        if (prefix2Uri == null) {
            this.ctx = Collections.emptyMap();
        } else {
            this.ctx = new LinkedHashMap<String, String>(prefix2Uri);
        }

    }

    /**
     * returns namespace declarations for all namespaces known to the current
     * context.
     */
    private String getNamespaceDeclarations() {
        StringBuffer nsDecls = new StringBuffer();
        String quoteStyle = "'";

        for (Entry<String, String> entry : ctx.entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();
            if (uri == null) {
                continue;
            }
            // this shouldn't have happened, but better safe than sorry
            if (prefix == null) {
                prefix = "";
            }

            if (uri.indexOf('\'') != -1) {
                quoteStyle = "\"";
            }
            nsDecls.append(' ').append(XSLTConstants.XMLNS_PREFIX);
            if (prefix.length() > 0) {
                nsDecls.append(':');
            }
            nsDecls.append(prefix).append('=')
                    .append(quoteStyle).append(uri).append(quoteStyle)
                    .append(' ');
        }
        return nsDecls.toString();
    }

    /**
     * XSLT stylesheet element using the configured XSLT version.
     */
    String createXsltStartTag() {
        return XSLTConstants.XSLT_START_NO_VERSION + " version=\"" + xsltVersion + "\">";
    }

}
