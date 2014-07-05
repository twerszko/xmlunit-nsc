package org.custommonkey.xmlunit;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.xmlunit.NamespaceContext;

public class XmlUnitBuilder {
    private final XmlUnitProperties properties;

    public XmlUnitBuilder() {
        this.properties = new XmlUnitProperties();
    }

    public XmlUnitBuilder(XmlUnitProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties cannot be null!");
        }
        this.properties = properties.clone();
    }

    public static XmlUnitBuilder xmlUnit() {
        return new XmlUnitBuilder();
    }

    public XmlUnitBuilder ignoringWhitespace(boolean ignore) {
        properties.setIgnoreWhitespace(ignore);
        return this;
    }

    /**
     * Whether whitespace characters inside text nodes or attributes should be
     * "normalized".
     * 
     * <p>
     * Normalized in this context means that all whitespace is replaced by the
     * space character and adjacent whitespace characters are collapsed to a
     * single space character. It will also trim the resulting character content
     * on both ends.
     * </p>
     * 
     * <p>
     * The default value is false.
     * </p>
     * 
     * <p>
     * Setting this parameter has no effect on {@link setIgnoreWhitespace
     * ignorable whitespace}.
     * </p>
     */
    public XmlUnitBuilder normalizingWhitespace(boolean normalize) {
        properties.setNormalizeWhitespace(normalize);
        return this;
    }

    /**
     * Whether comments should be ignored.
     * 
     * <p>
     * The default value is false
     * </p>
     */
    public XmlUnitBuilder ignoringComments(boolean ignore) {
        properties.setIgnoreComments(ignore);
        return this;
    }

    /**
     * Whether to compare unmatched control nodes to unmatched test nodes.
     * 
     * <p>
     * Defaults to true.
     * </p>
     */
    public XmlUnitBuilder comparingUnmatched(boolean compare) {
        properties.setCompareUnmatched(compare);
        return this;
    }

    /**
     * Sets the XSLT version to set on stylesheets used internally.
     * 
     * <p>
     * Defaults to "1.0".
     * </p>
     * 
     * @throws ConfigurationException
     *             if the argument cannot be parsed as a positive number.
     */
    public XmlUnitBuilder usingXsltVersion(String xsltVersion) {
        properties.setXsltVersion(xsltVersion);
        return this;
    }

    /**
     * Set the NamespaceContext to use in XPath tests.
     */
    public XmlUnitBuilder withXpathNamespaceContext(@Nullable NamespaceContext xpathNamespaceContext) {
        properties.setXpathNamespaceContext(xpathNamespaceContext);
        return this;
    }

    /**
     * Whether to ignore the order of attributes on an element.
     * 
     * <p>
     * The order of attributes has never been relevant for XML documents, still
     * XMLUnit can consider two pieces of XML not-identical (but similar) if
     * they differ in order of attributes. Set this option to true to compare
     * the order.
     * </p>
     * 
     * <p>
     * The default value is true
     * </p>
     */
    public XmlUnitBuilder ignoringAttributeOrder(boolean ignore) {
        properties.setIgnoreAttributeOrder(ignore);
        return this;
    }

    /**
     * Whether the parser shall be instructed to expand entity references.
     * 
     * <p>
     * Defaults to false.
     * </p>
     * 
     * @see javax.xml.parsers.DocumentBuilderFactory#setExpandEntityReferences
     */
    public XmlUnitBuilder expandingEntityReferences(boolean expand) {
        properties.setExpandEntityReferences(expand);
        return this;
    }

    // TODO: docu
    public XmlUnitBuilder usingControlDocumentBuilderFactoryClass(
            @Nullable Class<? extends DocumentBuilderFactory> controlDocumentBuilderFactory) {
        properties.setControlDocumentBuilderFactoryClass(controlDocumentBuilderFactory);
        return this;
    }

    public XmlUnitBuilder usingTestDocumentBuilderFactoryClass(
            @Nullable Class<? extends DocumentBuilderFactory> testDocumentBuilderFactory) {
        properties.setTestDocumentBuilderFactoryClass(testDocumentBuilderFactory);
        return this;
    }

    public XmlUnitBuilder usingTransformerFactoryClass(
            @Nullable Class<? extends TransformerFactory> transformerFactory) {
        properties.setTransformerFactoryClass(transformerFactory);
        return this;
    }

    public XmlUnitBuilder usingSaxParserFactoryClass(
            @Nullable Class<? extends SAXParserFactory> saxParserFactory) {
        properties.setSaxParserFactoryClass(saxParserFactory);
        return this;
    }

    public XmlUnitBuilder usingXpathFactoryClass(
            @Nullable Class<? extends XPathFactory> xpathFactory) {
        properties.setXpathFactoryClass(xpathFactory);
        return this;
    }

    /**
     * Sets the URIResolver to use during transformations.
     */
    public XmlUnitBuilder usingUriResolver(@Nullable URIResolver uriResolver) {
        properties.setUriResolver(uriResolver);
        return this;
    }

    public XmlUnit build() {
        return new XmlUnit(properties);
    }

}
