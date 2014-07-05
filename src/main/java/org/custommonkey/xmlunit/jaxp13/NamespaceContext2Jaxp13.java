/*
 ******************************************************************
Copyright (c) 2006-2007, Jeff Martin, Tim Bacon
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

import static java.util.Map.Entry;
import static javax.xml.XMLConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.XMLConstants;

import org.xmlunit.NamespaceContext;
import org.xmlunit.util.Preconditions;

/**
 * Adapts {@link NamespaceContext XMLUnit's NamespaceContext} to
 * {@link javax.xml.namespace.NamespaceContext JAXP 1.3's NamespaceContext}.
 */
public class NamespaceContext2Jaxp13 implements javax.xml.namespace.NamespaceContext {

    private final Map<String, String> nsMap;

    public NamespaceContext2Jaxp13(NamespaceContext ctx) {
        nsMap = convertToMap(ctx);
    }

    public String getNamespaceURI(String prefix) {
        Preconditions.checkArgument(prefix != null, "prefix must not be null");
        String uri = nsMap.get(prefix);
        if (uri == null) {
            uri = XMLConstants.NULL_NS_URI;
        }
        return uri;
    }

    public Iterator<String> getPrefixes(String uri) {
        Preconditions.checkArgument(uri != null, "uri must not be null");

        // ensure that the empty string comes out first when asked for
        // the default namespace URI's prefix
        TreeSet<String> set = new TreeSet<String>();
        for (Entry<String, String> entry : nsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (uri != null && uri.equals(value)) {
                set.add(key);
            }
        }
        return set.iterator();
    }

    public String getPrefix(String uri) {
        Iterator<String> i = getPrefixes(uri);
        return i.hasNext() ? i.next() : null;
    }

    public static Map<String, String> convertToMap(NamespaceContext ctx) {
        Map<String, String> map = new HashMap<String, String>();

        Iterator<String> it = ctx.getPrefixes();
        while (it.hasNext()) {
            String prefix = it.next();
            String uri = ctx.getNamespaceURI(prefix);
            // according to the Javadocs only the constants defined in
            // XMLConstants are allowed as prefixes for the following
            // two URIs
            if (!XML_NS_URI.equals(uri) && !XMLNS_ATTRIBUTE_NS_URI.equals(uri)) {
                map.put(prefix, uri);
            }
        }
        map.put(XML_NS_PREFIX, XML_NS_URI);
        map.put(XMLNS_ATTRIBUTE, XMLNS_ATTRIBUTE_NS_URI);
        return map;
    }
}
