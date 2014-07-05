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
package org.xmlunit.transform;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.transform.TransformerFactory;

import org.junit.Test;

public class TransformerFactoryUtilsTest {
    @Test
    public void should_create_two_separate_transformer_factories() throws Exception {
        // given
        TransformerFactoryUtils transformerFactoryUtils = new TransformerFactoryUtils();
        TransformerFactory factoryBefore = transformerFactoryUtils.newTransformerFactory();
        Class<? extends TransformerFactory> factoryClass = factoryBefore.getClass();

        // when
        transformerFactoryUtils.setTransformerFactoryClass(factoryClass);
        TransformerFactory factoryAfter = transformerFactoryUtils.newTransformerFactory();

        // then
        assertThat(factoryBefore).isNotSameAs(factoryAfter);
        assertThat(factoryAfter.getClass().getName()).isEqualTo(factoryClass.getName());
    }
}
