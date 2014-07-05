/*
 ******************************************************************
Copyright (c) 200, Jeff Martin, Tim Bacon
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

package org.custommonkey.xmlunit.jaxp13;

import static javax.xml.XMLConstants.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class NamespaceContext2Jaxp13Test {
    private static final List<String> STANDARD_PREFIXES =
            ImmutableList.of(XML_NS_PREFIX, XMLNS_ATTRIBUTE);
    private static final List<String> STANDARD_URIS =
            ImmutableList.of(XML_NS_URI, XMLNS_ATTRIBUTE_NS_URI);

    private static final List<String> PREFIXES = ImmutableList.of("foo", "bar");
    private static final String URI = "urn:example";

    private Map<String, String> nsMap;

    @Before
    public void setUp() {
        nsMap = new HashMap<String, String>();
        for (String prefix : PREFIXES) {
            nsMap.put(prefix, URI);
        }
    }

    private NamespaceContext2Jaxp13 createContext(Map<String, String> nsMap) {
        return new NamespaceContext2Jaxp13(new SimpleNamespaceContext(nsMap));
    }

    @Test
    public void testBasics() {
        NamespaceContext2Jaxp13 ctx = createContext(nsMap);
        assertContextStructure(ctx);
    }

    @Test
    public void should_not_override_standard_prefixes() {
        // given
        for (String prefix : STANDARD_PREFIXES) {
            nsMap.put(prefix, URI);
        }
        NamespaceContext2Jaxp13 ctx = createContext(nsMap);

        // then
        assertContextStructure(ctx);
    }

    @Test
    public void should_not_override_standard_uris() {
        // given
        for (int i = 0; i < STANDARD_PREFIXES.size(); i++) {
            nsMap.put(STANDARD_PREFIXES.get(i) + "1", STANDARD_URIS.get(i));
        }
        NamespaceContext2Jaxp13 ctx = createContext(nsMap);

        // then
        assertContextStructure(ctx);
    }

    @Test
    public void default_prefix_always_should_be_first() {
        // given
        nsMap.put(DEFAULT_NS_PREFIX, URI);
        NamespaceContext2Jaxp13 ctx = createContext(nsMap);

        // then

        // no matter how many prefixes map to it, DEFAULT_NS must be
        // the first prefix
        assertThat(ctx.getPrefix(URI)).isEqualTo(DEFAULT_NS_PREFIX);

        Iterator<String> it = ctx.getPrefixes(URI);
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(DEFAULT_NS_PREFIX);
        assertAllPrefixesFound(it);
    }

    private void assertContextStructure(NamespaceContext2Jaxp13 ctx) {
        for (String prefix : PREFIXES) {
            assertThat(ctx.getNamespaceURI(prefix)).isEqualTo(URI);
        }
        for (int i = 0; i < STANDARD_PREFIXES.size(); i++) {
            assertThat(ctx.getNamespaceURI(STANDARD_PREFIXES.get(i))).isEqualTo(STANDARD_URIS.get(i));
        }
        assertThat(ctx.getNamespaceURI(PREFIXES.get(0) + PREFIXES.get(0))).isEqualTo(NULL_NS_URI);
        assertThat(ctx.getNamespaceURI(DEFAULT_NS_PREFIX)).isEqualTo(NULL_NS_URI);
        assertThat(PREFIXES).contains(ctx.getPrefix(URI));

        for (int i = 0; i < STANDARD_PREFIXES.size(); i++) {
            assertEquals(STANDARD_PREFIXES.get(i), ctx.getPrefix(STANDARD_URIS.get(i)));
        }

        assertAllPrefixesFound(ctx.getPrefixes(URI));
        for (int i = 0; i < STANDARD_PREFIXES.size(); i++) {
            Iterator<String> it = ctx.getPrefixes(STANDARD_URIS.get(i));
            assertThat(iteratorToList(it)).containsExactly(STANDARD_PREFIXES.get(i));
        }

        assertNull(ctx.getPrefix(URI + URI));
        assertFalse(ctx.getPrefixes(URI + URI).hasNext());
    }

    private void assertAllPrefixesFound(Iterator<String> it) {
        List<String> prefixes = iteratorToList(it);
        assertThat(PREFIXES).containsAll(prefixes);
        assertThat(prefixes).containsAll(PREFIXES);
    }

    private List<String> iteratorToList(Iterator<String> it) {
        return Lists.newArrayList(Iterators.toArray(it, String.class));
    }
}
