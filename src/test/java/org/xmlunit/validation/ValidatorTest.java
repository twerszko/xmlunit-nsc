package org.xmlunit.validation;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class ValidatorTest {

    @Test
    public void should_return_parsing_validator_for_dtd() throws Exception {
        Validator validator = Validator.forLanguage(Language.XML_DTD.getNsUri());
        assertThat(validator).isInstanceOf(ParsingValidator.class);
    }

    @Test
    public void should_return_jaxp_validator_for_schema() throws Exception {
        Validator validator = Validator.forLanguage(Language.XML_SCHEMA.getNsUri());
        assertThat(validator).isInstanceOf(JAXPValidator.class);
    }

    @Test
    public void should_return_jaxp_validator_for_relax_ng() throws Exception {
        Validator validator = Validator.forLanguage(Language.RELAXNG.getNsUri());
        assertThat(validator).isInstanceOf(JAXPValidator.class);
    }
}
