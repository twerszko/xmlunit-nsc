package net.sf.xmlunit.xpath;

import org.w3c.dom.Document;

//TODO: docu
public class XpathWrapper {
    private final String xpath;
    private final Document document;

    public XpathWrapper(String xpath, Document document) {
        if (xpath == null) {
            throw new IllegalArgumentException("Xpath cannot be null!");
        }
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.xpath = xpath;
        this.document = document;
    }

    public String getXpath() {
        return xpath;
    }

    public Document getDocument() {
        return document;
    }

}
