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
package net.sf.xmlunit.validation;

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
    public ValidationResult validateInstance(Source s) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            if (language == Language.XML_SCHEMA) {
                parser.setProperty(Properties.SCHEMA_LANGUAGE, language.getNsUri());
            }
            final Source[] source = getSchemaSources();
            Handler handler = new Handler();
            if (source.length != 0) {
                if (language == Language.XML_SCHEMA) {
                    InputSource[] schemaSource = new InputSource[source.length];
                    for (int i = 0; i < source.length; i++) {
                        schemaSource[i] = Convert.toInputSource(source[i]);
                    }
                    parser.setProperty(Properties.SCHEMA_SOURCE, schemaSource);
                } else if (source.length == 1) {
                    handler.setSchemaSystemId(source[0].getSystemId());
                }
            }
            InputSource input = Convert.toInputSource(s);
            try {
                parser.parse(input, handler);
            } catch (SAXParseException e) {
                handler.error(e);
            } catch (SAXException e) {
                throw new XMLUnitRuntimeException(e);
            }
            return handler.getResult();
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(ex);
        } catch (SAXNotRecognizedException ex) {
            throw new ConfigurationException(ex);
        } catch (SAXNotSupportedException ex) {
            throw new ConfigurationException(ex);
        } catch (SAXException ex) {
            throw new XMLUnitRuntimeException(ex);
        } catch (IOException ex) {
            throw new XMLUnitRuntimeException(ex);
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
        public InputSource resolveEntity(String publicId,
                String systemId)
                throws java.io.IOException, SAXException {
            if (this.systemId != null &&
                    (getSchemaURI() == null || getSchemaURI().equals(publicId))) {
                return new InputSource(this.systemId);
            }
            return super.resolveEntity(publicId, systemId);
        }

        ValidationResult getResult() {
            return v.getResult();
        }
    }
}
