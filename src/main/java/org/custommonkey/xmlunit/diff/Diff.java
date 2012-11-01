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

package org.custommonkey.xmlunit.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceEngineContract;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.ElementQualifier;
import org.custommonkey.xmlunit.MatchTracker;
import org.custommonkey.xmlunit.NewDifferenceEngine;
import org.custommonkey.xmlunit.XmlUnit;
import org.custommonkey.xmlunit.XmlUnitProperties;
import org.custommonkey.xmlunit.builder.Builder;
import org.custommonkey.xmlunit.builder.BuilderException;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.util.DocumentUtils;
import org.custommonkey.xmlunit.util.XsltUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Compares and describes any difference between XML documents. Two documents
 * are either: <br />
 * <ul>
 * <li><i>identical</i>: the content and sequence of the nodes in the documents
 * are exactly the same</li>
 * <li><i>similar</i>: the content of the nodes in the documents are the same,
 * but minor differences exist e.g. sequencing of sibling elements, values of
 * namespace prefixes, use of implied attribute values</li>
 * <li><i>different</i>: the contents of the documents are fundamentally
 * different</li>
 * </ul>
 * <br />
 * The difference between compared documents is contained in a message buffer
 * held in this class, accessible either through the <code>appendMessage</code>
 * or <code>toString</code> methods. NB: When comparing documents, the
 * comparison is halted as soon as the status (identical / similar / different)
 * is known with certainty. For a list of all differences between the documents
 * an instance of {@link DetailedDiff the DetailedDiff class} can be used
 * instead. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 */
public class Diff implements DifferenceListener, ComparisonController {
    private final XmlUnitProperties properties;

    private final Document controlDoc;
    private final Document testDoc;
    private boolean similar = true;
    private boolean identical = true;
    private boolean compared = false;
    private boolean haltComparison = false;
    private final StringBuffer messages;
    private final DifferenceEngineContract differenceEngine;
    private DifferenceListener differenceListenerDelegate;
    private ElementQualifier elementQualifierDelegate;
    private MatchTracker matchTrackerDelegate;

    /**
     * Construct a Diff that compares the XML in two Documents using a specific
     * DifferenceEngine and ElementQualifier
     */
    private Diff(DiffBuilder builder) {
        this.properties = builder.properties.clone();
        this.controlDoc = getManipulatedDocument(builder.controlDocument);
        this.testDoc = getManipulatedDocument(builder.testDocument);
        this.elementQualifierDelegate = builder.elementQualifier;
        this.differenceEngine = builder.differenceEngineContract;
        this.messages = new StringBuffer();
    }

    /**
     * Construct a Diff from a prototypical instance. Used by extension
     * subclasses
     * 
     * @param prototype
     *            a prototypical instance
     */
    protected Diff(Diff prototype) {
        // TODO clone?
        this.properties = prototype.properties.clone();
        this.controlDoc = getManipulatedDocument(prototype.controlDoc);
        this.testDoc = getManipulatedDocument(prototype.testDoc);
        this.elementQualifierDelegate = prototype.elementQualifierDelegate;
        this.differenceEngine = prototype.differenceEngine;
        this.differenceListenerDelegate = prototype.differenceListenerDelegate;
        this.messages = new StringBuffer();

    }

    /**
     * If {@link XmlUnit#getIgnoreWhitespace whitespace is ignored} in
     * differences then manipulate the content to strip the redundant whitespace
     * 
     * @param originalDoc
     *            a document making up one half of this difference
     * @return the original document with redundant whitespace removed if
     *         differences ignore whitespace
     */
    private Document getWhitespaceManipulatedDocument(Document originalDoc) {
        return properties.getIgnoreWhitespace()
                ? new XsltUtils(properties).getWhitespaceStrippedDocument(originalDoc)
                : originalDoc;
    }

    /**
     * Manipulates the given document according to the setting in the XMLUnit
     * class.
     * 
     * <p>
     * This may involve:
     * </p>
     * <ul>
     * <li>{@link XmlUnit.setIgnoreWhitespace stripping redundant whitespace}</li>
     * <li>{@link XmlUnit.setIgnoreComments stripping comments}</li>
     * <li>{@link XmlUnit.setNormalize normalizing Text nodes}</li>
     * </ul>
     * 
     * @param orig
     *            a document making up one half of this difference
     * @return manipulated doc
     */
    private Document getManipulatedDocument(Document orig) {
        return getNormalizedDocument(getCommentlessDocument(getWhitespaceManipulatedDocument(orig)));
    }

    /**
     * Removes all comment nodes if {@link XmlUnit.getIgnoreComments comments
     * are ignored}.
     * 
     * @param originalDoc
     *            a document making up one half of this difference
     * @return manipulated doc
     */
    private Document getCommentlessDocument(Document orig) {
        if (!properties.getIgnoreComments()) {
            return orig;
        }

        return new XsltUtils(properties).getStripCommentsTransform(orig).toDocument();
    }

    private Document getNormalizedDocument(Document orig) {
        if (!properties.getNormalize()) {
            return orig;
        }
        Document d = (Document) orig.cloneNode(true);
        d.normalize();
        return d;
    }

    /**
     * Top of the recursive comparison execution tree
     */
    protected final void compare() {
        if (compared) {
            return;
        }
        getDifferenceEngine().compare(controlDoc, testDoc, this,
                elementQualifierDelegate);
        compared = true;
    }

    /**
     * Return the result of a comparison. Two documents are considered to be
     * "similar" if they contain the same elements and attributes regardless of
     * order.
     */
    public boolean similar() {
        compare();
        return similar;
    }

    /**
     * Return the result of a comparison. Two documents are considered to be
     * "identical" if they contain the same elements and attributes in the same
     * order.
     */
    public boolean identical() {
        compare();
        return identical;
    }

    /**
     * Append a meaningful message to the buffer of messages
     * 
     * @param appendTo
     *            the messages buffer
     * @param expected
     * @param actual
     * @param control
     * @param test
     * @param difference
     */
    private void appendDifference(StringBuffer appendTo, Difference difference) {
        appendTo.append(' ').append(difference).append('\n');
    }

    /**
     * DifferenceListener implementation. If the
     * {@link Diff#overrideDifferenceListener overrideDifferenceListener} method
     * has been called then the interpretation of the difference will be
     * delegated.
     * 
     * @param difference
     * @return a DifferenceListener.RETURN_... constant indicating how the
     *         difference was interpreted. Always RETURN_ACCEPT_DIFFERENCE if
     *         the call is not delegated.
     */
    public int differenceFound(Difference difference) {
        int returnValue = evaluate(difference);

        switch (returnValue) {
        case RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL:
            return returnValue;
        case RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR:
            identical = false;
            haltComparison = false;
            break;
        case RETURN_ACCEPT_DIFFERENCE:
            identical = false;
            if (difference.isRecoverable()) {
                haltComparison = false;
            } else {
                similar = false;
                haltComparison = true;
            }
            break;
        case RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT:
            identical = similar = false;
            haltComparison = true;
            break;
        default:
            throw new IllegalArgumentException(returnValue
                    + " is not a defined DifferenceListener.RETURN_... value");
        }
        if (haltComparison) {
            messages.append("\n[different]");
        } else {
            messages.append("\n[not identical]");
        }
        appendDifference(messages, difference);
        return returnValue;
    }

    public int evaluate(Difference difference) {
        int returnValue = RETURN_ACCEPT_DIFFERENCE;
        if (differenceListenerDelegate != null) {
            returnValue = differenceListenerDelegate.differenceFound(difference);
        }
        return returnValue;
    }

    /**
     * DifferenceListener implementation. If the
     * {@link Diff#overrideDifferenceListener overrideDifferenceListener} method
     * has been called then the call will be delegated otherwise a message is
     * printed to <code>System.err</code>.
     * 
     * @param control
     * @param test
     */
    public void skippedComparison(Node control, Node test) {
        if (differenceListenerDelegate != null) {
            differenceListenerDelegate.skippedComparison(control, test);
        } else {
            System.err.println("DifferenceListener.skippedComparison: "
                    + "unhandled control node type=" + control
                    + ", unhandled test node type=" + test);
        }
    }

    /**
     * ComparisonController implementation.
     * 
     * @param afterDifference
     * @return true if the difference is not recoverable and the comparison
     *         should be halted, or false if the difference is recoverable and
     *         the comparison can continue
     */
    public boolean haltComparison(Difference afterDifference) {
        return haltComparison;
    }

    /**
     * Append the message from the result of this Diff instance to a specified
     * StringBuffer
     * 
     * @param toAppendTo
     * @return specified StringBuffer with message appended
     */
    public StringBuffer appendMessage(StringBuffer toAppendTo) {
        compare();
        if (messages.length() == 0) {
            messages.append("[identical]");
        }
        // fix for JDK1.4 backwards incompatibility
        return toAppendTo.append(messages.toString());
    }

    /**
     * Get the result of this Diff instance as a String
     * 
     * @return result of this Diff
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getName());
        appendMessage(buf);
        return buf.toString();
    }

    /**
     * Override the <code>DifferenceListener</code> used to determine how to
     * handle differences that are found.
     * 
     * @param delegate
     *            the DifferenceListener instance to delegate handling to.
     */
    public void overrideDifferenceListener(DifferenceListener delegate) {
        this.differenceListenerDelegate = delegate;
    }

    /**
     * Override the <code>ElementQualifier</code> used to determine which
     * control and test nodes are comparable for this difference comparison.
     * 
     * @param delegate
     *            the ElementQualifier instance to delegate to.
     */
    public void overrideElementQualifier(ElementQualifier delegate) {
        this.elementQualifierDelegate = delegate;
    }

    /**
     * Override the <code>MatchTracker</code> used to track successfully matched
     * nodes.
     * 
     * @param delegate
     *            the MatchTracker instance to delegate handling to.
     */
    public void overrideMatchTracker(MatchTracker delegate) {
        this.matchTrackerDelegate = delegate;
        if (differenceEngine != null) {
            differenceEngine.setMatchTracker(delegate);
        }
    }

    /**
     * Lazily initializes the difference engine if it hasn't been set via a
     * constructor.
     */
    private DifferenceEngineContract getDifferenceEngine() {
        if (differenceEngine != null) {
            return differenceEngine;
        }

        if (properties.getIgnoreAttributeOrder()
                &&
                (!usesUnknownElementQualifier()
                || properties.getCompareUnmatched())) {
            return new NewDifferenceEngine(properties, this, matchTrackerDelegate);
        }
        return new DifferenceEngine(properties, this, matchTrackerDelegate);
    }

    private boolean usesUnknownElementQualifier() {
        return elementQualifierDelegate != null
                && !(elementQualifierDelegate instanceof ElementNameQualifier)
                && !(elementQualifierDelegate instanceof ElementNameAndTextQualifier)
                && !(elementQualifierDelegate instanceof ElementNameAndAttributeQualifier);
    }

    public static DiffBuilder newDiff(@Nullable XmlUnitProperties properties) {
        return new DiffBuilder(properties);
    }

    public static class DiffBuilder {
        private XmlUnitProperties properties;
        private final DocumentUtils documentUtils;

        private Document testDocument;
        private Document controlDocument;

        private DifferenceEngineContract differenceEngineContract = null;
        private ElementQualifier elementQualifier = new ElementNameQualifier();

        public DiffBuilder(@Nullable XmlUnitProperties properties) {
            if (properties == null) {
                this.properties = new XmlUnitProperties();
            } else {
                this.properties = properties.clone();
            }

            documentUtils = new DocumentUtils(this.properties);
        }

        private Document prepareDocumentFrom(InputSource inputSource, DocumentBuilder parser) throws BuilderException {
            try {
                Document document = documentUtils.buildDocument(parser, inputSource);
                return document;
            } catch (IOException e) {
                throw new BuilderException("Failed to build Diff!", e);
            } catch (SAXException e) {
                throw new BuilderException("Failed to build Diff!", e);
            }
        }

        private Document prepareDocumentFrom(Reader reader, DocumentBuilder parser) throws BuilderException {
            try {
                Document document = documentUtils.buildDocument(parser, reader);
                return document;
            } catch (ConfigurationException e) {
                throw new BuilderException("Failed to build Diff!", e);
            } catch (SAXException e) {
                throw new BuilderException("Failed to build Diff!", e);
            } catch (IOException e) {
                throw new BuilderException("Failed to build Diff!", e);
            }
        }

        public DiffTestDocBuilder betweenControlDocument(Document controlDoc) {
            if (controlDoc == null) {
                throw new IllegalArgumentException("Document cannot be null");
            }
            this.controlDocument = controlDoc;
            return new DiffTestDocBuilder();
        }

        public DiffTestDocBuilder betweenControlDocument(DOMSource controlDomSource) {
            if (controlDomSource == null) {
                throw new IllegalArgumentException("DOMSource cannot be null");
            }
            this.controlDocument = controlDomSource.getNode().getOwnerDocument();
            return new DiffTestDocBuilder();
        }

        public DiffTestDocBuilder betweenControlDocument(InputSource controlInputSource) throws BuilderException {
            if (controlInputSource == null) {
                throw new IllegalArgumentException("InputSource cannot be null");
            }

            this.controlDocument = prepareDocumentFrom(controlInputSource, documentUtils.newControlDocumentBuilder());
            return new DiffTestDocBuilder();
        }

        public DiffTestDocBuilder betweenControlDocument(String controlDocString) throws BuilderException {
            if (controlDocString == null) {
                throw new IllegalArgumentException("String cannot be null");
            }
            this.controlDocument = prepareDocumentFrom(new StringReader(controlDocString),
                    documentUtils.newControlDocumentBuilder());
            return new DiffTestDocBuilder();
        }

        public DiffTestDocBuilder betweenControlDocument(Reader controlDocReader) throws BuilderException {
            if (controlDocReader == null) {
                throw new IllegalArgumentException("Reader cannot be null");
            }
            this.controlDocument = prepareDocumentFrom(controlDocReader, documentUtils.newControlDocumentBuilder());
            return new DiffTestDocBuilder();
        }

        private void validate() throws BuilderException {
            if (controlDocument == null) {
                throw new BuilderException("Control document must be provided!");
            }
            if (testDocument == null) {
                throw new BuilderException("Test document must be provided!");
            }
            if (elementQualifier == null) {
                throw new BuilderException("Element qualifier cannot be null!");
            }
        }

        public class DiffTestDocBuilder {

            private DiffTestDocBuilder() {
            }

            public DiffPropertiesBuilder andTestDocument(Document testDoc) throws BuilderException {
                if (testDoc == null) {
                    throw new IllegalArgumentException("Document cannot be null");
                }
                testDocument = testDoc;
                return new DiffPropertiesBuilder();
            }

            public DiffPropertiesBuilder andTestDocument(DOMSource testDomSource) throws BuilderException {
                if (testDomSource == null) {
                    throw new IllegalArgumentException("DOMSource cannot be null");
                }
                testDocument = testDomSource.getNode().getOwnerDocument();
                return new DiffPropertiesBuilder();
            }

            public DiffPropertiesBuilder andTestDocument(InputSource testDomSource) throws BuilderException {
                if (testDomSource == null) {
                    throw new IllegalArgumentException("InputSource cannot be null");
                }

                testDocument = prepareDocumentFrom(testDomSource, documentUtils.newTestDocumentBuilder());
                return new DiffPropertiesBuilder();
            }

            public DiffPropertiesBuilder andTestDocument(String testDocString) throws BuilderException {
                if (testDocString == null) {
                    throw new IllegalArgumentException("String cannot be null");
                }
                testDocument = prepareDocumentFrom(new StringReader(testDocString),
                        documentUtils.newTestDocumentBuilder());
                return new DiffPropertiesBuilder();
            }

            public DiffPropertiesBuilder andTestDocument(Reader testDocReader) throws BuilderException {
                if (testDocReader == null) {
                    throw new IllegalArgumentException("Reader cannot be null");
                }
                testDocument = prepareDocumentFrom(testDocReader, documentUtils.newTestDocumentBuilder());
                return new DiffPropertiesBuilder();
            }

        }

        public class DiffPropertiesBuilder implements Builder<Diff> {

            private DiffPropertiesBuilder() {
            }

            public DiffPropertiesBuilder usingDifferenceEngineContract(@Nullable DifferenceEngineContract contract) {
                differenceEngineContract = contract;
                return this;
            }

            public DiffPropertiesBuilder withElementQualifier(ElementQualifier qualifier) {
                if (elementQualifier == null) {
                    throw new IllegalArgumentException("ElementQualifier cannot be null");
                }
                elementQualifier = qualifier;
                return this;
            }

            public Diff build() throws BuilderException {
                validate();
                return new Diff(DiffBuilder.this);
            }

        }
    }

}
