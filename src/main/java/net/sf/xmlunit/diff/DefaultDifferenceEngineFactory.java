package net.sf.xmlunit.diff;


public class DefaultDifferenceEngineFactory implements DifferenceEngineFactory {

    @Override
    public DifferenceEngine newEngine() {
        return new DOMDifferenceEngine();
    }

}
