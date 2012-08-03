//  Copyright 2012 Tomasz Werszko
//      
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.custommonkey.xmlunit.matchers.xpath;

import static junitparams.JUnitParamsRunner.$;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXpath;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXpath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.xmlunit.xpath.XpathWrapper;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith(JUnitParamsRunner.class)
public class XpathsEqualMatcherTest {

    private static final String xpathNodesControlXML =
            "<trees>" +
                    "<tree evergreen=\"false\">oak</tree>" +
                    "<tree evergreen=\"false\">ash</tree>" +
                    "<tree evergreen=\"true\">scots pine</tree>" +
                    "<tree evergreen=\"true\">spruce</tree>" +
                    "<favourite><!-- is this a tree or a bush?! -->" +
                    "<tree evergreen=\"false\">magnolia</tree>" +
                    "</favourite>" +
                    "<fruit>" +
                    "<apples>" +
                    "<crunchy/>" +
                    "<yum/>" +
                    "<tree evergreen=\"false\">apple</tree>" +
                    "</apples>" +
                    "</fruit>" +
                    "</trees>";
    private static final String xpathNodesTestXML =
            "<trees>" +
                    "<tree evergreen=\"false\">oak</tree>" +
                    "<tree evergreen=\"false\">ash</tree>" +
                    "<tree evergreen=\"true\">scots pine</tree>" +
                    "<tree evergreen=\"true\">spruce</tree>" +
                    "<tree flowering=\"true\">cherry</tree>" +
                    "<tree flowering=\"true\">apple</tree>" +
                    "<favourite><!-- is this a tree or a bush?! -->" +
                    "<tree evergreen=\"false\">magnolia</tree>" +
                    "</favourite>" +
                    "<apples>" +
                    "<crunchy/>" +
                    "<yum/>" +
                    "<tree evergreen=\"false\">apple</tree>" +
                    "</apples>" +
                    "</trees>";

    @Test
    @Parameters(method = "provideNotEqualXpaths")
    public void should_pass_when_not_equal_xpaths(
            XpathWrapper actualXpath, XpathWrapper expectedWrapper) {

        // then
        assertThat(actualXpath, is(notEqualToXpath(expectedWrapper)));
    }

    @Test
    @Parameters(method = "provideNotEqualXpaths")
    public void should_pass_when_equal_fails_on_not_equal_xpaths(
            XpathWrapper actualXpath, XpathWrapper expectedWrapper) {

        // then
        try {
            assertThat(actualXpath, is(equalToXpath(expectedWrapper)));
            fail("should not be equal!");
        } catch (AssertionError e) {
        }
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
                ),
                $(
                        getTestXpath("//tree", xpathNodesTestXML),
                        getControlXpath("/trees/tree[@evergreen]", xpathNodesControlXML)
                ),
                $(
                        getTestXpath("//tree[@evergreen='true']", xpathNodesTestXML),
                        getControlXpath("//tree[@evergreen='false']", xpathNodesControlXML)
                ),
                $(
                        getTestXpath("//favourite/apples", xpathNodesTestXML),
                        getControlXpath("/trees/favourite", xpathNodesControlXML)
                ),
                $(
                        getTestXpath("//apples/tree", xpathNodesTestXML),
                        getControlXpath("//fruit/apples", xpathNodesControlXML)
                ));
    }

    @Test
    @Parameters(method = "provideEqualXpaths")
    public void should_pass_when_equal_xpaths(
            XpathWrapper actualXpath, XpathWrapper expectedWrapper) {

        // then
        assertThat(actualXpath, is(equalToXpath(expectedWrapper)));
    }

    @Test
    @Parameters(method = "provideEqualXpaths")
    public void should_pass_when_not_equals_fails_on_equal_xpaths(
            XpathWrapper actualXpath, XpathWrapper expectedWrapper) {

        // then
        try {
            assertThat(actualXpath, is(notEqualToXpath(expectedWrapper)));
            fail("should not be notEqual!");
        } catch (AssertionError e) {
        }
    }

    @SuppressWarnings("unused")
    private Object[] provideEqualXpaths() throws Exception {
        return $(
                // bug 3290264
                $(
                        getTestXpath("/foo/Bar/@a", "<foo><Bar a=\"1\" b=\"2\"/></foo>"),
                        getControlXpath("/foo/Bar/@a", "<foo><Bar a=\"1\" b=\"2\"/></foo>")
                ),
                $(
                        getTestXpath("/trees/tree[@evergreen]", xpathNodesTestXML),
                        getControlXpath("/trees/tree[@evergreen]", xpathNodesControlXML)
                ),
                $(
                        getTestXpath("//tree[@evergreen='false']", xpathNodesTestXML),
                        getControlXpath("//tree[@evergreen='false']", xpathNodesControlXML)
                ),
                $(
                        getTestXpath("//favourite", xpathNodesTestXML),
                        getControlXpath("/trees/favourite", xpathNodesControlXML)
                ),
                $(
                        getTestXpath("//apples", xpathNodesTestXML),
                        getControlXpath("//fruit/apples", xpathNodesControlXML)
                ));
    }

    private XpathWrapper getTestXpath(String testXpath, String testXml) throws SAXException, IOException {
        Document testDocument = new DocumentUtils(new XmlUnitProperties()).buildTestDocument(testXml);
        XpathWrapper xpath = new XpathWrapper(testXpath, testDocument);
        return xpath;
    }

    private XpathWrapper getControlXpath(String controlXpath, String controlXml) throws SAXException, IOException {
        Document testDocument = new DocumentUtils(new XmlUnitProperties()).buildControlDocument(controlXml);
        XpathWrapper xpath = new XpathWrapper(controlXpath, testDocument);
        return xpath;
    }
}
