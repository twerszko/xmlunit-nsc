/*  
    Copyright 2012 Tomasz Werszko
    	
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class containing convenience methods related to DOM
 * 
 */
// TODO maybe change of name?
public class DocumentUtils {

    private final DocumentBuilderFactory controlBuilderFactory;
    private final DocumentBuilderFactory testBuilderFactory;
    private EntityResolver testEntityResolver = null;
    private EntityResolver controlEntityResolver = null;

    public DocumentUtils() {
        this(new XmlUnitProperties());
    }

    public DocumentUtils(XmlUnitProperties properties) {
        properties = properties.clone();

        Class<? extends DocumentBuilderFactory> controlDocumentBuilderFactoryClass =
                properties.getControlDocumentBuilderFactoryClass();
        Class<? extends DocumentBuilderFactory> testDocumentBuilderFactoryClass =
                properties.getTestDocumentBuilderFactoryClass();

        if (controlDocumentBuilderFactoryClass == null) {
            controlBuilderFactory = DocumentBuilderFactory.newInstance();
        } else {
            controlBuilderFactory = DocumentBuilderFactory.newInstance(
                    controlDocumentBuilderFactoryClass.getName(),
                    getClass().getClassLoader());
        }
        controlBuilderFactory.setNamespaceAware(true);
        controlBuilderFactory.setIgnoringElementContentWhitespace(properties.getIgnoreWhitespace());
        controlBuilderFactory.setExpandEntityReferences(properties.getExpandEntityReferences());
        controlBuilderFactory.setCoalescing(properties.getIgnoreDiffBetweenTextAndCDATA());

        if (testDocumentBuilderFactoryClass == null) {
            testBuilderFactory = DocumentBuilderFactory.newInstance();
        } else {
            testBuilderFactory = DocumentBuilderFactory.newInstance(
                    testDocumentBuilderFactoryClass.getName(),
                    getClass().getClassLoader());
        }
        testBuilderFactory.setNamespaceAware(true);
        testBuilderFactory.setIgnoringElementContentWhitespace(properties.getIgnoreWhitespace());
        testBuilderFactory.setExpandEntityReferences(properties.getExpandEntityReferences());
        testBuilderFactory.setCoalescing(properties.getIgnoreDiffBetweenTextAndCDATA());
    }

    // TODO constructor without properties
    /**
     * Get the <code>DocumentBuilderFactory</code> instance used to instantiate
     * parsers for the control XML.
     * 
     * @return factory for control parsers
     */
    public DocumentBuilderFactory getControlDocumentBuilderFactory() {
        return controlBuilderFactory;
    }

    /**
     * Get the <code>DocumentBuilderFactory</code> instance used to instantiate
     * parsers for the test XML in an XMLTestCase.
     * 
     * @return factory for test parsers
     */
    public DocumentBuilderFactory getTestDocumentBuilderFactory() {
        return testBuilderFactory;
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
     * @param fromXML
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildControlDocument(String fromXML)
            throws SAXException, IOException {
        return buildDocument(newControlParser(), new StringReader(fromXML));
    }

    /**
     * Utility method to build a Document using the control DocumentBuilder and
     * the specified InputSource
     * 
     * @param fromSource
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildControlDocument(InputSource fromSource)
            throws IOException, SAXException {
        return buildDocument(newControlParser(), fromSource);
    }

    /**
     * Utility method to build a Document using the test DocumentBuilder to
     * parse the specified String.
     * 
     * @param fromXML
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildTestDocument(String fromXML)
            throws SAXException, IOException {
        return buildDocument(newTestParser(), new StringReader(fromXML));
    }

    /**
     * Utility method to build a Document using the test DocumentBuilder and the
     * specified InputSource
     * 
     * @param fromSource
     * @return Document representation of the String content
     * @throws SAXException
     * @throws IOException
     */
    public Document buildTestDocument(InputSource fromSource)
            throws IOException, SAXException {
        return buildDocument(newTestParser(), fromSource);
    }

    /**
     * Utility method to build a Document using a specific DocumentBuilder and
     * reading characters from a specific Reader.
     * 
     * @param withBuilder
     * @param fromReader
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
     * @param withBuilder
     * @param fromSource
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
    public DocumentBuilder newControlParser()
            throws ConfigurationException {
        try {
            DocumentBuilder builder = controlBuilderFactory.newDocumentBuilder();
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
    public DocumentBuilder newTestParser()
            throws ConfigurationException {
        try {
            DocumentBuilder builder = testBuilderFactory.newDocumentBuilder();
            if (testEntityResolver != null) {
                builder.setEntityResolver(testEntityResolver);
            }
            return builder;
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Converts document into string
     * 
     * @param document
     * @return
     * @throws TransformerException
     *             when transformation fails
     * @throws IllegalArgumentException
     *             when document is null
     */
    public static String documentToString(Document document) throws TransformerException {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null!");
        }

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
