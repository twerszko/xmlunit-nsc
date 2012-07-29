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

package org.custommonkey.xmlunit.matchers;

import org.custommonkey.xmlunit.NodeTest;
import org.custommonkey.xmlunit.NodeTestException;
import org.custommonkey.xmlunit.NodeTester;
import org.hamcrest.Description;
import org.hamcrest.Factory;

/**
 * Execute a <code>NodeTest<code> for multiple node types and make an
 * assertion about it whether it is expected to pass
 */
public class NoteTestPassesMatcher extends AbstractXmlUnitMatcher<NodeTest> {

    private final NodeTester tester;
    private final short[] nodeTypes;

    private final boolean reverse;

    public NoteTestPassesMatcher(NodeTester tester, short[] nodeTypes) {
        this.tester = tester;
        this.nodeTypes = nodeTypes;
        this.reverse = false;
    }

    public NoteTestPassesMatcher(NodeTester tester, short[] nodeTypes, boolean reverse) {
        this.tester = tester;
        this.nodeTypes = nodeTypes;
        this.reverse = reverse;
    }

    public void describeTo(Description description) {
        // TODO
    }

    @Override
    public boolean matchesSafely(NodeTest actualNodeTest) {
        boolean result = true;
        try {
            actualNodeTest.performTest(tester, nodeTypes);
        } catch (NodeTestException e) {
            result = false;
        }

        if (reverse) {
            return !result;
        }
        return result;
    }

    @Factory
    public static NoteTestPassesMatcher passesWith(NodeTester tester, short[] nodeTypes) {
        return new NoteTestPassesMatcher(tester, nodeTypes);
    }

    @Factory
    public static NoteTestPassesMatcher passesWith(NodeTester tester, short nodeType) {
        return new NoteTestPassesMatcher(tester, new short[] { nodeType });
    }

    @Factory
    public static NoteTestPassesMatcher notPassesWith(NodeTester tester, short[] nodeTypes) {
        return new NoteTestPassesMatcher(tester, nodeTypes, true);
    }

    @Factory
    public static NoteTestPassesMatcher notPassesWith(NodeTester tester, short nodeType) {
        return new NoteTestPassesMatcher(tester, new short[] { nodeType }, true);
    }

}
