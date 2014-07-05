/*  	
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package org.custommonkey.xmlunit.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;

import net.sf.xmlunit.xpath.JaxpXpathEngine;
import net.sf.xmlunit.xpath.SimpleXpathEngine;
import net.sf.xmlunit.xpath.XpathEngine;

import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.jaxp13.XmlUnitNamespaceContext2Jaxp13;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.input.CommentLessSource;
import org.xmlunit.input.WhitespaceNormalizedSource;
import org.xmlunit.input.WhitespaceStrippedSource;
import org.xmlunit.util.Preconditions;

/**
 * Class containing convenience methods related to DOM
 * 
 */
// TODO maybe change of name?
@NotThreadSafe
public class DocumentUtils {

    private final XmlUnitProperties properties;
    private EntityResolver testEntityResolver;
    private EntityResolver controlEntityResolver;

    public DocumentUtils() {
        this(new XmlUnitProperties());
    }

    public DocumentUtils(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    /**
     * Get the <code>DocumentBuilderFactory</code> instance used to instantiate
     * parsers for the control XML.
     * 
     * @return factory for control parsers
     */
    public DocumentBuilderFactory newControlDocumentBuilderFactory() {
        Class<? extends DocumentBuilderFactory> controlFactoryClass =
                properties.getControlDocumentBuilderFactoryClass();
        return newDocumentBuilderFactory(controlFactoryClass);
    }

    /**
     * Get the <code>DocumentBuilderFactory</code> instance used to instantiate
     * parsers for the test XML in an XMLTestCase.
     * 
     * @return factory for test parsers
     */
    public DocumentBuilderFactory newTestDocumentBuilderFactory() {
        Class<? extends DocumentBuilderFactory> testFactoryClass =
                properties.getTestDocumentBuilderFactoryClass();
        return newDocumentBuilderFactory(testFactoryClass);
    }

    private DocumentBuilderFactory newDocumentBuilderFactory(
            @Nullable Class<? extends DocumentBuilderFactory> factoryClass) {

        DocumentBuilderFactory factory;
        if (factoryClass == null) {
            factory = DocumentBuilderFactory.newInstance();
        } else {
            factory = DocumentBuilderFactory.newInstance(factoryClass.getName(), factoryClass.getClassLoader());
        }
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(properties.getIgnoreWhitespace());
        factory.setExpandEntityReferences(properties.getExpandEntityReferences());

        return factory;
    }

    /**
     * Sets an EntityResolver to be added to all new test parsers. Setting to
     * null will reset to the default EntityResolver
     */
    public void setTestEntityResolver(EntityResolver resolver) {
        testEntityResolver = resolver;
    }

    /**
     * Obtains the EntityResolver to be added to all new test parsers.
     */
    public EntityResolver getTestEntityResolver() {
        return testEntityResolver;
    }

    /**
     * Sets an EntityResolver to be added to all new control parsers. Setting to
     * null will reset to the default EntityResolver
     */
    public void setControlEntityResolver(EntityResolver resolver) {
        controlEntityResolver = resolver;
    }

    /**
     * Obtains the EntityResolver to be added to all new control parsers.
     */
    public EntityResolver getControlEntityResolver() {
        return controlEntityResolver;
    }

    /**
     * Utility method to build a Document using the control DocumentBuilder to
     * parse the specified String.
     * 
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildControlDocument(String fromXML) throws SAXException, IOException {
        return buildDocument(newControlDocumentBuilder(), new StringReader(fromXML));
    }

    /**
     * Utility method to build a Document using the control DocumentBuilder and
     * the specified InputSource
     * 
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildControlDocument(InputSource fromSource) throws IOException, SAXException {
        return buildDocument(newControlDocumentBuilder(), fromSource);
    }

    /**
     * Utility method to build a Document using the test DocumentBuilder to
     * parse the specified String.
     * 
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildTestDocument(String fromXML) throws SAXException, IOException {
        return buildDocument(newTestDocumentBuilder(), new StringReader(fromXML));
    }

    /**
     * Utility method to build a Document using the test DocumentBuilder and the
     * specified InputSource
     * 
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildTestDocument(InputSource fromSource)
            throws IOException, SAXException {
        return buildDocument(newTestDocumentBuilder(), fromSource);
    }

    /**
     * Utility method to build a Document using a specific DocumentBuilder and
     * reading characters from a specific Reader.
     * 
     * @return Document built
     * @throws SAXException
     * @throws IOException
     */
    public Document buildDocument(DocumentBuilder withBuilder,
            Reader fromReader) throws SAXException, IOException {
        return buildDocument(withBuilder, new InputSource(fromReader));
    }

    /**
     * Utility method to build a Document using a specific DocumentBuilder and a
     * specific InputSource
     * 
     * @return Document built
     * @throws SAXException
     * @throws IOException
     */
    public Document buildDocument(DocumentBuilder withBuilder,
            InputSource fromSource) throws IOException, SAXException {
        return withBuilder.parse(fromSource);
    }

    /**
     * Get the <code>DocumentBuilder</code> instance used to parse the control
     * XML.
     * 
     * @return parser for control values
     * @throws ConfigurationException
     */
    // TODO change name?
    public DocumentBuilder newControlDocumentBuilder() throws ConfigurationException {
        try {
            DocumentBuilder builder = newControlDocumentBuilderFactory().newDocumentBuilder();
            if (controlEntityResolver != null) {
                builder.setEntityResolver(controlEntityResolver);
            }
            return builder;
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Get the <code>DocumentBuilder</code> instance used to parse the test XML.
     * 
     * @return parser for test values
     * @throws ConfigurationException
     */
    public DocumentBuilder newTestDocumentBuilder() throws ConfigurationException {
        try {
            DocumentBuilder builder = newTestDocumentBuilderFactory().newDocumentBuilder();
            if (testEntityResolver != null) {
                builder.setEntityResolver(testEntityResolver);
            }
            return builder;
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Get the SAX parser to use in tests.
     * 
     * @return new SAXParserFactory instance used by the {@link Validator
     *         Validator} to perform DTD validation
     */
    public SAXParserFactory newSaxParserFactory() {
        Class<? extends SAXParserFactory> clazz = properties.getSaxParserFactoryClass();

        SAXParserFactory factory;
        if (clazz == null) {
            factory = SAXParserFactory.newInstance();
        } else {
            factory = SAXParserFactory.newInstance(clazz.getName(), clazz.getClassLoader());
        }
        factory.setNamespaceAware(true);

        return factory;
    }

    public XPathFactory newXpathFactory() throws ConfigurationException {
        Class<? extends XPathFactory> clazz = properties.getXpathFactoryClass();

        XPathFactory factory;
        if (clazz == null) {
            factory = XPathFactory.newInstance();
        } else {
            try {
                factory = clazz.newInstance();
            } catch (InstantiationException e) {
                throw new ConfigurationException("Failed to instantiate XPathFactory.", e);
            } catch (IllegalAccessException e) {
                throw new ConfigurationException("Failed to instantiate XPathFactory.", e);
            }
        }

        return factory;
    }

    /**
     * Obtains an XpathEngine to use in XPath tests.
     */
    public XpathEngine newXpathEngine() {
        XpathEngine eng;
        try {
            Class.forName("javax.xml.xpath.XPath");
            eng = new JaxpXpathEngine(newXpathFactory());
        } catch (Throwable ex) {
            // should probably only catch ClassNotFoundException, but some
            // constellations - like Ant shipping a more recent version of
            // xml-apis than the JDK - may contain the JAXP 1.3 interfaces
            // without implementations
            SimpleXpathEngine simpleEngine = new SimpleXpathEngine(properties);
            simpleEngine.setXsltVersion(properties.getXsltVersion());
            eng = simpleEngine;
        }
        if (properties.getXpathNamespaceContext() != null) {
            // TODO
            eng.setNamespaceContext(XmlUnitNamespaceContext2Jaxp13.turnIntoMap(properties.getXpathNamespaceContext()));
        }
        return eng;
    }

    public Source applySourceProperties(Source input) {
        Source result = input;
        if (properties.getIgnoreComments()) {
            result = new CommentLessSource(result);
        }
        if (properties.getNormalizeWhitespace()) {
            result = new WhitespaceNormalizedSource(result);
        }
        if (properties.getIgnoreWhitespace()) {
            result = new WhitespaceStrippedSource(result);
        }
        return result;
    }

    /**
     * Converts document into string
     * 
     * @throws TransformerException
     *             when transformation fails
     * @throws IllegalArgumentException
     *             when document is null
     */
    public static String documentToString(Document document) throws TransformerException {
        Preconditions.checkArgument(document != null, "Document cannot be null!");

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
