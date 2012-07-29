package org.custommonkey.xmlunit;

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
}
