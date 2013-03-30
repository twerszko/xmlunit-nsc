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
import net.sf.xmlunit.util.Preconditions;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Node;

/**
 * Difference engine based on DOM.
 */
public class DOMDifferenceEngine extends ObservableDifferenceEngine {

	private DifferenceEvaluator diffEvaluator = Evaluators.Default;

	private NodeMatcher nodeMatcher = new DefaultNodeMatcher();

	private ComparisonFilter filter = new DefaultComparisonFilter();

	private boolean ignoreAttributeOrder = true;

	private DOMComparator comparator;

	public boolean getIgnoreAttributeOrder() {
		return ignoreAttributeOrder;
	}

	@Override
	public void setIgnoreAttributeOrder(boolean ignoreAttributeOrder) {
		this.ignoreAttributeOrder = ignoreAttributeOrder;
	}

	@Override
	public void compare(Source control, Source test) {
		Preconditions.checkArgument(control != null, "control must not be null");
		Preconditions.checkArgument(test != null, "test must not be null");
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
		comparator = createComparator();
		comparator.compare(control, test);
	}

	DOMComparator createComparator() {
		ComparisonProviders providers = new ConfiguredComparisonProviders();
		DOMComparator comparator = new DOMComparator(providers) {
			@Override
			protected ComparisonResult evaluateResult(Comparison comparison, ComparisonResult result) {
				return getDifferenceEvaluator().evaluate(comparison, result);
			};

			@Override
			protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {
				getListeners().fireComparisonPerformed(comparison, result);
			}

			@Override
			protected boolean ignoreComparison(Comparison comparison) {
				return getFilter().ignore(comparison);
			}
		};
		return comparator;
	}

	@Override
	public void stop() {
		if (comparator != null) {
			comparator.setInterrupted(true);
		}
	}

	@Override
	public void setEvaluator(DifferenceEvaluator evaluator) {
		Preconditions.checkArgument(evaluator != null, "difference evaluator must not be null");
		diffEvaluator = evaluator;
	}

	protected DifferenceEvaluator getDifferenceEvaluator() {
		return diffEvaluator;
	}

	@Override
	public void setNodeMatcher(NodeMatcher n) {
		Preconditions.checkArgument(n != null, "node matcher must not be null");
		nodeMatcher = n;
	}

	protected ComparisonFilter getFilter() {
		return filter;
	}

	public void setFilter(ComparisonFilter filter) {
		Preconditions.checkArgument(filter != null, "filter must not be null");
		this.filter = filter;
	}

	private class ConfiguredComparisonProviders extends ComparisonProviders {
		public ConfiguredComparisonProviders() {
			setElementComparisonProvider(new ElementComparisonProvider(getIgnoreAttributeOrder()));
			setNodeMatcher(nodeMatcher);
		}
	}
}
