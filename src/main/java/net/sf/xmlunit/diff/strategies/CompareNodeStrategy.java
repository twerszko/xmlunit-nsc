package net.sf.xmlunit.diff.strategies;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.util.Linqy;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class CompareNodeStrategy extends ComparisonStrategyBase<Node> {

	final boolean ignoreAttributeOrder;

	public CompareNodeStrategy(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder) {
		super(compPerformer);
		this.ignoreAttributeOrder = ignoreAttributeOrder;
	}

	@Override
	public Queue<Comparison> provideComparisons(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
		Node controlNode = control.getNode();
		Node testNode = test.getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(
		        Comparison.ofType(ComparisonType.NODE_TYPE)
		                .between(control, controlNode.getNodeType())
		                .and(test, testNode.getNodeType()));

		comparisons.addAll(new CompareNamespaceStrategy(performer)
		        .provideComparisons(control, test));

		if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
			comparisons.addAll(provideChildrenNumberComparisons(control, test));
		}

		comparisons.addAll(provideNodeTypeSpecificComparison(control, test));

		return comparisons;
	}

	public Queue<Comparison> provideChildrenNumberComparisons(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
		Node controlNode = control.getNode();
		Node testNode = test.getNode();

		Iterable<Node> controlChildren = getFilteredChildNodes(controlNode);
		Iterable<Node> testChildren = getFilteredChildNodes(testNode);

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		int controlChildrenCount = Linqy.count(controlChildren);
		int testChildrenCount = Linqy.count(testChildren);
		if (controlChildrenCount > 0 && testChildrenCount > 0) {
			comparisons.add(
			        Comparison.ofType(ComparisonType.CHILD_NODELIST_LENGTH)
			                .between(control, controlChildrenCount)
			                .and(test, testChildrenCount));
		} else {
			comparisons.add(
			        Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
			                .between(control, controlChildrenCount > 0)
			                .and(test, testChildrenCount > 0));
		}
		return comparisons;
	}

	/**
	 * Dispatches to the node type specific comparison if one is defined for the
	 * given combination of nodes.
	 */
	private Queue<Comparison> provideNodeTypeSpecificComparison(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {

		Node controlNode = control.getNode();
		Node testNode = test.getNode();

		XPathContext controlContext = control.getXpathCtx();
		XPathContext testContext = test.getXpathCtx();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		switch (controlNode.getNodeType()) {
			case Node.CDATA_SECTION_NODE:
			case Node.COMMENT_NODE:
			case Node.TEXT_NODE:
				if (testNode instanceof CharacterData) {
					comparisons.addAll(new CompareCharacterDataStrategy(performer)
					        .provideComparisons(
					                NodeAndXpath.from((CharacterData) controlNode, controlContext),
					                NodeAndXpath.from((CharacterData) testNode, testContext)));
				}
				break;
			case Node.DOCUMENT_NODE:
				if (testNode instanceof Document) {
					comparisons.addAll(new CompareDocumentStrategy(performer)
					        .provideComparisons(
					                NodeAndXpath.from((Document) controlNode, controlContext),
					                NodeAndXpath.from((Document) testNode, testContext)));
				}
				break;
			case Node.ELEMENT_NODE:
				if (testNode instanceof Element) {
					comparisons.addAll(new CompareElementStrategy(performer, ignoreAttributeOrder)
					        .provideComparisons(
					                NodeAndXpath.from((Element) controlNode, controlContext),
					                NodeAndXpath.from((Element) testNode, testContext)));
				}
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				if (testNode instanceof ProcessingInstruction) {
					comparisons.addAll(new CompareProcInstrStrategy(performer)
					        .provideComparisons(
					                NodeAndXpath.from((ProcessingInstruction) controlNode, controlContext),
					                NodeAndXpath.from((ProcessingInstruction) testNode, testContext)));
				}
				break;
			case Node.DOCUMENT_TYPE_NODE:
				if (testNode instanceof DocumentType) {
					comparisons.addAll(new CompareDoctypeStrategy(performer)
					        .provideComparisons(
					                NodeAndXpath.from((DocumentType) controlNode, controlContext),
					                NodeAndXpath.from((DocumentType) testNode, testContext)));
				}
				break;
			case Node.ATTRIBUTE_NODE:
				if (testNode instanceof Attr) {
					comparisons.addAll(new CompareAttributeStrategy(performer).
					        provideComparisons(
					                NodeAndXpath.from((Attr) controlNode, controlContext),
					                NodeAndXpath.from((Attr) testNode, testContext)));
				}
				break;
		}

		return comparisons;
	}
}
