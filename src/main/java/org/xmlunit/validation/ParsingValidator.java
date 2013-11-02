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
package org.xmlunit.validation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;

import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Validator implementation that uses "the old way" of validating an XML input
 * by parsing the input.
 * 
 * <p>
 * Even though this implementation supports W3C Schema you shouldn't use it for
 * that language but rather use JAXPValidator.
 * </p>
 */
public class ParsingValidator extends Validator {
    private final Language language;

    public ParsingValidator(Language language) {
        switch (language) {
        case XML_SCHEMA:
        case XML_DTD:
            break;
        default:
            throw new IllegalArgumentException("only DTD and W3C Schema"
                    + " validation are supported by ParsingValidator");
        }
        this.language = language;
    }

    @Override
    public ValidationResult validateSchema() {
        throw new XMLUnitRuntimeException("Schema validation is not supported by"
                + " ParsingValidator");
    }

    @Override
    public ValidationResult validateInstance(Source source) {
        SAXParser parser;
        try {
            parser = createParser();
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(ex);
        } catch (SAXException ex) {
            throw new XMLUnitRuntimeException(ex);
        }

        try {
            return doInstanceValidation(parser, source);
        } catch (SAXNotRecognizedException ex) {
            throw new ConfigurationException(ex);
        } catch (SAXNotSupportedException ex) {
            throw new ConfigurationException(ex);
        } catch (IOException ex) {
            throw new XMLUnitRuntimeException(ex);
        }
    }

    private ValidationResult doInstanceValidation(SAXParser parser, Source source)
            throws SAXNotRecognizedException, SAXNotSupportedException, IOException {
        if (language == Language.XML_SCHEMA) {
            parser.setProperty(Properties.SCHEMA_LANGUAGE, language.getNsUri());
        }
        final Source[] sources = getSchemaSources();
        Handler handler = new Handler();
        if (sources.length != 0) {
            if (language == Language.XML_SCHEMA) {
                InputSource[] schemaSource = new InputSource[sources.length];
                for (int i = 0; i < sources.length; i++) {
                    schemaSource[i] = Convert.toInputSource(sources[i]);
                }
                parser.setProperty(Properties.SCHEMA_SOURCE, schemaSource);
            } else if (sources.length == 1) {
                handler.setSchemaSystemId(sources[0].getSystemId());
            }
        }
        parse(source, parser, handler);
        return handler.getResult();
    }

    private SAXParser createParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        SAXParser parser = factory.newSAXParser();
        return parser;
    }

    private void parse(Source source, SAXParser parser, Handler handler) throws IOException {
        InputSource input = Convert.toInputSource(source);
        try {
            parser.parse(input, handler);
        } catch (SAXParseException e) {
            handler.error(e);
        } catch (SAXException e) {
            throw new XMLUnitRuntimeException(e);
        }
    }

    private static class Properties {
        static final String SCHEMA_LANGUAGE =
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

        static final String SCHEMA_SOURCE =
                "http://java.sun.com/xml/jaxp/properties/schemaSource";
    }

    private class Handler extends DefaultHandler {
        private final ValidationHandler v = new ValidationHandler();
        private String systemId;

        @Override
        public void error(SAXParseException e) {
            v.error(e);
        }

        @Override
        public void fatalError(SAXParseException e) {
            v.fatalError(e);
        }

        @Override
        public void warning(SAXParseException e) {
            v.warning(e);
        }

        private void setSchemaSystemId(String id) {
            systemId = id;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (this.systemId != null && (getSchemaURI() == null || getSchemaURI().equals(publicId))) {
                return new InputSource(this.systemId);
            }
            return super.resolveEntity(publicId, systemId);
        }

        ValidationResult getResult() {
            return v.getResult();
        }
    }
}
