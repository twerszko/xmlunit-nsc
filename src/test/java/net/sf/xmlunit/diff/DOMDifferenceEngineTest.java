/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package net.sf.xmlunit.diff;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.xmlunit.NullNode;
import net.sf.xmlunit.TestResources;
import net.sf.xmlunit.builder.Input;
import net.sf.xmlunit.diff.internal.NodeAndXpathCtx;
import net.sf.xmlunit.util.Convert;

import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMDifferenceEngineTest extends AbstractDifferenceEngineTest {

    @Override
    protected AbstractDifferenceEngine getDifferenceEngine() {
        return new DOMDifferenceEngine(null);
    }

    private static class DiffExpecter implements ComparisonListener {
        private int invoked = 0;
        private final int expectedInvocations;
        private final ComparisonType type;
        private final boolean withXPath;
        private final String controlXPath;
        private final String testXPath;

        private DiffExpecter(ComparisonType type) {
            this(type, 1);
        }

        private DiffExpecter(ComparisonType type, int expected) {
            this(type, expected, false, null, null);
        }

        private DiffExpecter(ComparisonType type, String controlXPath, String testXPath) {
            this(type, 1, true, controlXPath, testXPath);
        }

        private DiffExpecter(ComparisonType type, int expected, boolean withXPath, String controlXPath, String testXPath) {
            this.type = type;
            this.expectedInvocations = expected;
            this.withXPath = withXPath;
            this.controlXPath = controlXPath;
            this.testXPath = testXPath;
        }

        @Override
        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
            assertTrue(invoked + " should be less than " + expectedInvocations, invoked < expectedInvocations);
            invoked++;
            assertEquals(type, comparison.getType());
            assertEquals(ComparisonResult.CRITICAL, outcome);
            if (withXPath) {
                assertEquals("Control XPath", controlXPath, comparison.getControlDetails().getXpath());
                assertEquals("Test XPath", testXPath, comparison.getTestDetails().getXpath());
            }
        }
    }

    private Document doc;

    @Before
    public void createDoc() throws Exception {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @Test
    public void should_compare_nodes_of_different_type() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.NODE_TYPE);
        diffEngine.addDifferenceListener(ex);

        // when
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        ComparisonResult comparisonResult = diffEngine.compareNodes(
                doc.createElement("x"), new XPathContext(), doc.createComment("x"), new XPathContext());

        // then
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));
        assertThat(ex.invoked, is(equalTo(1)));
    }

    @Test
    public void should_compare_nodes_without_NS() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.NODE_TYPE, 0);
        diffEngine.addDifferenceListener(ex);

        // when
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        ComparisonResult comparisonResult = diffEngine.compareNodes(
                doc.createElement("x"), new XPathContext(), doc.createElement("x"), new XPathContext());

        // then
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));
        assertThat(ex.invoked, is(equalTo(0)));
    }

    @Test
    public void should_compare_nodes_different_NS() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.NAMESPACE_URI);
        diffEngine.addDifferenceListener(ex);

        // when
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        ComparisonResult comparisonResult = diffEngine.compareNodes(
                doc.createElementNS("x", "y"), new XPathContext(), doc.createElementNS("z", "y"), new XPathContext());

        // then
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));
        assertThat(ex.invoked, is(equalTo(1)));
    }

    @Test
    public void should_compare_nodes_with_different_prefix() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.NAMESPACE_PREFIX);
        diffEngine.addDifferenceListener(ex);

        // when
        diffEngine.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.NAMESPACE_PREFIX) {
                    assertThat(outcome, is(equalTo(ComparisonResult.DIFFERENT)));
                    return ComparisonResult.CRITICAL;
                }
                assertThat(outcome, is(equalTo(ComparisonResult.EQUAL)));
                return ComparisonResult.EQUAL;
            }
        });
        ComparisonResult comparisonResult = diffEngine.compareNodes(
                doc.createElementNS("x", "x:y"), new XPathContext(),
                doc.createElementNS("x", "z:y"), new XPathContext());

        // then
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));
        assertThat(ex.invoked, is(equalTo(1)));
    }

    @Test
    public void should_compare_nodes_one_with_children_the_other_without() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.HAS_CHILD_NODES, 2);
        diffEngine.addDifferenceListener(ex);
        ComparisonResult comparisonResult;

        // then
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        Element e1 = doc.createElement("x");
        Element e2 = doc.createElement("x");

        comparisonResult = diffEngine.compareNodes(e1, new XPathContext(), e2, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        e1.appendChild(doc.createElement("x"));
        comparisonResult = diffEngine.compareNodes(e1, new XPathContext(), e2, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));
        assertThat(ex.invoked, is(equalTo(1)));

        e2.appendChild(doc.createElement("x"));
        comparisonResult = diffEngine.compareNodes(e1, new XPathContext(), e2, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));
    }

    @Test
    public void should_compare_nodes_different_number_of_children() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.CHILD_NODELIST_LENGTH, 2);
        diffEngine.addDifferenceListener(ex);
        ComparisonResult comparisonResult;

        // then
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        Element e1 = doc.createElement("x");
        Element e2 = doc.createElement("x");

        comparisonResult = diffEngine.compareNodes(e1, new XPathContext(), e2, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        e1.appendChild(doc.createElement("x"));
        e2.appendChild(doc.createElement("x"));
        comparisonResult = diffEngine.compareNodes(e1, new XPathContext(), e2, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        e2.appendChild(doc.createElement("x"));
        comparisonResult = diffEngine.compareNodes(e1, new XPathContext(), e2, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));
        assertThat(ex.invoked, is(equalTo(1)));
    }

    @Test
    public void should_compare_character_data_comment() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.COMMENT_VALUE, 1);
        diffEngine.addDifferenceListener(ex);
        ComparisonResult comparisonResult;

        Comment fooComment = doc.createComment("foo");
        Comment barComment = doc.createComment("bar");

        // when
        diffEngine.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.NODE_TYPE) {
                    Object controlTarget = comparison.getControlDetails().getTarget();
                    Object testTarget = comparison.getTestDetails().getTarget();

                    if (outcome == ComparisonResult.EQUAL
                            || (controlTarget instanceof CharacterData && testTarget instanceof CharacterData)) {
                        return ComparisonResult.EQUAL;
                    }
                }
                return DifferenceEvaluators.DefaultStopWhenDifferent.evaluate(comparison, outcome);
            }
        });

        // then
        comparisonResult = diffEngine.compareNodes(fooComment, new XPathContext(), fooComment, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooComment, new XPathContext(), barComment, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        assertThat(ex.invoked, is(equalTo(1)));
    }

    @Test
    public void should_compare_character_data_cdata() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.CDATA_VALUE, 1);
        diffEngine.addDifferenceListener(ex);
        ComparisonResult comparisonResult;

        CDATASection fooCDATASection = doc.createCDATASection("foo");
        CDATASection barCDATASection = doc.createCDATASection("bar");

        // when
        diffEngine.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.NODE_TYPE) {
                    Object controlTarget = comparison.getControlDetails().getTarget();
                    Object testTarget = comparison.getTestDetails().getTarget();

                    if (outcome == ComparisonResult.EQUAL
                            || (controlTarget instanceof CharacterData && testTarget instanceof CharacterData)) {
                        return ComparisonResult.EQUAL;
                    }
                }
                return DifferenceEvaluators.DefaultStopWhenDifferent.evaluate(comparison, outcome);
            }
        });

        // then
        comparisonResult = diffEngine.compareNodes(
                fooCDATASection, new XPathContext(), fooCDATASection, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(
                fooCDATASection, new XPathContext(), barCDATASection, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        assertThat(ex.invoked, is(equalTo(1)));
    }

    @Test
    public void should_compare_character_data() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.TEXT_VALUE, 7);
        diffEngine.addDifferenceListener(ex);
        ComparisonResult comparisonResult;

        Comment fooComment = doc.createComment("foo");
        Comment barComment = doc.createComment("bar");
        Text fooText = doc.createTextNode("foo");
        Text barText = doc.createTextNode("bar");
        CDATASection fooCDATASection = doc.createCDATASection("foo");
        CDATASection barCDATASection = doc.createCDATASection("bar");

        // when
        diffEngine.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.NODE_TYPE) {
                    Node controlTarget = comparison.getControlDetails().getTarget();
                    Node testTarget = comparison.getTestDetails().getTarget();

                    if (outcome == ComparisonResult.EQUAL
                            || (controlTarget instanceof CharacterData && testTarget instanceof CharacterData)) {
                        return ComparisonResult.EQUAL;
                    }
                }
                return DifferenceEvaluators.DefaultStopWhenDifferent.evaluate(comparison, outcome);
            }
        });

        // then
        comparisonResult = diffEngine.compareNodes(fooText, new XPathContext(), fooText, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooText, new XPathContext(), barText, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        comparisonResult = diffEngine.compareNodes(fooComment, new XPathContext(), fooText, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooComment, new XPathContext(), barText, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        comparisonResult = diffEngine.compareNodes(fooComment, new XPathContext(), fooCDATASection, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooComment, new XPathContext(), barCDATASection, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        comparisonResult = diffEngine.compareNodes(fooText, new XPathContext(), fooComment, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooText, new XPathContext(), barComment, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        comparisonResult = diffEngine.compareNodes(fooText, new XPathContext(), fooCDATASection, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooText, new XPathContext(), barCDATASection, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        comparisonResult = diffEngine.compareNodes(fooCDATASection, new XPathContext(), fooText, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooCDATASection, new XPathContext(), barText, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        comparisonResult = diffEngine.compareNodes(fooCDATASection, new XPathContext(), fooComment, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.EQUAL)));

        comparisonResult = diffEngine.compareNodes(fooCDATASection, new XPathContext(), barComment, new XPathContext());
        assertThat(comparisonResult, is(equalTo(ComparisonResult.CRITICAL)));

        assertThat(ex.invoked, is(equalTo(7)));
    }

    @Test
    public void should_compare_processing_instructions() {
        // given
        DOMDifferenceEngine diffEngine = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.PROCESSING_INSTRUCTION_TARGET);
        diffEngine.addDifferenceListener(ex);

        // when
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);

        ProcessingInstruction foo1 = doc.createProcessingInstruction("foo", "1");
        ProcessingInstruction bar1 = doc.createProcessingInstruction("bar", "1");

        assertEquals(ComparisonResult.EQUAL,
                diffEngine.compareNodes(foo1, new XPathContext(), foo1, new XPathContext()));
        assertEquals(ComparisonResult.CRITICAL,
                diffEngine.compareNodes(foo1, new XPathContext(), bar1, new XPathContext()));
        assertEquals(1, ex.invoked);

        diffEngine = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.PROCESSING_INSTRUCTION_DATA);
        diffEngine.addDifferenceListener(ex);
        diffEngine.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        ProcessingInstruction foo2 = doc.createProcessingInstruction("foo", "2");
        assertEquals(ComparisonResult.EQUAL,
                diffEngine.compareNodes(foo1, new XPathContext(), foo1, new XPathContext()));
        assertEquals(ComparisonResult.CRITICAL,
                diffEngine.compareNodes(foo1, new XPathContext(), foo2, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    @Test
    public void compareDocuments() throws IOException {
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.HAS_DOCTYPE_DECLARATION);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.HAS_DOCTYPE_DECLARATION) {
                    assertEquals(ComparisonResult.DIFFERENT, outcome);
                    return ComparisonResult.CRITICAL;
                }
                assertEquals("Expected EQUAL for " + comparison.getType() + " comparison.", ComparisonResult.EQUAL,
                        outcome);
                return ComparisonResult.EQUAL;
            }
        });
        Document d1 = Convert.toDocument(Input.fromMemory("<Book/>").build());
        Document d2 = Convert.toDocument(Input.fromMemory(
                "<!DOCTYPE Book PUBLIC " + "\"XMLUNIT/TEST/PUB\" " + "\"" + TestResources.BOOK_DTD.getFile() + "\">"
                        + "<Book/>").build());
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(d1, new XPathContext(), d2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.XML_VERSION);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);

        d1 = Convert
                .toDocument(Input.fromMemory("<?xml version=\"1.0\"" + " encoding=\"UTF-8\"?>" + "<Book/>").build());
        d2 = Convert
                .toDocument(Input.fromMemory("<?xml version=\"1.1\"" + " encoding=\"UTF-8\"?>" + "<Book/>").build());
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(d1, new XPathContext(), d2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.XML_STANDALONE);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);

        d1 = Convert
                .toDocument(Input.fromMemory("<?xml version=\"1.0\"" + " standalone=\"yes\"?>" + "<Book/>").build());
        d2 = Convert.toDocument(Input.fromMemory("<?xml version=\"1.0\"" + " standalone=\"no\"?>" + "<Book/>").build());
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(d1, new XPathContext(), d2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.XML_ENCODING);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.XML_ENCODING) {
                    assertEquals(ComparisonResult.DIFFERENT, outcome);
                    return ComparisonResult.CRITICAL;
                }
                assertEquals(ComparisonResult.EQUAL, outcome);
                return ComparisonResult.EQUAL;
            }
        });

        d1 = Convert
                .toDocument(Input.fromMemory("<?xml version=\"1.0\"" + " encoding=\"UTF-8\"?>" + "<Book/>").build());
        d2 = Convert.toDocument(Input.fromMemory("<?xml version=\"1.0\"" + " encoding=\"UTF-16\"?>" + "<Book/>")
                .build());
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(d1, new XPathContext(), d2, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    private static class DocType extends NullNode implements DocumentType {
        private final String name, publicId, systemId;

        private DocType(String name, String publicId, String systemId) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        @Override
        public short getNodeType() {
            return Node.DOCUMENT_TYPE_NODE;
        }

        @Override
        public NamedNodeMap getEntities() {
            return null;
        }

        @Override
        public String getInternalSubset() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public NamedNodeMap getNotations() {
            return null;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public String getSystemId() {
            return systemId;
        }
    }

    @Test
    public void compareDocTypes() {
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.DOCTYPE_NAME);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        DocumentType dt1 = new DocType("name", "pub", "system");
        DocumentType dt2 = new DocType("name2", "pub", "system");
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(dt1, new XPathContext(), dt2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.DOCTYPE_PUBLIC_ID);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        dt2 = new DocType("name", "pub2", "system");
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(dt1, new XPathContext(), dt2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.DOCTYPE_SYSTEM_ID);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.DOCTYPE_SYSTEM_ID) {
                    assertEquals(ComparisonResult.DIFFERENT, outcome);
                    return ComparisonResult.CRITICAL;
                }
                assertEquals(ComparisonResult.EQUAL, outcome);
                return ComparisonResult.EQUAL;
            }
        });
        dt2 = new DocType("name", "pub", "system2");
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(dt1, new XPathContext(), dt2, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    @Test
    public void compareElements() {
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.ELEMENT_TAG_NAME);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        Element e1 = doc.createElement("foo");
        Element e2 = doc.createElement("foo");
        Element e3 = doc.createElement("bar");
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e3, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.ELEMENT_NUM_ATTRIBUTES);
        e1.setAttribute("attr1", "value1");
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.ATTR_NAME_LOOKUP, "/@attr1", "/");
        e2.setAttributeNS("urn:xmlunit:test", "attr1", "value1");
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        d.addDifferenceListener(new ComparisonListener() {
            @Override
            public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
                fail("unexpected Comparison of type " + comparison.getType() + " with outcome " + outcome
                        + " and values '" + comparison.getControlDetails().getValue() + "' and '"
                        + comparison.getTestDetails().getValue() + "'");
            }
        });
        e1.setAttributeNS("urn:xmlunit:test", "attr1", "value1");
        e2.setAttributeNS(null, "attr1", "value1");
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
    }

    @Test
    public void compareAttributes() {
        Attr a1 = doc.createAttribute("foo");
        Attr a2 = doc.createAttribute("foo");

        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.ATTR_VALUE_EXPLICITLY_SPECIFIED);
        /*
         * Can't reset "explicitly set" state for Documents created via API
         * d.addDifferenceListener(ex);
         * d.setDifferenceEvaluator(DifferenceEvaluators.Accept);
         * a2.setValue(""); assertEquals(ComparisonResult.CRITICAL,
         * d.compareNodes(a1, new XPathContext(), a2, new XPathContext()));
         * assertEquals(1, ex.invoked);
         * 
         * d = new DOMDifferenceEngine();
         */
        ex = new DiffExpecter(ComparisonType.ATTR_VALUE);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        Attr a3 = doc.createAttribute("foo");
        a1.setValue("foo");
        a2.setValue("foo");
        a3.setValue("bar");
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(a1, new XPathContext(), a2, new XPathContext()));
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(a1, new XPathContext(), a3, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    @Test
    public void naiveRecursion() {
        Element e1 = doc.createElement("foo");
        Element e2 = doc.createElement("foo");
        Element c1 = doc.createElement("bar");
        e1.appendChild(c1);
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.CHILD_LOOKUP, "/bar[1]", null);
        d.addDifferenceListener(ex);
        DifferenceEvaluator ev = new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.HAS_CHILD_NODES) {
                    return ComparisonResult.EQUAL;
                }
                return DifferenceEvaluators.DefaultStopWhenDifferent.evaluate(comparison, outcome);
            }
        };
        d.setDifferenceEvaluator(ev);
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);

        // symmetric?
        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.CHILD_LOOKUP, null, "/bar[1]");
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(ev);
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e2, new XPathContext(), e1, new XPathContext()));
        assertEquals(1, ex.invoked);

        Element c2 = doc.createElement("bar");
        e2.appendChild(c2);
        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.CHILD_LOOKUP);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(ev);
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e2, new XPathContext(), e1, new XPathContext()));
        assertEquals(0, ex.invoked);
    }

    @Test
    public void textAndCDataMatchRecursively() {
        Element e1 = doc.createElement("foo");
        Element e2 = doc.createElement("foo");
        Text fooText = doc.createTextNode("foo");
        e1.appendChild(fooText);
        CDATASection fooCDATASection = doc.createCDATASection("foo");
        e2.appendChild(fooCDATASection);
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e2, new XPathContext(), e1, new XPathContext()));
    }

    @Test
    public void recursionUsesElementSelector() {
        Element e1 = doc.createElement("foo");
        Element e2 = doc.createElement("foo");
        Element e3 = doc.createElement("bar");
        e1.appendChild(e3);
        Element e4 = doc.createElement("baz");
        e2.appendChild(e4);
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.ELEMENT_TAG_NAME, "/bar[1]", "/baz[1]");
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);

        d = new DOMDifferenceEngine(null);
        d.setNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName));
        ex = new DiffExpecter(ComparisonType.CHILD_LOOKUP, "/bar[1]", null);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(DifferenceEvaluators.DefaultStopWhenDifferent);
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    @Test
    public void schemaLocationDifferences() {
        Element e1 = doc.createElement("foo");
        Element e2 = doc.createElement("foo");
        e1.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "somewhere");
        e2.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "somewhere else");

        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.SCHEMA_LOCATION);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.SCHEMA_LOCATION) {
                    assertEquals(ComparisonResult.DIFFERENT, outcome);
                    return ComparisonResult.CRITICAL;
                }
                assertEquals(ComparisonResult.EQUAL, outcome);
                return ComparisonResult.EQUAL;
            }
        });
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);

        e1 = doc.createElement("foo");
        e2 = doc.createElement("foo");
        e1.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation", "somewhere");
        e2.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation", "somewhere else");
        d = new DOMDifferenceEngine(null);
        ex = new DiffExpecter(ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION);
        d.addDifferenceListener(ex);
        d.setDifferenceEvaluator(new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION) {
                    assertEquals(ComparisonResult.DIFFERENT, outcome);
                    return ComparisonResult.CRITICAL;
                }
                assertEquals(ComparisonResult.EQUAL, outcome);
                return ComparisonResult.EQUAL;
            }
        });
        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    @Test
    public void compareElementsNS() {
        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.ELEMENT_TAG_NAME);
        d.addDifferenceListener(ex);
        DifferenceEvaluator ev = new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (comparison.getType() == ComparisonType.NAMESPACE_PREFIX) {
                    return ComparisonResult.EQUAL;
                }
                return DifferenceEvaluators.DefaultStopWhenDifferent.evaluate(comparison, outcome);
            }
        };
        d.setDifferenceEvaluator(ev);
        Element e1 = doc.createElementNS("urn:xmlunit:test", "foo");
        e1.setPrefix("p1");
        Element e2 = doc.createElementNS("urn:xmlunit:test", "foo");
        e2.setPrefix("p2");
        assertEquals(ComparisonResult.EQUAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(0, ex.invoked);
    }

    @Test
    public void should_detect_equal_string_values() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

        Comparison comparison = new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                null, null, "black",
                null, null, "black");

        // when
        ComparisonResult result = engine.compare(comparison);

        // then
        assertThat(result).isEqualTo(ComparisonResult.EQUAL);
        assertThat(evaluator.getDifferences()).hasSize(0);
    }

    @Test
    public void should_detect_different_string_values() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

        Comparison comparison = new Comparison(ComparisonType.ATTR_NAME_LOOKUP,
                null, null, "black",
                null, null, "white");

        // when
        ComparisonResult result = engine.compare(comparison);

        // then
        assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
        assertThat(evaluator.getDifferences()).hasSize(1);
        assertThat(evaluator.getDifferences()).contains(comparison);
    }

    @Test
    public void should_detect_equal_boolean_values() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

        Comparison comparison = new Comparison(ComparisonType.HAS_CHILD_NODES,
                null, null, true,
                null, null, true);

        // when
        ComparisonResult result = engine.compare(comparison);

        // then
        assertThat(result).isEqualTo(ComparisonResult.EQUAL);
        assertThat(evaluator.getDifferences()).hasSize(0);
    }

    @Test
    public void should_detect_different_boolean_values() throws Exception {
        // given
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        engine.setDifferenceEvaluator(evaluator);

        Comparison comparison = new Comparison(ComparisonType.HAS_CHILD_NODES,
                null, null, false,
                null, null, true);

        // when
        ComparisonResult result = engine.compare(comparison);

        // then
        assertThat(result).isEqualTo(ComparisonResult.DIFFERENT);
        assertThat(evaluator.getDifferences()).hasSize(1);
        assertThat(evaluator.getDifferences()).contains(comparison);
    }

    @Test
    public void should_detect_different_tags_in_child_node_list() throws Exception {
        // given
        Document document = new DocumentUtils().buildControlDocument(
                "<down>" +
                        "<im><standing/>alone</im>" +
                        "<im><watching/>you all</im>" +
                        "</down>");

        Node control = document.getDocumentElement().getFirstChild();
        Node test = control.getNextSibling();

        // when
        List<Comparison> differences = findDifferences(control, test);

        // then
        assertThat(differences).hasSize(2);
        Comparison tagDifference = differences.get(0);
        Comparison textDifference = differences.get(1);
        assertThat(tagDifference.getType()).isEqualTo(ComparisonType.ELEMENT_TAG_NAME);
        assertThat(tagDifference.getControlDetails().getValue()).isEqualTo("standing");
        assertThat(tagDifference.getTestDetails().getValue()).isEqualTo("watching");
        assertThat(textDifference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
    }

    @Test
    public void should_detect_different_text_in_child_nodes() throws Exception {
        // given
        Document document = new DocumentUtils().buildControlDocument(
                "<down>" +
                        "<im><watching/>you all</im>" +
                        "<im><watching/>you sinking</im>" +
                        "</down>");

        Node control = document.getDocumentElement().getFirstChild();
        Node test = control.getNextSibling();

        // when
        List<Comparison> differences = findDifferences(control, test);

        // then
        assertThat(differences).hasSize(1);
        Comparison textDifference = differences.get(0);
        assertThat(textDifference.getType()).isEqualTo(ComparisonType.TEXT_VALUE);
        assertThat(textDifference.getControlDetails().getValue()).isEqualTo("you all");
        assertThat(textDifference.getTestDetails().getValue()).isEqualTo("you sinking");
    }

    private List<Comparison> findDifferences(Node control, Node test) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);
        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);

        engine.compareNodes(control, new XPathContext(), test, new XPathContext());
        List<Comparison> differences = evaluator.getDifferences();
        return differences;
    }

    @Test
    public void should_not_detect_differences_in_namespace() throws Exception {
        DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        String namespace = "http://example.org/StoneRoses";
        String prefix = "music";
        String elemName = "nowPlaying";

        Element control = document.createElementNS(namespace, prefix + ':' + elemName);

        // when
        List<Comparison> differences = testNamespaceDetails(control, control);

        // then
        assertThat(differences).hasSize(0);
    }

    @Test
    public void should_detect_different_namespace_uri() throws Exception {
        DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        String namespaceA = "http://example.org/StoneRoses";
        String namespaceB = "http://example.org/Stone/Roses";
        String prefixA = "music";
        String elemName = "nowPlaying";

        Element control = document.createElementNS(namespaceA, prefixA + ':' + elemName);
        Element test = document.createElementNS(namespaceB, prefixA + ':' + elemName);

        // when
        List<Comparison> differences = testNamespaceDetails(control, test);

        // then
        assertThat(differences).hasSize(1);
        Comparison difference = differences.get(0);
        assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_URI);
    }

    @Test
    public void should_detect_different_namespace_prefix() throws Exception {
        DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        String namespaceA = "http://example.org/StoneRoses";
        String prefixA = "music";
        String prefixB = "cd";
        String elemName = "nowPlaying";

        Element control = document.createElementNS(namespaceA, prefixA + ':' + elemName);
        Element test = document.createElementNS(namespaceA, prefixB + ':' + elemName);

        // when
        List<Comparison> differences = testNamespaceDetails(control, test);

        // then
        assertThat(differences).hasSize(1);
        Comparison difference = differences.get(0);
        assertThat(difference.getType()).isEqualTo(ComparisonType.NAMESPACE_PREFIX);
    }

    private List<Comparison> testNamespaceDetails(Node control, Node test) {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(null);

        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);

        engine.compareNodes(control, new XPathContext(), test, new XPathContext());

        return evaluator.getDifferences();
    }

    @Test
    public void childNodeListSequence() {
        Element e1 = doc.createElement("foo");
        Element e3 = doc.createElement("bar");
        Element e4 = doc.createElement("baz");
        e1.appendChild(e3);
        e1.appendChild(e4);

        Element e2 = doc.createElement("foo");
        Element e5 = doc.createElement("bar");
        Element e6 = doc.createElement("baz");
        e2.appendChild(e6);
        e2.appendChild(e5);

        DOMDifferenceEngine d = new DOMDifferenceEngine(null);
        DiffExpecter ex = new DiffExpecter(ComparisonType.CHILD_NODELIST_SEQUENCE, "/bar[1]", "/bar[1]");
        d.addDifferenceListener(ex);
        DifferenceEvaluator ev = new DifferenceEvaluator() {
            @Override
            public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
                if (outcome != ComparisonResult.EQUAL && comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
                    return ComparisonResult.CRITICAL;
                }
                return DifferenceEvaluators.DefaultStopWhenDifferent.evaluate(comparison, outcome);
            }
        };
        d.setDifferenceEvaluator(ev);
        d.setNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName));

        assertEquals(ComparisonResult.CRITICAL, d.compareNodes(e1, new XPathContext(), e2, new XPathContext()));
        assertEquals(1, ex.invoked);
    }

    @Test
    public void should_ignore_different_attribute_sequence() throws Exception {
        // given
        XmlUnitProperties properties = new XmlUnitProperties();

        // when
        List<Comparison> differences = testAttributeSequence(properties);

        // then
        assertThat(differences).hasSize(0);
    }

    @Test
    public void should_detect_differenct_attribute_sequence() throws Exception {
        // given
        XmlUnitProperties properties = new XmlUnitProperties();
        properties.setIgnoreAttributeOrder(false);

        // when
        List<Comparison> differences = testAttributeSequence(properties);

        // then
        assertThat(differences).hasSize(2);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
        assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
    }

    private List<Comparison> testAttributeSequence(XmlUnitProperties properties) throws Exception {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(properties);
        DocumentBuilder documentBuilder = new DocumentUtils(properties).newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);

        Element control = document.createElement("foo");
        Element test = document.createElement("foo");
        OrderPreservingNamedNodeMap controlMap = new OrderPreservingNamedNodeMap();
        OrderPreservingNamedNodeMap testMap = new OrderPreservingNamedNodeMap();
        for (int i = 0; i < 2; i++) {
            int j = 1 - i;
            Attr attrI = document.createAttribute("attr" + i);
            attrI.setValue(String.valueOf(i));
            Attr attrJ = document.createAttribute("attr" + j);
            attrJ.setValue(String.valueOf(j));

            control.setAttributeNode(attrI);
            controlMap.add(attrI);
            test.setAttributeNode(attrJ);
            testMap.add(attrJ);
        }
        engine.compareElementAttributes(
                NodeAndXpathCtx.from(control, new XPathContext()), controlMap,
                NodeAndXpathCtx.from(test, new XPathContext()), testMap);
        return evaluator.getDifferences();
    }

    @Test
    public void should_ignore_different_attribute_ns_sequence() throws Exception {
        // given
        XmlUnitProperties properties = new XmlUnitProperties();
        properties.setIgnoreAttributeOrder(true);

        // when
        List<Comparison> differences = testAttributeSequenceNS(properties);

        // then
        assertThat(differences).hasSize(0);
    }

    @Test
    public void should_detect_differenct_ns_attribute_sequence() throws Exception {
        // given
        XmlUnitProperties properties = new XmlUnitProperties();
        properties.setIgnoreAttributeOrder(false);

        // when
        List<Comparison> differences = testAttributeSequenceNS(properties);

        // then
        assertThat(differences).hasSize(2);
        assertThat(differences.get(0).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
        assertThat(differences.get(1).getType()).isEqualTo(ComparisonType.ATTR_SEQUENCE);
    }

    private List<Comparison> testAttributeSequenceNS(XmlUnitProperties properties) throws Exception {
        DOMDifferenceEngine engine = new DOMDifferenceEngine(properties);
        DocumentBuilder documentBuilder = new DocumentUtils(properties).newControlDocumentBuilder();
        Document document = documentBuilder.newDocument();

        ListingDifferenceEvaluator evaluator = new ListingDifferenceEvaluator();
        engine.setDifferenceEvaluator(evaluator);

        Element control = document.createElementNS("ns", "foo");
        Element test = document.createElementNS("ns", "foo");
        OrderPreservingNamedNodeMap controlMap = new OrderPreservingNamedNodeMap();
        OrderPreservingNamedNodeMap testMap = new OrderPreservingNamedNodeMap();
        for (int i = 0; i < 2; i++) {
            int j = 1 - i;
            Attr attrI = document.createAttributeNS("ns", "attr" + i);
            attrI.setValue(String.valueOf(i));
            Attr attrJ = document.createAttributeNS("ns", "attr" + j);
            attrJ.setValue(String.valueOf(j));

            control.setAttributeNode(attrI);
            controlMap.add(attrI);
            test.setAttributeNode(attrJ);
            testMap.add(attrJ);
        }
        engine.compareElementAttributes(
                NodeAndXpathCtx.from(control, new XPathContext()), controlMap,
                NodeAndXpathCtx.from(test, new XPathContext()), testMap);
        return evaluator.getDifferences();
    }

}
