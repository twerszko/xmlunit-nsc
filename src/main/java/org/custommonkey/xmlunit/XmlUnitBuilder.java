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

    public XMLUnit build() {
        return new XMLUnit(properties);
    }
}
