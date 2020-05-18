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

package app.coronawarn.server.services.distribution.assembly.structure.file.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

/**
 * Decorates a {@link File} (e.g. to modify its content) on {@link Writable#prepare}. This class proxies all function
 * calls to the {@link File} it contains.
 */
public abstract class FileDecorator implements File {

  private final File file;

  protected FileDecorator(File file) {
    this.file = file;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.file.prepare(indices);
  }

  @Override
  public byte[] getBytes() {
    return this.file.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    this.file.setBytes(bytes);
  }

  @Override
  public void write() {
    this.file.write();
  }

  @Override
  public String getName() {
    return this.file.getName();
  }

  @Override
  public Directory getParent() {
    return this.file.getParent();
  }

  @Override
  public void setParent(Directory parent) {
    this.file.setParent(parent);
  }

  @Override
  public java.io.File getFileOnDisk() {
    return this.file.getFileOnDisk();
  }
}
