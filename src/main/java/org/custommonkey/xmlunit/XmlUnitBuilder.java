package org.custommonkey.xmlunit;

import javax.annotation.Nullable;

public class XmlUnitBuilder {
    private final XMLUnitProperties properties;

    public XmlUnitBuilder() {
        this.properties = new XMLUnitProperties();
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
    public XmlUnitBuilder normalizing(boolean normalize) {
        properties.setNormalize(normalize);
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

    public XMLUnit build() {
        return new XMLUnit(properties);
    }

    /**
     * Set the NamespaceContext to use in XPath tests.
     */
    public XmlUnitBuilder withXpathNamespaceContext(@Nullable NamespaceContext xpathNamespaceContext) {
        properties.setXpathNamespaceContext(xpathNamespaceContext);
        return this;
    }
}
