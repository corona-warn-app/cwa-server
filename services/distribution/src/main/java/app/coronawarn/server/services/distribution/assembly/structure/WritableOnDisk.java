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

package app.coronawarn.server.services.distribution.assembly.structure;

import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import java.util.Objects;

/**
 * A {@link Writable} that can be written to disk.
 */
public abstract class WritableOnDisk implements Writable<WritableOnDisk> {

  private String name;
  private Directory<WritableOnDisk> parent;
  private java.io.File fileOnDisk;

  protected WritableOnDisk(String name) {
    this.name = name;
  }

  protected WritableOnDisk(java.io.File fileOnDisk) {
    this.fileOnDisk = fileOnDisk;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Directory<WritableOnDisk> getParent() {
    return this.parent;
  }

  @Override
  public void setParent(Directory<WritableOnDisk> parent) {
    this.parent = parent;
  }

  /**
   * Returns the {@link java.io.File} that this {@link Writable} represents on disk.
   */
  public java.io.File getFileOnDisk() {
    return Objects.requireNonNullElseGet(this.fileOnDisk,
        () -> ((WritableOnDisk) this.getParent()).getFileOnDisk().toPath().resolve(this.getName()).toFile());
  }

  @Override
  public boolean isFile() {
    return this instanceof File;
  }

  @Override
  public boolean isDirectory() {
    return this instanceof Directory;
  }

  @Override
  public boolean isArchive() {
    return this instanceof Archive;
  }
}
