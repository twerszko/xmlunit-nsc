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

import static org.custommonkey.xmlunit.diff.Diff.newDiff;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.DefaultDifferenceEngineFactory;
import net.sf.xmlunit.diff.DifferenceEngine;
import net.sf.xmlunit.diff.DifferenceEngineFactory;
import net.sf.xmlunit.diff.ListingDifferenceEvaluator;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;


public class DiffTest extends DiffTestAbstract {

    @Test
    public void should_stop_comparison_after_first_difference() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        ComparisonListener listener = mock(ComparisonListener.class);
        Diff diff = newDiff().betweenControlDocument(buildControlDocument())
                .andTestDocument(buildTestDocument()).build();
        diff.setEngineFactory(createEngineFactory(evaluator, listener));
        // when
        boolean identical = diff.identical();
        // then
        assertThat(evaluator.getDifferences()).hasSize(1);
        assertThat(identical).isFalse();
        verify(listener, times(1)).comparisonPerformed(any(Comparison.class), any(ComparisonResult.class));
    }

    private DifferenceEngineFactory createEngineFactory(ListingDifferenceEvaluator evaluator,
            final ComparisonListener listener) {
        DifferenceEngineFactory engineFactory = new DefaultDifferenceEngineFactory(properties) {
            @Override
            public DifferenceEngine newEngine() {
                DifferenceEngine engine = super.newEngine();
                engine.addDifferenceListener(listener);
                return engine;
            }
        };
        engineFactory.useEvaluator(evaluator);
        return engineFactory;
    }

    private Document buildTestDocument() {
        Document doc = new DocumentUtils().newTestDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("bar"));
        return doc;
    }

    private Document buildControlDocument() {
        Document doc = new DocumentUtils().newControlDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("foo")).appendChild(doc.createTextNode("foo text"));
        return doc;
    }
}
