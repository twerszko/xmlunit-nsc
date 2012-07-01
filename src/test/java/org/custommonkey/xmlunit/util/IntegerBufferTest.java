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

package org.custommonkey.xmlunit.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IntegerBufferTest {

    @Test
    public void should_get_empty_int_array() {
        // given
        // when
        int[] intArray = new IntegerBuffer().toIntArray();

        // then
        assertNotNull(intArray);
        assertThat(intArray.length, is(equalTo(0)));
    }

    @Test
    public void should_append_to_empty_buffer() {
        // given
        IntegerBuffer buffer = new IntegerBuffer();

        // when
        buffer.append(1);

        // then
        assertNotNull(buffer.toIntArray());
        assertThat(buffer.toIntArray().length, is(equalTo(1)));
        assertThat(buffer.toIntArray()[0], is(equalTo(1)));
    }

    @Test
    public void should_append_array_with_one_element_to_empty_buffer() {
        // given
        IntegerBuffer buffer = new IntegerBuffer();

        // when
        buffer.append(new int[] { 1, 2 });
        int[] intArray = buffer.toIntArray();

        // then
        assertNotNull(intArray);
        assertThat(intArray.length, is(equalTo(2)));
        assertThat(intArray[0], is(equalTo(1)));
        assertThat(intArray[1], is(equalTo(2)));
    }

    @Test
    public void should_append_two_elements_to_buffer() {
        // given
        IntegerBuffer buffer = new IntegerBuffer(1);

        // when
        buffer.append(0);
        buffer.append(1);
        int[] intArray = buffer.toIntArray();

        // then
        assertNotNull(intArray);
        assertEquals(2, intArray.length);
        assertThat(intArray[0], is(equalTo(0)));
        assertThat(intArray[1], is(equalTo(1)));
    }

    @Test
    public void should_append_array_to_empty_buffer() {
        // given
        IntegerBuffer buffer = new IntegerBuffer(1);

        // when
        buffer.append(new int[] { 1, 2 });

        // then
        assertNotNull(buffer.toIntArray());
        assertEquals(2, buffer.toIntArray().length);
        assertThat(buffer.toIntArray()[0], is(equalTo(1)));
        assertThat(buffer.toIntArray()[1], is(equalTo(2)));
    }

    @Test
    public void should_get_size() {
        IntegerBuffer buffer = new IntegerBuffer();
        assertThat(buffer.size(), is(equalTo(0)));
        buffer.append(0);
        assertThat(buffer.size(), is(equalTo(1)));
        buffer.append(new int[] { 1, 2 });
        assertThat(buffer.size(), is(equalTo(3)));
    }

    @Test
    public void should_test_capacity() {
        IntegerBuffer buffer = new IntegerBuffer(1);
        assertThat(buffer.capacity(), is(equalTo(1)));
        buffer.append(0);
        assertThat(buffer.capacity(), is(equalTo(1)));
        buffer.append(0);
        assertThat(buffer.capacity(), is(greaterThan(1)));
    }

    @Test
    public void should_get_index_of_array() {
        IntegerBuffer buffer = new IntegerBuffer();
        int[] testArray = new int[] { 1, 2, 3 };
        assertThat(buffer.indexOf(testArray), is(equalTo(-1)));
        buffer.append(testArray);
        assertThat(buffer.indexOf(testArray), is(equalTo(0)));
        buffer.append(testArray);
        assertThat(buffer.indexOf(testArray), is(equalTo(0)));
    }

    @Test
    public void should_get_index_of_array_2() {
        IntegerBuffer buffer = new IntegerBuffer();
        int[] testArray = new int[] { 1, 2, 3 };
        buffer.append(0);
        assertThat(buffer.indexOf(testArray), is(equalTo(-1)));
        buffer.append(testArray);
        assertThat(buffer.indexOf(testArray), is(equalTo(1)));
    }

    @Test
    public void should_get_index_of_array_when_first_element_is_repeated() {
        IntegerBuffer buffer = new IntegerBuffer();
        int[] testArray = new int[] { 1, 2, 3 };
        buffer.append(1);
        assertThat(buffer.indexOf(testArray), is(equalTo(-1)));
        buffer.append(testArray);
        assertThat(buffer.indexOf(testArray), is(equalTo(1)));
    }

    @Test
    public void should_get_index_of_subsequence() {
        IntegerBuffer buffer = new IntegerBuffer();
        int[] testArray = new int[] { 1, 2, 3 };
        buffer.append(new int[] { 1, 2 });
        buffer.append(4);
        assertThat(buffer.indexOf(testArray), is(equalTo(-1)));
    }

    @Test
    public void should_test_all_bytes() {
        IntegerBuffer buffer = new IntegerBuffer();
        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
            buffer.append(b);
        }
        buffer.append(Byte.MAX_VALUE);
        int[] intArray = buffer.toIntArray();
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            assertEquals((byte) i, intArray[i + Math.abs(Byte.MIN_VALUE)]);
        }
    }

    public void should_test_all_chars() {
        IntegerBuffer buffer = new IntegerBuffer();
        for (char c = Character.MIN_VALUE; c < Character.MAX_VALUE; c++) {
            buffer.append(c);
        }
        buffer.append(Character.MAX_VALUE);
        int[] is = buffer.toIntArray();
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            assertEquals((char) i, is[i + Math.abs(Character.MIN_VALUE)]);
        }
    }
}
