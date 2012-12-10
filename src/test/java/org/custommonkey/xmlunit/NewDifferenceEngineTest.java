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

package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;

import org.junit.Test;
import org.w3c.dom.Element;

public class NewDifferenceEngineTest extends DifferenceEngineTestAbstract {

	@Override
	protected DifferenceEngineContract newDifferenceEngine() {
		return new NewDifferenceEngine(properties);
	}

	@Override
	protected DifferenceEngineContract newDifferenceEngine(XmlUnitProperties properties) {
		return new NewDifferenceEngine(properties);
	}

	@Test
	public void testIssue1027863() throws Exception {
		engine = new NewDifferenceEngine(new XmlUnitProperties());
		String control = "<stuff><item id=\"1\"><thing/></item></stuff>";
		String test = "<stuff><item id=\"2\"/></stuff>";
		listenToDifferences(control, test);
		assertEquals("15th difference type",
		        ComparisonType.HAS_CHILD_NODES,
		        evaluator.comparingWhat);
		assertEquals("15th difference control value", "true",
		        evaluator.expected);
		assertEquals("15th difference test value", "false",
		        evaluator.actual);
		assertEquals("15th control xpath", "/stuff[1]/item[1]",
		        evaluator.controlXpath);
		assertEquals("15th test xpath", "/stuff[1]/item[1]",
		        evaluator.testXpath);
	}

	@Test
	public void testMissingSchemaLocation() throws Exception {
		testMissingXSIAttribute(XMLConstants
		        .W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR,
		        ComparisonType.SCHEMA_LOCATION);
	}

	@Test
	public void testMissingNoNamespaceSchemaLocation() throws Exception {
		testMissingXSIAttribute(XMLConstants
		        .W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR,
		        ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
	}

	private void testMissingXSIAttribute(String attrName,
	        ComparisonType expectedDifference)
	        throws Exception {
		Element control = document.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
		        attrName, "bar");
		Element test = document.createElement("foo");
		engine.compare(control, test, evaluator, null);
		assertEquals(expectedDifference, evaluator.comparingWhat);
		resetEvaluator();
		engine.compare(test, control, evaluator, null);
		assertEquals(expectedDifference, evaluator.comparingWhat);
	}

	@Test
	public void testMatchTrackerSetViaConstructor() throws Exception {
		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		final int[] count = new int[1];
		NewDifferenceEngine d =
		        new NewDifferenceEngine(new XmlUnitProperties(),
		                new ComparisonListener() {
			                @Override
			                public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
				                count[0]++;
			                }
		                });
		d.compare(control, test, evaluator, null);
		// NODE_TYPE(Element), NAMESPACE_URI(none),
		// NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
		// ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
		// SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
		assertEquals(8, count[0]);
	}

	@Test
	public void testMatchTrackerSetViaSetter() throws Exception {
		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		final int[] count = new int[1];
		engine.setMatchListener(new ComparisonListener() {
			@Override
			public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
				count[0]++;
			}
		});
		engine.compare(control, test, evaluator, null);
		// NODE_TYPE(Element), NAMESPACE_URI(none),
		// NAMESPACE_PREFIX(none), HAS_CHILD_NODES(false),
		// ELEMENT_TAG_NAME(foo), ELEMENT_NUM_ATTRIBUTE(none),
		// SCHEMA_LOCATION(none), NO_NAMESPACE_SCHEMA_LOCATION(none)
		assertEquals(8, count[0]);
	}

	@Override
	@Test
	public void should_obtain_xpath_of_extra_node() throws Exception {
		// TODO Investigate difference
		// given
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"1\"/></stuff>";

		// when
		listenToAllDifferences(control, test);

		// then
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/item[2]");
		// this is different from DifferenceEngine - the test node is null
		// if there is no match
		assertThat(evaluator.testXpath).isNull();
	}

	@Test
	@Override
	public void should_obtain_xpath_of_attribute() throws Exception {
		// TODO Investigate difference
		// given
		String control = "<stuff><thing id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"2\"/><item id=\"1\"/></stuff>";

		// when
		listenToAllDifferences(control, test);

		// then
		assertThat(evaluator.comparingWhat).isEqualTo(ComparisonType.CHILD_NODELIST_SEQUENCE);
		assertThat(evaluator.controlXpath).isEqualTo("/stuff[1]/item[1]");
		assertThat(evaluator.testXpath).isEqualTo("/stuff[1]/item[1]");
	}
}
