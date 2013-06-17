package net.sf.xmlunit.diff;

import net.sf.xmlunit.util.Preconditions;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.w3c.dom.Node;

public class DefaultDifferenceEngineFactory implements DifferenceEngineFactory {
    private final XmlUnitProperties properties;
    private DifferenceEvaluator evaluator;
    private ElementSelector selector = ElementSelectors.byName;

    // TODO
    public DefaultDifferenceEngineFactory(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    @Override
    public DifferenceEngine newEngine() {
        DOMDifferenceEngine engine = new DOMDifferenceEngine();
        applyProperties(engine);
        applyEvaluator(engine);
        applyComparisonFilter(engine);
        applyNodeMatcher(engine);
        return engine;
    }

    @Override
    public void useEvaluator(DifferenceEvaluator evaluator) {
        Preconditions.checkArgument(evaluator != null, "Evaluator cannot be null");
        this.evaluator = evaluator;
    }

    @Override
    public void useSelector(ElementSelector selector) {
        Preconditions.checkArgument(selector != null, "Element selector cannot be null");
        this.selector = selector;
    }

    protected void applyEvaluator(DOMDifferenceEngine engine) {
        if (evaluator != null) {
            engine.setEvaluator(evaluator);
        }
    }

    protected void applyProperties(DifferenceEngine engine) {
        engine.setIgnoreAttributeOrder(properties.getIgnoreAttributeOrder());
    }

    protected void applyComparisonFilter(DifferenceEngine engine) {
        ComparisonFilter filter = new DefaultComparisonFilter();
        engine.setFilter(filter);
    }

    protected void applyNodeMatcher(DifferenceEngine engine) {
        NodeMatcher matcher = createNodeMatcher(selector);
        engine.setNodeMatcher(matcher);
    }

    protected NodeMatcher createNodeMatcher(ElementSelector selector) {
        NodeMatcher nodeMatcher = new DefaultNodeMatcher(selector);
        if (properties.getCompareUnmatched()) {
            nodeMatcher = new CompareUnmatchedNodeMatcher(nodeMatcher);
        }
        return nodeMatcher;
    }

    private final class DefaultComparisonFilter implements ComparisonFilter {
        @Override
        public boolean ignore(Comparison comparison) {
            switch (comparison.getType()) {
                case XML_ENCODING:
                case XML_STANDALONE:
                case XML_VERSION:
                    return true;
                default:
                    break;
            }
            if (comparesChildNodeListLengthOfDocNode(comparison)) {
                return true;
            }
            if (comparesNonElementChildrenOfDocNode(comparison)) {
                return true;
            }
            return false;
        }

        private boolean comparesChildNodeListLengthOfDocNode(Comparison comparison) {
            ComparisonType type = comparison.getType();
            Node target = comparison.getControlDetails().getTarget();
            if (type == ComparisonType.CHILD_NODELIST_LENGTH && isDocument(target)) {
                return true;
            }
            return false;
        }

        private boolean isDocument(Node node) {
            return node != null && node.getNodeType() == Node.DOCUMENT_NODE;
        }

        private boolean comparesNonElementChildrenOfDocNode(Comparison comparison) {
            ComparisonType type = comparison.getType();
            Node ctrlNode = comparison.getControlDetails().getTarget();
            Node testNode = comparison.getTestDetails().getTarget();

            if (type == ComparisonType.CHILD_LOOKUP &&
                    (isNonElementChildOfDocNode(ctrlNode) || isNonElementChildOfDocNode(testNode))) {
                return true;
            }
            return false;
        }

        private boolean isNonElementChildOfDocNode(Node node) {
            return node != null && isNotElement(node) && isChildOfDocNode(node);
        }

        private boolean isChildOfDocNode(Node node) {
            Node parent = node.getParentNode();
            return parent != null && parent.getNodeType() == Node.DOCUMENT_NODE;
        }

        private boolean isNotElement(Node node) {
            return node.getNodeType() != Node.ELEMENT_NODE;
        }
    }
}
