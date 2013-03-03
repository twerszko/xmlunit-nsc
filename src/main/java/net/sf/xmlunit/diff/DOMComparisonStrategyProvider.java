package net.sf.xmlunit.diff;

import net.sf.xmlunit.diff.strategies.ComparisonStrategy;
import net.sf.xmlunit.diff.strategies.ComparisonStrategyProvider;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class DOMComparisonStrategyProvider implements ComparisonStrategyProvider {

	private ComparisonStrategy<Attr> attrStrategy;
	private ComparisonStrategy<CharacterData> charDataStrategy;
	private ComparisonStrategy<DocumentType> doctypeStrategy;
	private ComparisonStrategy<Document> documentStrategy;
	private ComparisonStrategy<Element> elementStrategy;
	private ComparisonStrategy<Node> namespaceStrategy;
	private ComparisonStrategy<ProcessingInstruction> procInstrStrategy;

	@Override
	public ComparisonStrategy<Attr> getAttributeComparisonStrategy() {
		return attrStrategy;
	}

	void setAttributeComparisonStrategy(ComparisonStrategy<Attr> attrStrategy) {
		this.attrStrategy = attrStrategy;
	}

	@Override
	public ComparisonStrategy<CharacterData> getCharacterDataComparisonStrategy() {
		return charDataStrategy;
	}

	void setCharacterDataComparisonStrategy(ComparisonStrategy<CharacterData> charDataStrategy) {
		this.charDataStrategy = charDataStrategy;
	}

	@Override
	public ComparisonStrategy<DocumentType> getDoctypeComparisonStrategy() {
		return doctypeStrategy;
	}

	void setDoctypeComparisonStrategy(ComparisonStrategy<DocumentType> doctypeStrategy) {
		this.doctypeStrategy = doctypeStrategy;
	}

	@Override
	public ComparisonStrategy<Document> getDocumentComparisonStrategy() {
		return documentStrategy;
	}

	void setDocumentComparisonStrategyStrategy(ComparisonStrategy<Document> documentStrategy) {
		this.documentStrategy = documentStrategy;
	}

	@Override
	public ComparisonStrategy<Element> getElementComparisonStrategy() {
		return elementStrategy;
	}

	void setElementComparisonStrategy(ComparisonStrategy<Element> elementStrategy) {
		this.elementStrategy = elementStrategy;
	}

	@Override
	public ComparisonStrategy<Node> getNamespaceComparisonStrategy() {
		return namespaceStrategy;
	}

	public void setNamespaceComparisonStrategy(ComparisonStrategy<Node> namespaceStrategy) {
		this.namespaceStrategy = namespaceStrategy;
	}

	@Override
	public ComparisonStrategy<ProcessingInstruction> getProcInstrComparisonStrategy() {
		return procInstrStrategy;
	}

	void setProcInstrComparisonStrategy(ComparisonStrategy<ProcessingInstruction> procInstrStrategy) {
		this.procInstrStrategy = procInstrStrategy;
	}

}
