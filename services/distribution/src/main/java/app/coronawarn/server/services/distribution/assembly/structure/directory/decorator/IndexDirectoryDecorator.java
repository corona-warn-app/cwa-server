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
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.Formatter;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.WritableFunction;
import java.util.Set;

/**
 * Decorates an {@link IndexDirectory} (e.g. to modify its files, subdirectories, etc.) on {@link Writable#prepare}.
 * This class proxies all function calls to the {@link IndexDirectory} it decorates.
 */
public abstract class IndexDirectoryDecorator<T, W extends Writable<W>> extends DirectoryDecorator<W> implements
    IndexDirectory<T, W> {

  private final IndexDirectory<T, W> directory;

  protected IndexDirectoryDecorator(IndexDirectory<T, W> directory) {
    super(directory);
    this.directory = directory;
  }

  @Override
  public void addWritableToAll(WritableFunction<W> writableFunction) {
    this.directory.addWritableToAll(writableFunction);
  }

  @Override
  public Set<T> getIndex(ImmutableStack<Object> indices) {
    return this.directory.getIndex(indices);
  }

  @Override
  public Formatter<T> getIndexFormatter() {
    return this.directory.getIndexFormatter();
  }
}
