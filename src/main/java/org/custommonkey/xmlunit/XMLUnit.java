/*
 *****************************************************************
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

package org.custommonkey.xmlunit;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.builder.Transform;
import net.sf.xmlunit.builder.Transform.TransformationResult;

import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.XmlUnitException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows access to project control parameters such as which Parser to use and
 * provides some convenience methods for building Documents from Strings etc. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 */
public final class XMLUnit {
    private final XmlUnitProperties properties;

    private static TransformerFactory transformerFactory;
    private static SAXParserFactory saxParserFactory;
    private static URIResolver uriResolver = null;
    private static String xpathFactoryName = null;

    private static final String XSLT_VERSION_START = " version=\"";
    private static final String XSLT_VERSION_END = "\">";

    private static final String STRIP_WHITESPACE_STYLESHEET_START = new StringBuffer(XMLConstants.XML_DECLARATION)
            .append(XSLTConstants.XSLT_START_NO_VERSION)
            .append(XSLT_VERSION_START)
            .toString();

    private static final String STRIP_WHITESPACE_STYLESHEET_END = new StringBuffer(XSLT_VERSION_END)
            .append(XSLTConstants.XSLT_XML_OUTPUT_NOINDENT)
            .append(XSLTConstants.XSLT_STRIP_WHITESPACE)
            .append(XSLTConstants.XSLT_IDENTITY_TEMPLATE)
            .append(XSLTConstants.XSLT_END)
            .toString();

    private static final String STRIP_COMMENTS_STYLESHEET_START = new StringBuffer(XMLConstants.XML_DECLARATION)
            .append(XSLTConstants.XSLT_START_NO_VERSION)
            .append(XSLT_VERSION_START)
            .toString();

    private static final String STRIP_COMMENTS_STYLESHEET_END = new StringBuffer(XSLT_VERSION_END)
            .append(XSLTConstants.XSLT_XML_OUTPUT_NOINDENT)
            .append(XSLTConstants.XSLT_STRIP_COMMENTS_TEMPLATE)
            .append(XSLTConstants.XSLT_END)
            .toString();

    /**
     * Creates XMLUnit with defensive copy of given properties object.
     * 
     * @param properties
     */
    XMLUnit(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    /**
     * Returns defensive copy of properties.
     * 
     * @return
     */
    public XmlUnitProperties getProperties() {
        return properties.clone();
    }

    /**
     * Overide the transformer to use for XSLT transformations (and by
     * implication serialization and XPaths). This is useful when comparing
     * transformer implementations.
     */
    public static void setTransformerFactory(String className) {
        System.setProperty("javax.xml.transform.TransformerFactory",
                className);
        transformerFactory = null;
        getTransformerFactory();
    }

    /**
     * Get the transformer to use for XSLT transformations (and by implication
     * serialization and XPaths).
     * 
     * @return the current transformer factory in use a new instance of the
     *         default transformer factory
     */
    public static TransformerFactory getTransformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = newTransformerFactory();
        }
        return transformerFactory;
    }

    /**
     * Get a fresh transformer to use for XSLT transformations (and by
     * implication serialization and XPaths).
     * 
     * @return a new instance of the default transformer factory
     */
    static TransformerFactory newTransformerFactory() {
        TransformerFactory tf = TransformerFactory.newInstance();
        if (uriResolver != null) {
            tf.setURIResolver(uriResolver);
        }
        return tf;
    }

    /**
     * Sets the URIResolver to use during transformations.
     */
    public static void setURIResolver(URIResolver resolver) {
        if (uriResolver != resolver) {
            uriResolver = resolver;
            transformerFactory = null;
            getTransformerFactory();
        }
    }

    /**
     * Gets the URIResolver used during Transformations.
     */
    public static URIResolver getURIResolver() {
        return uriResolver;
    }

    /**
     * Override the SAX parser to use in tests. Currently only used by
     * {@link Validator Validator class}
     * 
     * @param className
     */
    public static void setSAXParserFactory(String className) {
        System.setProperty("javax.xml.parsers.SAXParserFactory", className);
        saxParserFactory = null;
        getSAXParserFactory();
    }

    /**
     * Override the SAX parser to use in tests. Currently only used by
     * {@link Validator Validator class}
     * 
     * @param factory
     */
    public static void setSAXParserFactory(SAXParserFactory factory) {
        saxParserFactory = factory;
    }

    /**
     * Get the SAX parser to use in tests.
     * 
     * <p>
     * Unless an instance has been given via {@link
     * setSAXParserFactory(SAXParserFactory) setSAXParserFactory} explicitly,
     * the returned factory will be namespace aware.
     * </p>
     * 
     * @return the SAXParserFactory instance used by the {@link Validator
     *         Validator} to perform DTD validation
     */
    public static SAXParserFactory getSAXParserFactory() {
        if (saxParserFactory == null) {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
        }
        return saxParserFactory;
    }

    private String getStripWhitespaceStylesheet() {
        return STRIP_WHITESPACE_STYLESHEET_START + properties.getXsltVersion()
                + STRIP_WHITESPACE_STYLESHEET_END;
    }

    /**
     * Obtain the transformation that will strip whitespace from a DOM
     * containing empty Text nodes
     * 
     * @param forDocument
     * @return a <code>Transform</code> to do the whitespace stripping
     */
    public TransformationResult getStripWhitespaceTransform(Document forDocument) {
        Source source = Input.fromDocument(forDocument).build();
        Source stylesheet = Input.fromMemory(getStripWhitespaceStylesheet()).build();

        TransformationResult transformationResult = Transform.source(source)
                .withStylesheet(stylesheet)
                .usingFactory(XMLUnit.getTransformerFactory())
                .build();
        return transformationResult;
    }

    /**
     * Returns a new Document instance that is identical to the one passed in
     * with element content whitespace removed.
     * 
     * <p>
     * Will use {@link #getStripWhitespaceTransform getStripWhitespaceTransform}
     * unless we are operating under the severly broken XSLTC Transformer
     * shipping with JDK 1.5.
     * </p>
     */
    public Document getWhitespaceStrippedDocument(Document forDoc) {
        String factory = getTransformerFactory().getClass().getName();
        if (XSLTConstants.JAVA5_XSLTC_FACTORY_NAME.equals(factory)) {
            return stripWhiteSpaceWithoutXSLT(forDoc);
        } else {
            return stripWhiteSpaceUsingXSLT(forDoc);
        }
    }

    private Document stripWhiteSpaceUsingXSLT(Document forDoc) {
        return getStripWhitespaceTransform(forDoc).toDocument();
    }

    private static Document stripWhiteSpaceWithoutXSLT(Document forDoc) {
        Document copy = (Document) forDoc.cloneNode(true);
        stripEmptyTextNodes(copy);
        return copy;
    }

    private static void stripEmptyTextNodes(Node n) {
        final NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                stripEmptyTextNodes(child);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String value = child.getNodeValue();
                if (value == null || value.trim().length() == 0) {
                    n.removeChild(child);
                    --i;
                }
            }
        }
    }

    private String getStripCommentsStylesheet() {
        return STRIP_COMMENTS_STYLESHEET_START + properties.getXsltVersion()
                + STRIP_COMMENTS_STYLESHEET_END;
    }

    /**
     * Obtain the transformation that will strip comments from a DOM.
     * 
     * @param forDocument
     * @return a <code>Transform</code> to do the whitespace stripping
     */
    public TransformationResult getStripCommentsTransform(Document forDocument) {
        Source source = Input.fromNode(forDocument).build();
        String stylesheetString = getStripCommentsStylesheet();
        Source stylesheet = Input.fromMemory(stylesheetString).build();

        TransformationResult transformationResult = Transform.source(source)
                .withStylesheet(stylesheet)
                .usingFactory(XMLUnit.getTransformerFactory())
                .build();

        return transformationResult;
    }

    /**
     * Place holder for current version info.
     * 
     * @return current version
     */
    public static String getVersion() {
        return "1.3alpha";
    }

    /**
     * Compare XML documents provided by two InputSource classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     * @throws SAXException
     * @throws IOException
     */
    public Diff compareXML(InputSource control, InputSource test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare XML documents provided by two Reader classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(Reader control, Reader test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare XML documents provided by two Reader classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(String control, Reader test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare XML documents provided by two Reader classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(Reader control, String test)
            throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare two XML documents provided as strings
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(String control, String test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare two XML documents provided as strings
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(Document control, Document test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Obtains an XpathEngine to use in XPath tests.
     */
    public XpathEngine newXpathEngine() {
        XpathEngine eng = null;
        try {
            Class.forName("javax.xml.xpath.XPath");
            Class<?> c = Class.forName("org.custommonkey.xmlunit.jaxp13.Jaxp13XpathEngine");
            eng = (XpathEngine) c.newInstance();
        } catch (Throwable ex) {
            // should probably only catch ClassNotFoundException, but some
            // constellations - like Ant shipping a more recent version of
            // xml-apis than the JDK - may contain the JAXP 1.3 interfaces
            // without implementations
            eng = new SimpleXpathEngine(properties);
        }
        if (properties.getXpathNamespaceContext() != null) {
            // TODO
            eng.setNamespaceContext(properties.getXpathNamespaceContext());
        }
        return eng;
    }

    /**
     * Sets the class to use as XPathFactory when using JAXP 1.3.
     */
    public static void setXPathFactory(String className) {
        xpathFactoryName = className;
    }

    /**
     * Gets the class to use as XPathFactory when using JAXP 1.3.
     */
    public static String getXPathFactory() {
        return xpathFactoryName;
    }

    /**
     * XSLT stylesheet element using the configured XSLT version.
     */
    String getXSLTStart() {
        return XSLTConstants.XSLT_START_NO_VERSION
                + XSLT_VERSION_START + properties.getXsltVersion() + XSLT_VERSION_END;
    }
}
