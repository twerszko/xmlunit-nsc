package net.sf.xmlunit.diff.strategies;

import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Node;

public interface ComparisonStrategy<U extends Node> {

	boolean isInterrupted();

	void compare(NodeAndXpath<U> control, NodeAndXpath<U> test);
}
