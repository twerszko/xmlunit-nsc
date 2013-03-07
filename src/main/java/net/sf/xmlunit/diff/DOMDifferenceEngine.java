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

package net.sf.xmlunit.diff;

import javax.xml.transform.Source;

import net.sf.xmlunit.diff.comparators.ComparisonProviders;
import net.sf.xmlunit.diff.comparators.DOMComparator;
import net.sf.xmlunit.diff.comparators.ElementComparisonProvider;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Node;

/**
 * Difference engine based on DOM.
 */
public class DOMDifferenceEngine extends ObservableDifferenceEngine {

	private NodeMatcher nodeMatcher = new DefaultNodeMatcher();

	private boolean ignoreAttributeOrder = true;

	public boolean getIgnoreAttributeOrder() {
		return ignoreAttributeOrder;
	}

	public void setIgnoreAttributeOrder(boolean ignoreAttributeOrder) {
		this.ignoreAttributeOrder = ignoreAttributeOrder;
	}

	@Override
	public void compare(Source control, Source test) {
		if (control == null) {
			throw new IllegalArgumentException("control must not be null");
		}
		if (test == null) {
			throw new IllegalArgumentException("test must not be null");
		}
		try {
			compareNodes(
			        NodeAndXpath.from(Convert.toNode(control)),
			        NodeAndXpath.from(Convert.toNode(test)));
		} catch (Exception ex) {
			// TODO remove pokemon exception handling
			throw new XMLUnitRuntimeException("Caught exception during comparison", ex);
		}
	}

	private void compareNodes(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
		DOMComparator comparator = createComparator();
		comparator.compare(control, test);
	}

	DOMComparator createComparator() {
		ComparisonProviders providers = createProviders();
		DOMComparator comparator = new DOMComparator(getNodeMatcher(), providers) {
			protected ComparisonResult evaluateResult(Comparison comparison, ComparisonResult result) {
				return getDifferenceEvaluator().evaluate(comparison, result);
			};

			@Override
			protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {
				getListeners().fireComparisonPerformed(comparison, result);
			}
		};
		return comparator;
	}

	private ComparisonProviders createProviders() {
		ComparisonProviders providers = new ComparisonProviders();
		providers.setElementComparisonProvider(new ElementComparisonProvider(getIgnoreAttributeOrder()));
		return providers;
	}

	@Override
	public void setNodeMatcher(NodeMatcher n) {
		if (n == null) {
			throw new IllegalArgumentException("node matcher must not be null");
		}
		nodeMatcher = n;
	}

	public NodeMatcher getNodeMatcher() {
		return nodeMatcher;
	}
}
