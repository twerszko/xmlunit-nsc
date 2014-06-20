package org.xmlunit.diff.providers;

import net.sf.xmlunit.diff.DefaultNodeMatcher;
import net.sf.xmlunit.diff.NodeMatcher;

import org.w3c.dom.*;
import org.w3c.dom.CharacterData;

public class ComparisonProviders {
    private ComparisonProvider<Attr> attrComparisonProvider;
    private ComparisonProvider<CharacterData> charDataComarisonProvider;
    private ComparisonProvider<DocumentType> doctypeComparisonProvider;
    private ComparisonProvider<Document> docComparisonProvider;
    private ComparisonProvider<Element> elementComparisonProvider;
    private ComparisonProvider<Node> nsComparisonProvider;
    private ComparisonProvider<ProcessingInstruction> procInstrComparisonProvider;
    private NodeMatcher nodeMatcher = new DefaultNodeMatcher();

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

    protected void setAttributeComparisonProvider(ComparisonProvider<Attr> attrComparisonProvider) {
        this.attrComparisonProvider = attrComparisonProvider;
    }

    public ComparisonProvider<CharacterData> getCharDataComarisonProvider() {
        return charDataComarisonProvider;
    }

    protected void setCharDataComarisonProvider(ComparisonProvider<CharacterData> charDataComarisonProvider) {
        this.charDataComarisonProvider = charDataComarisonProvider;
    }

    public ComparisonProvider<DocumentType> getDoctypeComparisonProvider() {
        return doctypeComparisonProvider;
    }

    protected void setDoctypeComparisonProvider(ComparisonProvider<DocumentType> doctypeComparisonProvider) {
        this.doctypeComparisonProvider = doctypeComparisonProvider;
    }

    public ComparisonProvider<Document> getDocumentComparisonProvider() {
        return docComparisonProvider;
    }

    protected void setDocumentComparisonProvider(ComparisonProvider<Document> docComparisonProvider) {
        this.docComparisonProvider = docComparisonProvider;
    }

    public ComparisonProvider<Element> getElementComparisonProvider() {
        return elementComparisonProvider;
    }

    protected void setElementComparisonProvider(ComparisonProvider<Element> elementComparisonProvider) {
        this.elementComparisonProvider = elementComparisonProvider;
    }

    public ComparisonProvider<Node> getNamespaceComparisonProvider() {
        return nsComparisonProvider;
    }

    protected void setNamespaceComparisonProvider(ComparisonProvider<Node> nsComparisonProvider) {
        this.nsComparisonProvider = nsComparisonProvider;
    }

    public ComparisonProvider<ProcessingInstruction> getProcInstrComparisonProvider() {
        return procInstrComparisonProvider;
    }

    protected void
            setProcInstrComparisonProvider(ComparisonProvider<ProcessingInstruction> procInstrComparisonProvider) {
        this.procInstrComparisonProvider = procInstrComparisonProvider;
    }

    public NodeMatcher getNodeMatcher() {
        return nodeMatcher;
    }

    protected void setNodeMatcher(NodeMatcher nodeMatcher) {
        this.nodeMatcher = nodeMatcher;
    }

}
