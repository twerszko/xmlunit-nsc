package net.sf.xmlunit.diff.comparators.commands;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.IterableNodeList;
import net.sf.xmlunit.util.Linqy;
import net.sf.xmlunit.util.Predicate;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class CompareNodeCommand extends ComparisonCommandBase<Node> {

	final boolean ignoreAttributeOrder;

	public CompareNodeCommand(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder,
	        NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
		super(compPerformer, control, test);
		this.ignoreAttributeOrder = ignoreAttributeOrder;
	}

	@Override
	public Queue<Comparison> provideComparisons() {
		final Node controlNode = getControl().getNode();
		final Node testNode = getTest().getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(new Comparison(
		        ComparisonType.NODE_TYPE,
		        getControl(), controlNode.getNodeType(),
		        getTest(), testNode.getNodeType()));

		comparisons.addAll(new CompareNamespaceCommand(compPerformer,
		        getControl(), getTest()).provideComparisons());

		if (controlNode.getNodeType() != Node.ATTRIBUTE_NODE) {
			comparisons.addAll(provideChildrenNumberComparisons());
		}

		comparisons.addAll(provideNodeTypeSpecificComparison());

		return comparisons;
	}

	public Queue<Comparison> provideChildrenNumberComparisons() {
		Node controlNode = getControl().getNode();
		Node testNode = getTest().getNode();

		Iterable<Node> controlChildren =
		        Linqy.filter(new IterableNodeList(controlNode.getChildNodes()), INTERESTING_NODES);
		Iterable<Node> testChildren =
		        Linqy.filter(new IterableNodeList(testNode.getChildNodes()), INTERESTING_NODES);

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		if (Linqy.count(controlChildren) > 0 && Linqy.count(testChildren) > 0) {
			comparisons.add(new Comparison(
			        ComparisonType.CHILD_NODELIST_LENGTH,
			        getControl(), Linqy.count(controlChildren),
			        getTest(), Linqy.count(testChildren)));
		} else {
			comparisons.add(new Comparison(ComparisonType.HAS_CHILD_NODES,
			        getControl(), Linqy.count(controlChildren) > 0,
			        getTest(), Linqy.count(testChildren) > 0));
		}
		return comparisons;
	}

	/**
	 * Dispatches to the node type specific comparison if one is defined for the
	 * given combination of nodes.
	 */
	private Queue<Comparison> provideNodeTypeSpecificComparison() {

		final Node controlNode = getControl().getNode();
		final Node testNode = getTest().getNode();

		final XPathContext controlContext = getControl().getXpathCtx();
		final XPathContext testContext = getTest().getXpathCtx();

		ComparisonCommandBase<? extends Node> command = null;

		switch (controlNode.getNodeType()) {
			case Node.CDATA_SECTION_NODE:
			case Node.COMMENT_NODE:
			case Node.TEXT_NODE:
				if (testNode instanceof CharacterData) {
					command = new CompareCharacterDataCommand(compPerformer,
					        NodeAndXpathCtx.from((CharacterData) controlNode, controlContext),
					        NodeAndXpathCtx.from((CharacterData) testNode, testContext));
				}
				break;
			case Node.DOCUMENT_NODE:
				if (testNode instanceof Document) {
					command = new CompareDocumentCommand(compPerformer,
					        NodeAndXpathCtx.from((Document) controlNode, controlContext),
					        NodeAndXpathCtx.from((Document) testNode, testContext));
				}
				break;
			case Node.ELEMENT_NODE:
				if (testNode instanceof Element) {
					command = new CompareElementCommand(
					        compPerformer, ignoreAttributeOrder,
					        NodeAndXpathCtx.from((Element) controlNode, controlContext),
					        NodeAndXpathCtx.from((Element) testNode, testContext));
				}
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				if (testNode instanceof ProcessingInstruction) {
					command = new CompareProcInstrCommand(compPerformer,
					        NodeAndXpathCtx.from((ProcessingInstruction) controlNode, controlContext),
					        NodeAndXpathCtx.from((ProcessingInstruction) testNode, testContext));
				}
				break;
			case Node.DOCUMENT_TYPE_NODE:
				if (testNode instanceof DocumentType) {
					command = new CompareDoctypeCommand(compPerformer,
					        NodeAndXpathCtx.from((DocumentType) controlNode, controlContext),
					        NodeAndXpathCtx.from((DocumentType) testNode, testContext));
				}
				break;
			case Node.ATTRIBUTE_NODE:
				if (testNode instanceof Attr) {
					command = new CompareAttributeCommand(compPerformer,
					        NodeAndXpathCtx.from((Attr) controlNode, controlContext),
					        NodeAndXpathCtx.from((Attr) testNode, testContext));
				}
				break;
		}

		Queue<Comparison> comparisons = new LinkedList<Comparison>();
		if (command != null) {
			comparisons.addAll(command.provideComparisons());
		}
		return comparisons;
	}

	/**
	 * Suppresses document-type nodes.
	 */
	// TODO duplication in nodecomparator
	protected static final Predicate<Node> INTERESTING_NODES =
	        new Predicate<Node>() {
		        @Override
		        public boolean matches(Node n) {
			        return n.getNodeType() != Node.DOCUMENT_TYPE_NODE;
		        }
	        };
}
