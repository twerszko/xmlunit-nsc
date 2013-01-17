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
package net.sf.xmlunit.diff.strategies;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Node;

public class CompareNamespaceStrategy extends ComparisonStrategyBase<Node> {

	public CompareNamespaceStrategy(ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	@Override
	public Queue<Comparison> provideComparisons(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
		Node controlNode = control.getNode();
		Node testNode = test.getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(
		        Comparison.ofType(ComparisonType.NAMESPACE_URI)
		                .between(control, controlNode.getNamespaceURI())
		                .and(test, testNode.getNamespaceURI()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.NAMESPACE_PREFIX)
		                .between(control, controlNode.getPrefix())
		                .and(test, testNode.getPrefix()));

		return comparisons;
	}

}
