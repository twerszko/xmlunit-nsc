/*
 ******************************************************************
Copyright (c) 2001-2007, Jeff Martin, Tim Bacon
All rights reserved.

Modified 2012 by Tomasz Werszko

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
 * Neither the name of the xmlunit.sourceforge.net nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************
 */

package org.custommonkey.xmlunit;

import net.sf.xmlunit.diff.Comparison.Detail;

import org.custommonkey.xmlunit.diff.DifferenceType;

/**
 * Value object that describes a difference between DOM Nodes using one of the
 * DifferenceType values and a NodeDetail instance. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 * 
 * @see NodeDetail
 */
public class Difference {
    /** Simple unique identifier */
    private final DifferenceType type;
    /** TRUE if the difference represents a similarity, FALSE otherwise */
    private boolean recoverable;

    private Detail controlNodeDetail = null;
    private Detail testNodeDetail = null;

    /**
     * Constructor for non-similar Difference instances
     * 
     * @param type
     */
    protected Difference(DifferenceType type) {
        this.type = type;
        this.recoverable = type.isRecoverable();
    }

    /**
     * Copy constructor using prototype Difference and encountered NodeDetails
     */
    protected Difference(
            Difference prototype,
            Detail controlNodeDetail,
            Detail testNodeDetail) {

        this(prototype.getType());
        this.setRecoverable(prototype.isRecoverable());
        this.controlNodeDetail = controlNodeDetail;
        this.testNodeDetail = testNodeDetail;
    }

    /**
     * @return the difference type
     */
    public DifferenceType getType() {
        return this.type;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return type.getDescription();
    }

    /**
     * @return TRUE if the difference represents a similarity, FALSE otherwise
     */
    public boolean isRecoverable() {
        return recoverable;
    }

    /**
     * Allow the recoverable field value to be overridden. Used when an override
     * DifferenceListener is used in conjunction with a DetailedDiff.
     */
    protected void setRecoverable(boolean overrideValue) {
        recoverable = overrideValue;
    }

    /**
     * @return the NodeDetail from the piece of XML used as the control at the
     *         Node where this difference was encountered
     */
    public Detail getControlNodeDetail() {
        return controlNodeDetail;
    }

    /**
     * @return the NodeDetail from the piece of XML used as the test at the Node
     *         where this difference was encountered
     */
    public Detail getTestNodeDetail() {
        return testNodeDetail;
    }

    /**
     * Now that Differences can be constructed from prototypes we need to be
     * able to compare them to those in DifferenceConstants
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other instanceof Difference) {
            Difference otherDifference = (Difference) other;
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

    /**
     * @return a basic representation of the object state and identity and if
     *         <code>NodeDetail</code> instances are populated append their
     *         details also
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (controlNodeDetail == null || testNodeDetail == null) {
            appendBasicRepresentation(buf);
        } else {
            appendDetailedRepresentation(buf);
        }
        return buf.toString();
    }

    private void appendBasicRepresentation(StringBuffer buf) {
        buf.append("Difference (#").append(type).
                append(") ").append(type.getDescription());
    }

    private void appendDetailedRepresentation(StringBuffer buf) {
        buf.append("Expected ").append(getDescription())
                .append(" '").append(controlNodeDetail.getValue())
                .append("' but was '").append(testNodeDetail.getValue())
                .append("' - comparing ");
        NodeDescriptor.appendDetail(buf, controlNodeDetail);
        buf.append(" to ");
        NodeDescriptor.appendDetail(buf, testNodeDetail);
    }

}
