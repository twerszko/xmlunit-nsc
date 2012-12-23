package net.sf.xmlunit.diff.internal;

import java.util.LinkedList;
import java.util.List;

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
     * Separates XML namespace related attributes from "normal" attributes.xb
     */
    public static Attributes createFrom(NamedNodeMap map) {
        Attr sLoc = (Attr) map
                .getNamedItemNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
        Attr nNsLoc = (Attr) map
                .getNamedItemNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "noNamespaceSchemaLocation");
        List<Attr> rest = new LinkedList<Attr>();
        final int len = map.getLength();
        for (int i = 0; i < len; i++) {
            Attr a = (Attr) map.item(i);
            if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())
                    && !XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(a.getNamespaceURI())) {
                rest.add(a);
            }
        }
        return new Attributes(sLoc, nNsLoc, rest);
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

}