package net.sf.xmlunit.diff;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.junit.Before;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class DOMDifferenceEngineTestAbstract {
    protected ListingDifferenceEvaluator evaluator;
    protected DOMDifferenceEngine engine;
    protected Document doc;

    protected abstract DOMDifferenceEngine createEngine();

    @Before
    public void init() throws Exception {
        evaluator = new ListingDifferenceEvaluator();
        engine = createEngine();
        engine.setDifferenceEvaluator(evaluator);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    protected List<Comparison> findDifferences(Node control, Node test) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine();
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);

        engine.compareNodes(
                NodeAndXpath.from(control),
                NodeAndXpath.from(test));

        return evaluator.getDifferences();
    }

    protected List<Comparison> findDifferencesWithMatcher(Node control, Node test, NodeMatcher nodeMatcher) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine();
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);
        engine.setNodeMatcher(nodeMatcher);

        engine.compareNodes(
                NodeAndXpath.from(control),
                NodeAndXpath.from(test));

        return evaluator.getDifferences();
    }
}
