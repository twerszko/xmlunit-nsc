package org.custommonkey.xmlunit.builder;

public interface Builder<T> {
    T build() throws BuilderException;
}
