package net.sf.xmlunit.diff;

public abstract class ObservableDifferenceEngine implements DifferenceEngine {
	private DifferenceEvaluator diffEvaluator = DifferenceEvaluators.Default;
	private final ComparisonListenerSupport listeners = new ComparisonListenerSupport();

	protected ComparisonListenerSupport getListeners() {
		return listeners;
	}

	@Override
	public void addComparisonListener(ComparisonListener l) {
		if (l == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		listeners.addComparisonListener(l);
	}

	@Override
	public void addMatchListener(ComparisonListener l) {
		if (l == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		listeners.addMatchListener(l);
	}

	@Override
	public void addDifferenceListener(ComparisonListener l) {
		if (l == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		listeners.addDifferenceListener(l);
	}

	@Override
	public void setDifferenceEvaluator(DifferenceEvaluator evaluator) {
		if (evaluator == null) {
			throw new IllegalArgumentException("difference evaluator must" + " not be null");
		}
		diffEvaluator = evaluator;
	}

	protected DifferenceEvaluator getDifferenceEvaluator() {
		return diffEvaluator;
	}

}
