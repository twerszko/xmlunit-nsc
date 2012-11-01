/*
 *****************************************************************
Copyright (c) 2001-2008, Jeff Martin, Tim Bacon
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

import java.io.IOException;
import java.io.Reader;

import org.custommonkey.xmlunit.diff.Diff;
import org.custommonkey.xmlunit.exceptions.XmlUnitException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows access to project control parameters such as which Parser to use and
 * provides some convenience methods for building Documents from Strings etc. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 */
public final class XmlUnit {
    private final XmlUnitProperties properties;

    private static String xpathFactoryName = null;

    /**
     * Creates XMLUnit with defensive copy of given properties object.
     * 
     * @param properties
     */
    XmlUnit(XmlUnitProperties properties) {
        this.properties = properties.clone();
    }

    /**
     * Returns defensive copy of properties.
     * 
     * @return
     */
    public XmlUnitProperties getProperties() {
        return properties.clone();
    }

    /**
     * Place holder for current version info.
     * 
     * @return current version
     */
    public static String getVersion() {
        return "1.3alpha";
    }

    /**
     * Compare XML documents provided by two InputSource classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     * @throws SAXException
     * @throws IOException
     */
    public Diff compareXML(InputSource control, InputSource test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare XML documents provided by two Reader classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(Reader control, Reader test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare XML documents provided by two Reader classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(String control, Reader test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare XML documents provided by two Reader classes
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(Reader control, String test)
            throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare two XML documents provided as strings
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(String control, String test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Compare two XML documents provided as strings
     * 
     * @param control
     *            Control document
     * @param test
     *            Document to test
     * @return Diff object describing differences in documents
     * @throws XmlUnitException
     */
    public Diff compareXML(Document control, Document test) throws XmlUnitException {
        return Diff.newDiff(properties)
                .betweenControlDocument(control)
                .andTestDocument(test)
                .build();
    }

    /**
     * Obtains an XpathEngine to use in XPath tests.
     */
    public XpathEngine newXpathEngine() {
        XpathEngine eng = null;
        try {
            Class.forName("javax.xml.xpath.XPath");
            Class<?> c = Class.forName("org.custommonkey.xmlunit.jaxp13.Jaxp13XpathEngine");
            eng = (XpathEngine) c.newInstance();
        } catch (Throwable ex) {
            // should probably only catch ClassNotFoundException, but some
            // constellations - like Ant shipping a more recent version of
            // xml-apis than the JDK - may contain the JAXP 1.3 interfaces
            // without implementations
            eng = new SimpleXpathEngine(properties);
        }
        if (properties.getXpathNamespaceContext() != null) {
            // TODO
            eng.setNamespaceContext(properties.getXpathNamespaceContext());
        }
        return eng;
    }

    /**
     * Sets the class to use as XPathFactory when using JAXP 1.3.
     */
    public static void setXPathFactory(String className) {
        xpathFactoryName = className;
    }

    /**
     * Gets the class to use as XPathFactory when using JAXP 1.3.
     */
    public static String getXPathFactory() {
        return xpathFactoryName;
    }

}
