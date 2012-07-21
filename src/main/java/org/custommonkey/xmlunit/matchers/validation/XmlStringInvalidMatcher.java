package org.custommonkey.xmlunit.matchers.validation;

import java.io.StringReader;

import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.matchers.AbstractXmlValidMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * It is negation of: Assert that a String containing XML contains valid XML:
 * the String must contain a DOCTYPE declaration to be validated, but the
 * validation will use the systemId to obtain the DTD
 */
public class XmlStringInvalidMatcher extends AbstractXmlValidMatcher<String> {

    public void describeTo(Description description) {
        description.appendText("invalid xml");
    }

    @Override
    public boolean matchesSafely(String xmlString) {
        Validator validator;
        if (doctype == null) {
            validator = new Validator(xmlString, systemId);
        } else {
            validator = new Validator(new StringReader(xmlString), systemId, doctype);
        }
        boolean valid = validator.isValid();
        return !valid;
    }

    @Factory
    public static XmlStringInvalidMatcher invalidXmlString() {
        return new XmlStringInvalidMatcher();
    }
}
