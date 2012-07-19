// Generated source.
package org.custommonkey.xmlunit.matchers;

public class XmlUnitMatchers {

  public static org.custommonkey.xmlunit.matchers.validation.XmlStringValidMatcher validXmlString() {
    return org.custommonkey.xmlunit.matchers.validation.XmlStringValidMatcher.validXmlString();
  }

  public static org.custommonkey.xmlunit.matchers.validation.XmlStringInvalidMatcher invalidXmlString() {
    return org.custommonkey.xmlunit.matchers.validation.XmlStringInvalidMatcher.invalidXmlString();
  }

  public static org.custommonkey.xmlunit.matchers.validation.XmlInputSourceValidMatcher validXmlSource() {
    return org.custommonkey.xmlunit.matchers.validation.XmlInputSourceValidMatcher.validXmlSource();
  }

  public static org.custommonkey.xmlunit.matchers.validation.XmlInputSourceInvalidMatcher invalidXmlSource() {
    return org.custommonkey.xmlunit.matchers.validation.XmlInputSourceInvalidMatcher.invalidXmlSource();
  }

  public static org.custommonkey.xmlunit.matchers.validation.XmlValidatorValidMatcher validXml() {
    return org.custommonkey.xmlunit.matchers.validation.XmlValidatorValidMatcher.validXml();
  }

  public static org.custommonkey.xmlunit.matchers.validation.XmlValidatorInvalidMatcher invalidXml() {
    return org.custommonkey.xmlunit.matchers.validation.XmlValidatorInvalidMatcher.invalidXml();
  }

  public static org.custommonkey.xmlunit.matchers.comparison.XmlEqualDocumentMatcher equalToXmlDocument(org.w3c.dom.Document expectedDoc) {
    return org.custommonkey.xmlunit.matchers.comparison.XmlEqualDocumentMatcher.equalToXmlDocument(expectedDoc);
  }

  public static org.custommonkey.xmlunit.matchers.comparison.XmlNotEqualDocumentMatcher notEqualToXmlDocument(org.w3c.dom.Document expectedDoc) {
    return org.custommonkey.xmlunit.matchers.comparison.XmlNotEqualDocumentMatcher.notEqualToXmlDocument(expectedDoc);
  }

  public static org.custommonkey.xmlunit.matchers.comparison.XmlEqualStringMatcher equalToXmlString(java.lang.String expectedString) {
    return org.custommonkey.xmlunit.matchers.comparison.XmlEqualStringMatcher.equalToXmlString(expectedString);
  }

  public static org.custommonkey.xmlunit.matchers.comparison.XmlNotEqualStringMatcher notEqualToXmlString(java.lang.String expectedString) {
    return org.custommonkey.xmlunit.matchers.comparison.XmlNotEqualStringMatcher.notEqualToXmlString(expectedString);
  }

  public static org.custommonkey.xmlunit.matchers.comparison.XmlEqualReaderMatcher equalToXmlReader(java.io.Reader expectedReader) {
    return org.custommonkey.xmlunit.matchers.comparison.XmlEqualReaderMatcher.equalToXmlReader(expectedReader);
  }

  public static org.custommonkey.xmlunit.matchers.comparison.XmlNotEqualReaderMatcher notEqualToXmlReader(java.io.Reader expectedReader) {
    return org.custommonkey.xmlunit.matchers.comparison.XmlNotEqualReaderMatcher.notEqualToXmlReader(expectedReader);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathExistsInXmlStringMatcher containsXpath(java.lang.String expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathExistsInXmlStringMatcher.containsXpath(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathNotExistsInXmlStringMatcher notContainsXpath(java.lang.String expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathNotExistsInXmlStringMatcher.notContainsXpath(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathExistsInDocumentMatcher documentNotContainsXpath(java.lang.String expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathExistsInDocumentMatcher.documentNotContainsXpath(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathNotExistsInDocumentMatcher documentContainsXpath(java.lang.String expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathNotExistsInDocumentMatcher.documentContainsXpath(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathsEqualMatcher equalToXpath(net.sf.xmlunit.xpath.XpathWrapper expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathsEqualMatcher.equalToXpath(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathsNotEqualMatcher notEqualToXpath(net.sf.xmlunit.xpath.XpathWrapper expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathsNotEqualMatcher.notEqualToXpath(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathEvaluatesToValueMatcher xpathEvaluatesTo(java.lang.String expectedXpathValue) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathEvaluatesToValueMatcher.xpathEvaluatesTo(expectedXpathValue);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathValuesEqualMatcher valueIsEqualToXpathValue(net.sf.xmlunit.xpath.XpathWrapper expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathValuesEqualMatcher.valueIsEqualToXpathValue(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.xpath.XpathValuesNotEqualMatcher valueIsNotEqualToXpathValue(net.sf.xmlunit.xpath.XpathWrapper expectedXpath) {
    return org.custommonkey.xmlunit.matchers.xpath.XpathValuesNotEqualMatcher.valueIsNotEqualToXpathValue(expectedXpath);
  }

  public static org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher passesWith(org.custommonkey.xmlunit.NodeTester param1, short[] param2) {
    return org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher.passesWith(param1, param2);
  }

  public static org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher passesWith(org.custommonkey.xmlunit.NodeTester tester, short nodeType) {
    return org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher.passesWith(tester, nodeType);
  }

  public static org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher notPassesWith(org.custommonkey.xmlunit.NodeTester param1, short[] param2) {
    return org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher.notPassesWith(param1, param2);
  }

  public static org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher notPassesWith(org.custommonkey.xmlunit.NodeTester tester, short nodeType) {
    return org.custommonkey.xmlunit.matchers.NoteTestPassesMatcher.notPassesWith(tester, nodeType);
  }

}
