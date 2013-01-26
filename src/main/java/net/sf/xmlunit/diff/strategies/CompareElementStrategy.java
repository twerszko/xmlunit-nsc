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

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.Attributes;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Nodes;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CompareElementStrategy extends ComparisonStrategyBase<Element> {

	final boolean ignoreAttributeOrder;

	CompareElementStrategy(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder,
	        NodeAndXpath<Element> control, NodeAndXpath<Element> test) {
		super(compPerformer);
		this.ignoreAttributeOrder = ignoreAttributeOrder;
	}

	public CompareElementStrategy(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder) {
		super(compPerformer);
		this.ignoreAttributeOrder = ignoreAttributeOrder;
	}

	@Override
	public Comparisons provideComparisons(NodeAndXpath<Element> control, NodeAndXpath<Element> test) {
		NamedNodeMap controlAttrMap = control.getNode().getAttributes();
		NamedNodeMap testAttrMap = test.getNode().getAttributes();
		return provideComparisons(control, controlAttrMap, test, testAttrMap);
	}

	public Comparisons provideComparisons(
	        NodeAndXpath<Element> control, NamedNodeMap controlAttrMap,
	        NodeAndXpath<Element> test, NamedNodeMap testAttrMap) {
		Element controlElement = control.getNode();
		Element testElement = test.getNode();

		Attributes controlAttributes = Attributes.from(controlAttrMap);
		Attributes testAttributes = Attributes.from(testAttrMap);

		Comparisons comparisons = new Comparisons();

		String controlElementName = Nodes.getQName(controlElement).getLocalPart();
		String testElementName = Nodes.getQName(testElement).getLocalPart();
		comparisons.add(
		        Comparison.ofType(ComparisonType.ELEMENT_TAG_NAME)
		                .between(control, controlElementName)
		                .and(test, testElementName));
		comparisons.addAll(provideAttrComparisons(control, controlAttributes, test, testAttributes).getAll());
		return comparisons;
	}

	private Comparisons provideAttrComparisons(
	        NodeAndXpath<Element> control, Attributes controlAttributes,
	        NodeAndXpath<Element> test, Attributes testAttributes) {

		Comparisons comparisons = new Comparisons();
		comparisons.add(
		        Comparison.ofType(ComparisonType.ELEMENT_NUM_ATTRIBUTES)
		                .between(control, controlAttributes.getRegularAttributes().size())
		                .and(test, testAttributes.getRegularAttributes().size()));

		comparisons.addAll(provideAttrListComparisons(control, controlAttributes, test, testAttributes));
		comparisons.addAll(provideSchemaComparisons(control, controlAttributes, test, testAttributes));

		return comparisons;
	}

	private Comparisons provideAttrListComparisons(
	        NodeAndXpath<Element> control, Attributes controlAttributes,
	        NodeAndXpath<Element> test, Attributes testAttributes) {

		final Element controlElement = control.getNode();
		final Element testElement = test.getNode();
		final XPathContext controlContext = control.getXpathCtx();
		final XPathContext testContext = test.getXpathCtx();

		Comparisons comparisons = new Comparisons();

		controlContext.addAttributes(Linqy.map(controlAttributes.getRegularAttributes(),
		        QNAME_MAPPER));
		testContext.addAttributes(Linqy.map(testAttributes.getRegularAttributes(), QNAME_MAPPER));

		for (final Attr controlAttr : controlAttributes.getRegularAttributes()) {
			final Attr testAttr = testAttributes.findMatchingRegularAttr(controlAttr);
			final boolean hasMatchingAttr = testAttr != null;

			controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
			comparisons.add(
			        Comparison.ofType(ComparisonType.ATTR_NAME_LOOKUP)
			                .between(NodeAndXpath.from(controlElement, controlContext), true)
			                .and(NodeAndXpath.from(testElement, testContext), hasMatchingAttr));
			controlContext.navigateToParent();

			if (testAttr != null) {
				comparisons.addAll(provideMatchedAttrComparisons(
				        NodeAndXpath.from(controlAttr, controlContext), controlAttributes,
				        NodeAndXpath.from(testAttr, testContext), testAttributes));
			}
		}

		if (testAttributes.getRegularAttributes().size() > 0) {
			comparisons.addAll(provideUnmatchedTestAttrComparisons(control, controlAttributes, test, testAttributes));
		}

		return comparisons;
	}

	private Comparisons provideSchemaComparisons(
	        NodeAndXpath<Element> control, Attributes controlAttributes,
	        NodeAndXpath<Element> test, Attributes testAttributes) {

		Comparisons comparisons = new Comparisons();

		comparisons.add(
		        Comparison.ofType(ComparisonType.SCHEMA_LOCATION)
		                .between(control, controlAttributes.getSchemaLocationValue())
		                .and(test, testAttributes.getSchemaLocationValue()));
		comparisons.add(
		        Comparison.ofType(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION)
		                .between(control, controlAttributes.getNoNamespaceSchemaLocationValue())
		                .and(test, testAttributes.getNoNamespaceSchemaLocationValue()));

		return comparisons;
	}

	private Comparisons provideMatchedAttrComparisons(
	        NodeAndXpath<Attr> control, Attributes controlAttributes,
	        NodeAndXpath<Attr> test, Attributes testAttributes) {

		Attr controlAttr = control.getNode();
		Attr testAttr = test.getNode();

		XPathContext controlContext = control.getXpathCtx();
		XPathContext testContext = test.getXpathCtx();

		Comparisons comparisons = new Comparisons();

		controlContext.navigateToAttribute(Nodes.getQName(controlAttr));
		if (!ignoreAttributeOrder) {
			comparisons.addAll(provideAttrSequenceNumberComparisons(control, controlAttributes, test, testAttributes));
		}

		testContext.navigateToAttribute(Nodes.getQName(testAttr));

		comparisons.addAll(new CompareNamespaceStrategy(performer)
		        .provideComparisons(
		                NodeAndXpath.<Node> from(controlAttr, controlContext),
		                NodeAndXpath.<Node> from(testAttr, testContext)));
		comparisons.addAll(new CompareAttributeStrategy(performer)
		        .provideComparisons(
		                NodeAndXpath.from(controlAttr, controlContext),
		                NodeAndXpath.from(testAttr, testContext)));

		testContext.navigateToParent();
		controlContext.navigateToParent();

		return comparisons;
	}

	private Comparisons provideAttrSequenceNumberComparisons(
	        NodeAndXpath<Attr> control, Attributes controlAttributes,
	        NodeAndXpath<Attr> test, Attributes testAttributes) {
		Attr controlAttr = control.getNode();
		Attr testAttr = test.getNode();
		XPathContext controlContext = control.getXpathCtx();
		XPathContext testContext = test.getXpathCtx();

		Comparisons comparisons = new Comparisons();

		int controlAttrIndex = controlAttributes.getRegularAttributes().indexOf(controlAttr);
		int testAttrIndex = testAttributes.getRegularAttributes().indexOf(testAttr);
		if (testAttrIndex == controlAttrIndex) {
			return comparisons;
		}

		Attr mirrorTestAttr = findMirrorTestAttr(testAttributes, controlAttrIndex);
		String orderedTestNodeName = findMirrorTestAttrName(mirrorTestAttr);

		if (mirrorTestAttr == null) {
			return comparisons;
		}
		testContext.navigateToAttribute(Nodes.getQName(mirrorTestAttr));
		comparisons.add(
		        Comparison.ofType(ComparisonType.ATTR_SEQUENCE)
		                .between(NodeAndXpath.from(controlAttr, controlContext), getUnNamespacedNodeName(controlAttr))
		                .and(NodeAndXpath.from(mirrorTestAttr, testContext), orderedTestNodeName));
		testContext.navigateToParent();
		return comparisons;
	}

	private Comparisons provideUnmatchedTestAttrComparisons(
	        NodeAndXpath<Element> control, Attributes controlAttributes,
	        NodeAndXpath<Element> test, Attributes testAttributes) {

		final Element controlElement = control.getNode();
		final Element testElement = test.getNode();

		final XPathContext controlContext = control.getXpathCtx();
		final XPathContext testContext = test.getXpathCtx();

		Comparisons comparisons = new Comparisons();

		for (Attr testAttr : testAttributes.getRegularAttributes()) {
			testContext.navigateToAttribute(Nodes.getQName(testAttr));

			Attr matchingControlAttr = controlAttributes.findMatchingRegularAttr(testAttr);
			final boolean hasMatchingAttr = matchingControlAttr != null;

			comparisons.add(
			        Comparison.ofType(ComparisonType.ATTR_NAME_LOOKUP)
			                .between(NodeAndXpath.from(controlElement, controlContext), hasMatchingAttr)
			                .and(NodeAndXpath.from(testElement, testContext), true));

			testContext.navigateToParent();
		}

		return comparisons;
	}

	@Nullable
	private Attr findMirrorTestAttr(Attributes testAttributes, int controlAttrIndex) {
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
