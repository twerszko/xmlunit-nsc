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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.transform.Source;

import net.sf.xmlunit.diff.comparators.AttributeComparator;
import net.sf.xmlunit.diff.comparators.CharacterDataComparator;
import net.sf.xmlunit.diff.comparators.DoctypeComparator;
import net.sf.xmlunit.diff.comparators.DocumentComparator;
import net.sf.xmlunit.diff.comparators.ElementComparator;
import net.sf.xmlunit.diff.comparators.NodeComparator;
import net.sf.xmlunit.diff.comparators.ProcessingInstructionComparator;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Convert;
import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.util.Linqy;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import com.google.common.annotations.VisibleForTesting;

/**
 * Difference engine based on DOM.
 */
public final class DOMDifferenceEngine extends AbstractDifferenceEngine {

	private final XmlUnitProperties properties;

	public DOMDifferenceEngine(XmlUnitProperties properties) {
		if (properties == null) {
			this.properties = new XmlUnitProperties();
		} else {
			this.properties = properties.clone();
		}
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
			        NodeAndXpathCtx.from(Convert.toNode(control), new XPathContext()),
			        NodeAndXpathCtx.from(Convert.toNode(test), new XPathContext()));
		} catch (Exception ex) {
			// TODO remove pokemon exception handling
			throw new XMLUnitRuntimeException("Caught exception during comparison", ex);
		}
	}

	@VisibleForTesting
	ComparisonResult compareNodes(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
		return new MasterComparator(getComparisonPerformer()).compare(control, test);
	}

	@VisibleForTesting
	class MasterComparator extends NodeComparator<Node> {

		public MasterComparator(ComparisonPerformer compPerformer) {
			super(compPerformer);
		}

		/**
		 * Recursively compares two XML nodes.
		 * 
		 * <p>
		 * Performs comparisons common to all node types, then performs the node
		 * type specific comparisons and finally recurses into the node's child
		 * lists.
		 * </p>
		 * 
		 * <p>
		 * Stops as soon as any comparison returns ComparisonResult.CRITICAL.
		 * </p>
		 * 
		 * <p>
		 * package private to support tests.
		 * </p>
		 */
		@Override
		public ComparisonResult compare(final NodeAndXpathCtx<Node> control, final NodeAndXpathCtx<Node> test) {
			final Node controlNode = control.getNode();
			final Node testNode = test.getNode();

			final XPathContext controlContext = control.getXpathCtx();
			final XPathContext testContext = test.getXpathCtx();

			Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();

			operations.add(new ComparisonOperation() {
				@Override
				public ComparisonResult executeComparison() {
					return compPerformer.performComparison(
					        new Comparison(
					                ComparisonType.NODE_TYPE,
					                control, controlNode.getNodeType(),
					                test, testNode.getNodeType()));
				}
			});
			operations.add(new CompareNamespaceOperation(control, test));

			if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
				operations.add(new ChildrenNumberComparisonOperation(control, test));
			}

			operations.add(new ComparisonOperation() {
				@Override
				public ComparisonResult executeComparison() {
					return nodeTypeSpecificComparison(
					        controlNode, controlContext, testNode, testContext);
				}
			});

			if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
				final Iterable<Node> controlChildren =
				        Linqy.filter(new IterableNodeList(controlNode.getChildNodes()), INTERESTING_NODES);
				final Iterable<Node> testChildren =
				        Linqy.filter(new IterableNodeList(testNode.getChildNodes()), INTERESTING_NODES);

				controlContext.setChildren(Linqy.map(controlChildren, TO_NODE_INFO));
				testContext.setChildren(Linqy.map(testChildren, TO_NODE_INFO));

				operations.add(new ComparisonOperation() {
					@Override
					public ComparisonResult executeComparison() {
						return compareNodeLists(controlChildren, controlContext, testChildren, testContext);
					}
				});
			}
			return execute(operations);
		}

		/**
		 * Matches nodes of two node lists and invokes compareNode on each pair.
		 * 
		 * <p>
		 * Also performs CHILD_LOOKUP comparisons for each node that couldn't be
		 * matched to one of the "other" list.
		 * </p>
		 */
		private ComparisonResult compareNodeLists(Iterable<Node> controlSeq,
		        XPathContext controlContext,
		        Iterable<Node> testSeq,
		        XPathContext testContext) {
			// if there are no children on either Node, the result is equal
			ComparisonResult lastResult = ComparisonResult.EQUAL;

			Iterable<Map.Entry<Node, Node>> matches = getNodeMatcher().match(controlSeq, testSeq);
			List<Node> controlList = Linqy.asList(controlSeq);
			List<Node> testList = Linqy.asList(testSeq);
			Set<Node> seen = new HashSet<Node>();
			for (Map.Entry<Node, Node> pair : matches) {
				Node control = pair.getKey();
				Node test = pair.getValue();
				seen.add(control);
				seen.add(test);
				int controlIndex = controlList.indexOf(control);
				int testIndex = testList.indexOf(test);

				controlContext.navigateToChild(controlIndex);
				testContext.navigateToChild(testIndex);
				try {
					lastResult =
					        getComparisonPerformer().performComparison(
					                new Comparison(ComparisonType.CHILD_NODELIST_SEQUENCE,
					                        NodeAndXpathCtx.from(control, controlContext), Integer
					                                .valueOf(controlIndex),
					                        NodeAndXpathCtx.from(test, testContext), Integer.valueOf(testIndex)));
					if (lastResult == ComparisonResult.CRITICAL) {
						return lastResult;
					}

					lastResult = compare(
					        NodeAndXpathCtx.from(control, controlContext),
					        NodeAndXpathCtx.from(test, testContext));
					if (lastResult == ComparisonResult.CRITICAL) {
						return lastResult;
					}
				} finally {
					testContext.navigateToParent();
					controlContext.navigateToParent();
				}
			}

			final int controlSize = controlList.size();
			for (int i = 0; i < controlSize; i++) {
				if (!seen.contains(controlList.get(i))) {
					controlContext.navigateToChild(i);
					try {
						lastResult =
						        getComparisonPerformer().performComparison(
						                new Comparison(ComparisonType.CHILD_LOOKUP,
						                        controlList.get(i),
						                        getXPath(controlContext),
						                        controlList.get(i).getNodeName(),
						                        null, null, null));
						if (lastResult == ComparisonResult.CRITICAL) {
							return lastResult;
						}
					} finally {
						controlContext.navigateToParent();
					}
				}
			}

			final int testSize = testList.size();
			for (int i = 0; i < testSize; i++) {
				if (!seen.contains(testList.get(i))) {
					testContext.navigateToChild(i);
					try {
						lastResult =
						        getComparisonPerformer().performComparison(
						                new Comparison(ComparisonType.CHILD_LOOKUP,
						                        null, null, null,
						                        testList.get(i),
						                        getXPath(testContext),
						                        testList.get(i).getNodeName()));
						if (lastResult == ComparisonResult.CRITICAL) {
							return lastResult;
						}
					} finally {
						testContext.navigateToParent();
					}
				}
			}
			return lastResult;
		}

		/**
		 * Dispatches to the node type specific comparison if one is defined for
		 * the given combination of nodes.
		 */
		private ComparisonResult nodeTypeSpecificComparison(
		        Node control, XPathContext controlContext,
		        Node test, XPathContext testContext) {

			switch (control.getNodeType()) {
				case Node.CDATA_SECTION_NODE:
				case Node.COMMENT_NODE:
				case Node.TEXT_NODE:
					if (test instanceof CharacterData) {
						return new CharacterDataComparator(getComparisonPerformer()).compare(
						        NodeAndXpathCtx.from((CharacterData) control, controlContext),
						        NodeAndXpathCtx.from((CharacterData) test, testContext));
					}
					break;
				case Node.DOCUMENT_NODE:
					if (test instanceof Document) {
						return new DocumentComparator(getComparisonPerformer()).compare(
						        NodeAndXpathCtx.from((Document) control, controlContext),
						        NodeAndXpathCtx.from((Document) test, testContext));
					}
					break;
				case Node.ELEMENT_NODE:
					if (test instanceof Element) {
						return new ElementComparator(getComparisonPerformer(), properties.getIgnoreAttributeOrder())
						        .compare(
						                NodeAndXpathCtx.from((Element) control, controlContext),
						                NodeAndXpathCtx.from((Element) test, testContext));
					}
					break;
				case Node.PROCESSING_INSTRUCTION_NODE:
					if (test instanceof ProcessingInstruction) {
						return new ProcessingInstructionComparator(getComparisonPerformer()).compare(
						        NodeAndXpathCtx.from((ProcessingInstruction) control, controlContext),
						        NodeAndXpathCtx.from((ProcessingInstruction) test, testContext));
					}
					break;
				case Node.DOCUMENT_TYPE_NODE:
					if (test instanceof DocumentType) {
						return new DoctypeComparator(comparisonPerformer).compare(
						        NodeAndXpathCtx.from((DocumentType) control, controlContext),
						        NodeAndXpathCtx.from((DocumentType) test, testContext));
					}
					break;
				case Node.ATTRIBUTE_NODE:
					if (test instanceof Attr) {
						return new AttributeComparator(getComparisonPerformer()).compare(
						        NodeAndXpathCtx.from((Attr) control, controlContext),
						        NodeAndXpathCtx.from((Attr) test, testContext));
					}
					break;
			}
			return ComparisonResult.EQUAL;
		}
	}

	/**
	 * Maps Nodes to their NodeInfo equivalent.
	 */
	private static final Linqy.Mapper<Node, XPathContext.NodeInfo> TO_NODE_INFO =
	        new Linqy.Mapper<Node, XPathContext.NodeInfo>() {
		        @Override
		        public XPathContext.NodeInfo map(Node n) {
			        return new XPathContext.DOMNodeInfo(n);
		        }
	        };
}
