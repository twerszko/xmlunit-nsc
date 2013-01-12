package net.sf.xmlunit.diff.commands;

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

public class CompareNodeCommand extends ComparisonCommandBase<Node> {

    final boolean ignoreAttributeOrder;

    public CompareNodeCommand(ComparisonPerformer compPerformer, boolean ignoreAttributeOrder,
            NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
        super(compPerformer, control, test);
        this.ignoreAttributeOrder = ignoreAttributeOrder;
    }

    @Override
    public Queue<Comparison> provideComparisons() {
        Node controlNode = getControl().getNode();
        Node testNode = getTest().getNode();

        Queue<Comparison> comparisons = new LinkedList<Comparison>();

        comparisons.add(
                Comparison.ofType(ComparisonType.NODE_TYPE)
                        .between(getControl(), controlNode.getNodeType())
                        .and(getTest(), testNode.getNodeType()));

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

        Iterable<Node> controlChildren = getFilteredChildNodes(controlNode);
        Iterable<Node> testChildren = getFilteredChildNodes(testNode);

        Queue<Comparison> comparisons = new LinkedList<Comparison>();

        int controlChildrenCount = Linqy.count(controlChildren);
        int testChildrenCount = Linqy.count(testChildren);
        if (controlChildrenCount > 0 && testChildrenCount > 0) {
            comparisons.add(
                    Comparison.ofType(ComparisonType.CHILD_NODELIST_LENGTH)
                            .between(getControl(), controlChildrenCount)
                            .and(getTest(), testChildrenCount));
        } else {
            comparisons.add(
                    Comparison.ofType(ComparisonType.HAS_CHILD_NODES)
                            .between(getControl(), controlChildrenCount > 0)
                            .and(getTest(), testChildrenCount > 0));
        }
        return comparisons;
    }

    /**
     * Dispatches to the node type specific comparison if one is defined for the
     * given combination of nodes.
     */
    private Queue<Comparison> provideNodeTypeSpecificComparison() {

        Node controlNode = getControl().getNode();
        Node testNode = getTest().getNode();

        XPathContext controlContext = getControl().getXpathCtx();
        XPathContext testContext = getTest().getXpathCtx();

        ComparisonCommandBase<? extends Node> command = null;

        switch (controlNode.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.TEXT_NODE:
                if (testNode instanceof CharacterData) {
                    command = new CompareCharacterDataCommand(compPerformer,
                            NodeAndXpath.from((CharacterData) controlNode, controlContext),
                            NodeAndXpath.from((CharacterData) testNode, testContext));
                }
                break;
            case Node.DOCUMENT_NODE:
                if (testNode instanceof Document) {
                    command = new CompareDocumentCommand(compPerformer,
                            NodeAndXpath.from((Document) controlNode, controlContext),
                            NodeAndXpath.from((Document) testNode, testContext));
                }
                break;
            case Node.ELEMENT_NODE:
                if (testNode instanceof Element) {
                    command = new CompareElementCommand(
                            compPerformer, ignoreAttributeOrder,
                            NodeAndXpath.from((Element) controlNode, controlContext),
                            NodeAndXpath.from((Element) testNode, testContext));
                }
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                if (testNode instanceof ProcessingInstruction) {
                    command = new CompareProcInstrCommand(compPerformer,
                            NodeAndXpath.from((ProcessingInstruction) controlNode, controlContext),
                            NodeAndXpath.from((ProcessingInstruction) testNode, testContext));
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                if (testNode instanceof DocumentType) {
                    command = new CompareDoctypeCommand(compPerformer,
                            NodeAndXpath.from((DocumentType) controlNode, controlContext),
                            NodeAndXpath.from((DocumentType) testNode, testContext));
                }
                break;
            case Node.ATTRIBUTE_NODE:
                if (testNode instanceof Attr) {
                    command = new CompareAttributeCommand(compPerformer,
                            NodeAndXpath.from((Attr) controlNode, controlContext),
                            NodeAndXpath.from((Attr) testNode, testContext));
                }
                break;
        }

        Queue<Comparison> comparisons = new LinkedList<Comparison>();
        if (command != null) {
            comparisons.addAll(command.provideComparisons());
        }
        return comparisons;
    }
}
