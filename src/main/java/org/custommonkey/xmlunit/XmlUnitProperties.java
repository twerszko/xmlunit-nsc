package org.custommonkey.xmlunit;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;

public class XmlUnitProperties implements Cloneable {

    private boolean ignoreWhitespace = false;
    private boolean normalizeWhitespace = false;
    private boolean ignoreComments = false;
    private final boolean normalize = false;
    private NamespaceContext xpathNamespaceContext;
    private boolean compareUnmatched = true;
    private String xsltVersion = "1.0";
    private boolean ignoreAttributeOrder = true;
    private boolean expandEntityReference = false;
    private boolean ignoreDiffBetweenTextAndCDATA = false;
    private Class<? extends DocumentBuilderFactory> controlDocumentBuilderFactoryClass;
    private Class<? extends DocumentBuilderFactory> testDocumentBuilderFactoryClass;
    private Class<? extends TransformerFactory> transformerFactoryClass;
    private Class<? extends SAXParserFactory> saxParserFactoryClass;
    private Class<? extends XPathFactory> xpathFactoryClass;
    private URIResolver uriResolver;

    public boolean getIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
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
    public boolean getNormalizeWhitespace() {
        return normalizeWhitespace;
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
    public void setNormalizeWhitespace(boolean normalizeWhitespace) {
        this.normalizeWhitespace = normalizeWhitespace;
    }

    /**
     * Whether comments should be ignored.
     * 
     * <p>
     * The default value is false
     * </p>
     */
    public boolean getIgnoreComments() {
        return ignoreComments;
    }

    /**
     * Whether comments should be ignored.
     * 
     * <p>
     * The default value is false
     * </p>
     */
    public void setIgnoreComments(boolean ignoreComments) {
        this.ignoreComments = ignoreComments;
    }

    /**
     * Get the NamespaceContext to use in XPath tests.
     */
    public NamespaceContext getXpathNamespaceContext() {
        return xpathNamespaceContext;
    }

    /**
     * Set the NamespaceContext to use in XPath tests.
     */
    public void setXpathNamespaceContext(@Nullable NamespaceContext xpathNamespaceContext) {
        this.xpathNamespaceContext = xpathNamespaceContext;
    }

    /**
     * Whether the parser shall be instructed to expand entity references.
     */
    public boolean getCompareUnmatched() {
        return compareUnmatched;
    }

    /**
     * Whether to compare unmatched control nodes to unmatched test nodes.
     * 
     * <p>
     * Defaults to true.
     * </p>
     */
    public void setCompareUnmatched(boolean compare) {
        this.compareUnmatched = compare;
    }

    /**
     * The XSLT version set on stylesheets used internally.
     * 
     * <p>
     * Defaults to "1.0".
     * </p>
     */
    public String getXsltVersion() {
        return xsltVersion;
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
    public void setXsltVersion(String xsltVersion) {
        try {
            Number n = NumberFormat.getInstance(Locale.US).parse(xsltVersion);
            if (n.doubleValue() < 0) {
                throw new ConfigurationException(xsltVersion + " doesn't reperesent a positive number.");
            }
        } catch (ParseException e) {
            throw new ConfigurationException(e);
        }
        this.xsltVersion = xsltVersion;
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
    public boolean getIgnoreAttributeOrder() {
        return ignoreAttributeOrder;
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
    public void setIgnoreAttributeOrder(boolean ignore) {
        this.ignoreAttributeOrder = ignore;
    }

    /**
     * Whether the parser shall be instructed to expand entity references.
     */
    public boolean getExpandEntityReferences() {
        return expandEntityReference;
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
    public void setExpandEntityReferences(boolean expandEntityReferences) {
        this.expandEntityReference = expandEntityReferences;
    }

    /**
     * Whether CDATA sections and Text nodes should be considered the same.
     * 
     * <p>
     * The default is false.
     * </p>
     * 
     * <p>
     * This also set the DocumentBuilderFactory's
     * {@link javax.xml.parsers.DocumentBuilderFactory#setCoalescing coalescing}
     * flag on the factories for the control and test document.
     * </p>
     */
    public void setIgnoreDiffBetweenTextAndCDATA(boolean ignore) {
        ignoreDiffBetweenTextAndCDATA = ignore;
    }

    /**
     * Whether CDATA sections and Text nodes should be considered the same.
     * 
     * @return false by default
     */
    public boolean getIgnoreDiffBetweenTextAndCDATA() {
        return ignoreDiffBetweenTextAndCDATA;
    }

    // TODO docu
    /**
     * Override the <code>DocumentBuilderFactory</code> used to instantiate
     * parsers for the test XML in an XMLTestCase.
     */
    @Nullable
    public Class<? extends DocumentBuilderFactory> getControlDocumentBuilderFactoryClass() {
        return controlDocumentBuilderFactoryClass;
    }

    public void setControlDocumentBuilderFactoryClass(
            @Nullable Class<? extends DocumentBuilderFactory> controlDocumentBuilderFactory) {
        this.controlDocumentBuilderFactoryClass = controlDocumentBuilderFactory;
    }

    @Nullable
    public Class<? extends DocumentBuilderFactory> getTestDocumentBuilderFactoryClass() {
        return testDocumentBuilderFactoryClass;
    }

    public void setTestDocumentBuilderFactoryClass(
            @Nullable Class<? extends DocumentBuilderFactory> testDocumentBuilderFactory) {
        this.testDocumentBuilderFactoryClass = testDocumentBuilderFactory;
    }

    @Nullable
    public Class<? extends TransformerFactory> getTransformerFactoryClass() {
        return transformerFactoryClass;
    }

    public void setTransformerFactoryClass(
            @Nullable Class<? extends TransformerFactory> transformerFactory) {
        this.transformerFactoryClass = transformerFactory;
    }

    @Nullable
    public Class<? extends SAXParserFactory> getSaxParserFactoryClass() {
        return saxParserFactoryClass;
    }

    public void setSaxParserFactoryClass(
            @Nullable Class<? extends SAXParserFactory> saxParserFactory) {
        this.saxParserFactoryClass = saxParserFactory;
    }

    /**
     * Gets the class to use as XPathFactory when using JAXP 1.3.
     */
    @Nullable
    public Class<? extends XPathFactory> getXpathFactoryClass() {
        return xpathFactoryClass;
    }

    /**
     * Sets the class to use as XPathFactory when using JAXP 1.3.
     */
    public void setXpathFactoryClass(
            @Nullable Class<? extends XPathFactory> xpathFactory) {
        this.xpathFactoryClass = xpathFactory;
    }

    /**
     * Gets the URIResolver used during Transformations.
     */
    @Nullable
    public URIResolver getUriResolver() {
        return uriResolver;
    }

    /**
     * Sets the URIResolver to use during transformations.
     */
    public void setUriResolver(@Nullable URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    @Override
    public XmlUnitProperties clone() {
        try {
            return (XmlUnitProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
