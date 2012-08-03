package org.custommonkey.xmlunit.diff;

import java.io.IOException;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;

import org.custommonkey.xmlunit.DifferenceEngineContract;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.ElementQualifier;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.builders.Builder;
import org.custommonkey.xmlunit.builders.BuilderException;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DiffBuilder implements Builder<Diff> {

    private final XmlUnitProperties properties;
    private final DocumentUtils documentUtils;

    private Document controlDocument;
    private Document testDocument;

    private DifferenceEngineContract differenceEngineContract = null;
    private ElementQualifier elementQualifier = new ElementNameQualifier();

    // TODO This class is not finished

    public DiffBuilder(@Nullable XmlUnitProperties properties) {
        if (properties == null) {
            this.properties = new XmlUnitProperties();
        } else {
            this.properties = properties.clone();
        }

        documentUtils = new DocumentUtils(this.properties);
    }

    public DiffBuilder withElementQualifier(ElementQualifier elementQualifier) {
        this.elementQualifier = elementQualifier;
        return this;
    }

    public DiffBuilder withDifferenceEngineContract(DifferenceEngineContract differenceEngineContract) {
        this.differenceEngineContract = differenceEngineContract;
        return this;
    }

    public DiffBuilder withControlDocument(String controlDocString) throws BuilderException {
        this.controlDocument = prepareDocumentFrom(controlDocString);
        return this;
    }

    public DiffBuilder withTestDocument(String testDocString) throws BuilderException {
        this.testDocument = prepareDocumentFrom(testDocString);
        return this;
    }

    private Document prepareDocumentFrom(String string) throws BuilderException {
        try {
            DocumentBuilder parser = documentUtils.newControlParser();
            Document document = documentUtils.buildDocument(parser, new StringReader(string));
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
