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
package net.sf.xmlunit.diff.strategies;

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
public class CompareDocumentStrategy extends ComparisonStrategyBase<Document> {

	public CompareDocumentStrategy(ComparisonPerformer compPerformer) {
		super(compPerformer);
	}

	@Override
	public Queue<Comparison> provideComparisons(NodeAndXpath<Document> control, NodeAndXpath<Document> test) {
		Document controlNode = control.getNode();
		Document testNode = test.getNode();

		XPathContext controlContext = control.getXpathCtx();
		XPathContext testContext = test.getXpathCtx();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.addAll(provideHasDocTypeComparisons(control, test));
		comparisons.addAll(new CompareDoctypeStrategy(performer)
		        .provideComparisons(
		                NodeAndXpath.from(controlNode.getDoctype(), controlContext),
		                NodeAndXpath.from(testNode.getDoctype(), testContext)));
		comparisons.addAll(provideXmlDeclComparisons(control, test));
		return comparisons;
	}

	private Queue<Comparison> provideHasDocTypeComparisons(NodeAndXpath<Document> control, NodeAndXpath<Document> test) {
		DocumentType controlDt = control.getNode().getDoctype();
		DocumentType testDt = test.getNode().getDoctype();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(
		        Comparison.ofType(ComparisonType.HAS_DOCTYPE_DECLARATION)
		                .between(control, controlDt != null)
		                .and(test, testDt != null));

		return comparisons;
	}

	private Queue<Comparison> provideXmlDeclComparisons(NodeAndXpath<Document> control, NodeAndXpath<Document> test) {
		Document controlDoc = control.getNode();
		Document testDoc = test.getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(
		        Comparison.ofType(ComparisonType.XML_VERSION)
		                .between(control, controlDoc.getXmlVersion())
		                .and(test, testDoc.getXmlVersion()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.XML_STANDALONE)
		                .between(control, controlDoc.getXmlStandalone())
		                .and(test, testDoc.getXmlStandalone()));

		comparisons.add(
		        Comparison.ofType(ComparisonType.XML_ENCODING)
		                .between(control, controlDoc.getXmlEncoding())
		                .and(test, testDoc.getXmlEncoding()));

		return comparisons;
	}
}
