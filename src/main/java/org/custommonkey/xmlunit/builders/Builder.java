package org.custommonkey.xmlunit.builders;

public interface Builder<T> {
    T build() throws BuilderException;
}
