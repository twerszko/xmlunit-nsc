package org.custommonkey.xmlunit.matchers;

import org.hamcrest.TypeSafeMatcher;

public abstract class AbstractXmlValidMatcher<T> extends TypeSafeMatcher<T> {
    protected String systemId;
    protected String doctype;

    public AbstractXmlValidMatcher<T> withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    public AbstractXmlValidMatcher<T> withDoctype(String doctype) {
        this.doctype = doctype;
        return this;
    }
}
