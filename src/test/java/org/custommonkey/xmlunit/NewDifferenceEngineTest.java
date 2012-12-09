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
import static org.junit.Assert.assertFalse;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;

import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

public class NewDifferenceEngineTest extends DifferenceEngineTestAbstract {

	@Override
	protected DifferenceEngineContract newDifferenceEngine() {
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
	public void testExtraComment() {
		testExtraComment(true);
		resetEvaluator();

		properties.setIgnoreComments(true);
		engine = new NewDifferenceEngine(properties);

		testExtraComment(false);
	}

	private void testExtraComment(boolean expectDifference) {
		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		Comment c = document.createComment("bar");
		control.appendChild(c);
		Element cChild = document.createElement("baz");
		control.appendChild(cChild);
		Element tChild = document.createElement("baz");
		test.appendChild(tChild);
		engine.compare(control, test, evaluator, null);
		assertEquals(expectDifference, evaluator.different);
		resetEvaluator();
		engine.compare(test, control, evaluator, null);
		assertEquals(expectDifference, evaluator.different);
	}

	@Test
	public void testCommentContent() {
		testCommentContent(true);
		resetEvaluator();

		properties.setIgnoreComments(true);
		engine = new NewDifferenceEngine(properties);

		testCommentContent(false);
	}

	private void testCommentContent(boolean expectDifference) {
		Element control = document.createElement("foo");
		Element test = document.createElement("foo");
		Comment c = document.createComment("bar");
		control.appendChild(c);
		Comment c2 = document.createComment("baz");
		test.appendChild(c2);
		engine.compare(control, test, evaluator, null);
		assertEquals(expectDifference, evaluator.different);
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
	public void testDifferentSchemaLocation() throws Exception {
		testDifferentXSIAttribute(XMLConstants
		        .W3C_XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTR,
		        ComparisonType.SCHEMA_LOCATION);
	}

	@Test
	public void testDifferentNoNamespaceSchemaLocation() throws Exception {
		testDifferentXSIAttribute(XMLConstants
		        .W3C_XML_SCHEMA_INSTANCE_NO_NAMESPACE_SCHEMA_LOCATION_ATTR,
		        ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
	}

	private void testDifferentXSIAttribute(String attrName,
	        ComparisonType expectedDifference)
	        throws Exception {
		Element control = document.createElement("foo");
		control.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
		        attrName, "bar");
		Element test = document.createElement("foo");
		test.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
		        attrName, "baz");
		engine.compare(control, test, evaluator, null);
		assertEquals(expectedDifference, evaluator.comparingWhat);
	}

	@Test
	public void testMissingAttribute() throws Exception {
		Element control = document.createElement("foo");
		control.setAttribute("bar", "baz");
		Element test = document.createElement("foo");
		test.setAttribute("baz", "bar");
		engine.compare(control, test, evaluator, null);
		assertEquals(ComparisonType.ATTR_NAME_LOOKUP, evaluator.comparingWhat);
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

	/**
	 * @see http 
	 *      ://sourceforge.net/forum/forum.php?thread_id=3284504&forum_id=73274
	 */
	@Test
	public void testNamespaceAttributeDifferences() throws Exception {
		String control = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		        + "<ns0:Message xmlns:ns0 = \"http://mynamespace\">"
		        + "<ns0:EventHeader>"
		        + "<ns0:EventID>9999</ns0:EventID>"
		        + "<ns0:MessageID>1243409665297</ns0:MessageID>"
		        + "<ns0:MessageVersionID>1.0</ns0:MessageVersionID>"
		        + "<ns0:EventName>TEST-EVENT</ns0:EventName>"
		        + "<ns0:BWDomain>TEST</ns0:BWDomain>"
		        + "<ns0:DateTimeStamp>2009-01-01T12:00:00</ns0:DateTimeStamp>"
		        + "<ns0:SchemaPayloadRef>anything</ns0:SchemaPayloadRef>"
		        + "<ns0:MessageURI>anything</ns0:MessageURI>"
		        + "<ns0:ResendFlag>F</ns0:ResendFlag>"
		        + "</ns0:EventHeader>"
		        + "<ns0:EventBody>"
		        + "<ns0:XMLContent>"
		        + "<xyz:root xmlns:xyz=\"http://test.com/xyz\">"
		        + "<xyz:test1>A</xyz:test1>"
		        + "<xyz:test2>B</xyz:test2>"
		        + "</xyz:root>"
		        + "</ns0:XMLContent>"
		        + "</ns0:EventBody>"
		        + "</ns0:Message>";
		String test =
		        "<abc:Message xmlns:abc=\"http://mynamespace\" xmlns:xyz=\"http://test.com/xyz\">"
		                + "<abc:EventHeader>"
		                + "<abc:EventID>9999</abc:EventID>"
		                + "<abc:MessageID>1243409665297</abc:MessageID>"
		                + "<abc:MessageVersionID>1.0</abc:MessageVersionID>"
		                + "<abc:EventName>TEST-EVENT</abc:EventName>"
		                + "<abc:BWDomain>TEST</abc:BWDomain>"
		                + "<abc:DateTimeStamp>2009-01-01T12:00:00</abc:DateTimeStamp>"
		                + "<abc:SchemaPayloadRef>anything</abc:SchemaPayloadRef>"
		                + "<abc:MessageURI>anything</abc:MessageURI>"
		                + "<abc:ResendFlag>F</abc:ResendFlag>"
		                + "</abc:EventHeader>"
		                + "<abc:EventBody>"
		                + "<abc:XMLContent>"
		                + "<xyz:root>"
		                + "<xyz:test1>A</xyz:test1>"
		                + "<xyz:test2>B</xyz:test2>"
		                + "</xyz:root>"
		                + "</abc:XMLContent>"
		                + "</abc:EventBody>"
		                + "</abc:Message>";
		listenToDifferences(control, test);
		assertFalse(evaluator.different);
	}

	/**
	 * XMLUnit 1.3 jumps from the document node straight to the root element,
	 * ignoring any other children the document might have. Some people consider
	 * this a bug (Issue 2770386) others rely on it.
	 * 
	 * <p>
	 * XMLUnit 2.x doesn't ignore differences in the prelude but we want to keep
	 * the behavior for the legacy code base.
	 * </p>
	 */
	@Test
	public void testIgnoresDifferencesBetweenDocAndRootElement()
	        throws Throwable {
		String control =
		        "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		                + "<!-- some comment -->"
		                + "<?foo some PI ?>"
		                + "<bar/>";
		String test = "<bar/>";
		listenToDifferences(control, test);
		assertFalse("unexpected difference: " + evaluator.comparingWhat,
		        evaluator.different);
		resetEvaluator();
		listenToDifferences(test, control);
		assertFalse("unexpected difference: " + evaluator.comparingWhat,
		        evaluator.different);
		resetEvaluator();
		control =
		        "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		                + "<!-- some comment -->"
		                + "<?foo some PI ?>"
		                + "<bar/>";
		test =
		        "<?xml version = \"1.0\" encoding = \"UTF-8\"?>"
		                + "<?foo some other PI ?>"
		                + "<!-- some other comment -->"
		                + "<bar/>";
		listenToDifferences(control, test);
		assertFalse("unexpected difference: " + evaluator.comparingWhat,
		        evaluator.different);
		resetEvaluator();
		listenToDifferences(test, control);
		assertFalse("unexpected difference: " + evaluator.comparingWhat,
		        evaluator.different);
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
