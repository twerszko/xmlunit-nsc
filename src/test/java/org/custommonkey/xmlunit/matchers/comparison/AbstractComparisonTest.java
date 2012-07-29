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

import static junitparams.JUnitParamsRunner.$;

import java.util.List;

import com.google.common.collect.Lists;

public class AbstractComparisonTest {
    protected final String[] control = new String[] {
            "<root/>",
            "<root></root>",
            "<root>test</root>",
            "<root attr=\"test\">test</root>",
            "<test/>",
            "<root>test</root>",
            "<root attr=\"test\"/>",
            "<root><outer><inner></inner></outer></root>",
            "<root attr=\"test\"><outer>test<inner>test</inner></outer></root>",
            "<root attr=\"test\"><outer>test<inner>test</inner></outer></root>"
    };
    protected final String[] test = new String[] {
            "<fail/>",
            "<fail/>",
            "<fail>test</fail>",
            "<root>test</root>",
            "<fail/>",
            "<root>fail</root>",
            "<root attr=\"fail\"/>",
            "<root><outer><inner>test</inner></outer></root>",
            "<root attr=\"test\"><outer>fail<inner>test</inner></outer></root>",
            "<root attr=\"fail\"><outer>test<inner>test</inner></outer></root>" };

    protected Object[] provideEqualXmlStringPairs() {
        List<Object[]> providedPairs = Lists.newLinkedList();

        for (String xmlString : control) {
            providedPairs.add($(xmlString, xmlString));
        }

        return providedPairs.toArray();
    }

    protected Object[] provideNotEqualXmlStringPairs() {
        List<Object[]> providedPairs = Lists.newLinkedList();

        for (int i = 0; i < Math.min(test.length, control.length); i++) {
            providedPairs.add($(test[i], control[i]));
        }

        return providedPairs.toArray();
    }
}
