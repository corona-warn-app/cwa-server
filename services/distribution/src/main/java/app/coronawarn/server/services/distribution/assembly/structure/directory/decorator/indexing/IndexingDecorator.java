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

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

/**
 * An {@link IndexDirectoryDecorator} that writes a file called {@code "index"}, containing a JSON array containing all
 * elements returned by {@link IndexDirectoryOnDisk#getIndex}, formatted with the {@link
 * IndexDirectoryOnDisk#getIndexFormatter} on {@link Writable#prepare}.
 */
public interface IndexingDecorator<T, W extends Writable<W>> extends IndexDirectory<T, W> {

  /**
   * Returns the file containing the index.
   */
  File<W> getIndexFile(String indexFileName, ImmutableStack<Object> indices);

}
