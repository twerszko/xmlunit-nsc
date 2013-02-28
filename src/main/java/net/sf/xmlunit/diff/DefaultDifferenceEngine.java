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

import net.sf.xmlunit.input.CommentLessSource;
import net.sf.xmlunit.input.WhitespaceNormalizedSource;
import net.sf.xmlunit.input.WhitespaceStrippedSource;

import org.custommonkey.xmlunit.XmlUnitProperties;
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

	private final XmlUnitProperties properties;
	private final IsBetweenDocumentNodeAndRootElement checkPrelude = new IsBetweenDocumentNodeAndRootElement();

	public DefaultDifferenceEngine(XmlUnitProperties properties) {
		if (properties == null) {
			this.properties = new XmlUnitProperties();
		} else {
			this.properties = properties.clone();
		}
		setIgnoreAttributeOrder(this.properties.getIgnoreAttributeOrder());

		addComparisonListener(checkPrelude);
		DifferenceEvaluator defaultEvaluator = super.getDifferenceEvaluator();
		setDifferenceEvaluator(defaultEvaluator);
	}

	@Override
	public void addMatchListener(final ComparisonListener listener) {
		if (listener != null) {
			super.addMatchListener(new ComparisonListener() {
				@Override
				public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
					comparison = filter(comparison);
					if (comparison != null) {
						listener.comparisonPerformed(comparison, outcome);
					}
				}
			});
		} else {
			super.addMatchListener(listener);
		}
	}

	@Override
	public void setDifferenceEvaluator(final DifferenceEvaluator evaluator) {
		if (evaluator != null) {
			super.setDifferenceEvaluator(new DifferenceEvaluator() {
				@Override
				public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
					if (swallowComparison(comparison, outcome, checkPrelude)) {
						return outcome;
					}

					comparison = filter(comparison);
					if (comparison != null) {
						return evaluator.evaluate(comparison, outcome);
					}
					return outcome;
				}
			});
		} else {
			super.setDifferenceEvaluator(evaluator);
		}
	}

	@Override
	public void compare(Source ctrlSource, Source testSource) {
		// TODO properties can be removed
		checkPrelude.reset();

		if (properties.getIgnoreComments()) {
			ctrlSource = new CommentLessSource(ctrlSource);
			testSource = new CommentLessSource(testSource);
		}
		if (properties.getNormalizeWhitespace()) {
			ctrlSource = new WhitespaceNormalizedSource(ctrlSource);
			testSource = new WhitespaceNormalizedSource(testSource);
		} else if (properties.getIgnoreWhitespace()) {
			ctrlSource = new WhitespaceStrippedSource(ctrlSource);
			testSource = new WhitespaceStrippedSource(testSource);
		}
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

	private static final Short TEXT_TYPE = Node.TEXT_NODE;
	private static final Short CDATA_TYPE = Node.CDATA_SECTION_NODE;

	private boolean swallowComparison(Comparison comparison,
	        ComparisonResult outcome,
	        IsBetweenDocumentNodeAndRootElement checkPrelude) {
		if (outcome == ComparisonResult.EQUAL) {
			return true;
		}
		if ((comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
		        && comparison.getControlDetails().getTarget() instanceof Document)
		        ||
		        (
		        comparison.getType() == ComparisonType.CHILD_LOOKUP
		        &&
		        (isNonElementDocumentChild(comparison.getControlDetails())
		        || isNonElementDocumentChild(comparison.getTestDetails()))
		        )
		        || checkPrelude.shouldSkip()) {
			return true;
		}
		if (properties.getIgnoreDiffBetweenTextAndCDATA()
		        && comparison.getType() == ComparisonType.NODE_TYPE) {
			return (
			        TEXT_TYPE.equals(comparison.getControlDetails().getValue())
			        ||
			        CDATA_TYPE.equals(comparison.getControlDetails().getValue())
			        )
			        && (
			        TEXT_TYPE.equals(comparison.getTestDetails().getValue())
			        ||
			        CDATA_TYPE.equals(comparison.getTestDetails().getValue())
			        );
		}
		return false;
	}

	private static boolean isNonElementDocumentChild(Comparison.Detail detail) {
		return detail != null && detail.getTarget() instanceof Node
		        && !(detail.getTarget() instanceof Element)
		        && detail.getTarget().getParentNode() instanceof Document;
	}

	/**
	 * Tests whether the DifferenceEngine is currently processing comparisons of
	 * "things" between the document node and the document's root element
	 * (comments or PIs, mostly) since these must be ignored for backwards
	 * compatibility reasons.
	 * 
	 * <p>
	 * Relies on the following assumptions:
	 * <ul>
	 * 
	 * <li>the last comparison DOMDifferenceEngine performs on the document node
	 * is an XML_ENCODING comparison.</li>
	 * <li>the first comparison DOMDifferenceEngine performs on matching root
	 * elements is a NODE_TYPE comparison. The control Node is an Element Node.</li>
	 * <li>the first comparison DOMDifferenceEngine performs if the root
	 * elements don't match is a CHILD_LOOKUP comparison. The control Node is an
	 * Element Node.</li>
	 * </ul>
	 * </p>
	 */
	private static class IsBetweenDocumentNodeAndRootElement implements ComparisonListener {

		private boolean haveSeenXmlEncoding = false;
		private boolean haveSeenElementNodeComparison = false;

		@Override
		public void comparisonPerformed(Comparison comparison,
		        ComparisonResult outcome) {
			if (comparison.getType() == ComparisonType.XML_ENCODING) {
				haveSeenXmlEncoding = true;
			} else if (comparison.getControlDetails().getTarget()
			        instanceof Element
			        && (comparison.getType() == ComparisonType.NODE_TYPE
			        || comparison.getType() == ComparisonType.CHILD_LOOKUP)) {
				haveSeenElementNodeComparison = true;
			}
		}

		private boolean shouldSkip() {
			return haveSeenXmlEncoding && !haveSeenElementNodeComparison;
		}

		public void reset() {
			haveSeenXmlEncoding = false;
			haveSeenElementNodeComparison = false;
		}
	}
}
