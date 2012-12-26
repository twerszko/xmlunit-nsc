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
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Attr;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

public abstract class NodeComparator<T extends Node> {
    protected final ComparisonPerformer compPerformer;

    public NodeComparator(ComparisonPerformer compPerformer) {
        this.compPerformer = compPerformer;
    }

    public abstract ComparisonResult compare(NodeAndXpathCtx<T> control, NodeAndXpathCtx<T> test);

    @Nullable
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

    protected class CompareAttributeOperation extends AbstractComparisonOperation<Attr> {

        public CompareAttributeOperation(NodeAndXpathCtx<Attr> control, NodeAndXpathCtx<Attr> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult executeComparison() {
            final Attr controlAttr = getControl().getNode();
            final Attr testAttr = getTest().getNode();

            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED,
                                    getControl(), controlAttr.getSpecified(),
                                    getTest(), testAttr.getSpecified()));
                }
            });
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.ATTR_VALUE,
                                    getControl(), controlAttr.getValue(),
                                    getTest(), testAttr.getValue()));
                }
            });

            return execute(operations);
        }
    }

    protected class CompareDoctypeOperation extends AbstractComparisonOperation<DocumentType> {

        public CompareDoctypeOperation(
                NodeAndXpathCtx<DocumentType> control,
                NodeAndXpathCtx<DocumentType> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult executeComparison() {
            final DocumentType controlDt = getControl().getNode();
            final DocumentType testDt = getTest().getNode();

            if (controlDt == null || testDt == null) {
                return null;
            }

            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.DOCTYPE_NAME,
                                    getControl(), controlDt.getName(),
                                    getTest(), testDt.getName()));
                }
            });
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.DOCTYPE_PUBLIC_ID,
                                    getControl(), controlDt.getPublicId(),
                                    getTest(), testDt.getPublicId()));
                }
            });
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.DOCTYPE_SYSTEM_ID,
                                    getControl(), controlDt.getSystemId(),
                                    getTest(), testDt.getSystemId()));
                }
            });

            return execute(operations);
        }
    }
}
