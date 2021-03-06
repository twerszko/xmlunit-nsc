/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.custommonkey.xmlunit.diff;

import java.io.Reader;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEngineFactory;
import net.sf.xmlunit.util.Preconditions;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.builder.Builder;
import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.Input;

public class DiffBuilder {
    final DocumentUtils documentUtils;

    Source testSource;
    Source controlSource;

    DifferenceEngineFactory engineFactory;

    public DiffBuilder(@Nullable XmlUnitProperties properties) {
        XmlUnitProperties xmlUnitProperties = properties;
        if (xmlUnitProperties == null) {
            xmlUnitProperties = new XmlUnitProperties();
        }

        documentUtils = new DocumentUtils(xmlUnitProperties);
        engineFactory = new DefaultDifferenceEngineFactory(xmlUnitProperties);
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

        @Override
        public Diff build() throws BuilderException {
            validate();
            testSource = documentUtils.applySourceProperties(testSource);
            controlSource = documentUtils.applySourceProperties(controlSource);
            return new Diff(DiffBuilder.this);
        }
    }
}
