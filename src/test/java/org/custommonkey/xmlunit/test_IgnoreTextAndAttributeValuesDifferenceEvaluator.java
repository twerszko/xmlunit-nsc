/*
 ******************************************************************
Copyright (c) 200, Jeff Martin, Tim Bacon
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.DifferenceEvaluator;

import org.custommonkey.xmlunit.diff.Diff;
import org.junit.Before;
import org.junit.Test;

/**
 * @author TimBacon
 */
public class test_IgnoreTextAndAttributeValuesDifferenceEvaluator {
	private DifferenceEvaluator evaluator;

	@Before
	public void setUp() {
		evaluator = new IgnoreTextAndAttributeValuesDifferenceEvaluator();
	}

	@Test
	public void testDifferenceFound() {
		assertThat(evaluator.evaluate(
		        createComparison(ComparisonType.ATTR_NAME_LOOKUP), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.ATTR_SEQUENCE), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.ATTR_VALUE), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.SIMILAR);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED),
		                ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.SIMILAR);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.CDATA_VALUE), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.CHILD_NODELIST_SEQUENCE),
		                ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.COMMENT_VALUE), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.DOCTYPE_NAME), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.DOCTYPE_PUBLIC_ID), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.DOCTYPE_SYSTEM_ID), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.ELEMENT_NUM_ATTRIBUTES),
		                ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.ELEMENT_TAG_NAME), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.HAS_CHILD_NODES), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.HAS_DOCTYPE_DECLARATION),
		                ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.NAMESPACE_PREFIX), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.NAMESPACE_URI), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(evaluator.evaluate(createComparison(ComparisonType.NODE_TYPE), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.PROCESSING_INSTRUCTION_DATA),
		                ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);
		assertThat(
		        evaluator.evaluate(createComparison(ComparisonType.PROCESSING_INSTRUCTION_TARGET),
		                ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.DIFFERENT);

		assertThat(evaluator.evaluate(createComparison(ComparisonType.TEXT_VALUE), ComparisonResult.DIFFERENT))
		        .isEqualTo(ComparisonResult.SIMILAR);
	}

	private Comparison createComparison(ComparisonType type) {
		return new Comparison(type, null, null, null, null, null, null);

	}

	@Test
	public void should_ignore_text_and_attribute_values() throws Exception {
		// given
		String control = "<clouds><cloud name=\"cumulus\" rain=\"maybe\">fluffy</cloud></clouds>";
		String similarTest = "<clouds><cloud name=\"cirrus\" rain=\"no\">wispy</cloud></clouds>";

		// when
		Diff diff = Diff.newDiff(null)
		        .betweenControlDocument(control)
		        .andTestDocument(similarTest)
		        .build();
		diff.overrideDifferenceEvaluator(evaluator);

		// then
		assertThat(diff.similar()).isTrue();
		assertThat(diff.identical()).isFalse();
	}

	@Test
	public void should_detect_differences() throws Exception {
		// given
		String control = "<clouds><cloud name=\"cumulus\" rain=\"maybe\">fluffy</cloud></clouds>";
		String similarTest = "<clouds><cloud name=\"cirrus\" rain=\"no\">wispy</cloud></clouds>";

		// when
		DetailedDiff detailedDiff = new DetailedDiff(
		        Diff.newDiff(null)
		                .betweenControlDocument(control)
		                .andTestDocument(similarTest)
		                .build());

		// then
		assertThat(detailedDiff.getAllDifferences()).hasSize(3);
	}

	@Test
	public void should_mark_as_dissimilar() throws Exception {
		// given
		String control = "<clouds><cloud name=\"cumulus\" rain=\"maybe\">fluffy</cloud></clouds>";
		String dissimilarTest = "<clouds><cloud name=\"nimbus\"/></clouds>";

		// when
		Diff dissimilarDiff = Diff.newDiff(null)
		        .betweenControlDocument(control)
		        .andTestDocument(dissimilarTest)
		        .build();
		dissimilarDiff.overrideDifferenceEvaluator(evaluator);

		// then
		assertThat(dissimilarDiff.similar()).isFalse();
	}

	@Test
	public void testClassInUse() throws Exception {
		// given
		String control = "<clouds><cloud name=\"cumulus\" rain=\"maybe\">fluffy</cloud></clouds>";
		String dissimilarTest = "<clouds><cloud name=\"nimbus\"/></clouds>";

		// when
		DetailedDiff dissimilarDetailedDiff = new DetailedDiff(
		        Diff.newDiff(null)
		                .betweenControlDocument(control)
		                .andTestDocument(dissimilarTest)
		                .build());
		dissimilarDetailedDiff.overrideDifferenceEvaluator(evaluator);
		List<Comparison> differences = dissimilarDetailedDiff.getAllDifferences();

		// then

		// has children, wrong number of attributes, missing attribute,
		// different attribute value, and missing text node
		assertThat(differences).hasSize(5);

		int recoverableCounter = 0;
		for (Comparison comparison : differences) {
			if (comparison.isRecoverable()) {
				recoverableCounter++;
			}
		}

		// attribute value difference has been overridden as similar
		assertThat(recoverableCounter).isEqualTo(1);
	}

	@Test
	public void testIssue771839() throws Exception {
		String xmlString1 = "<location>"
		        + "<street-address>22 any street</street-address>"
		        + "<postcode id='3'>XY0099Z</postcode>"
		        + "</location>";
		String xmlString2 = "<location>"
		        + "<postcode1 id='1'>EC3M 1EB</postcode1>"
		        + "<street-address>20 east cheap</street-address>"
		        + "</location>";

		Diff d = Diff.newDiff(null)
		        .betweenControlDocument(xmlString1)
		        .andTestDocument(xmlString2)
		        .build();
		d.overrideDifferenceEvaluator(evaluator);
		assertFalse(d.similar());
		assertTrue("postcode was matched against postcode1",
		        d.toString().indexOf("Expected element tag name 'postcode'"
		                + " but was 'postcode1'") > -1);
	}

}
