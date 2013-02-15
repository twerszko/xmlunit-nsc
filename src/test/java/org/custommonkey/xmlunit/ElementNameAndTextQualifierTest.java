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

import static org.fest.assertions.api.Assertions.assertThat;
import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ElementSelectors;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementNameAndTextQualifierTest {
    private static final String TAG_NAME = "tagYoureIt";
    private static final String TEXT_A = "textA";
    private static final String TEXT_B = "textB";
    private Document document;
    private ElementSelector elementNameAndTextQualifier;

    @Before
    public void setUp() throws Exception {
        document = new DocumentUtils(new XmlUnitProperties()).newControlDocumentBuilder().newDocument();
        elementNameAndTextQualifier = ElementSelectors.byNameAndText;
    }

    @Test
    public void should_check_single_text_value() {
        // given
        Element control = document.createElement(TAG_NAME);
        control.appendChild(document.createTextNode(TEXT_A));
        Element test = document.createElement(TAG_NAME);

        // when
        boolean qualifiedForComparison1 = elementNameAndTextQualifier
                .canBeCompared(control, test);

        test.appendChild(document.createTextNode(TEXT_A));
        boolean qualifiedForComparison2 = elementNameAndTextQualifier
                .canBeCompared(control, test);

        test = document.createElement(TAG_NAME);
        test.appendChild(document.createTextNode(TEXT_B));
        boolean qualifiedForComparison3 = elementNameAndTextQualifier
                .canBeCompared(control, test);

        // then
        assertThat(qualifiedForComparison1).isFalse();
        assertThat(qualifiedForComparison2).isTrue();
        assertThat(qualifiedForComparison3).isFalse();
    }

    @Test
    public void should_check_multiple_text_values() {
        // given
        Element control = document.createElement(TAG_NAME);
        control.appendChild(document.createTextNode(TEXT_A));
        control.appendChild(document.createTextNode(TEXT_B));

        Element test = document.createElement(TAG_NAME);
        test.appendChild(document.createTextNode(TEXT_A + TEXT_B));

        // when
        boolean qualifiedForComparison = elementNameAndTextQualifier.canBeCompared(control, test);

        // then
        assertThat(qualifiedForComparison).isTrue();
    }

}
