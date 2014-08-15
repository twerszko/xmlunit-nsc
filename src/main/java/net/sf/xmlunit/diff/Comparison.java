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

import net.sf.xmlunit.diff.internal.NodeAndXpath;

import org.custommonkey.xmlunit.diff.DifferenceFormater;
import org.w3c.dom.Node;

/**
 * Details of a single comparison XMLUnit has performed.
 */
public class Comparison {

    private boolean recoverable;
    private final Detail control, test;
    private final ComparisonType type;

    public Comparison(ComparisonType type, Detail controlDetail, Detail testDetail) {
        this.type = type;
        control = controlDetail;
        test = testDetail;
        recoverable = type.isRecoverable();
    }

    public Comparison(ComparisonBuilder builder) {
        this.type = builder.type;
        control = new Detail(builder.controlTarget, builder.controlValue);
        test = new Detail(builder.testTarget, builder.testValue);
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

        public Detail(NodeAndXpath<? extends Node> node, Object value) {
            if (node == null) {
                this.target = null;
                this.xpath = null;
            } else {
                this.target = node.getNode();
                this.xpath = node.getXpath();
            }
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

    public static ComparisonBuilder ofType(ComparisonType type) {
        return new ComparisonBuilder(type);
    }

    public final static class ComparisonBuilder {
        private final ComparisonType type;
        private NodeAndXpath<? extends Node> controlTarget;
        private NodeAndXpath<? extends Node> testTarget;

        private Object controlValue;
        private Object testValue;

        public ComparisonBuilder(ComparisonType type) {
            this.type = type;
        }

        public TestNodeComparisonBuilder between(NodeAndXpath<? extends Node> controlTarget, Object controlValue) {
            this.controlTarget = controlTarget;
            this.controlValue = controlValue;
            return new TestNodeComparisonBuilder();
        }

        public final class TestNodeComparisonBuilder {
            public Comparison and(NodeAndXpath<? extends Node> testTarget, Object testValue) {
                ComparisonBuilder.this.testTarget = testTarget;
                ComparisonBuilder.this.testValue = testValue;
                return new Comparison(ComparisonBuilder.this);
            }
        }

    }
}
