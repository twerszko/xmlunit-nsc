package net.sf.xmlunit.diff.strategies;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public interface ComparisonStrategyProvider {

	public ComparisonStrategy<Attr> getAttributeComparisonStrategy();

	public ComparisonStrategy<CharacterData> getCharacterDataComparisonStrategy();

	public ComparisonStrategy<DocumentType> getDoctypeComparisonStrategy();

	public ComparisonStrategy<Document> getDocumentComparisonStrategy();

	public ComparisonStrategy<Element> getElementComparisonStrategy();

	public ComparisonStrategy<Node> getNamespaceComparisonStrategy();

	public ComparisonStrategy<ProcessingInstruction> getProcInstrComparisonStrategy();
}
