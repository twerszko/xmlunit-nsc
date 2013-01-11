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

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.Attributes;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Nodes;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CompareElementCommand extends ComparisonCommandBase<Element> {

	private final Attributes controlAttributes;
	private final Attributes testAttributes;

	final boolean ignoreAttributeOrder;

	CompareElementCommand(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder,
	        NodeAndXpathCtx<Element> control, NamedNodeMap controlAttributes,
	        NodeAndXpathCtx<Element> test, NamedNodeMap testAttributes) {
		super(compPerformer, control, test);
		this.ignoreAttributeOrder = ignoreAttributeOrder;
		this.controlAttributes = Attributes.from(controlAttributes);
		this.testAttributes = Attributes.from(testAttributes);
	}

	public CompareElementCommand(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder,
	        NodeAndXpathCtx<Element> control, NodeAndXpathCtx<Element> test) {
		super(compPerformer, control, test);
		this.ignoreAttributeOrder = ignoreAttributeOrder;
		this.controlAttributes = Attributes.from(control.getNode().getAttributes());
		this.testAttributes = Attributes.from(test.getNode().getAttributes());
	}

	@Override
	public Queue<Comparison> provideComparisons() {
		final Element controlElement = getControl().getNode();
		final Element testElement = getTest().getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();
		comparisons.add(new Comparison(
		        ComparisonType.ELEMENT_TAG_NAME,
		        getControl(), Nodes.getQName(controlElement).getLocalPart(),
		        getTest(), Nodes.getQName(testElement).getLocalPart()));
		comparisons.addAll(provideAttrComparisons());
		return comparisons;
	}

	private Queue<Comparison> provideAttrComparisons() {
		Queue<Comparison> comparisons = new LinkedList<Comparison>();
		comparisons.add(new Comparison(
		        ComparisonType.ELEMENT_NUM_ATTRIBUTES,
		        getControl(), controlAttributes.getRegularAttributes().size(),
		        getTest(), testAttributes.getRegularAttributes().size()));

		comparisons.addAll(provideAttrListComparisons());
		comparisons.addAll(provideSchemaComparisons());

		return comparisons;
	}

	private Queue<Comparison> provideAttrListComparisons() {
		final Element controlElement = getControl().getNode();
		final Element testElement = getTest().getNode();
		final XPathContext controlContext = getControl().getXpathCtx();
		final XPathContext testContext = getTest().getXpathCtx();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(),
		        QNAME_MAPPER));
		testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

		for (final Attr controlAttr : controlAttributes.getRegularAttributes()) {
			final Attr testAttr = testAttributes.findMatchingRegularAttr(controlAttr);
			final boolean hasMatchingAttr = testAttr != null;

			controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
			comparisons.add(new Comparison(
			        ComparisonType.ATTR_NAME_LOOKUP,
			        NodeAndXpathCtx.from(controlElement, controlContext), true,
			        NodeAndXpathCtx.from(testElement, testContext), hasMatchingAttr));
			controlContext.navigateToParent();

			if (testAttr != null) {
				comparisons.addAll(provideMatchedAttrComparisons(
				        NodeAndXpathCtx.from(controlAttr, controlContext),
				        NodeAndXpathCtx.from(testAttr, testContext)));
			}
		}

		if (testAttributes.getRegularAttributes().size() > 0) {
			comparisons.addAll(provideUnmatchedTestAttrComparisons());
		}

		return comparisons;
	}

	private Queue<Comparison> provideSchemaComparisons() {
		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(new Comparison(
		        ComparisonType.SCHEMA_LOCATION,
		        getControl(), controlAttributes.getSchemaLocationValue(),
		        getTest(), testAttributes.getSchemaLocationValue()));
		comparisons.add(new Comparison(
		        ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION,
		        getControl(), controlAttributes.getNoNamespaceSchemaLocationValue(),
		        getTest(), testAttributes.getNoNamespaceSchemaLocationValue()));

		return comparisons;
	}

	private Queue<Comparison> provideMatchedAttrComparisons(
	        NodeAndXpathCtx<Attr> control, NodeAndXpathCtx<Attr> test) {

		Attr controlAttr = control.getNode();
		Attr testAttr = test.getNode();

		XPathContext controlContext = control.getXpathCtx();
		XPathContext testContext = test.getXpathCtx();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
		if (!ignoreAttributeOrder) {
			comparisons.addAll(provideAttrSequenceNumberComparisons(control, test));
		}

		testContext.navigateToAttribute(Nodes.getQName(testAttr));

		comparisons.addAll(new CompareNamespaceCommand(compPerformer,
		        NodeAndXpathCtx.<Node> from(controlAttr, controlContext),
		        NodeAndXpathCtx.<Node> from(testAttr, testContext))
		        .provideComparisons());
		comparisons.addAll(new CompareAttributeCommand(compPerformer,
		        NodeAndXpathCtx.from(controlAttr, controlContext),
		        NodeAndXpathCtx.from(testAttr, testContext))
		        .provideComparisons());

		testContext.navigateToParent();
		controlContext.navigateToParent();

		return comparisons;
	}

	private Queue<Comparison> provideAttrSequenceNumberComparisons(NodeAndXpathCtx<Attr> control,
	        NodeAndXpathCtx<Attr> test) {
		Attr controlAttr = control.getNode();
		Attr testAttr = test.getNode();
		XPathContext controlContext = control.getXpathCtx();
		XPathContext testContext = test.getXpathCtx();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		int controlAttrIndex = controlAttributes.getRegularAttributes().indexOf(controlAttr);
		int testAttrIndex = testAttributes.getRegularAttributes().indexOf(testAttr);
		if (testAttrIndex == controlAttrIndex) {
			return comparisons;
		}

		Attr mirrorTestAttr = findMirrorTestAttr(controlAttrIndex);
		String orderedTestNodeName = findMirrorTestAttrName(mirrorTestAttr);

		if (mirrorTestAttr == null) {
			return comparisons;
		}
		testContext.navigateToAttribute(Nodes.getQName(mirrorTestAttr));
		comparisons.add(new Comparison(
		        ComparisonType.ATTR_SEQUENCE,
		        NodeAndXpathCtx.from(controlAttr, controlContext), getUnNamespacedNodeName(controlAttr),
		        NodeAndXpathCtx.from(mirrorTestAttr, testContext), orderedTestNodeName));
		testContext.navigateToParent();
		return comparisons;
	}

	private Queue<Comparison> provideUnmatchedTestAttrComparisons() {
		final Element controlElement = getControl().getNode();
		final Element testElement = getTest().getNode();

		final XPathContext controlContext = getControl().getXpathCtx();
		final XPathContext testContext = getTest().getXpathCtx();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		for (Attr testAttr : testAttributes.getRegularAttributes()) {
			testContext.navigateToAttribute(Nodes.getQName(testAttr));

			Attr matchingControlAttr = controlAttributes.findMatchingRegularAttr(testAttr);
			final boolean hasMatchingAttr = matchingControlAttr != null;

			comparisons.add(new Comparison(
			        ComparisonType.ATTR_NAME_LOOKUP,
			        NodeAndXpathCtx.from(controlElement, controlContext), hasMatchingAttr,
			        NodeAndXpathCtx.from(testElement, testContext), true));

			testContext.navigateToParent();
		}

		return comparisons;
	}

	@Nullable
	private Attr findMirrorTestAttr(int controlAttrIndex) {
		if (testAttributes.getRegularAttributes().size() > controlAttrIndex) {
			return testAttributes.getRegularAttributes().get(controlAttrIndex);
		}
		return null;
	}

	private String findMirrorTestAttrName(@Nullable Attr mirrorTestAttr) {
		if (mirrorTestAttr == null) {
			return "[attribute absent]";
		}
		return getUnNamespacedNodeName(mirrorTestAttr);
	}

	/**
	 * @param aNode
	 * @return true if the node has a namespace
	 */
	private boolean isNamespaced(Node aNode) {
		String namespace = aNode.getNamespaceURI();
		return namespace != null && namespace.length() > 0;
	}

	private String getUnNamespacedNodeName(Node aNode) {
		if (isNamespaced(aNode)) {
			return aNode.getLocalName();
		}
		return aNode.getNodeName();
	}

	/**
	 * Maps Nodes to their QNames.
	 */
	private final Linqy.Mapper<Node, QName> QNAME_MAPPER =
	        new Linqy.Mapper<Node, QName>() {
		        @Override
		        public QName map(Node n) {
			        return Nodes.getQName(n);
		        }
	        };
}
