package org.custommonkey.xmlunit.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;

import org.custommonkey.xmlunit.DifferenceEngineContract;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.ElementQualifier;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.builder.Builder;
import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//TODO Add docu
public class DiffBuilder implements Builder<Diff> {

    private final XmlUnitProperties properties;
    private final DocumentUtils documentUtils;

    private Document controlDocument;
    private Document testDocument;

    private DifferenceEngineContract differenceEngineContract = null;
    private ElementQualifier elementQualifier = new ElementNameQualifier();

    public DiffBuilder(@Nullable XmlUnitProperties properties) {
        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }

        documentUtils = new DocumentUtils(this.properties);
    }

    public DiffBuilder withElementQualifier(ElementQualifier elementQualifier) {
        if (elementQualifier == null) {
            throw new IllegalArgumentException("ElementQualifier cannot be null");
        }
        this.elementQualifier = elementQualifier;
        return this;
    }

    public DiffBuilder withDifferenceEngineContract(@Nullable DifferenceEngineContract differenceEngineContract) {
        this.differenceEngineContract = differenceEngineContract;
        return this;
    }

    public DiffBuilder withControlDocument(String controlDocString) throws BuilderException {
        if (controlDocString == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.controlDocument = prepareDocumentFrom(new StringReader(controlDocString),
                documentUtils.newControlDocumentBuilder());
        return this;
    }

    public DiffBuilder withControlDocument(Reader controlDocReader) throws BuilderException {
        if (controlDocReader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        this.controlDocument = prepareDocumentFrom(controlDocReader, documentUtils.newControlDocumentBuilder());
        return this;
    }

    public DiffBuilder withControlDocument(Document controlDoc) throws BuilderException {
        if (controlDoc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.controlDocument = controlDoc;
        return this;
    }

    public DiffBuilder withControlDocument(DOMSource controlDomSource) throws BuilderException {
        if (controlDomSource == null) {
            throw new IllegalArgumentException("DOMSource cannot be null");
        }
        this.controlDocument = controlDomSource.getNode().getOwnerDocument();
        return this;
    }

    public DiffBuilder withControlDocument(InputSource controlDomSource) throws BuilderException {
        if (controlDomSource == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }

        this.controlDocument = prepareDocumentFrom(controlDomSource, documentUtils.newControlDocumentBuilder());
        return this;
    }

    public DiffBuilder withTestDocument(String testDocString) throws BuilderException {
        if (testDocString == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.testDocument = prepareDocumentFrom(new StringReader(testDocString), documentUtils.newTestDocumentBuilder());
        return this;
    }

    public DiffBuilder withTestDocument(Reader testDocReader) throws BuilderException {
        if (testDocReader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        this.testDocument = prepareDocumentFrom(testDocReader, documentUtils.newTestDocumentBuilder());
        return this;
    }

    public DiffBuilder withTestDocument(Document testDoc) throws BuilderException {
        if (testDoc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.testDocument = testDoc;
        return this;
    }

    public DiffBuilder withTestDocument(DOMSource testDomSource) throws BuilderException {
        if (testDomSource == null) {
            throw new IllegalArgumentException("DOMSource cannot be null");
        }
        this.testDocument = testDomSource.getNode().getOwnerDocument();
        return this;
    }

    public DiffBuilder withTestDocument(InputSource testDomSource) throws BuilderException {
        if (testDomSource == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }

        this.testDocument = prepareDocumentFrom(testDomSource, documentUtils.newTestDocumentBuilder());
        return this;
    }

    private Document prepareDocumentFrom(InputSource inputSource, DocumentBuilder parser) throws BuilderException {
        try {
            Document document = documentUtils.buildDocument(parser, inputSource);
            return document;
        } catch (IOException e) {
            throw new BuilderException("Failed to build Diff!", e);
        } catch (SAXException e) {
            throw new BuilderException("Failed to build Diff!", e);
        }
    }

    private Document prepareDocumentFrom(Reader reader, DocumentBuilder parser) throws BuilderException {
        try {
            Document document = documentUtils.buildDocument(parser, reader);
            return document;
        } catch (ConfigurationException e) {
            throw new BuilderException("Failed to build Diff!", e);
        } catch (SAXException e) {
            throw new BuilderException("Failed to build Diff!", e);
        } catch (IOException e) {
            throw new BuilderException("Failed to build Diff!", e);
        }
    }

    private void validate() throws BuilderException {
        if (controlDocument == null) {
            throw new BuilderException("Control document must be provided!");
        }
        if (testDocument == null) {
            throw new BuilderException("Test document must be provided!");
        }
        if (elementQualifier == null) {
            throw new BuilderException("Element qualifier cannot be null!");
        }
    }

    public Diff build() throws BuilderException {
        validate();
        return new Diff(
                properties,
                controlDocument,
                testDocument,
                differenceEngineContract,
                elementQualifier);
    }
}
