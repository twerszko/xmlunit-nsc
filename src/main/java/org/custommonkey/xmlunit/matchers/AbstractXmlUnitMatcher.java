package org.custommonkey.xmlunit.matchers;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLUnitProperties;
import org.custommonkey.xmlunit.XmlUnitBuilder;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractXmlUnitMatcher<T> extends TypeSafeMatcher<T> {
    protected XMLUnit xmlUnit;

    public AbstractXmlUnitMatcher<T> using(@Nullable XMLUnit xmlUnit) {
        this.xmlUnit = xmlUnit;
        return this;
    }

    protected XMLUnit getXmlUnit() {
        XMLUnit xmlUnit;
        if (this.xmlUnit == null) {
            xmlUnit = new XmlUnitBuilder().build();
        } else {
            xmlUnit = this.xmlUnit;
        }
        return xmlUnit;
    }

    protected boolean equalXpaths(
            String expectedXpath,
            Document expectedDocument,
            String actualXpath,
            Document actualDocument) throws ConfigurationException, XpathException {

        XpathEngine xpath = XMLUnit.newXpathEngine();
        XMLUnitProperties properties = getXmlUnit().getProperties();
        Document expectedXpathDocument = asXpathResultDocument(XMLUnit.newControlParser(),
                xpath.getMatchingNodes(expectedXpath, expectedDocument));
        Document actualXpathDocument = asXpathResultDocument(XMLUnit.newTestParser(),
                xpath.getMatchingNodes(actualXpath, actualDocument));
        Diff diff = new Diff(properties, expectedXpathDocument, actualXpathDocument);

        return diff.similar();
    }

    private Document asXpathResultDocument(
            DocumentBuilder builder,
            NodeList nodes) {

        Document document = builder.newDocument();
        Element root = document.createElement("xpathResult");
        document.appendChild(root);
        final int length = nodes.getLength();
        for (int i = 0; i < length; i++) {
            Node n = document.importNode(nodes.item(i), true);
            if (n instanceof Attr) {
                root.setAttributeNodeNS((Attr) n);
            } else {
                root.appendChild(n);
            }
        }
        return document;
    }

    protected String quote(String string) {
        return "\"" + string + "\"";
    }
}
