package org.custommonkey.xmlunit;

public class XMLUnitProperties implements Cloneable {

    private boolean ignoreWhitespace = false;
    private boolean normalizeWhitespace = false;
    private boolean ignoreComments = false;

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

    @Override
    public XMLUnitProperties clone() {
        try {
            return (XMLUnitProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
