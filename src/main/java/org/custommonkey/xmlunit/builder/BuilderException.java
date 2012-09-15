package org.custommonkey.xmlunit.builder;

import org.custommonkey.xmlunit.exceptions.XmlUnitException;

@SuppressWarnings("serial")
public class BuilderException extends XmlUnitException {

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderException(String message) {
        super(message);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }

}
