package org.custommonkey.xmlunit.util;

import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XmlUnitProperties;

public class XsltUtils {
    private final XmlUnitProperties properties;

    public XsltUtils() {
        this(new XmlUnitProperties());
    }

    public XsltUtils(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    /**
     * Get the transformer to use for XSLT transformations (and by implication
     * serialization and XPaths).
     * 
     * @return If specific TransformerFactoryClass was specified in properties
     *         then new instance of this class will be returned. New instance of
     *         default TransformerFactory otherwise.
     */
    public TransformerFactory newTransformerFactory() {
        Class<? extends TransformerFactory> transformerFactoryClass = properties.getTransformerFactoryClass();
        if (transformerFactoryClass == null) {
            return TransformerFactory.newInstance();
        } else {
            return TransformerFactory.newInstance(
                    transformerFactoryClass.getName(),
                    transformerFactoryClass.getClassLoader());
        }
    }
}
