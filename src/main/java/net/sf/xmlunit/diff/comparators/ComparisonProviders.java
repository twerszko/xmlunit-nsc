package net.sf.xmlunit.diff.comparators;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class ComparisonProviders {
	private ComparisonProvider<Attr> attrComparisonProvider;
	private ComparisonProvider<CharacterData> charDataComarisonProvider;
	private ComparisonProvider<DocumentType> doctypeComparisonProvider;
	private ComparisonProvider<Document> docComparisonProvider;
	private ComparisonProvider<Element> elementComparisonProvider;
	private ComparisonProvider<Node> nsComparisonProvider;
	private ComparisonProvider<ProcessingInstruction> procInstrComparisonProvider;

	public ComparisonProviders() {
		setAttributeComparisonProvider(new AttributeComparisonProvider());
		setCharDataComarisonProvider(new CharacterDataComparisonProvider());
		setDoctypeComparisonProvider(new DoctypeComparisonProvider());
		setDocumentComparisonProvider(new DocumentComparisonProvider());
		setElementComparisonProvider(new ElementComparisonProvider(true));
		setNamespaceComparisonProvider(new NamespaceComparisonProvider());
		setProcInstrComparisonProvider(new ProcInstrComparisonProvider());
	}

	public ComparisonProvider<Attr> getAttributeComparisonProvider() {
		return attrComparisonProvider;
	}

	public void setAttributeComparisonProvider(ComparisonProvider<Attr> attrComparisonProvider) {
		this.attrComparisonProvider = attrComparisonProvider;
	}

	public ComparisonProvider<CharacterData> getCharDataComarisonProvider() {
		return charDataComarisonProvider;
	}

	public void setCharDataComarisonProvider(ComparisonProvider<CharacterData> charDataComarisonProvider) {
		this.charDataComarisonProvider = charDataComarisonProvider;
	}

	public ComparisonProvider<DocumentType> getDoctypeComparisonProvider() {
		return doctypeComparisonProvider;
	}

	public void setDoctypeComparisonProvider(ComparisonProvider<DocumentType> doctypeComparisonProvider) {
		this.doctypeComparisonProvider = doctypeComparisonProvider;
	}

	public ComparisonProvider<Document> getDocumentComparisonProvider() {
		return docComparisonProvider;
	}

	public void setDocumentComparisonProvider(ComparisonProvider<Document> docComparisonProvider) {
		this.docComparisonProvider = docComparisonProvider;
	}

	public ComparisonProvider<Element> getElementComparisonProvider() {
		return elementComparisonProvider;
	}

	public void setElementComparisonProvider(ComparisonProvider<Element> elementComparisonProvider) {
		this.elementComparisonProvider = elementComparisonProvider;
	}

	public ComparisonProvider<Node> getNamespaceComparisonProvider() {
		return nsComparisonProvider;
	}

	public void setNamespaceComparisonProvider(ComparisonProvider<Node> nsComparisonProvider) {
		this.nsComparisonProvider = nsComparisonProvider;
	}

	public ComparisonProvider<ProcessingInstruction> getProcInstrComparisonProvider() {
		return procInstrComparisonProvider;
	}

	public void setProcInstrComparisonProvider(ComparisonProvider<ProcessingInstruction> procInstrComparisonProvider) {
		this.procInstrComparisonProvider = procInstrComparisonProvider;
	}

}
