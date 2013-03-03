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

import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.diff.strategies.CompareAttributeStrategy;
import net.sf.xmlunit.diff.strategies.CompareCharacterDataStrategy;
import net.sf.xmlunit.diff.strategies.CompareDoctypeStrategy;
import net.sf.xmlunit.diff.strategies.CompareDocumentStrategy;
import net.sf.xmlunit.diff.strategies.CompareElementStrategy;
import net.sf.xmlunit.diff.strategies.CompareNamespaceStrategy;
import net.sf.xmlunit.diff.strategies.CompareProcInstrStrategy;
import net.sf.xmlunit.diff.strategies.ComparisonPerformer;
import net.sf.xmlunit.diff.strategies.ComparisonStrategyProvider;
import net.sf.xmlunit.diff.strategies.DOMComparator;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Node;

/**
 * Difference engine based on DOM.
 */
public class DOMDifferenceEngine implements DifferenceEngine {

	private final ComparisonListenerSupport listeners = new ComparisonListenerSupport();
	private NodeMatcher nodeMatcher = new DefaultNodeMatcher();
	private DifferenceEvaluator diffEvaluator = DifferenceEvaluators.Default;

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
		ComparisonStrategyProvider provider = createStrategyProvider(getComparisonPerformer());
		new DOMComparator(getComparisonPerformer(), getNodeMatcher(), provider).compare(control, test);
	}

	ComparisonStrategyProvider createStrategyProvider(ComparisonPerformer performer) {
		DOMComparisonStrategyProvider provider = new DOMComparisonStrategyProvider();
		provider.setAttributeComparisonStrategy(new CompareAttributeStrategy(performer));
		provider.setCharacterDataComparisonStrategy(new CompareCharacterDataStrategy(performer));
		provider.setDoctypeComparisonStrategy(new CompareDoctypeStrategy(performer));
		provider.setDocumentComparisonStrategyStrategy(new CompareDocumentStrategy(performer));
		provider.setElementComparisonStrategy(new CompareElementStrategy(performer, getIgnoreAttributeOrder()));
		provider.setNamespaceComparisonStrategy(new CompareNamespaceStrategy(performer));
		provider.setProcInstrComparisonStrategy(new CompareProcInstrStrategy(performer));
		return provider;
	}

	protected final ComparisonPerformer comparisonPerformer = new ComparisonPerformer() {
		@Override
		protected ComparisonResult evaluateResult(Comparison comparison, ComparisonResult result) {
			return getDifferenceEvaluator().evaluate(comparison, result);
		};

		@Override
		protected void comparisonPerformed(Comparison comparison, ComparisonResult result) {
			listeners.fireComparisonPerformed(comparison, result);
		}
	};

	public ComparisonPerformer getComparisonPerformer() {
		return comparisonPerformer;
	}

	@Override
	public void addComparisonListener(ComparisonListener l) {
		if (l == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		listeners.addComparisonListener(l);
	}

	@Override
	public void addMatchListener(ComparisonListener l) {
		if (l == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		listeners.addMatchListener(l);
	}

	@Override
	public void addDifferenceListener(ComparisonListener l) {
		if (l == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		listeners.addDifferenceListener(l);
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

	@Override
	public void setDifferenceEvaluator(DifferenceEvaluator evaluator) {
		if (evaluator == null) {
			throw new IllegalArgumentException("difference evaluator must" + " not be null");
		}
		diffEvaluator = evaluator;
	}

	// TODO protected?
	public DifferenceEvaluator getDifferenceEvaluator() {
		return diffEvaluator;
	}

	/**
	 * Compares the detail values for object equality, lets the difference
	 * evaluator evaluate the result, notifies all listeners and returns the
	 * outcome.
	 */
	protected final ComparisonResult performComparison(Comparison comp) {
		return comparisonPerformer.performComparison(comp);
	}
}
