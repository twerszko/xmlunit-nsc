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

/**
 * The kinds of comparisons XMLUnit performs.
 */
public enum ComparisonType {
    /**
     * Do both documents specify the same version in their XML declaration?
     */
    XML_VERSION("", true),
    /**
     * Do both documents specify the same standalone declaration in their XML
     * declaration?
     */
    XML_STANDALONE("", true),
    /**
     * Do both documents specify the same encoding in their XML declaration?
     */
    XML_ENCODING("", true),
    /**
     * Do both documents have a DOCTYPE (or neither of each)?
     */
    HAS_DOCTYPE_DECLARATION("presence of doctype declaration", true),
    /**
     * If the documents both have DOCTYPEs, compare the names.
     */
    DOCTYPE_NAME("doctype name", false),
    /**
     * If the documents both have DOCTYPEs, compare the PUBLIC identifiers.
     */
    DOCTYPE_PUBLIC_ID("doctype public identifier", false),
    /**
     * If the documents both have DOCTYPEs, compare the SYSTEM identifiers.
     */
    DOCTYPE_SYSTEM_ID("doctype system identifier", true),

    /**
     * Check whether both documents provide the same values for
     * xsi:schemaLocation (may even be null).
     */
    SCHEMA_LOCATION("xsi:schemaLocation attribute", true),
    /**
     * Check whether both documents provide the same values for
     * xsi:noNamspaceSchemaLocation (may even be null).
     */
    NO_NAMESPACE_SCHEMA_LOCATION("xsi:noNamespaceSchemaLocation attribute", true),

    /**
     * Compare the node types.
     */
    NODE_TYPE("node type", false),

    /**
     * Compare the node's namespace prefixes.
     */
    NAMESPACE_PREFIX("namespace prefix", true),
    /**
     * Compare the node's namespace URIs.
     */
    NAMESPACE_URI("namespace URI", false),

    /**
     * Compare content of text nodes and CDATA sections.
     */
    TEXT_VALUE("text value", false),

    /**
     * Compare content of comment nodes
     * */
    COMMENT_VALUE("comment value", false),

    /**
     * Compare cdata sections
     */
    CDATA_VALUE("CDATA section value", false),

    /**
     * Compare targets of processing instructions.
     */
    PROCESSING_INSTRUCTION_TARGET("processing instruction target", false),
    /**
     * Compare data of processing instructions.
     */
    PROCESSING_INSTRUCTION_DATA("processing instruction data", false),

    /**
     * Compare element names.
     */
    ELEMENT_TAG_NAME("element tag name", false),
    /**
     * Compare explicit/implicit status of attributes.
     */
    ATTR_VALUE_EXPLICITLY_SPECIFIED("attribute value explicitly specified", true),
    /**
     * Compare number of attributes.
     */
    ELEMENT_NUM_ATTRIBUTES("number of element attributes", false),
    /**
     * Compare attribute's value.
     */
    ATTR_VALUE("attribute value", false),
    /**
     * Comparing 2 nodes but only one has any children
     * */
    HAS_CHILD_NODES("presence of child nodes to be", false),
    /**
     * Compare number of child nodes.
     */
    CHILD_NODELIST_LENGTH("number of child nodes", false),
    /**
     * Compare order of child nodes.
     */
    CHILD_NODELIST_SEQUENCE("sequence of child nodes", true),

    /**
     * Search for a child node matching a specific child node of the other node.
     */
    CHILD_LOOKUP("presence of child node", false),
    /**
     * Search for an atribute with a name matching a specific attribute of the
     * other node.
     */
    ATTR_NAME_LOOKUP("attribute name", false),

    /**
     * Comparing 2 attribute lists with the same attributes in different
     * sequence
     */
    ATTR_SEQUENCE("sequence of attributes", true);

    private final String description;
    private final boolean recoverable;

    private ComparisonType(String description, boolean recoverable) {
        this.description = description;
        this.recoverable = recoverable;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRecoverable() {
        return recoverable;
    }
}
