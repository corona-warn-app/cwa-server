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

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritablesContainerImpl;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

/**
 * Implementation of {@link Directory} that interfaces with {@link java.io.File Files} on disk.
 */
public class DirectoryImpl extends WritablesContainerImpl implements Directory {

  /**
   * A root {@link DirectoryImpl} representing an already existing directory on disk.
   *
   * @param file The {@link File File} that this {@link DirectoryImpl} represents on disk.
   */
  public DirectoryImpl(java.io.File file) {
    super(file);
  }

  /**
   * A {@link DirectoryImpl} that does not yet represent an already existing directory on disk, but
   * one that shall be created on disk when calling {@link DirectoryImpl#write}. A parent needs to
   * be defined by calling {@link DirectoryImpl#setParent}, before writing can succeed.
   *
   * @param name The name that this directory should have on disk.
   */
  public DirectoryImpl(String name) {
    super(name);
  }

  /**
   * Delegates the {@link Writable#prepare} call to all contained {@link
   * DirectoryImpl#getWritables()} writables}.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.getWritables().forEach(writable -> writable.prepare(indices));
  }

  /**
   * Writes this {@link DirectoryImpl} and all of its {@link DirectoryImpl#getWritables()}
   * writables} to disk.
   */
  @Override
  public void write() {
    this.writeSelf();
    this.writeContainedWritables();
  }

  private void writeSelf() {
    java.io.File file = this.getFileOnDisk();
    file.mkdirs();
  }

  private void writeContainedWritables() {
    this.getWritables().forEach(Writable::write);
  }
}
