/*
 ******************************************************************
Copyright (c) 2001, Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DifferenceEvaluator;

import org.custommonkey.xmlunit.diff.Diff;

/**
 * Class to use when performing a Diff that only compares the structure of 2
 * pieces of XML, i.e. where the values of text and attribute nodes should be
 * ignored.
 * 
 * @see Diff#overrideDifferenceListener
 */
public class IgnoreTextAndAttributeValuesDifferenceEvaluator implements DifferenceEvaluator {

    private static final ComparisonType[] IGNORED_TYPES = new ComparisonType[] {
            ComparisonType.ATTR_VALUE,
            ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED,
            ComparisonType.TEXT_VALUE
    };

    private boolean isIgnoredDifference(Comparison comparison) {
        ComparisonType differenceType = comparison.getType();
        for (ComparisonType ignoredType : IGNORED_TYPES) {
            if (differenceType == ignoredType) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR to ignore differences in
     *         values of TEXT or ATTRIBUTE nodes, and RETURN_ACCEPT_DIFFERENCE
     *         to accept all other differences.
     * @see DifferenceListener#differenceFound(Difference)
     */
    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) {
            return outcome;
        }
        if (isIgnoredDifference(comparison)) {
            return ComparisonResult.SIMILAR;
        } else {
            return ComparisonResult.DIFFERENT;
        }
    }

}
