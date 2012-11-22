/*
 ******************************************************************
Copyright (c) 2001-2007, Jeff Martin, Tim Bacon
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

import java.util.Arrays;

import net.sf.xmlunit.diff.ElementSelector;
import net.sf.xmlunit.diff.ElementSelectors;

import org.custommonkey.xmlunit.diff.Diff;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * More complex interface implementation that tests two elements for tag name
 * and attribute name comparability. <br />
 * Examples and more at <a
 * href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 * 
 * @see DifferenceEngine#compareNodeList(NodeList, NodeList, int,
 *      DifferenceListener, ElementQualifier)
 * @see Diff#overrideElementSelector(ElementQualifier)
 */
public class ElementNameAndAttributeSelector extends ElementNameSelector {
	private final ElementSelector selector;
	private static final String[] ALL_ATTRIBUTES = { "*" };

	private final String[] qualifyingAttrNames;

	/**
	 * No-args constructor: use all attributes from all elements to determine
	 * whether elements qualify for comparability
	 */
	public ElementNameAndAttributeSelector() {
		this(ALL_ATTRIBUTES);
	}

	/**
	 * Simple constructor for a single qualifying attribute name
	 * 
	 * @param attrName
	 *            the value to use to qualify whether two elements can be
	 *            compared further for differences
	 */
	public ElementNameAndAttributeSelector(String attrName) {
		this(new String[] { attrName });
	}

	/**
	 * Extended constructor for multiple qualifying attribute names
	 * 
	 * @param attrNames
	 *            the array of values to use to qualify whether two elements can
	 *            be compared further for differences
	 */
	public ElementNameAndAttributeSelector(String[] attrNames) {
		this.qualifyingAttrNames = new String[attrNames.length];
		System.arraycopy(attrNames, 0, qualifyingAttrNames, 0,
		        attrNames.length);
		selector = matchesAllAttributes(attrNames)
		        ? ElementSelectors.byNameAndAllAttributes
		        : ElementSelectors.byNameAndAttributesControlNS(attrNames);
	}

	/**
	 * Determine whether two elements qualify for further Difference comparison.
	 * 
	 * @param differenceEngine
	 *            the DifferenceEngine instance wanting to determine if the
	 *            elements are comparable
	 * @param control
	 * @param test
	 * @return true if the two elements qualify for further comparison based on
	 *         both the superclass qualification (namespace URI and non-
	 *         namespaced tag name), and the presence of qualifying attributes
	 *         with the same values; false otherwise
	 */
	public boolean canBeCompared(Element control, Element test) {
		return selector.canBeCompared(control, test);
	}

	private static boolean matchesAllAttributes(String[] attributes) {
		return Arrays.equals(attributes, ALL_ATTRIBUTES);
	}
}
