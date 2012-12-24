package net.sf.xmlunit.diff.internal;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

public class Attributes {
    private final Attr schemaLocation;
    private final Attr noNamespaceSchemaLocation;
    private final List<Attr> regularAttributes;

    private Attributes(Attr schemaLocation, Attr noNamespaceSchemaLocation,
            List<Attr> remainingAttributes) {
        this.schemaLocation = schemaLocation;
        this.noNamespaceSchemaLocation = noNamespaceSchemaLocation;
        this.regularAttributes = remainingAttributes;
    }

    /**
     * Separates XML namespace related attributes from "regular" attributes.xb
     */
    public static Attributes from(NamedNodeMap map) {
        Attr sLoc = (Attr) map.getNamedItemNS(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
        Attr nNsLoc = (Attr) map.getNamedItemNS(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "noNamespaceSchemaLocation");
        List<Attr> regularAttrs = extractRegularAtrributes(map);
        return new Attributes(sLoc, nNsLoc, regularAttrs);
    }

    private static List<Attr> extractRegularAtrributes(NamedNodeMap map) {
        List<Attr> regularAttrs = new LinkedList<Attr>();
        final int len = map.getLength();
        for (int i = 0; i < len; i++) {
            Attr attr = (Attr) map.item(i);
            if (hasXmlNsUri(attr) || hasXmlSchemaInstNsUri(attr)) {
                continue;
            }
            regularAttrs.add(attr);
        }
        return regularAttrs;
    }

    private static boolean hasXmlSchemaInstNsUri(Attr attr) {
        return XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(attr.getNamespaceURI());
    }

    private static boolean hasXmlNsUri(Attr attr) {
        return XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI());
    }

    public Attr getSchemaLocation() {
        return schemaLocation;
    }

    public Attr getNoNamespaceSchemaLocation() {
        return noNamespaceSchemaLocation;
    }

    public List<Attr> getRegularAttributes() {
        return regularAttributes;
    }

    /**
     * Find the attribute with the same namespace and local name as a given
     * attribute in a list of attributes.
     */
    @Nullable
    public Attr findMatchingRegularAttr(Attr attrToMatch) {
        final boolean hasNs = attrToMatch.getNamespaceURI() != null;
        final String nsToMatch = attrToMatch.getNamespaceURI();
        final String nameToMatch = hasNs ? attrToMatch.getLocalName() : attrToMatch.getName();
        for (Attr attr : getRegularAttributes()) {
            if ((
                    (!hasNs && attr.getNamespaceURI() == null) ||
                    (hasNs && nsToMatch.equals(attr.getNamespaceURI()))
                    ) && (
                    (hasNs && nameToMatch.equals(attr.getLocalName())) ||
                    (!hasNs && nameToMatch.equals(attr.getName())))) {
                return attr;
            }
        }
        return null;
    }

}