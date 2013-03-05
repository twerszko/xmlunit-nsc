package net.sf.xmlunit.diff.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;

public class Comparisons {
	private final Queue<Comparison> comparisons = new LinkedList<Comparison>();

	public void add(Comparison comparison) {
		comparisons.add(comparison);
	}

	public void addAll(Collection<Comparison> collection) {
		comparisons.addAll(collection);
	}

	public void addAll(Comparisons other) {
		comparisons.addAll(other.getAll());
	}

	/**
	 * Returns unmodifiable collection of all contained comparisons
	 * 
	 * @return
	 */
	public Collection<Comparison> getAll() {
		return Collections.unmodifiableCollection(comparisons);
	}
}
