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

package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.io.IO;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

/**
 * A {@link File} that can be written to disk.
 */
public class FileOnDisk extends WritableOnDisk implements File<WritableOnDisk> {

  private byte[] bytes;

  public FileOnDisk(String name, byte[] bytes) {
    super(name);
    this.bytes = bytes;
  }

  /**
   * Creates a {@link java.io.File} with name {@link Writable#getName} on disk and writes the {@link File#getBytes
   * bytes} of this {@link File} into that {@link java.io.File}.
   */
  @Override
  public void write() {
    IO.makeNewFile(getRoot(), this.getName());
    IO.writeBytesToFile(this.getBytes(), this.getFileOnDisk());
  }

  protected java.io.File getRoot() {
    return ((WritableOnDisk) this.getParent()).getFileOnDisk();
  }

  @Override
  public byte[] getBytes() {
    return this.bytes;
  }

  @Override
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Does nothing.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    // Method override exists here to comply with the implementation rules for the Writable interface.
  }

  public byte[] getBytesForChecksum() {
    return this.getBytes();
  }
}
