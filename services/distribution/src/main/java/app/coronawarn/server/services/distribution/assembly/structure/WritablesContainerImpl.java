/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.structure;

import java.util.HashSet;
import java.util.Set;

public abstract class WritablesContainerImpl extends WritableImpl implements WritablesContainer {

  private final Set<Writable> writables = new HashSet<>();

  protected WritablesContainerImpl(String name) {
    super(name);
  }

  protected WritablesContainerImpl(java.io.File file) {
    super(file);
  }

  @Override
  public void addWritable(Writable writable) {
    this.writables.add(writable);
    writable.setParent(this);
  }

  @Override
  public Set<Writable> getWritables() {
    return this.writables;
  }
}
