package net.sf.xmlunit.diff;

import org.xmlunit.diff.ElementSelector;

public interface DifferenceEngineFactory {
    DifferenceEngine newEngine();

    void useEvaluator(DifferenceEvaluator evaluator);

    void useSelector(ElementSelector selector);
}
