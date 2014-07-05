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
package org.xmlunit.util;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class PairTest {
    @Test
    public void should_be_equal() {
        // given
        String a = "a";
        String b = "b";

        // when
        Pair<String> firstPair = Pair.of(a, b);
        Pair<String> secondPair = Pair.of(a, b);

        // then
        assertThat(firstPair).isEqualTo(secondPair);
        assertThat(firstPair.hashCode()).isEqualTo(secondPair.hashCode());
        assertThat(firstPair.getFirst()).isEqualTo(secondPair.getFirst());
        assertThat(firstPair.getSecond()).isEqualTo(secondPair.getSecond());
    }

    @Test
    public void should_be_unequal() {
        // given
        int a = 0;
        int b = 1;

        // when
        Pair<Integer> firstPair = Pair.of(a, b);
        Pair<Integer> secondPair = Pair.of(b, a);

        // then
        assertThat(firstPair).isNotEqualTo(secondPair);
        assertThat(firstPair.hashCode()).isNotEqualTo(secondPair.hashCode());
        assertThat(firstPair.getFirst()).isEqualTo(secondPair.getSecond());
        assertThat(firstPair.getSecond()).isEqualTo(secondPair.getFirst());
    }

    @Test
    public void should_extract_pair_elements_from_collection() {
        // given
        double a1 = 1;
        double a2 = 2;
        double a3 = 3;

        double b1 = 9;
        double b2 = 8;
        double b3 = 7;

        // when
        List<Pair<Double>> pairs = new LinkedList<Pair<Double>>();
        pairs.add(Pair.of(a1, b1));
        pairs.add(Pair.of(a2, b2));
        pairs.add(Pair.of(a3, b3));

        List<Double> firstElements = Pair.getFirstElements(pairs);
        List<Double> secondElements = Pair.getSecondElements(pairs);

        // then
        assertThat(firstElements).hasSize(3);
        assertThat(secondElements).hasSize(3);
        assertThat(firstElements).containsSequence(a1, a2, a3);
        assertThat(secondElements).containsSequence(b1, b2, b3);

    }
}
