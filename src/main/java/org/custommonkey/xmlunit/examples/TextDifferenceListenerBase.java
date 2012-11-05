/*
 ******************************************************************
Copyright (c) 2008, Jeff Martin, Tim Bacon
All rights reserved.

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
package org.custommonkey.xmlunit.examples;

import net.sf.xmlunit.diff.ComparisonResult;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * Base class that delegates all differences to another DifferenceListener.
 * 
 * <p>
 * Subclasses get a chance to hook into special methods that will be invoked for
 * differences in textual values of attributes, CDATA sections, Text or comment
 * nodes.
 * </p>
 */
public abstract class TextDifferenceListenerBase
        implements DifferenceListener {

    private final DifferenceListener delegateTo;

    protected TextDifferenceListenerBase(DifferenceListener delegateTo) {
        this.delegateTo = delegateTo;
    }

    /**
     * Delegates to the nested DifferenceListener unless the Difference is of
     * type {@link DifferenceConstants#ATTR_VALUE_ID ATTR_VALUE_ID},
     * {@link DifferenceConstants#CDATA_VALUE_ID CDATA_VALUE_ID},
     * {@link DifferenceConstants#COMMENT_VALUE_ID COMMENT_VALUE_ID} or
     * {@link DifferenceConstants#TEXT_VALUE_ID TEXT_VALUE_ID} - for those
     * special differences {@link #attributeDifference attributeDifference},
     * {@link #cdataDifference cdataDifference}, {@link #commentDifference
     * commentDifference} or {@link #textDifference textDifference} are invoked
     * respectively.
     */
    public ComparisonResult differenceFound(Difference difference, ComparisonResult outcome) {
        switch (difference.getType()) {
        case ATTR_VALUE:
            return attributeDifference(difference, outcome);
        case CDATA_VALUE:
            return cdataDifference(difference, outcome);
        case COMMENT_VALUE:
            return commentDifference(difference, outcome);
        case TEXT_VALUE:
            return textDifference(difference, outcome);
        default:
            return delegateTo.differenceFound(difference, outcome);
        }
    }

    /**
     * Delegates to {@link #textualDifference textualDifference}.
     */
    protected ComparisonResult attributeDifference(Difference d, ComparisonResult outcome) {
        return textualDifference(d, outcome);
    }

    /**
     * Delegates to {@link #textualDifference textualDifference}.
     */
    protected ComparisonResult cdataDifference(Difference d, ComparisonResult outcome) {
        return textualDifference(d, outcome);
    }

    /**
     * Delegates to {@link #textualDifference textualDifference}.
     */
    protected ComparisonResult commentDifference(Difference d, ComparisonResult outcome) {
        return textualDifference(d, outcome);
    }

    /**
     * Delegates to {@link #textualDifference textualDifference}.
     */
    protected ComparisonResult textDifference(Difference d, ComparisonResult outcome) {
        return textualDifference(d, outcome);
    }

    /**
     * Delegates to the nested DifferenceListener.
     */
    protected ComparisonResult textualDifference(Difference d, ComparisonResult outcome) {
        return delegateTo.differenceFound(d, outcome);
    }

    public void skippedComparison(Node control, Node test) {
        delegateTo.skippedComparison(control, test);
    }

}
