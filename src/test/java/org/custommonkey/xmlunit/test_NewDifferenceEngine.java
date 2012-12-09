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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import net.sf.xmlunit.diff.Comparison;
import net.sf.xmlunit.diff.ComparisonListener;
import net.sf.xmlunit.diff.ComparisonResult;
import net.sf.xmlunit.diff.ComparisonType;
import net.sf.xmlunit.diff.ElementSelector;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class test_NewDifferenceEngine extends DifferenceEngineTestAbstract {
	private final static ElementSelector SEQUENCE_ELEMENT_QUALIFIER = null;
	private final static String TEXT_A = "the pack on my back is aching";
	private final static String TEXT_B = "the straps seem to cut me like a knife";
	private final static String COMMENT_A = "Im no clown I wont back down";
	private final static String COMMENT_B = "dont need you to tell me whats going down";
	private final static String[] PROC_A = { "down", "down down" };
	private final static String[] PROC_B = { "dadada", "down" };
	private final static String CDATA_A = "I'm standing alone, you're weighing the gold";
	private final static String CDATA_B = "I'm watching you sinking... Fools Gold";
	private final static String ATTR_A = "These boots were made for walking";
	private final static String ATTR_B = "The marquis de sade never wore no boots like these";

	@Override
	protected DifferenceEngineContract newDifferenceEngine() {
		return new NewDifferenceEngine(properties);
	}

	@Test
	public void testCompareNode() throws Exception {
		Document controlDocument = new DocumentUtils(properties).buildControlDocument("<root>"
		        + "<!-- " + COMMENT_A + " -->"
		        + "<?" + PROC_A[0] + " " + PROC_A[1] + " ?>"
		        + "<elem attr=\"" + ATTR_A + "\">" + TEXT_A + "</elem></root>");
		Document testDocument = new DocumentUtils(properties).buildTestDocument("<root>"
		        + "<!-- " + COMMENT_B + " -->"
		        + "<?" + PROC_B[0] + " " + PROC_B[1] + " ?>"
		        + "<elem attr=\"" + ATTR_B + "\">" + TEXT_B + "</elem></root>");

		engine.compare(controlDocument, testDocument, evaluator, null);

		Node control = controlDocument.getDocumentElement().getFirstChild();
		Node test = testDocument.getDocumentElement().getFirstChild();

		do {
			resetEvaluator();
			engine.compare(control, test, evaluator, null);
			assertEquals(true, null != evaluator.comparingWhat);
			assertEquals(false, evaluator.nodesSkipped);

			resetEvaluator();
			engine.compare(control, control, evaluator, null);
			assertEquals(null, evaluator.comparingWhat);

			control = control.getNextSibling();
			test = test.getNextSibling();
		} while (control != null);
	}

	@Test
	public void testXpathLocation1() throws Exception {
		String control = "<dvorak><keyboard/><composer/></dvorak>";
		String test = "<qwerty><keyboard/></qwerty>";
		listenToDifferences(control, test, SEQUENCE_ELEMENT_QUALIFIER);
		assertEquals("1st control xpath", "/dvorak[1]", evaluator.controlXpath);
		assertEquals("1st test xpath", "/qwerty[1]", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation2() throws Exception {
		String control = "<dvorak><keyboard/><composer/></dvorak>";
		String test = "<qwerty><keyboard/></qwerty>";
		String start = "<a>", end = "</a>";
		listenToDifferences(start + control + end, start + test + end, SEQUENCE_ELEMENT_QUALIFIER);
		assertEquals("2nd control xpath", "/a[1]/dvorak[1]", evaluator.controlXpath);
		assertEquals("2nd test xpath", "/a[1]/qwerty[1]", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation3() throws Exception {
		String control = "<stuff><wood type=\"rough\"/></stuff>";
		String test = "<stuff><wood type=\"smooth\"/></stuff>";
		listenToDifferences(control, test);
		assertEquals("3rd control xpath", "/stuff[1]/wood[1]/@type", evaluator.controlXpath);
		assertEquals("3rd test xpath", "/stuff[1]/wood[1]/@type", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation4() throws Exception {
		String control = "<stuff><glass colour=\"clear\"/><glass colour=\"green\"/></stuff>";
		String test = "<stuff><glass colour=\"clear\"/><glass colour=\"blue\"/></stuff>";
		;
		listenToDifferences(control, test);
		assertEquals("4th control xpath", "/stuff[1]/glass[2]/@colour", evaluator.controlXpath);
		assertEquals("4th test xpath", "/stuff[1]/glass[2]/@colour", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation5() throws Exception {
		String control = "<stuff><wood>maple</wood><wood>oak</wood></stuff>";
		String test = "<stuff><wood>maple</wood><wood>ash</wood></stuff>";
		listenToDifferences(control, test);
		assertEquals("5th control xpath", "/stuff[1]/wood[2]/text()[1]", evaluator.controlXpath);
		assertEquals("5th test xpath", "/stuff[1]/wood[2]/text()[1]", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation6() throws Exception {
		String control = "<stuff><list><wood/><glass/></list><item/></stuff>";
		String test = "<stuff><list><wood/><glass/></list><item>description</item></stuff>";
		listenToDifferences(control, test);
		assertEquals("6th control xpath", "/stuff[1]/item[1]", evaluator.controlXpath);
		assertEquals("6th test xpath", "/stuff[1]/item[1]", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation7() throws Exception {
		String control = "<stuff><list><wood/></list></stuff>";
		String test = "<stuff><list><glass/></list></stuff>";
		listenToDifferences(control, test, SEQUENCE_ELEMENT_QUALIFIER);
		assertEquals("7th control xpath", "/stuff[1]/list[1]/wood[1]", evaluator.controlXpath);
		assertEquals("7th test xpath", "/stuff[1]/list[1]/glass[1]", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation8() throws Exception {
		String control = "<stuff><list><!--wood--></list></stuff>";
		String test = "<stuff><list><!--glass--></list></stuff>";
		listenToDifferences(control, test);
		assertEquals("8th control xpath", "/stuff[1]/list[1]/comment()[1]", evaluator.controlXpath);
		assertEquals("8th test xpath", "/stuff[1]/list[1]/comment()[1]", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation9() throws Exception {
		String control = "<stuff><list/><?wood rough?><list/></stuff>";
		String test = "<stuff><list/><?glass clear?><list/></stuff>";
		listenToDifferences(control, test);
		assertEquals("9th control xpath", "/stuff[1]/processing-instruction()[1]",
		        evaluator.controlXpath);
		assertEquals("9th test xpath", "/stuff[1]/processing-instruction()[1]",
		        evaluator.testXpath);
	}

	@Test
	public void testXpathLocation10() throws Exception {
		String control = "<stuff><list/>list<![CDATA[wood]]></stuff>";
		String test = "<stuff><list/>list<![CDATA[glass]]></stuff>";
		listenToDifferences(control, test);
		assertEquals("10th control xpath", "/stuff[1]/text()[2]",
		        evaluator.controlXpath);
		assertEquals("10th test xpath", "/stuff[1]/text()[2]",
		        evaluator.testXpath);
	}

	@Test
	public void testXpathLocation11() throws Exception {
		String control = "<stuff><list><item/></list></stuff>";
		String test = "<stuff><list>item text</list></stuff>";
		listenToDifferences(control, test);
		assertEquals("11th control xpath", "/stuff[1]/list[1]/item[1]",
		        evaluator.controlXpath);
		assertEquals("11th test xpath", "/stuff[1]/list[1]/text()[1]",
		        evaluator.testXpath);
	}

	@Test
	public void testXpathLocation12() throws Exception {
		engine = new NewDifferenceEngine(new XmlUnitProperties());
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"1\"/></stuff>";
		listenToAllDifferences(control, test);
		assertEquals("12th control xpath", "/stuff[1]/item[2]",
		        evaluator.controlXpath);
		// this is different from DifferenceEngine - the test node is null
		// if there is no match
		assertNull("12th test xpath", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation13() throws Exception {
		engine = new NewDifferenceEngine(new XmlUnitProperties());
		String control = "<stuff><item id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><?item data?></stuff>";
		listenToAllDifferences(control, test);
		// mutiple Differences, we only see the last one, missing second element
		assertEquals("13 difference type",
		        ComparisonType.CHILD_LOOKUP,
		        evaluator.comparingWhat);
		assertEquals("13th control xpath", "/stuff[1]/item[2]",
		        evaluator.controlXpath);
		assertNull("13th test xpath", evaluator.testXpath);
	}

	@Test
	public void testXpathLocation14() throws Exception {
		engine = new NewDifferenceEngine(new XmlUnitProperties());
		String control = "<stuff><thing id=\"1\"/><item id=\"2\"/></stuff>";
		String test = "<stuff><item id=\"2\"/><item id=\"1\"/></stuff>";
		listenToAllDifferences(control, test);
		assertEquals("14th difference type",
		        ComparisonType.CHILD_NODELIST_SEQUENCE,
		        evaluator.comparingWhat);
		assertEquals("14th control xpath", "/stuff[1]/item[1]",
		        evaluator.controlXpath);
		assertEquals("14th test xpath", "/stuff[1]/item[1]",
		        evaluator.testXpath);
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
}
