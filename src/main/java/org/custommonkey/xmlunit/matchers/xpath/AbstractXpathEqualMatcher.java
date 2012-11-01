package org.custommonkey.xmlunit.matchers.xpath;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.custommonkey.xmlunit.matchers.AbstractXmlUnitMatcher;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractXpathEqualMatcher extends AbstractXmlUnitMatcher<XpathWrapper> {

    protected Document actualXpathDocument;
    protected Document expectedXpathDocument;

    protected boolean equalXpaths(
            String expectedXpath,
            Document expectedDocument,
            String actualXpath,
            Document actualDocument) throws ConfigurationException, XpathException {

        XmlUnitProperties properties = getXmlUnit().getProperties();
        DocumentUtils documentUtils = getXmlUnit().newDocumentUtils();
        XpathEngine xpath = documentUtils.newXpathEngine();

        expectedXpathDocument = asXpathResultDocument(
                documentUtils.newControlDocumentBuilder(),
                xpath.getMatchingNodes(expectedXpath, expectedDocument));
        actualXpathDocument = asXpathResultDocument(
                documentUtils.newTestDocumentBuilder(),
                xpath.getMatchingNodes(actualXpath, actualDocument));
        Diff diff;
        try {
            diff = Diff.newDiff(properties)
                    .betweenControlDocument(expectedXpathDocument)
                    .andTestDocument(actualXpathDocument)
                    .build();
        } catch (BuilderException e) {
            throw new IllegalArgumentException("Unexpected error!", e);
        }
        return diff.similar();
    }

    private Document asXpathResultDocument(
            DocumentBuilder builder,
            Iterable<Node> nodes) {

        Document document = builder.newDocument();
        Element root = document.createElement("xpathResult");
        document.appendChild(root);

        Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
            Node n = document.importNode(it.next(), true);
            if (n instanceof Attr) {
                root.setAttributeNodeNS((Attr) n);
            } else {
                root.appendChild(n);
            }
        }
        return document;
    }
}
