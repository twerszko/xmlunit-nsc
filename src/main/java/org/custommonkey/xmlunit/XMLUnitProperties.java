package org.custommonkey.xmlunit;

import javax.annotation.Nullable;

public class XMLUnitProperties implements Cloneable {

    private boolean ignoreWhitespace = false;
    private boolean normalizeWhitespace = false;
    private boolean ignoreComments = false;
    private boolean normalize = false;
    private NamespaceContext xpathNamespaceContext;

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

    @Override
    public XMLUnitProperties clone() {
        try {
            return (XMLUnitProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
