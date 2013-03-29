package org.custommonkey.xmlunit.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ElementSelectors;
import net.sf.xmlunit.diff.comparators.DifferenceEngineFactory;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.builder.Builder;
import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DiffBuilder {
    XmlUnitProperties properties;
    final DocumentUtils documentUtils;

    Document testDocument;
    Document controlDocument;

    DifferenceEngineFactory engineFactory = null;
    ElementSelector elementSelector = ElementSelectors.byName;

    public DiffBuilder(@Nullable XmlUnitProperties properties) {
        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }

        documentUtils = new DocumentUtils(this.properties);
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

    public DiffTestDocBuilder betweenControlDocument(Document controlDoc) {
        if (controlDoc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.controlDocument = controlDoc;
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(DOMSource controlDomSource) {
        if (controlDomSource == null) {
            throw new IllegalArgumentException("DOMSource cannot be null");
        }
        this.controlDocument = controlDomSource.getNode().getOwnerDocument();
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(InputSource controlInputSource) throws BuilderException {
        if (controlInputSource == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }

        this.controlDocument = prepareDocumentFrom(controlInputSource, documentUtils.newControlDocumentBuilder());
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(String controlDocString) throws BuilderException {
        if (controlDocString == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.controlDocument = prepareDocumentFrom(new StringReader(controlDocString),
                documentUtils.newControlDocumentBuilder());
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(Reader controlDocReader) throws BuilderException {
        if (controlDocReader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        this.controlDocument = prepareDocumentFrom(controlDocReader, documentUtils.newControlDocumentBuilder());
        return new DiffTestDocBuilder();
    }

    private void validate() throws BuilderException {
        if (controlDocument == null) {
            throw new BuilderException("Control document must be provided!");
        }
        if (testDocument == null) {
            throw new BuilderException("Test document must be provided!");
        }
        if (elementSelector == null) {
            throw new BuilderException("Element qualifier cannot be null!");
        }
    }

    public class DiffTestDocBuilder {

        private DiffTestDocBuilder() {
        }

        public DiffPropertiesBuilder andTestDocument(Document testDoc) throws BuilderException {
            if (testDoc == null) {
                throw new IllegalArgumentException("Document cannot be null");
            }
            testDocument = testDoc;
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(DOMSource testDomSource) throws BuilderException {
            if (testDomSource == null) {
                throw new IllegalArgumentException("DOMSource cannot be null");
            }
            testDocument = testDomSource.getNode().getOwnerDocument();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(InputSource testDomSource) throws BuilderException {
            if (testDomSource == null) {
                throw new IllegalArgumentException("InputSource cannot be null");
            }

            testDocument = prepareDocumentFrom(testDomSource, documentUtils.newTestDocumentBuilder());
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(String testDocString) throws BuilderException {
            if (testDocString == null) {
                throw new IllegalArgumentException("String cannot be null");
            }
            testDocument = prepareDocumentFrom(new StringReader(testDocString),
                    documentUtils.newTestDocumentBuilder());
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(Reader testDocReader) throws BuilderException {
            if (testDocReader == null) {
                throw new IllegalArgumentException("Reader cannot be null");
            }
            testDocument = prepareDocumentFrom(testDocReader, documentUtils.newTestDocumentBuilder());
            return new DiffPropertiesBuilder();
        }

    }

    public class DiffPropertiesBuilder implements Builder<Diff> {

        private DiffPropertiesBuilder() {
        }

        public DiffPropertiesBuilder usingDifferenceEngineFactory(@Nullable DifferenceEngineFactory factory) {
            engineFactory = factory;
            return this;
        }

        public DiffPropertiesBuilder withElementSelector(ElementSelector selector) {
            if (elementSelector == null) {
                throw new IllegalArgumentException("ElementQualifier cannot be null");
            }
            elementSelector = selector;
            return this;
        }

        @Override
        public Diff build() throws BuilderException {
            validate();
            return new Diff(DiffBuilder.this);
        }

    }
}