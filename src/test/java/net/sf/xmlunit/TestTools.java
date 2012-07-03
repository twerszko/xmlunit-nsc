package net.sf.xmlunit;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import net.sf.xmlunit.util.Convert;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class containing utility methods for tests.
 * 
 * @author Tomasz Werszko
 * 
 */
public class TestTools {
    public static Document parseDocument(Source s) {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(Convert.toInputSource(s));
            return document;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to parse document.", e);
        } catch (SAXException e) {
            throw new IllegalStateException("Failed to parse document.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse document.", e);
        }
    }
}
