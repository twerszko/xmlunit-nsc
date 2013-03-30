package org.custommonkey.xmlunit.util;

import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XSLTConstants;
import org.custommonkey.xmlunit.XmlUnitProperties;

public class XsltUtils {
    private final XmlUnitProperties properties;

    private static final String XSLT_VERSION_START = " version=\"";
    private static final String XSLT_VERSION_END = "\">";

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

        TransformerFactory factory;
        if (transformerFactoryClass == null) {
            factory = TransformerFactory.newInstance();
        } else {
            factory = TransformerFactory.newInstance(
                    transformerFactoryClass.getName(),
                    transformerFactoryClass.getClassLoader());
        }

        if (properties.getUriResolver() != null) {
            factory.setURIResolver(properties.getUriResolver());
        }

        return factory;
    }

    /**
     * XSLT stylesheet element using the configured XSLT version.
     */
    // TODO make it non public
    public String getXSLTStart() {
        return XSLTConstants.XSLT_START_NO_VERSION
                + XSLT_VERSION_START + properties.getXsltVersion() + XSLT_VERSION_END;
    }

}
