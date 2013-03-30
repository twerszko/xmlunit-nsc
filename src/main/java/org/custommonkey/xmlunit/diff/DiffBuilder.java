package org.custommonkey.xmlunit.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.DifferenceEngineFactory;
import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ElementSelectors;
import net.sf.xmlunit.input.CommentLessSource;
import net.sf.xmlunit.input.WhitespaceNormalizedSource;
import net.sf.xmlunit.input.WhitespaceStrippedSource;
import net.sf.xmlunit.util.Preconditions;

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

    Source testSource;
    Source controlSource;

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
        Preconditions.checkArgument(controlDoc != null, "Document cannot be null");
        this.controlSource = Input.fromDocument(controlDoc).build();
        ;
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(DOMSource controlDomSource) {
        Preconditions.checkArgument(controlDomSource != null, "DOMSource cannot be null");
        this.controlSource = controlDomSource;
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(InputSource controlInputSource) throws BuilderException {
        Preconditions.checkArgument(controlInputSource != null, "InputSource cannot be null");
        Document controlDoc = prepareDocumentFrom(controlInputSource, documentUtils.newControlDocumentBuilder());
        this.controlSource = Input.fromDocument(controlDoc).build();
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(String controlDocString) throws BuilderException {
        Preconditions.checkArgument(controlDocString != null, "String cannot be null");
        Document controlDoc = prepareDocumentFrom(
                new StringReader(controlDocString),
                documentUtils.newControlDocumentBuilder());
        this.controlSource = Input.fromDocument(controlDoc).build();
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(Reader controlDocReader) throws BuilderException {
        Preconditions.checkArgument(controlDocReader != null, "Reader cannot be null");
        Document controlDoc = prepareDocumentFrom(controlDocReader, documentUtils.newControlDocumentBuilder());
        this.controlSource = Input.fromDocument(controlDoc).build();
        return new DiffTestDocBuilder();
    }

    private void validate() throws BuilderException {
        if (controlSource == null) {
            throw new BuilderException("Control document must be provided!");
        }
        if (testSource == null) {
            throw new BuilderException("Test document must be provided!");
        }
        if (elementSelector == null) {
            throw new BuilderException("Element qualifier cannot be null!");
        }
    }

    private Source applyProperties(Source input) {
        Source result = input;
        if (properties.getIgnoreComments()) {
            result = new CommentLessSource(result);
        }
        if (properties.getNormalizeWhitespace()) {
            result = new WhitespaceNormalizedSource(result);
        }
        if (properties.getIgnoreWhitespace()) {
            result = new WhitespaceStrippedSource(result);
        }
        return result;
    }

    public class DiffTestDocBuilder {

        private DiffTestDocBuilder() {
        }

        public DiffPropertiesBuilder andTestDocument(Document testDoc) throws BuilderException {
            Preconditions.checkArgument(testDoc != null, "Document cannot be null");
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(DOMSource testDomSource) throws BuilderException {
            Preconditions.checkArgument(testDomSource != null, "DOMSource cannot be null");
            Document testDoc = testDomSource.getNode().getOwnerDocument();
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(InputSource testDomSource) throws BuilderException {
            Preconditions.checkArgument(testDomSource != null, "InputSource cannot be null");
            Document testDoc = prepareDocumentFrom(testDomSource, documentUtils.newTestDocumentBuilder());
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(String testDocString) throws BuilderException {
            Preconditions.checkArgument(testDocString != null, "String cannot be null");
            Document testDoc = prepareDocumentFrom(
                    new StringReader(testDocString),
                    documentUtils.newTestDocumentBuilder());
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(Reader testDocReader) throws BuilderException {
            Preconditions.checkArgument(testDocReader != null, "Reader cannot be null");
            Document testDoc = prepareDocumentFrom(testDocReader, documentUtils.newTestDocumentBuilder());
            testSource = Input.fromDocument(testDoc).build();
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
            Preconditions.checkArgument(elementSelector != null, "ElementQualifier cannot be null");
            elementSelector = selector;
            return this;
        }

        @Override
        public Diff build() throws BuilderException {
            validate();
            testSource = applyProperties(testSource);
            controlSource = applyProperties(controlSource);
            return new Diff(DiffBuilder.this);
        }
    }
}