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

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Set;

/**
 * Decorates a {@link Directory} (e.g. to modify its files, subdirectories, etc.) on {@link Writable#prepare}. This
 * class proxies all function calls to the {@link Directory} it decorates.
 */
public abstract class DirectoryDecorator<W extends Writable<W>> implements Directory<W> {

  private final Directory<W> directory;

  protected DirectoryDecorator(Directory<W> directory) {
    this.directory = directory;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.directory.prepare(indices);
  }

  @Override
  public void addWritable(Writable<W> writable) {
    this.directory.addWritable(writable);
  }

  @Override
  public Set<Writable<W>> getWritables() {
    return this.directory.getWritables();
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
  public Directory<W> getParent() {
    return this.directory.getParent();
  }

  @Override
  public void setParent(Directory<W> parent) {
    this.directory.setParent(parent);
  }

  @Override
  public boolean isFile() {
    return this.directory.isFile();
  }

  @Override
  public boolean isDirectory() {
    return this.directory.isDirectory();
  }

  @Override
  public boolean isArchive() {
    return this.directory.isArchive();
  }
}
