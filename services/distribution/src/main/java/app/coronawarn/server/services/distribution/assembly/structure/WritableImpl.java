/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import java.util.Objects;

public abstract class WritableImpl implements Writable {

  private String name;
  private Directory parent;
  private java.io.File fileOnDisk;

  protected WritableImpl(String name) {
    this.name = name;
  }

  protected WritableImpl(java.io.File fileOnDisk) {
    this.fileOnDisk = fileOnDisk;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Directory getParent() {
    return this.parent;
  }

  @Override
  public void setParent(Directory parent) {
    this.parent = parent;
  }

  @Override
  public java.io.File getFileOnDisk() {
    return Objects.requireNonNullElseGet(this.fileOnDisk,
        () -> getParent().getFileOnDisk().toPath().resolve(this.getName()).toFile());
  }
}
