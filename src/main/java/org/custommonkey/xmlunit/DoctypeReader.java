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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Adapts the marked-up content in a source Reader to specify that it conforms
 * to a different DTD. Combines Reader semantics with the ability to specify a
 * target doctype for a character stream containing XML markup. Used by
 * Validator class to wrap a Reader when performing validation of a document
 * against a DTD. <br />
 */
public class DoctypeReader extends Reader {

    private final Reader originalReader;
    private final StringBuffer sourceBuffer = new StringBuffer(1024);

    private final DoctypeSupport support;

    /**
     * Create a Reader whose XML content is provided by the originalSource with
     * the exception of the DOCTYPE which is provided by the doctypeName and
     * systemID.
     */
    public DoctypeReader(Reader originalSource, String doctypeName, String systemID) {
        originalReader = new BufferedReader(originalSource);
        support = new DoctypeSupport(doctypeName, systemID,
                new DoctypeSupport.Readable() {
                    public int read() throws IOException {
                        return originalReader.read();
                    }
                },
                true, null);
    }

    /**
     * @return the content of the original source, without amendments or
     *         substitutions. Safe to call multiple times.
     * @throws IOException
     *             if thrown while reading from the original source
     */
    protected String getContent() throws IOException {
        return getContent(originalReader).toString();
    }

    /**
     * @return the contents of the originalSource within a StringBuffer
     * @throws IOException
     *             if thrown while reading from the original source
     */
    private StringBuffer getContent(Reader originalSource) throws IOException {
        if (sourceBuffer.length() == 0) {
            BufferedReader bufferedReader;
            if (originalSource instanceof BufferedReader) {
                bufferedReader = (BufferedReader) originalSource;
            } else {
                bufferedReader = new BufferedReader(originalSource);
            }
            String newline = System.getProperty("line.separator");
            String source;
            boolean atFirstLine = true;
            while ((source = bufferedReader.readLine()) != null) {
                if (atFirstLine) {
                    atFirstLine = false;
                } else {
                    sourceBuffer.append(newline);
                }
                sourceBuffer.append(source);
            }

            bufferedReader.close();
        }

        return sourceBuffer;
    }

    /**
     * Read DOCTYPE-replaced content from the wrapped Reader
     * 
     * @return The number of characters read, or -1 if the end of the stream has
     *         been reached
     * @throws IOException
     */
    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        int startPos = off;
        int currentlyRead;
        while (off - startPos < len && (currentlyRead = read()) != -1) {
            cbuf[off++] = (char) currentlyRead;
        }
        return off == startPos && len != 0 ? -1 : off - startPos;
    }

    /**
     * Read DOCTYPE-replaced content from the wrapped Reader
     */
    @Override
    public int read() throws IOException {
        return support.read();
    }

    @Override
    public void close() throws IOException {
        originalReader.close();
    }
}
