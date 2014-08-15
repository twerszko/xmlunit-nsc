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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Pair<T> {
    private final T first;
    private final T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public static <T> Pair<T> of(T first, T second) {
        return new Pair<T>(first, second);
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public static <T> List<T> getFirstElements(Collection<Pair<T>> pairs) {
        List<T> firstElements = new LinkedList<T>();
        for (Pair<T> pair : pairs) {
            firstElements.add(pair.getFirst());
        }

        return firstElements;
    }

    public static <T> List<T> getSecondElements(Collection<Pair<T>> pairs) {
        List<T> secondElements = new LinkedList<T>();
        for (Pair<T> pair : pairs) {
            secondElements.add(pair.getSecond());
        }

        return secondElements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?> other = (Pair<?>) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Pair [first=" + first + ", second=" + second + "]";
    }
}
