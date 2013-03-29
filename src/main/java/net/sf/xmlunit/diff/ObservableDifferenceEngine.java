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
package net.sf.xmlunit.diff;

import net.sf.xmlunit.util.Preconditions;

public abstract class ObservableDifferenceEngine implements DifferenceEngine {
    private final ComparisonListenerSupport listeners = new ComparisonListenerSupport();

    protected ComparisonListenerSupport getListeners() {
        return listeners;
    }

    @Override
    public void addComparisonListener(ComparisonListener l) {
        Preconditions.checkArgument(l != null, "listener must not be null");
        listeners.addComparisonListener(l);
    }

    @Override
    public void removeComparisonListener(ComparisonListener l) {
        Preconditions.checkArgument(l != null, "listener must not be null");
        listeners.removeComparisonListener(l);
    }

    @Override
    public void addMatchListener(ComparisonListener l) {
        Preconditions.checkArgument(l != null, "listener must not be null");
        listeners.addMatchListener(l);
    }

    @Override
    public void addDifferenceListener(ComparisonListener l) {
        Preconditions.checkArgument(l != null, "listener must not be null");
        listeners.addDifferenceListener(l);
    }
}
