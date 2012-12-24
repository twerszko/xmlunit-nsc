package net.sf.xmlunit.diff.comparators;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.Nullable;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class DocumentComparator extends NodeComparator<Document> {

    public DocumentComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    /**
     * Compares document node, doctype and XML declaration properties
     */
    @Override
    public ComparisonResult compare(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
        Queue<ComparisonStrategy<?>> strategies = new LinkedList<ComparisonStrategy<?>>();
        strategies.add(new HasDoctypeStrategy(control, test));
        strategies.add(new CompareDoctypeStrategy(control, test));
        strategies.add(new CompareXmlVersionStrategy(control, test));
        strategies.add(new CompareXmlStandaloneStrategy(control, test));
        strategies.add(new CompareXmlEncodingStrategy(control, test));

        ComparisonResult result = null;
        for (ComparisonStrategy<?> strategy : strategies) {
            result = strategy.performComparison();
            if (result == ComparisonResult.CRITICAL) {
                return result;
            }
        }

        return result;
    }

    protected class HasDoctypeStrategy extends ComparisonStrategy<Document> {

        public HasDoctypeStrategy(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult performComparison() {
            DocumentType controlDt = getControl().getNode().getDoctype();
            DocumentType testDt = getTest().getNode().getDoctype();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.HAS_DOCTYPE_DECLARATION,
                            getControl(), controlDt != null,
                            getTest(), testDt != null));
        }
    }

    protected class CompareDoctypeStrategy extends ComparisonStrategy<Document> {

        public CompareDoctypeStrategy(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        @Nullable
        public ComparisonResult performComparison() {
            DocumentType controlDt = getControl().getNode().getDoctype();
            DocumentType testDt = getTest().getNode().getDoctype();

            if (controlDt == null || testDt == null) {
                return null;
            }

            return compareDoctypes(
                    NodeAndXpathCtx.from(controlDt, getControl().getXpathCtx()),
                    NodeAndXpathCtx.from(testDt, getTest().getXpathCtx()));
        }
    }

    protected class CompareXmlVersionStrategy extends ComparisonStrategy<Document> {

        public CompareXmlVersionStrategy(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult performComparison() {
            Document controlDoc = getControl().getNode();
            Document testDoc = getTest().getNode();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.XML_VERSION,
                            getControl(), controlDoc.getXmlVersion(),
                            getTest(), testDoc.getXmlVersion()));
        }
    }

    protected class CompareXmlStandaloneStrategy extends ComparisonStrategy<Document> {

        public CompareXmlStandaloneStrategy(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult performComparison() {
            Document controlDoc = getControl().getNode();
            Document testDoc = getTest().getNode();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.XML_STANDALONE,
                            getControl(), controlDoc.getXmlStandalone(),
                            getTest(), testDoc.getXmlStandalone()));
        }
    }

    protected class CompareXmlEncodingStrategy extends ComparisonStrategy<Document> {

        public CompareXmlEncodingStrategy(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult performComparison() {
            Document controlDoc = getControl().getNode();
            Document testDoc = getTest().getNode();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.XML_ENCODING,
                            getControl(), controlDoc.getXmlEncoding(),
                            getTest(), testDoc.getXmlEncoding()));
        }

    }
}
