package org.custommonkey.xmlunit;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.annotation.Nullable;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;

public class XMLUnitProperties implements Cloneable {

    private boolean ignoreWhitespace = false;
    private boolean normalizeWhitespace = false;
    private boolean ignoreComments = false;
    private boolean normalize = false;
    private NamespaceContext xpathNamespaceContext;
    private boolean compareUnmatched = true;
    private String xsltVersion = "1.0";

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
     * Whether Text nodes should be normalized.
     * 
     * <p>
     * The default value is false
     * </p>
     */
    public boolean getNormalize() {
        return normalize;
    }

    /**
     * Whether Text nodes should be normalized.
     * 
     * <p>
     * The default value is false
     * </p>
     * 
     * <p>
     * <b>Note:</b> if you are only working with documents read from streams
     * (like files or network connections) or working with strings, there is no
     * reason to change the default since the XML parser is required to
     * normalize the documents. If you are testing {@link org.w3c.Document
     * Document} instances you've created in code, you may want to alter the
     * default behavior.
     * </p>
     * 
     * <p>
     * <b>Note2:</b> depending on the XML parser or XSLT transformer you use,
     * setting {@link setIgnoreWhitespace ignoreWhitespace} or
     * {@link setIgnoreComments ignoreComments} to true may have already
     * normalized your document and this setting doesn't have any effect
     * anymore.
     * </p>
     */
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
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

    @Override
    public XMLUnitProperties clone() {
        try {
            return (XMLUnitProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
