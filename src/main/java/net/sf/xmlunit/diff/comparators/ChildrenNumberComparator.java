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
package net.sf.xmlunit.diff.comparators;

import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Node;

public class ChildrenNumberComparator extends NodeComparator<Node> {

	public ChildrenNumberComparator(ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	@Override
	public ComparisonResult compare(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
		return new ChildrenNumberComparisonOperation(control, test).executeComparison();
	}

}
