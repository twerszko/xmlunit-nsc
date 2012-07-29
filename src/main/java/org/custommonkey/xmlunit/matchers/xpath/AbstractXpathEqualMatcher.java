package org.custommonkey.xmlunit.matchers.xpath;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLUnitProperties;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractXpathEqualMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    protected Document actualXpathDocument;
    protected Document expectedXpathDocument;

    protected boolean equalXpaths(
            String expectedXpath,
            Document expectedDocument,
            String actualXpath,
            Document actualDocument) throws ConfigurationException, XpathException {

        XpathEngine xpath = getXmlUnit().newXpathEngine();
        XMLUnitProperties properties = getXmlUnit().getProperties();
        expectedXpathDocument = asXpathResultDocument(
                XMLUnit.newControlParser(),
                xpath.getMatchingNodes(expectedXpath, expectedDocument));
        actualXpathDocument = asXpathResultDocument(
                XMLUnit.newTestParser(),
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
}
