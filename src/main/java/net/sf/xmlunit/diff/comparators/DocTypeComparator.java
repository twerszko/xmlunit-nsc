package net.sf.xmlunit.diff.comparators;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.DocumentType;

public class DocTypeComparator extends NodeComparator<DocumentType> {

    public DocTypeComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    @Override
    public ComparisonResult compare(NodeAndXpathCtx<DocumentType> control, NodeAndXpathCtx<DocumentType> test) {
        DocumentType controlNode = control.getNode();
        DocumentType testNode = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.DOCTYPE_NAME,
                        control, controlNode.getName(),
                        test, testNode.getName()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.DOCTYPE_PUBLIC_ID,
                        control, controlNode.getPublicId(),
                        test, testNode.getPublicId()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.DOCTYPE_SYSTEM_ID,
                        controlNode, null, controlNode.getSystemId(),
                        testNode, null, testNode.getSystemId()));

        return lastResult;
    }

}
