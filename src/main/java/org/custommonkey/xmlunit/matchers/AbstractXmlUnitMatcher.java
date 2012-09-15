//  Copyright 2012 Tomasz Werszko
//		
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//	
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.

package org.custommonkey.xmlunit.matchers;

import javax.annotation.Nullable;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XmlUnitBuilder;
import org.hamcrest.TypeSafeMatcher;

public abstract class AbstractXmlUnitMatcher<T> extends TypeSafeMatcher<T> {
    protected XMLUnit xmlUnit;

    public AbstractXmlUnitMatcher<T> using(@Nullable XMLUnit xmlUnit) {
        this.xmlUnit = xmlUnit;
        return this;
    }

    protected XMLUnit getXmlUnit() {
        XMLUnit xmlUnit;
        if (this.xmlUnit == null) {
            xmlUnit = new XmlUnitBuilder().build();
        } else {
            xmlUnit = this.xmlUnit;
        }
        return xmlUnit;
    }

    protected String quote(String string) {
        return "\"" + string + "\"";
    }
}
