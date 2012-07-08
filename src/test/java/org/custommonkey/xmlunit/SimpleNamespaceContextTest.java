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

package org.custommonkey.xmlunit;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimpleNamespaceContextTest {

	@Test
	public void should_check_if_empty_context_is_empty() {
		// given
		SimpleNamespaceContext emptyContext = SimpleNamespaceContext.EMPTY_CONTEXT;

		// when
		Iterator<String> prefixes = emptyContext.getPrefixes();

		// then
		assertThat(prefixes.hasNext()).isFalse();
	}

	@Test
	public void should_check_map_with_prefixes() {
		// given
		Map<String, String> prefixMap = Maps.newHashMap();
		String URI = "urn:example";
		prefixMap.put("foo", URI);
		prefixMap.put("bar", URI);

		// when
		SimpleNamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
		List<String> prefixes = Lists.newArrayList();
		Iterator<String> it = ctx.getPrefixes();
		while (it.hasNext()) {
			prefixes.add(it.next());
		}

		// then
		assertThat(ctx.getNamespaceURI("foo")).isEqualTo(URI);
		assertThat(ctx.getNamespaceURI("bar")).isEqualTo(URI);
		assertThat(prefixes).hasSize(2);
		assertThat(prefixes).contains("foo", "bar");
	}

	@Test
	public void should_check_if_prefix_map_is_copied_defensively() {
		// given
		Map<String, String> prefixMap = Maps.newHashMap();
		String URI = "urn:example";
		prefixMap.put("foo", URI);
		prefixMap.put("bar", URI);

		// when
		SimpleNamespaceContext ctx = new SimpleNamespaceContext(prefixMap);

		// change a mapping
		prefixMap.put("foo", URI + "foo");
		// add a new one
		prefixMap.put("foo" + "foo", URI);

		List<String> prefixes = Lists.newArrayList();
		Iterator<String> it = ctx.getPrefixes();
		while (it.hasNext()) {
			prefixes.add(it.next());
		}

		// then
		assertThat(ctx.getNamespaceURI("bar")).isEqualTo(URI);
		assertThat(ctx.getNamespaceURI("foo")).isEqualTo(URI);
		assertThat(prefixes).hasSize(2);
		assertThat(prefixes).contains("foo", "bar");
	}
}
