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

import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

public class CharacterDataComparator extends NodeComparator<CharacterData> {

    public CharacterDataComparator(ComparisonPerformer compPerformer) {
        super(compPerformer);
    }

    @Override
    public ComparisonResult compare(NodeAndXpathCtx<CharacterData> control, NodeAndXpathCtx<CharacterData> test) {
        CharacterData controlNode = control.getNode();
        CharacterData testNode = test.getNode();

        ComparisonType comparisonType = ComparisonType.TEXT_VALUE;
        if (controlNode.getNodeType() == testNode.getNodeType()) {
            switch (controlNode.getNodeType()) {
                case Node.CDATA_SECTION_NODE:
                    comparisonType = ComparisonType.CDATA_VALUE;
                    break;
                case Node.COMMENT_NODE:
                    comparisonType = ComparisonType.COMMENT_VALUE;
                    break;
                case Node.TEXT_NODE:
                default:
                    comparisonType = ComparisonType.TEXT_VALUE;
                    break;
            }
        }

        return compPerformer.performComparison(
                new Comparison(comparisonType,
                        control, controlNode.getData(),
                        test, testNode.getData()));
    }

}
