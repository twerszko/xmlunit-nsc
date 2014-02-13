package net.sf.xmlunit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;

/**
 * Convenience enum for test resources.
 * 
 * @author Tomasz Werszko
 * 
 */
public enum TestResources {
    ANIMAL_FILE("/test1.xml"),
    ANIMAL_FILE2("/test2.xml"),
    ANIMAL_XSL("/animal.xsl"),
    DOG_FILE("/testAnimal.xml"),
    BLAME_FILE("/test.blame.html"),
    BOOK_DTD("/Book.dtd"),
    BOOK_XSD("/Book.xsd"),
    BOOK_XSD2("/Book.xsd2"),
    BOOK_XSD_GENERATED("/BookXsdGenerated.xml"),
    BOOK_XSD_GENERATED_NO_SCHEMA("/BookXsdGeneratedNoSchema.xml"),
    INVALID_BOOK_XSD_GENERATED_NO_SCHEMA("/InvalidBookXsdGeneratedNoSchema.xml"),
    BOOK_RNG("/Book.rng"),
    BOOK_WITH_DOC_TYPE("/BookWithDoctype.xml"),
    INVALID_BOOK_WITH_DOC_TYPE("/invalidBookWithDoctype.xml"),
    BROKEN_XSD("/broken.xsd"),
    NAMESPACES_CONTROL("/controlNamespaces.xml"),
    NAMESPACES_TEST("/testNamespaces.xml"),
    INVALID_BOOK("/invalidBook.xml"),
    DETAIL_CONTROL("/controlDetail.xml"),
    DETAIL_TEST("/testDetail.xml"),
    SIMPLE_XML("/simple.xml"),
    SIMPLE_XML_WITH_DTD("/simpleWithDtd.xml"),
    SIMPLE_XML_WITH_LONGER_DTD("/simpleWithLongerDtd.xml"),
    SIMPLE_XML_DTD("/simple.dtd"),
    SIMPLE_XML_WITH_EXT_DTD("/simpleWithExtDtd.xml"){
        @Override
        public String getContents() throws IOException{
            return String.format(super.getContents(), SIMPLE_XML_DTD.getFile().getAbsolutePath());
        }
    };

    private String path;

    private TestResources(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public URL getUrl() throws IOException {
        URL resourceUrl = Resources.getResource(TestResources.class, this.getPath());
        return resourceUrl;
    }

    public URI getUri() throws IOException {
        URI resourceUri;
        try {
            resourceUri = getUrl().toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to get Uri", e);
        }
        return resourceUri;
    }

    public File getFile() throws IOException {
        File file = new File(getUri());
        return file;
    }

    public String getContents() throws IOException {
        File file = new File(getUri());
        return FileUtils.readFileToString(file);
    }

    public static File getFile(String path) throws IOException {
        URI resourceUri;
        try {
            URL resourceUrl = Resources.getResource(TestResources.class, path);
            resourceUri = resourceUrl.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to get Uri", e);
        }

        return new File(resourceUri);
    }
}
