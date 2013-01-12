/*
 This file is licensed to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package net.sf.xmlunit.diff.commands;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Attr;

public class CompareAttributeCommand extends ComparisonCommandBase<Attr> {

    public CompareAttributeCommand(ComparisonPerformer compPerformer,
            NodeAndXpath<Attr> control, NodeAndXpath<Attr> test) {
        super(compPerformer, control, test);
    }

    @Override
    public Queue<Comparison> provideComparisons() {
        Attr controlAttr = getControl().getNode();
        Attr testAttr = getTest().getNode();

        Queue<Comparison> comparisons = new LinkedList<Comparison>();

        comparisons.add(
                Comparison.ofType(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED)
                        .between(getControl(), controlAttr.getSpecified())
                        .and(getTest(), testAttr.getSpecified()));

        comparisons.add(
                Comparison.ofType(ComparisonType.ATTR_VALUE)
                        .between(getControl(), controlAttr.getValue())
                        .and(getTest(), testAttr.getValue()));

        return comparisons;
    }
}
