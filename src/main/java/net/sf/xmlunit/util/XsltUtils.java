package net.sf.xmlunit.util;

import javax.annotation.Nullable;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

public class XsltUtils {
    @Nullable
    private URIResolver uriResolver;

    @Nullable
    private Class<? extends TransformerFactory> transformerFactoryClass;

    public void setUriResolver(@Nullable URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public void setTransformerFactoryClass(@Nullable Class<? extends TransformerFactory> transformerFactoryClass) {
        this.transformerFactoryClass = transformerFactoryClass;
    }

    /**
     * Get the transformer to use for XSLT transformations (and by implication
     * serialization and XPaths).
     * 
     * @return If specific TransformerFactoryClass was specified then new
     *         instance of this class will be returned. New instance of default
     *         TransformerFactory otherwise.
     */
    public TransformerFactory newTransformerFactory() {
        TransformerFactory factory;
        if (transformerFactoryClass == null) {
            factory = TransformerFactory.newInstance();
        } else {
            factory = TransformerFactory.newInstance(
                    transformerFactoryClass.getName(),
                    transformerFactoryClass.getClassLoader());
        }

        if (uriResolver != null) {
            factory.setURIResolver(uriResolver);
        }

        return factory;
    }
}
