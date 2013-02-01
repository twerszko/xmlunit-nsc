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

package net.sf.xmlunit.diff;

import javax.xml.transform.Source;

import net.sf.xmlunit.diff.internal.NodeAndXpath;
import net.sf.xmlunit.diff.strategies.DOMComparator;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;

/**
 * Difference engine based on DOM.
 */
public final class DOMDifferenceEngine extends AbstractDifferenceEngine {

	private final XmlUnitProperties properties;

	public DOMDifferenceEngine(XmlUnitProperties properties) {
		if (properties == null) {
			this.properties = new XmlUnitProperties();
		} else {
			this.properties = properties.clone();
		}
	}

	@Override
	public void compare(Source control, Source test) {
		if (control == null) {
			throw new IllegalArgumentException("control must not be null");
		}
		if (test == null) {
			throw new IllegalArgumentException("test must not be null");
		}
		try {
			compareNodes(
			        NodeAndXpath.from(Convert.toNode(control)),
			        NodeAndXpath.from(Convert.toNode(test)));
		} catch (Exception ex) {
			// TODO remove pokemon exception handling
			throw new XMLUnitRuntimeException("Caught exception during comparison", ex);
		}
	}

	@VisibleForTesting
	void compareNodes(NodeAndXpath<Node> control, NodeAndXpath<Node> test) {
		new DOMComparator(
		        getComparisonPerformer(), getNodeMatcher(), properties.getIgnoreAttributeOrder())
		        .compare(control, test);
	}
}
