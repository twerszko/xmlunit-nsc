package org.custommonkey.xmlunit;

public class XMLUnitProperties implements Cloneable {

    private boolean ignoreWhitespace = false;

    public boolean getIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
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
