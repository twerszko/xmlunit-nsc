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

import org.custommonkey.xmlunit.diff.DifferenceFormater;
import org.w3c.dom.Node;

/**
 * Details of a single comparison XMLUnit has performed.
 */
public class Comparison {

	private boolean recoverable;
	private final Detail control, test;
	private final ComparisonType type;

	public Comparison(ComparisonType t,
	        Node controlTarget, String controlXPath, Object controlValue,
	        Node testTarget, String testXPath, Object testValue) {
		type = t;
		control = new Detail(controlTarget, controlXPath, controlValue);
		test = new Detail(testTarget, testXPath, testValue);
		recoverable = type.isRecoverable();
	}

	/**
	 * The details of a target (usually some representation of an XML Node) that
	 * took part in the comparison.
	 */
	public static class Detail {
		private final Node target;
		private final String xpath;
		private final Object value;

		public Detail(Node node, String xpath, Object value) {
			this.target = node;
			this.xpath = xpath;
			this.value = value;
		}

		/**
		 * The actual target.
		 */
		public Node getTarget() {
			return target;
		}

		/**
		 * XPath leading to the target.
		 */
		public String getXpath() {
			return xpath;
		}

		/**
		 * The value for comparison found at the current target.
		 */
		public Object getValue() {
			return value;
		}
	}

	/**
	 * The kind of comparison performed.
	 */
	public ComparisonType getType() {
		return type;
	}

	/**
	 * Details of the control target.
	 */
	public Detail getControlDetails() {
		return control;
	}

	/**
	 * Details of the test target.
	 */
	public Detail getTestDetails() {
		return test;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

	/**
	 * Now that Differences can be constructed from prototypes we need to be
	 * able to compare them to those in DifferenceConstants
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (other instanceof Comparison) {
			Comparison otherDifference = (Comparison) other;
			return type == otherDifference.getType();
		} else {
			return false;
		}
	}

	/**
	 * hashcode implementation to go with equals.
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public String toString() {
		// TODO: This shouldn be here
		return new DifferenceFormater(this).toString();
	}

}
