package org.custommonkey.xmlunit.matchers.validation;

import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.util.AbstractXmlValidMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.xml.sax.InputSource;

/**
 * Assert that an InputSource containing XML contains valid XML: the document
 * must contain a DOCTYPE declaration to be validated, but the validation will
 * use the systemId to obtain the DTD
 */
public class XmlInputSourceValidMatcher extends AbstractXmlValidMatcher<InputSource> {

    public void describeTo(Description description) {
        description.appendText("valid xml");
    }

    @Override
    public boolean matchesSafely(InputSource xmlSource) {
        Validator validator;
        if (doctype == null) {
            validator = new Validator(xmlSource, systemId);
        } else {
            validator = new Validator(xmlSource, systemId, doctype);
        }
        return validator.isValid();
    }

    @Factory
    public static XmlInputSourceValidMatcher validXmlSource() {
        return new XmlInputSourceValidMatcher();
    }
}
