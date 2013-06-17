package net.sf.xmlunit.diff;

public interface DifferenceEngineFactory {
    DifferenceEngine newEngine();

    void useEvaluator(DifferenceEvaluator evaluator);

    void useSelector(ElementSelector selector);
}
