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

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validator using the javax.xml.validation namespace.
 */
public class JAXPValidator extends Validator {
    private final String language;
    private final SchemaFactory factory;

    public JAXPValidator(String language) {
        this(language, null);
    }

    public JAXPValidator(String language, SchemaFactory factory) {
        this.language = language;
        this.factory = factory;
    }

    private SchemaFactory getFactory() {
        return factory == null ? SchemaFactory.newInstance(language) : factory;
    }

    @Override
    public ValidationResult validateSchema() {
        ValidationHandler handler = new ValidationHandler();
        SchemaFactory factory = getFactory();
        factory.setErrorHandler(handler);
        try {
            factory.newSchema(getSchemaSources());
        } catch (SAXParseException e) {
            handler.error(e);
        } catch (Exception e) {
            throw new XMLUnitRuntimeException(e);
        } finally {
            factory.setErrorHandler(null);
        }
        return handler.getResult();
    }

    @Override
    public ValidationResult validateInstance(Source s) {
        Schema schema;
        try {
            schema = getFactory().newSchema(getSchemaSources());
        } catch (SAXException e) {
            throw new XMLUnitRuntimeException("The schema is invalid", e);
        }
        ValidationHandler handler = new ValidationHandler();
        javax.xml.validation.Validator validator = schema.newValidator();
        validator.setErrorHandler(handler);
        try {
            validator.validate(s);
        } catch (SAXParseException e) {
            handler.error(e);
        } catch (Exception e) {
            throw new XMLUnitRuntimeException(e);
        }
        return handler.getResult();
    }

}
