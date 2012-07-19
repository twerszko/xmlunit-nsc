package org.custommonkey.xmlunit.matchers.validation;

import org.custommonkey.xmlunit.Validator;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

/**
 * Assert that a Validator instance returns <code>isValid() == true</code>
 * 
 */
public class XmlValidatorValidMatcher extends TypeSafeMatcher<Validator> {

    public void describeTo(Description description) {
        description.appendText("valid matcher");
    }

    @Override
    public boolean matchesSafely(Validator validator) {
        if (validator == null) {
            return false;
        }

        return validator.isValid();
    }

    @Factory
    public static XmlValidatorValidMatcher validXml() {
        return new XmlValidatorValidMatcher();
    }

}
