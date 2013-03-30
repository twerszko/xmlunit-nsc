package org.custommonkey.xmlunit.diff;

import java.io.Reader;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
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
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DiffBuilder {
    XmlUnitProperties properties;
    final DocumentUtils documentUtils;

    Source testSource;
    Source controlSource;

    DifferenceEngineFactory engineFactory;
    ElementSelector elementSelector = ElementSelectors.byName;

    public DiffBuilder(@Nullable XmlUnitProperties properties) {
        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }

        documentUtils = new DocumentUtils(this.properties);
        engineFactory = new DefaultDifferenceEngineFactory(this.properties);
    }

    private Document prepareDocumentFrom(InputSource inputSource, DocumentBuilder parser) throws BuilderException {
        try {
            Document document = documentUtils.buildDocument(parser, inputSource);
            return document;
        } catch (Exception e) {
            throw new BuilderException("Failed to build Diff!", e);
        }
    }

    private Document prepareDocumentFrom(Reader reader, DocumentBuilder parser) throws BuilderException {
        try {
            Document document = documentUtils.buildDocument(parser, reader);
            return document;
        } catch (Exception e) {
            throw new BuilderException("Failed to build Diff!", e);
        }
    }

    public DiffTestDocBuilder betweenControlDocument(Document document) {
        Preconditions.checkArgument(document != null, "Document cannot be null");
        this.controlSource = Input.fromDocument(document).build();
        ;
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(Source source) {
        Preconditions.checkArgument(source != null, "Source cannot be null");
        // TODO probably this should be converted to DOM first to apply all
        // properties like coalescing
        this.controlSource = source;
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(InputSource source) throws BuilderException {
        Preconditions.checkArgument(source != null, "Source cannot be null");
        Document controlDoc = prepareDocumentFrom(source, documentUtils.newControlDocumentBuilder());
        this.controlSource = Input.fromDocument(controlDoc).build();
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(String string) throws BuilderException {
        Preconditions.checkArgument(string != null, "String cannot be null");
        Document controlDoc = prepareDocumentFrom(
                new StringReader(string),
                documentUtils.newControlDocumentBuilder());
        this.controlSource = Input.fromDocument(controlDoc).build();
        return new DiffTestDocBuilder();
    }

    public DiffTestDocBuilder betweenControlDocument(Reader reader) throws BuilderException {
        Preconditions.checkArgument(reader != null, "Reader cannot be null");
        Document controlDoc = prepareDocumentFrom(reader, documentUtils.newControlDocumentBuilder());
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

        public DiffPropertiesBuilder andTestDocument(Document document) throws BuilderException {
            Preconditions.checkArgument(document != null, "Document cannot be null");
            testSource = Input.fromDocument(document).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(DOMSource source) throws BuilderException {
            Preconditions.checkArgument(source != null, "Source cannot be null");
            // TODO
            // Document testDoc = source.getNode().getOwnerDocument();
            // testSource = Input.fromDocument(testDoc).build();
            testSource = source;
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(InputSource source) throws BuilderException {
            Preconditions.checkArgument(source != null, "Source cannot be null");
            Document testDoc = prepareDocumentFrom(source, documentUtils.newTestDocumentBuilder());
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(String string) throws BuilderException {
            Preconditions.checkArgument(string != null, "String cannot be null");
            Document testDoc = prepareDocumentFrom(
                    new StringReader(string),
                    documentUtils.newTestDocumentBuilder());
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

        public DiffPropertiesBuilder andTestDocument(Reader reader) throws BuilderException {
            Preconditions.checkArgument(reader != null, "Reader cannot be null");
            Document testDoc = prepareDocumentFrom(reader, documentUtils.newTestDocumentBuilder());
            testSource = Input.fromDocument(testDoc).build();
            return new DiffPropertiesBuilder();
        }

    }

    public class DiffPropertiesBuilder implements Builder<Diff> {

        private DiffPropertiesBuilder() {
        }

        public DiffPropertiesBuilder usingDifferenceEngineFactory(DifferenceEngineFactory factory) {
            Preconditions.checkArgument(factory != null, "DifferenceEngineFactory canno be null");
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