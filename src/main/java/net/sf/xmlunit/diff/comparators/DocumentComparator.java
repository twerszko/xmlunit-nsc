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

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class DocumentComparator extends NodeComparator<Document> {

    public DocumentComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    /**
     * Compares document node, doctype and XML declaration properties
     */
    @Override
    public ComparisonResult compare(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
        Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
        operations.add(new HasDoctypeOperation(control, test));
        operations.add(new CompareDoctypeOperation(
                NodeAndXpathCtx.from(control.getNode().getDoctype(), control.getXpathCtx()),
                NodeAndXpathCtx.from(test.getNode().getDoctype(), test.getXpathCtx())));
        operations.add(new CompareXmlDeclarationOperation(control, test));
        return execute(operations);
    }

    private class HasDoctypeOperation extends AbstractComparisonOperation<Document> {

        public HasDoctypeOperation(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult executeComparison() {
            DocumentType controlDt = getControl().getNode().getDoctype();
            DocumentType testDt = getTest().getNode().getDoctype();

            return compPerformer.performComparison(
                    new Comparison(ComparisonType.HAS_DOCTYPE_DECLARATION,
                            getControl(), controlDt != null,
                            getTest(), testDt != null));
        }
    }

    private class CompareXmlDeclarationOperation extends AbstractComparisonOperation<Document> {

        public CompareXmlDeclarationOperation(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
            super(control, test);
        }

        @Override
        public ComparisonResult executeComparison() {
            final Document controlDoc = getControl().getNode();
            final Document testDoc = getTest().getNode();

            Queue<ComparisonOperation> operations = new LinkedList<ComparisonOperation>();
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.XML_VERSION,
                                    getControl(), controlDoc.getXmlVersion(),
                                    getTest(), testDoc.getXmlVersion()));
                }
            });
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.XML_STANDALONE,
                                    getControl(), controlDoc.getXmlStandalone(),
                                    getTest(), testDoc.getXmlStandalone()));
                }
            });
            operations.add(new ComparisonOperation() {
                @Override
                public ComparisonResult executeComparison() {
                    return compPerformer.performComparison(
                            new Comparison(ComparisonType.XML_ENCODING,
                                    getControl(), controlDoc.getXmlEncoding(),
                                    getTest(), testDoc.getXmlEncoding()));
                }
            });

            return execute(operations);
        }
    }
}
