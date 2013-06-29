/*
 ******************************************************************
Copyright (c) 2001-2008,2010 Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit.diff;

import javax.annotation.Nullable;
import javax.xml.transform.Source;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.DifferenceEngine;
import net.sf.xmlunit.diff.DifferenceEngineFactory;

import org.custommonkey.xmlunit.XmlUnitProperties;

/**
 * Compares and describes any difference between XML documents. Two documents
 * are either: <br />
 * <ul>
 * <li><i>identical</i>: the content and sequence of the nodes in the documents
 * are exactly the same</li>
 * <li><i>similar</i>: the content of the nodes in the documents are the same,
 * but minor differences exist e.g. sequencing of sibling elements, values of
 * namespace prefixes, use of implied attribute values</li>
 * <li><i>different</i>: the contents of the documents are fundamentally
 * different</li>
 * </ul>
 * <br />
 * The difference between compared documents is contained in a message buffer
 * held in this class, accessible either through the <code>appendMessage</code>
 * or <code>toString</code> methods. NB: When comparing documents, the
 * comparison is halted as soon as the status (identical / similar / different)
 * is known with certainty. For a list of all differences between the documents
 * an instance of {@link DetailedDiff the DetailedDiff class} can be used
 * instead. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 */
public class Diff {
    private final Source ctrlSource;
    private final Source testSource;
    private DifferenceEngineFactory engineFactory;

    private boolean similar = true;
    private boolean identical = true;
    private boolean compared = false;

    Diff(DiffBuilder builder) {
        this.ctrlSource = builder.controlSource;
        this.testSource = builder.testSource;
        this.engineFactory = builder.engineFactory;
    }

    /**
     * Construct a Diff from a prototypical instance. Used by extension
     * subclasses
     * 
     * @param prototype
     *            a prototypical instance
     */
    // TODO this can be removed
    protected Diff(Diff prototype) {
        // TODO clone?
        this.ctrlSource = prototype.ctrlSource;
        this.testSource = prototype.testSource;
        this.engineFactory = prototype.engineFactory;
    }

    public void setEngineFactory(DifferenceEngineFactory factory) {
        this.engineFactory = factory;
    }

    /**
     * Top of the recursive comparison execution tree
     */
    protected final void compare() {
        if (compared) {
            return;
        }

        DifferenceEngine engine = engineFactory.newEngine();
        engine.addComparisonListener(createControllingListener(engine));

        engine.compare(ctrlSource, testSource);
        compared = true;
    }

    protected ComparisonListener createControllingListener(DifferenceEngine engine) {
        return new ControllingListener(engine);
    }

    /**
     * Return the result of a comparison. Two documents are considered to be
     * "similar" if they contain the same elements and attributes regardless of
     * order.
     */
    public boolean similar() {
        compare();
        return similar;
    }

    /**
     * Return the result of a comparison. Two documents are considered to be
     * "identical" if they contain the same elements and attributes in the same
     * order.
     */
    public boolean identical() {
        compare();
        return identical;
    }

    // TODO
    protected class ControllingListener implements ComparisonListener {
        private final DifferenceEngine engine;

        public ControllingListener(DifferenceEngine engine) {
            this.engine = engine;
        }

        @Override
        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
            setVardict(comparison, outcome);
            boolean critical = isCritical(comparison, outcome);
            if (critical) {
                stopComparison();
            }
        }

        protected void stopComparison() {
            engine.stop();
        }
    }

    private boolean isCritical(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.CRITICAL) {
            return true;
        }
        boolean isNotRecoverable = !comparison.getType().isRecoverable();
        if (outcome == ComparisonResult.DIFFERENT && isNotRecoverable) {
            return true;
        }
        return false;
    }

    private void setVardict(Comparison comparison, ComparisonResult outcome) {
        boolean isRecoverable = comparison.getType().isRecoverable();
        switch (outcome) {
        case SIMILAR:
            identical = false;
            break;
        case DIFFERENT:
            identical = false;
            if (!isRecoverable) {
                similar = false;
            }
            break;
        case CRITICAL:
            identical = false;
            similar = false;
            break;
        default:
            break;
        }
    }

    public static DiffBuilder newDiff(@Nullable XmlUnitProperties properties) {
        return new DiffBuilder(properties);
    }
}
