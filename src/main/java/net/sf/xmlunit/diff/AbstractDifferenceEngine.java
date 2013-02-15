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

import java.util.Collections;
import java.util.Map;

import net.sf.xmlunit.diff.internal.ComparisonPerformer;

/**
 * Useful base-implementation of some parts of the DifferenceEngine interface.
 */
public abstract class AbstractDifferenceEngine implements DifferenceEngine {
    private final ComparisonListenerSupport listeners = new ComparisonListenerSupport();
    private NodeMatcher nodeMatcher = new DefaultNodeMatcher();
    private DifferenceEvaluator diffEvaluator = DifferenceEvaluators.Default;
    private Map<String, String> uri2Prefix = Collections.emptyMap();

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

    public DifferenceEvaluator getDifferenceEvaluator() {
        return diffEvaluator;
    }

    @Override
    public void setNamespaceContext(Map<String, String> uri2Prefix) {
        this.uri2Prefix = Collections.unmodifiableMap(uri2Prefix);
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
