package org.custommonkey.xmlunit.diff;

//Copyright 2012 Tomasz Werszko
//    
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//    
//    http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

/**
 * Defines possible types of differences.
 */
public enum DifferenceType {
    /** Comparing an implied attribute value against an explicit value */
    ATTR_VALUE_EXPLICITLY_SPECIFIED("attribute value explicitly specified", true),
    /** Comparing 2 elements and one has an attribute the other does not */
    ATTR_NAME_NOT_FOUND("attribute name", false),
    /** Comparing 2 attributes with the same name but different values */
    ATTR_VALUE("attribute value", false),
    /**
     * Comparing 2 attribute lists with the same attributes in different
     * sequence
     */
    ATTR_SEQUENCE("sequence of attributes", true),
    /** Comparing 2 CDATA sections with different values */
    CDATA_VALUE("CDATA section value", false),
    /** Comparing 2 comments with different values */
    COMMENT_VALUE("comment value", false),
    /** Comparing 2 document types with different names */
    DOCTYPE_NAME("doctype name", false),
    /** Comparing 2 document types with different public identifiers */
    DOCTYPE_PUBLIC_ID("doctype public identifier", false),
    /** Comparing 2 document types with different system identifiers */
    DOCTYPE_SYSTEM_ID("doctype system identifier", true),
    /** Comparing 2 elements with different tag names */
    ELEMENT_TAG_NAME("element tag name", false),
    /** Comparing 2 elements with different number of attributes */
    ELEMENT_NUM_ATTRIBUTES("number of element attributes", false),
    /** Comparing 2 processing instructions with different targets */
    PROCESSING_INSTRUCTION_TARGET("processing instruction target", false),
    /** Comparing 2 processing instructions with different instructions */
    PROCESSING_INSTRUCTION_DATA("processing instruction data", false),
    /** Comparing 2 different text values */
    TEXT_VALUE("text value", false),
    /** Comparing 2 nodes with different namespace prefixes */
    NAMESPACE_PREFIX("namespace prefix", true),
    /** Comparing 2 nodes with different namespace URIs */
    NAMESPACE_URI("namespace URI", false),
    /** Comparing 2 nodes with different node types */
    NODE_TYPE("node type", false),
    /** Comparing 2 nodes but only one has any children */
    HAS_CHILD_NODES("presence of child nodes to be", false),
    /** Comparing 2 nodes with different numbers of children */
    CHILD_NODELIST_LENGTH("number of child nodes", false),
    /** Comparing 2 nodes with children whose nodes are in different sequence */
    CHILD_NODELIST_SEQUENCE("sequence of child nodes", true),
    /** Comparing 2 Documents only one of which has a doctype */
    HAS_DOCTYPE_DECLARATION("presence of doctype declaration", true),
    /**
     * Comparing 2 nodes and one holds more childnodes than can be matched
     * against child nodes of the other.
     */
    CHILD_NODE_NOT_FOUND("presence of child node", false),
    /**
     * Comparing 2 nodes with different xsi:schemaLocation attributes,
     * potentially only one of the two provides such an attribute at all.
     */
    SCHEMA_LOCATION("xsi:schemaLocation attribute", true),
    /**
     * Comparing 2 nodes with different xsi:noNamespaceSchemaLocation
     * attributes, potentially only one of the two provides such an attribute at
     * all.
     */
    NO_NAMESPACE_SCHEMA_LOCATION("xsi:noNamespaceSchemaLocation attribute", true);

    private final String description;
    private final boolean recoverable;

    private DifferenceType(String description, boolean recoverable) {
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
