package net.sf.xmlunit.diff.comparators;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Attr;

public class AttributeComparator extends NodeComparator<Attr> {

    public AttributeComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    @Override
    public ComparisonResult compare(NodeAndXpathCtx<Attr> control, NodeAndXpathCtx<Attr> test) {
        Attr controlAttr = control.getNode();
        Attr testAttr = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED,
                        control, controlAttr.getSpecified(),
                        test, testAttr.getSpecified()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(new Comparison(ComparisonType.ATTR_VALUE,
                control, controlAttr.getValue(),
                test, testAttr.getValue()));

        return lastResult;
    }

}
