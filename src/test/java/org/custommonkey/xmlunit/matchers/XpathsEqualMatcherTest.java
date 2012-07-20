package org.custommonkey.xmlunit.matchers;

import static junitparams.JUnitParamsRunner.$;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXpath;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXpath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith(JUnitParamsRunner.class)
public class XpathsEqualMatcherTest {

    @Test
    @Parameters(method = "provideNotEqualXpaths")
    public void should_pass_when_not_equal_xpaths_with_attributes(
            XpathWrapper actualXpath, XpathWrapper expectedWrapper) {
        // then
        assertThat(actualXpath, is(notEqualToXpath(expectedWrapper)));
    }

    @SuppressWarnings("unused")
    private Object[] provideNotEqualXpaths() throws Exception {
        return $(
                $(
                        getTestXpath("/foo/Bar", "<foo><Bar a=\"1\" /></foo>"),
                        getControlXpath("/foo/Bar/@a", "<foo><Bar a=\"1\" /></foo>")
                ),
                $(
                        getTestXpath("/foo/Bar/@b", "<foo><Bar a=\"1\" b=\"1\"/></foo>"),
                        getControlXpath("/foo/Bar/@a", "<foo><Bar a=\"1\" b=\"1\"/></foo>")
                ));
    }

    // bug 3290264
    @Test
    public void testAssertXpathEqualsAndAttributes() throws Exception {
        XpathWrapper controlXpath3 = getControlXpath("/foo/Bar/@a", "<foo><Bar a=\"1\" b=\"2\"/></foo>");
        XpathWrapper testXpath3 = getTestXpath("/foo/Bar/@a", "<foo><Bar a=\"1\" b=\"2\"/></foo>");

        assertThat(testXpath3, is(equalToXpath(controlXpath3)));
    }

    private XpathWrapper getTestXpath(String testXpath, String testXml) throws SAXException, IOException {
        Document testDocument = XMLUnit.buildTestDocument(testXml);
        XpathWrapper xpath = new XpathWrapper(testXpath, testDocument);
        return xpath;
    }

    private XpathWrapper getControlXpath(String controlXpath, String controlXml) throws SAXException, IOException {
        Document testDocument = XMLUnit.buildControlDocument(controlXml);
        XpathWrapper xpath = new XpathWrapper(controlXpath, testDocument);
        return xpath;
    }
}
