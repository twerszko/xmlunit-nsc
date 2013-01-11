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

import org.w3c.dom.DocumentType;

public class CompareDoctypeCommand extends ComparisonCommandBase<DocumentType> {

	public CompareDoctypeCommand(
	        ComparisonPerformer compPerformer,
	        NodeAndXpathCtx<DocumentType> control, NodeAndXpathCtx<DocumentType> test) {
		super(compPerformer, control, test);
	}

	@Override
	public Queue<Comparison> provideComparisons() {
		final DocumentType controlDt = getControl().getNode();
		final DocumentType testDt = getTest().getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();
		if (controlDt == null || testDt == null) {
			return comparisons;
		}

		comparisons.add(new Comparison(
		        ComparisonType.DOCTYPE_NAME,
		        getControl(), controlDt.getName(),
		        getTest(), testDt.getName()));

		comparisons.add(new Comparison(
		        ComparisonType.DOCTYPE_PUBLIC_ID,
		        getControl(), controlDt.getPublicId(),
		        getTest(), testDt.getPublicId()));

		comparisons.add(new Comparison(
		        ComparisonType.DOCTYPE_SYSTEM_ID,
		        getControl(), controlDt.getSystemId(),
		        getTest(), testDt.getSystemId()));

		return comparisons;
	}
}
