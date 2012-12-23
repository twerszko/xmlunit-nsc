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

import org.w3c.dom.Document;

public class XmlHeaderComparator extends NodeComparator<Document> {

    public XmlHeaderComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    @Override
    public ComparisonResult compare(NodeAndXpathCtx<Document> control, NodeAndXpathCtx<Document> test) {
        Document controlDoc = control.getNode();
        Document testDoc = test.getNode();

        ComparisonResult lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.XML_VERSION,
                        control, controlDoc.getXmlVersion(),
                        test, testDoc.getXmlVersion()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.XML_STANDALONE,
                        control, controlDoc.getXmlStandalone(),
                        test, testDoc.getXmlStandalone()));
        if (lastResult == ComparisonResult.CRITICAL) {
            return lastResult;
        }

        lastResult = compPerformer.performComparison(
                new Comparison(ComparisonType.XML_ENCODING,
                        control, controlDoc.getXmlEncoding(),
                        test, testDoc.getXmlEncoding()));

        return lastResult;
    }

}
