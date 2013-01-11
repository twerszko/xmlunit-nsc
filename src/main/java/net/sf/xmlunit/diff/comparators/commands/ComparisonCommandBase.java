/*
 This file is licensed to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package net.sf.xmlunit.diff.comparators.commands;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.concurrent.NotThreadSafe;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Node;

@NotThreadSafe
public abstract class ComparisonCommandBase<U extends Node> implements ComparisonCommand {
	protected final ComparisonPerformer compPerformer;

	private final NodeAndXpathCtx<U> control;
	private final NodeAndXpathCtx<U> test;

	private boolean interrupted = false;

	public ComparisonCommandBase(ComparisonPerformer compPerformer, NodeAndXpathCtx<U> control, NodeAndXpathCtx<U> test) {
		this.compPerformer = compPerformer;
		this.control = control;
		this.test = test;
	}

	public NodeAndXpathCtx<U> getControl() {
		return control;
	}

	public NodeAndXpathCtx<U> getTest() {
		return test;
	}

	public abstract Queue<Comparison> provideComparisons();

	@Override
	public final void execute() {
		interrupted = false;
		Queue<Comparison> comparisons = provideComparisons();
		if (comparisons == null) {
			comparisons = new LinkedList<Comparison>();
		}
		for (Comparison comparison : comparisons) {
			ComparisonResult result = compPerformer.performComparison(comparison);
			if (result == ComparisonResult.CRITICAL) {
				interrupted = true;
				return;
			}
		}
	}

	@Override
	public boolean isInterrupted() {
		return interrupted;
	}
}
