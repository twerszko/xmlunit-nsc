package org.custommonkey.xmlunit.matchers.validation;

import org.custommonkey.xmlunit.Validator;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

/**
 * It is negation of: Assert that a Validator instance returns
 * <code>isValid() == true</code>
 * 
 */
public class XmlValidatorInvalidMatcher extends TypeSafeMatcher<Validator> {

    public void describeTo(Description description) {
        description.appendText("invalid matcher");
    }

    @Override
    public boolean matchesSafely(Validator validator) {
        if (validator == null) {
            return false;
        }

        return !validator.isValid();
    }

    @Factory
    public static XmlValidatorInvalidMatcher invalidXml() {
        return new XmlValidatorInvalidMatcher();
    }

}
