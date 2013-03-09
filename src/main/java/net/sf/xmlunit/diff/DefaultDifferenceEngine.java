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

import javax.annotation.Nullable;
import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class that has responsibility for comparing Nodes and notifying a
 * DifferenceListener of any differences or dissimilarities that are found.
 * Knows how to compare namespaces and nested child nodes, but currently only
 * compares nodes of type ELEMENT_NODE, CDATA_SECTION_NODE, COMMENT_NODE,
 * DOCUMENT_TYPE_NODE, PROCESSING_INSTRUCTION_NODE and TEXT_NODE. Nodes of other
 * types (eg ENTITY_NODE) will be skipped. <br />
 * Examples and more at <a href="http://xmlunit.sourceforge.net"/>xmlunit.
 * sourceforge.net</a>
 * 
 * @see DifferenceListener#differenceFound(Difference)
 */
public final class DefaultDifferenceEngine extends DOMDifferenceEngine {
    public DefaultDifferenceEngine() {
        DifferenceEvaluator defaultEvaluator = super.getDifferenceEvaluator();
        setDifferenceEvaluator(defaultEvaluator);
    }

    @Override
    public void addMatchListener(final ComparisonListener listener) {
        if (listener == null) {
            super.addMatchListener(listener);
            return;
        }
        super.addMatchListener(new ComparisonListener() {
            @Override
            public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
                comparison = filter(comparison);
                if (comparison != null) {
                    listener.comparisonPerformed(comparison, outcome);
                }
            }
        });
    }

    @Override
    public void setDifferenceEvaluator(final DifferenceEvaluator evaluator) {
        if (evaluator == null) {
            super.setDifferenceEvaluator(evaluator);
            return;
        }

        super.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (swallowComparison(comparison, outcome)) {
                    return outcome;
                }

                comparison = filter(comparison);
                if (comparison != null) {
                    return evaluator.evaluate(comparison, outcome);
                }
                return outcome;
            }
        });
    }

    @Override
    public void compare(Source ctrlSource, Source testSource) {
        super.compare(ctrlSource, testSource);
    }

    @Nullable
    private static Comparison filter(Comparison comp) {
        switch (comp.getType()) {
            case XML_ENCODING:
            case XML_STANDALONE:
            case XML_VERSION:
                return null;
            default:
                return comp;
        }
    }

    private boolean swallowComparison(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) {
            return true;
        }

        Node controlTarget = comparison.getControlDetails().getTarget();
        if ((comparesChildrenListLength(comparison) && isDocument(controlTarget))
                ||
                (comparison.getType() == ComparisonType.CHILD_LOOKUP
                &&
                (isNonElementDocumentChild(comparison.getControlDetails())
                || isNonElementDocumentChild(comparison.getTestDetails()))
                )) {
            return true;
        }
        return false;
    }

    private boolean comparesChildrenListLength(Comparison comparison) {
        return comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH;
    }

    private boolean isDocument(Node controlTarget) {
        return controlTarget instanceof Document;
    }

    private static boolean isNonElementDocumentChild(Comparison.Detail detail) {
        return detail != null && detail.getTarget() instanceof Node
                && !(detail.getTarget() instanceof Element)
                && detail.getTarget().getParentNode() instanceof Document;
    }
}
