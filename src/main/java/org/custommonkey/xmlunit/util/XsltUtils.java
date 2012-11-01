package org.custommonkey.xmlunit.util;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.builder.Transform;
import net.sf.xmlunit.builder.Transform.TransformationResult;

import org.custommonkey.xmlunit.XMLConstants;
import org.custommonkey.xmlunit.XSLTConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XsltUtils {
    private final XmlUnitProperties properties;

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

    public XsltUtils() {
        this(new XmlUnitProperties());
    }

    public XsltUtils(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    /**
     * Get the transformer to use for XSLT transformations (and by implication
     * serialization and XPaths).
     * 
     * @return If specific TransformerFactoryClass was specified in properties
     *         then new instance of this class will be returned. New instance of
     *         default TransformerFactory otherwise.
     */
    public TransformerFactory newTransformerFactory() {
        Class<? extends TransformerFactory> transformerFactoryClass = properties.getTransformerFactoryClass();

        TransformerFactory factory;
        if (transformerFactoryClass == null) {
            factory = TransformerFactory.newInstance();
        } else {
            factory = TransformerFactory.newInstance(
                    transformerFactoryClass.getName(),
                    transformerFactoryClass.getClassLoader());
        }

        if (properties.getUriResolver() != null) {
            factory.setURIResolver(properties.getUriResolver());
        }

        return factory;
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
                .usingFactory(newTransformerFactory())
                .build();

        return transformationResult;
    }

    private String getStripCommentsStylesheet() {
        return STRIP_COMMENTS_STYLESHEET_START + properties.getXsltVersion()
                + STRIP_COMMENTS_STYLESHEET_END;
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
                .usingFactory(new XsltUtils(properties).newTransformerFactory())
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
        String factory = new XsltUtils(properties).newTransformerFactory().getClass().getName();
        if (XSLTConstants.JAVA5_XSLTC_FACTORY_NAME.equals(factory)) {
            return stripWhiteSpaceWithoutXSLT(forDoc);
        } else {
            return stripWhiteSpaceUsingXSLT(forDoc);
        }
    }

    private Document stripWhiteSpaceUsingXSLT(Document forDoc) {
        return getStripWhitespaceTransform(forDoc).toDocument();
    }

    private Document stripWhiteSpaceWithoutXSLT(Document forDoc) {
        Document copy = (Document) forDoc.cloneNode(true);
        stripEmptyTextNodes(copy);
        return copy;
    }

    private String getStripWhitespaceStylesheet() {
        return STRIP_WHITESPACE_STYLESHEET_START + properties.getXsltVersion()
                + STRIP_WHITESPACE_STYLESHEET_END;
    }

    private void stripEmptyTextNodes(Node n) {
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

    /**
     * XSLT stylesheet element using the configured XSLT version.
     */
    // TODO make it non public
    public String getXSLTStart() {
        return XSLTConstants.XSLT_START_NO_VERSION
                + XSLT_VERSION_START + properties.getXsltVersion() + XSLT_VERSION_END;
    }

}
