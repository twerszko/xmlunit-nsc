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

import org.w3c.dom.DocumentType;

public class CompareDoctypeCommand extends ComparisonCommandBase<DocumentType> {

    public CompareDoctypeCommand(
            ComparisonPerformer compPerformer,
            NodeAndXpath<DocumentType> control, NodeAndXpath<DocumentType> test) {
        super(compPerformer, control, test);
    }

    @Override
    public Queue<Comparison> provideComparisons() {
        DocumentType controlDt = getControl().getNode();
        DocumentType testDt = getTest().getNode();

        Queue<Comparison> comparisons = new LinkedList<Comparison>();
        if (controlDt == null || testDt == null) {
            return comparisons;
        }

        comparisons.add(
                Comparison.ofType(ComparisonType.DOCTYPE_NAME)
                        .between(getControl(), controlDt.getName())
                        .and(getTest(), testDt.getName()));

        comparisons.add(
                Comparison.ofType(ComparisonType.DOCTYPE_PUBLIC_ID)
                        .between(getControl(), controlDt.getPublicId())
                        .and(getTest(), testDt.getPublicId()));

        comparisons.add(
                Comparison.ofType(ComparisonType.DOCTYPE_SYSTEM_ID)
                        .between(getControl(), controlDt.getSystemId())
                        .and(getTest(), testDt.getSystemId()));

        return comparisons;
    }
}
