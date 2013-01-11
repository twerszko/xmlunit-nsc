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
package net.sf.xmlunit.diff.comparators.commands;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Attr;

public class CompareAttributeCommand extends ComparisonCommandBase<Attr> {

	public CompareAttributeCommand(ComparisonPerformer compPerformer,
	        NodeAndXpathCtx<Attr> control, NodeAndXpathCtx<Attr> test) {
		super(compPerformer, control, test);
	}

	@Override
	public Queue<Comparison> provideComparisons() {
		final Attr controlAttr = getControl().getNode();
		final Attr testAttr = getTest().getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(new Comparison(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED,
		        getControl(), controlAttr.getSpecified(),
		        getTest(), testAttr.getSpecified()));
		comparisons.add(new Comparison(ComparisonType.ATTR_VALUE,
		        getControl(), controlAttr.getValue(),
		        getTest(), testAttr.getValue()));

		return comparisons;
	}

}
