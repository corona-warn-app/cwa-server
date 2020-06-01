/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import static app.coronawarn.server.services.distribution.assembly.structure.util.functional.CheckedConsumer.uncheckedConsumer;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Directory} that can be written to disk.
 */
public class DirectoryOnDisk extends WritableOnDisk implements Directory<WritableOnDisk> {

  private final Set<Writable<WritableOnDisk>> writables = new HashSet<>();

  /**
   * A root {@link DirectoryOnDisk} representing an already existing directory on disk.
   *
   * @param file The {@link File File} that this {@link DirectoryOnDisk} represents on disk.
   */
  public DirectoryOnDisk(java.io.File file) {
    super(file);
  }

  /**
   * A {@link DirectoryOnDisk} that does not yet represent an already existing directory on disk, but one that shall be
   * created on disk when calling {@link DirectoryOnDisk#write}. A parent needs to be defined by calling {@link
   * DirectoryOnDisk#setParent}, before writing can succeed.
   *
   * @param name The name that this directory should have on disk.
   */
  public DirectoryOnDisk(String name) {
    super(name);
  }

  @Override
  public void addWritable(Writable<WritableOnDisk> writable) {
    this.writables.add(writable);
    writable.setParent(this);
  }

  @Override
  public Set<Writable<WritableOnDisk>> getWritables() {
    return this.writables;
  }

  /**
   * Delegates the {@link Writable#prepare} call to all contained {@link DirectoryOnDisk#getWritables()} writables}.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.getWritables().forEach(writable -> writable.prepare(indices));
  }

  /**
   * Writes this {@link DirectoryOnDisk} and all of its {@link DirectoryOnDisk#getWritables()} writables} to disk.
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
    this.getWritables().forEach(uncheckedConsumer(Writable::write));
  }
}
