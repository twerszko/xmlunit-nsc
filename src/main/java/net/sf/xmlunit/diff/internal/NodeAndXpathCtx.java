/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package net.sf.xmlunit.diff.internal;

import net.sf.xmlunit.diff.XPathContext;

import org.w3c.dom.Node;

public class NodeAndXpathCtx<T extends Node> extends NodeAndXpath<T> {
    private final XPathContext xpathCtx;

    public NodeAndXpathCtx(T node, XPathContext xpathCtx) {
        super(node, getXPath(xpathCtx));
        this.xpathCtx = xpathCtx;
    }

    public XPathContext getXpathCtx() {
        return xpathCtx;
    }

    protected static String getXPath(XPathContext ctx) {
        return ctx == null ? null : ctx.getXPath();
    }

    public static <T extends Node> NodeAndXpathCtx<T> from(T node, XPathContext xpathCtx) {
        return new NodeAndXpathCtx<T>(node, xpathCtx);
    }

    public static <T extends Node> NodeAndXpathCtx<T> from(T node) {
        return new NodeAndXpathCtx<T>(node, new XPathContext());
    }

}
