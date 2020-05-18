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

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Set;

/**
 * Decorates a {@link Directory} (e.g. to modify its files, subdirectories, etc.) on {@link Writable#prepare}. This
 * class proxies all function calls to the {@link Directory} it contains.
 */
public abstract class DirectoryDecorator implements Directory {

  private final Directory directory;

  protected DirectoryDecorator(Directory directory) {
    this.directory = directory;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.directory.prepare(indices);
  }

  @Override
  public void addFile(File file) {
    this.directory.addFile(file);
  }

  @Override
  public Set<File> getFiles() {
    return this.directory.getFiles();
  }

  @Override
  public void addDirectory(Directory directory) {
    this.directory.addDirectory(directory);
  }

  @Override
  public Set<Directory> getDirectories() {
    return this.directory.getDirectories();
  }

  @Override
  public void write() {
    this.directory.write();
  }

  @Override
  public String getName() {
    return this.directory.getName();
  }

  @Override
  public Directory getParent() {
    return this.directory.getParent();
  }

  @Override
  public void setParent(Directory parent) {
    this.directory.setParent(parent);
  }

  @Override
  public java.io.File getFileOnDisk() {
    return this.directory.getFileOnDisk();
  }
}
