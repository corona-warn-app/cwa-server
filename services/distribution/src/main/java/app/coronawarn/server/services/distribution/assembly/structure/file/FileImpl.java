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

package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.io.IO;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

/**
 * Implementation of {@link File} that interfaces with {@link java.io.File Files} on disk.
 */
public class FileImpl extends WritableImpl implements File {

  private byte[] bytes;

  public FileImpl(String name, byte[] bytes) {
    super(name);
    this.bytes = bytes;
  }

  /**
   * Creates a {@link java.io.File} with name {@link Writable#getName} on disk and writes the {@link File#getBytes
   * bytes} of this {@link File} into the {@link java.io.File} to disk.
   */
  @Override
  public void write() {
    IO.makeNewFile(this.getParent().getFileOnDisk(), this.getName());
    IO.writeBytesToFile(this.getBytes(), this.getFileOnDisk());
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
  }
}
