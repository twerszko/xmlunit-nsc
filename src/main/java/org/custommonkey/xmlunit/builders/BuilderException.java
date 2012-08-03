package org.custommonkey.xmlunit.builders;

@SuppressWarnings("serial")
public class BuilderException extends Exception {

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderException(String message) {
        super(message);
    }

}
