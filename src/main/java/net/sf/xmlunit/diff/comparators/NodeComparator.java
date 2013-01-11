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
package net.sf.xmlunit.diff.comparators;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.Nullable;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.comparators.commands.ComparisonCommand;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Predicate;

import org.w3c.dom.Node;

public abstract class NodeComparator<T extends Node> {
	protected ComparisonPerformer compPerformer = new ComparisonPerformer();

	public NodeComparator() {
	}

	public NodeComparator(ComparisonPerformer compPerformer) {
		this.compPerformer = compPerformer;
	}

	public abstract ComparisonResult compare(NodeAndXpathCtx<T> control, NodeAndXpathCtx<T> test);

	@Nullable
	// TODO remove
	protected final ComparisonResult execute(Queue<ComparisonOperation> operations) {
		ComparisonResult result = ComparisonResult.EQUAL;
		for (ComparisonOperation operation : operations) {
			result = operation.executeComparison();
			if (result == ComparisonResult.CRITICAL) {
				return result;
			}
		}

		return result;
	}

	@Nullable
	// TODO modify
	protected final ComparisonResult executeCommands(Queue<ComparisonCommand> commands) {
		for (ComparisonCommand command : commands) {
			command.execute();
			if (command.isInterrupted()) {
				return ComparisonResult.CRITICAL;
			}
		}

		return ComparisonResult.EQUAL;
	}

	protected interface ComparisonOperation {
		public abstract ComparisonResult executeComparison();
	}

	protected abstract class AbstractComparisonOperation<U extends Node> implements ComparisonOperation {
		private final NodeAndXpathCtx<U> control;
		private final NodeAndXpathCtx<U> test;

		public AbstractComparisonOperation(NodeAndXpathCtx<U> control, NodeAndXpathCtx<U> test) {
			this.control = control;
			this.test = test;
		}

		public NodeAndXpathCtx<U> getControl() {
			return control;
		}

		public NodeAndXpathCtx<U> getTest() {
			return test;
		}
	}

	// TODO remove
	protected class CompareNamespaceOperation extends AbstractComparisonOperation<Node> {

		public CompareNamespaceOperation(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
			super(control, test);
		}

		@Override
		public ComparisonResult executeComparison() {
			final Node controlNode = getControl().getNode();
			final Node testNode = getTest().getNode();

			Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
			operations.add(new ComparisonOperation() {
				@Override
				public ComparisonResult executeComparison() {
					return compPerformer.performComparison(
					        new Comparison(ComparisonType.NAMESPACE_URI,
					                getControl(), controlNode.getNamespaceURI(),
					                getTest(), testNode.getNamespaceURI()));
				}
			});
			operations.add(new ComparisonOperation() {
				@Override
				public ComparisonResult executeComparison() {
					return compPerformer.performComparison(
					        new Comparison(ComparisonType.NAMESPACE_PREFIX,
					                getControl(), controlNode.getPrefix(),
					                getTest(), testNode.getPrefix()));
				}
			});
			return execute(operations);
		}
	}

	// TODO duplication in CompareNodeCommand.
	protected static final Predicate<Node> INTERESTING_NODES =
	        new Predicate<Node>() {
		        @Override
		        public boolean matches(Node n) {
			        return n.getNodeType() != Node.DOCUMENT_TYPE_NODE;
		        }
	        };
}
