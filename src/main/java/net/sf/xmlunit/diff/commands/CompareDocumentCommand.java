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
package net.sf.xmlunit.diff.commands;

import java.util.LinkedList;
import java.util.Queue;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.XPathContext;
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Compares document node, doctype and XML declaration properties
 */
public class CompareDocumentCommand extends ComparisonCommandBase<Document> {

    public CompareDocumentCommand(ComparisonPerformer compPerformer,
            NodeAndXpath<Document> control, NodeAndXpath<Document> test) {
        super(compPerformer, control, test);
    }

    @Override
    public Queue<Comparison> provideComparisons() {
        Document controlNode = getControl().getNode();
        Document testNode = getTest().getNode();

        XPathContext controlContext = getControl().getXpathCtx();
        XPathContext testContext = getTest().getXpathCtx();

        Queue<Comparison> comparisons = new LinkedList<Comparison>();

        comparisons.addAll(provideHasDocTypeComparisons());
        comparisons.addAll(new CompareDoctypeCommand(compPerformer,
                NodeAndXpath.from(controlNode.getDoctype(), controlContext),
                NodeAndXpath.from(testNode.getDoctype(), testContext))
                .provideComparisons());
        comparisons.addAll(provideXmlDeclComparisons());
        return comparisons;
    }

    private Queue<Comparison> provideHasDocTypeComparisons() {
        DocumentType controlDt = getControl().getNode().getDoctype();
        DocumentType testDt = getTest().getNode().getDoctype();

        Queue<Comparison> comparisons = new LinkedList<Comparison>();

        comparisons.add(
                Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
                        .between(getControl(), controlDt != null)
                        .and(getTest(), testDt != null));

        return comparisons;
    }

    private Queue<Comparison> provideXmlDeclComparisons() {
        Document controlDoc = getControl().getNode();
        Document testDoc = getTest().getNode();

        Queue<Comparison> comparisons = new LinkedList<Comparison>();

        comparisons.add(
                Comparison.ofType(ComparisonType.XML_VERSION)
                        .between(getControl(), controlDoc.getXmlVersion())
                        .and(getTest(), testDoc.getXmlVersion()));

        comparisons.add(
                Comparison.ofType(ComparisonType.XML_STANDALONE)
                        .between(getControl(), controlDoc.getXmlStandalone())
                        .and(getTest(), testDoc.getXmlStandalone()));

        comparisons.add(
                Comparison.ofType(ComparisonType.XML_ENCODING)
                        .between(getControl(), controlDoc.getXmlEncoding())
                        .and(getTest(), testDoc.getXmlEncoding()));

        return comparisons;
    }
}
