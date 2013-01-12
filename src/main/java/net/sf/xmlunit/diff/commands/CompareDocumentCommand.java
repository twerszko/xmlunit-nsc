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
import net.sf.xmlunit.diff.internal.ComparisonPerformer;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Compares document node, doctype and XML declaration properties
 */
public class CompareDocumentCommand extends ComparisonCommandBase<Document> {

	public CompareDocumentCommand(ComparisonPerformer compPerformer,
	        NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
		super(compPerformer, control, test);
	}

	@Override
	public Queue<Comparison> provideComparisons() {
		Queue<Comparison> comparisons = new LinkedList<Comparison>();
		comparisons.addAll(provideHasDocTypeComparisons());
		comparisons.addAll(new CompareDoctypeCommand(compPerformer,
		        NodeAndXpathCtx.from(getControl().getNode().getDoctype(), getControl().getXpathCtx()),
		        NodeAndXpathCtx.from(getTest().getNode().getDoctype(), getTest().getXpathCtx()))
		        .provideComparisons());
		comparisons.addAll(provideXmlDeclComparisons());
		return comparisons;
	}

	private Queue<Comparison> provideHasDocTypeComparisons() {
		DocumentType controlDt = getControl().getNode().getDoctype();
		DocumentType testDt = getTest().getNode().getDoctype();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(new Comparison(
		        ComparisonType.HAS_DOCTYPE_DECLARATION,
		        getControl(), controlDt != null,
		        getTest(), testDt != null));

		return comparisons;
	}

	private Queue<Comparison> provideXmlDeclComparisons() {
		final Document controlDoc = getControl().getNode();
		final Document testDoc = getTest().getNode();

		Queue<Comparison> comparisons = new LinkedList<Comparison>();

		comparisons.add(new Comparison(
		        ComparisonType.XML_VERSION,
		        getControl(), controlDoc.getXmlVersion(),
		        getTest(), testDoc.getXmlVersion()));

		comparisons.add(new Comparison(
		        ComparisonType.XML_STANDALONE,
		        getControl(), controlDoc.getXmlStandalone(),
		        getTest(), testDoc.getXmlStandalone()));

		comparisons.add(new Comparison(
		        ComparisonType.XML_ENCODING,
		        getControl(), controlDoc.getXmlEncoding(),
		        getTest(), testDoc.getXmlEncoding()));

		return comparisons;
	}

}
