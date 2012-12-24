package net.sf.xmlunit.diff;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class OrderPreservingNamedNodeMap implements NamedNodeMap {
    private final ArrayList<Attr> nodes = new ArrayList<Attr>();

    public void add(Attr attr) {
        nodes.add(attr);
    }

    @Override
    public int getLength() {
        return nodes.size();
    }

    @Override
    public Node item(int index) {
        return nodes.get(index);
    }

    @Override
    @Nullable
    public Node getNamedItem(String name) {
        for (Attr attr : nodes) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }

    @Override
    public Node getNamedItemNS(String ns, String localName) {
        for (Attr attr : nodes) {
            String attrLocalName = attr.getLocalName();
            String attrNamespaceURI = attr.getNamespaceURI();
            if (localName.equals(attrLocalName) && ns.equals(attrNamespaceURI)) {
                return attr;
            }
        }
        return null;
    }

    // not implemented, not needed in our case
    @Override
    public Node removeNamedItem(String n) {
        return fail();
    }

    @Override
    public Node removeNamedItemNS(String n1, String n2) {
        return fail();
    }

    @Override
    public Node setNamedItem(Node n) {
        return fail();
    }

    @Override
    public Node setNamedItemNS(Node n) {
        return fail();
    }

    private Node fail() {
        throw new RuntimeException("not implemented");
    }
}