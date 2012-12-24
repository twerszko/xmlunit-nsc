package net.sf.xmlunit.diff.internal;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;

import net.sf.xmlunit.diff.OrderPreservingNamedNodeMap;

import org.custommonkey.xmlunit.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

public class AttributesTest {
    private final DocumentBuilder documentBuilder = new DocumentUtils().newControlDocumentBuilder();

    @Test
    public void should_create_from_simple_attributes() throws Exception {
        // given
        Document document = documentBuilder.newDocument();
        OrderPreservingNamedNodeMap attrMap = new OrderPreservingNamedNodeMap();

        for (int i = 0; i < 5; i++) {
            Attr attr = document.createAttribute("attr_" + i);
            attr.setValue(String.valueOf(i));
            attrMap.add(attr);
        }

        // when
        Attributes attributes = Attributes.from(attrMap);

        // then
        assertThat(attributes.getRegularAttributes()).hasSize(5);
        assertThat(attributes.getNoNamespaceSchemaLocation()).isNull();
        assertThat(attributes.getSchemaLocation()).isNull();
    }

    @Test
    public void should_create_with_schema_attributes() throws Exception {
        // given
        Document document = documentBuilder.newDocument();
        OrderPreservingNamedNodeMap attrMap = new OrderPreservingNamedNodeMap();

        Attr schemaLoc = document.createAttributeNS(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
        schemaLoc.setValue("schemaLocForTest");
        Attr noSchemaLoc = document.createAttributeNS(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "noNamespaceSchemaLocation");
        noSchemaLoc.setValue("noNsSchemaForTest");
        attrMap.add(schemaLoc);
        attrMap.add(noSchemaLoc);

        for (int i = 0; i < 3; i++) {
            Attr attr = document.createAttribute("attr_" + i);
            attr.setValue(String.valueOf(i));
            attrMap.add(attr);
        }

        // when
        Attributes attributes = Attributes.from(attrMap);

        // then
        assertThat(attributes.getRegularAttributes()).hasSize(3);
        assertThat(attributes.getNoNamespaceSchemaLocation()).isEqualTo(noSchemaLoc);
        assertThat(attributes.getSchemaLocation()).isEqualTo(schemaLoc);
    }

    @Test
    public void should_fin_matching_attribute() throws Exception {
        // given
        Document document = documentBuilder.newDocument();
        OrderPreservingNamedNodeMap attrMap = new OrderPreservingNamedNodeMap();
        String expectedAttrName = "expected";

        Attr expectedMatchingAttr = document.createAttribute(expectedAttrName);
        expectedMatchingAttr.setValue("test");
        attrMap.add(expectedMatchingAttr);
        for (int i = 0; i < 3; i++) {
            Attr attr = document.createAttribute("attr_" + i);
            attr.setValue(String.valueOf(i));
            attrMap.add(attr);
        }

        Attr toBeMatched = document.createAttribute(expectedAttrName);

        // when
        Attributes attributes = Attributes.from(attrMap);
        Attr foundAttr = attributes.findMatchingRegularAttr(toBeMatched);

        // then
        assertThat(foundAttr).isEqualTo(expectedMatchingAttr);
    }

    @Test
    public void should_find_matching_attribute_with_ns() throws Exception {
        // given
        Document document = documentBuilder.newDocument();
        OrderPreservingNamedNodeMap attrMap = new OrderPreservingNamedNodeMap();
        String expectedAttrName = "expected";
        String expectedNs = "ns";

        Attr expectedMatchingAttr = document.createAttributeNS(expectedNs, expectedAttrName);
        expectedMatchingAttr.setValue("test");
        attrMap.add(expectedMatchingAttr);
        for (int i = 0; i < 3; i++) {
            Attr attr = document.createAttribute("attr_" + i);
            attr.setValue(String.valueOf(i));
            attrMap.add(attr);
        }

        Attr toBeMatched = document.createAttributeNS(expectedNs, expectedAttrName);

        // when
        Attributes attributes = Attributes.from(attrMap);
        Attr foundAttr = attributes.findMatchingRegularAttr(toBeMatched);

        // then
        assertThat(foundAttr).isEqualTo(expectedMatchingAttr);
    }

    @Test
    public void should_not_find_matching_attribute_when_different_ns() throws Exception {
        // given
        Document document = documentBuilder.newDocument();
        OrderPreservingNamedNodeMap attrMap = new OrderPreservingNamedNodeMap();
        String expectedAttrName = "expected";

        Attr expectedMatchingAttr = document.createAttributeNS("ns", expectedAttrName);
        expectedMatchingAttr.setValue("test");
        attrMap.add(expectedMatchingAttr);
        for (int i = 0; i < 3; i++) {
            Attr attr = document.createAttribute("attr_" + i);
            attr.setValue(String.valueOf(i));
            attrMap.add(attr);
        }

        Attr toBeMatched = document.createAttributeNS("otherNs", expectedAttrName);

        // when
        Attributes attributes = Attributes.from(attrMap);
        Attr foundAttr = attributes.findMatchingRegularAttr(toBeMatched);

        // then
        assertThat(foundAttr).isNull();
    }
}
