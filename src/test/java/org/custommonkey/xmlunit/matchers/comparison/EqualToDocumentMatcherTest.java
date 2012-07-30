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

package org.custommonkey.xmlunit.matchers.comparison;

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXmlDocument;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXmlDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.custommonkey.xmlunit.XMLUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith(JUnitParamsRunner.class)
public class EqualToDocumentMatcherTest extends AbstractComparisonTest {

    private DocumentUtils documentUtils;

    @Before
    public void setUp() {
        documentUtils = new DocumentUtils(new XMLUnitProperties());
    }

    @Test
    @Parameters(method = "provideEqualXmlStringPairs")
    public void should_compare_equal_xml_strings(String testString, String expectedString) throws SAXException,
            IOException {
        // given
        Document testDocument = documentUtils.buildTestDocument(testString);
        Document expectedDocument = documentUtils.buildControlDocument(expectedString);

        // then
        assertThat(testDocument, is(equalToXmlDocument(expectedDocument)));
    }

    @Test
    @Parameters(method = "provideNotEqualXmlStringPairs")
    public void should_compare_npt_equal_xml_strings(String testString, String expectedString) throws SAXException,
            IOException {
        // given
        Document testDocument = documentUtils.buildTestDocument(testString);
        Document expectedDocument = documentUtils.buildControlDocument(expectedString);

        // then
        assertThat(testDocument, is(notEqualToXmlDocument(expectedDocument)));
    }
}
