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

    protected ComparisonResult compareNamespaces(NodeAndXpathCtx<Node> control, NodeAndXpathCtx<Node> test) {
        Node controlNode = control.getNode();
        Node testNode = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.NAMESPACE_URI,
                        control, controlNode.getNamespaceURI(),
                        test, testNode.getNamespaceURI()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.NAMESPACE_PREFIX,
                        control, controlNode.getPrefix(),
                        test, testNode.getPrefix()));
        return lastResult;
    }

    protected ComparisonResult compareAttributes(NodeAndXpathCtx<Attr> control, NodeAndXpathCtx<Attr> test) {
        Attr controlAttr = control.getNode();
        Attr testAttr = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED,
                        control, controlAttr.getSpecified(),
                        test, testAttr.getSpecified()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(new Comparison(ComparisonType.ATTR_VALUE,
                control, controlAttr.getValue(),
                test, testAttr.getValue()));

        return lastResult;
    }

    protected ComparisonResult compareDoctypes(
            NodeAndXpathCtx<DocumentType> control, NodeAndXpathCtx<DocumentType> test) {
        DocumentType controlNode = control.getNode();
        DocumentType testNode = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.DOCTYPE_NAME,
                        control, controlNode.getName(),
                        test, testNode.getName()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.DOCTYPE_PUBLIC_ID,
                        control, controlNode.getPublicId(),
                        test, testNode.getPublicId()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.DOCTYPE_SYSTEM_ID,
                        controlNode, null, controlNode.getSystemId(),
                        testNode, null, testNode.getSystemId()));

        return lastResult;
    }

    protected abstract class ComparisonStrategy<U extends Node> {
        private final NodeAndXpathCtx<U> control;
        private final NodeAndXpathCtx<U> test;

        public ComparisonStrategy(NodeAndXpathCtx<U> control, NodeAndXpathCtx<U> test) {
            this.control = control;
            this.test = test;
        }

        public NodeAndXpathCtx<U> getControl() {
            return control;
        }

        public NodeAndXpathCtx<U> getTest() {
            return test;
        }

        public abstract ComparisonResult performComparison();
    }
}
