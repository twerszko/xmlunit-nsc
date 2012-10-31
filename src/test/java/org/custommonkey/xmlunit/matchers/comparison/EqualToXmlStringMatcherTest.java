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

import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.equalToXmlString;
import static org.custommonkey.xmlunit.matchers.XmlUnitMatchers.notEqualToXmlString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.XmlUnitBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class EqualToXmlStringMatcherTest extends AbstractComparisonTest {

    @Test
    @Parameters(method = "provideEqualXmlStringPairs")
    public void should_compare_equal_xml_strings(String testString, String expectedString) {
        assertThat(testString, is(equalToXmlString(expectedString)));
    }

    @Test
    @Parameters(method = "provideNotEqualXmlStringPairs")
    public void should_compare_npt_equal_xml_strings(String testString, String expectedString) {
        assertThat(testString, is(notEqualToXmlString(expectedString)));
    }

    // Bug 585555
    @Test
    public void should_check_if_nused_namespaces_dont_matter() throws Exception {
        XmlUnit xmlUnit = new XmlUnitBuilder().ignoringWhitespace(true).build();
        String a =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<outer xmlns:NS2=\"http://namespace2/foo\">\n" +
                        "    <inner xmlns:NS2=\"http://namespace2/\">5</inner>\n" +
                        "</outer>\n";

        String b =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<outer xmlns:NS2=\"http://namespace2\">\n" +
                        "    <inner xmlns:NS2=\"http://namespace2/\">5</inner>\n" +
                        "</outer>\n";

        assertThat(b, is(equalToXmlString(a).using(xmlUnit)));
    }

    // Bug 585555
    @Test
    public void should_check_if_namespace_matters() throws Exception {
        XmlUnit xmlUnit = new XmlUnitBuilder().ignoringWhitespace(true).build();

        String a =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<outer xmlns=\"http://namespace2/\">\n" +
                        "</outer>";

        String b =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<outer xmlns=\"http://namespace2\">\n" +
                        "</outer>\n";

        assertThat(b, is(notEqualToXmlString(a).using(xmlUnit)));
    }
}
