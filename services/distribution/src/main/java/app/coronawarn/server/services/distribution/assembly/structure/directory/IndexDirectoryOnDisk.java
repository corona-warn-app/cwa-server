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

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.Formatter;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.IndexFunction;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.WritableFunction;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link IndexDirectory} that can be written to disk.
 *
 * @param <T> The type of the elements in the index.
 */
public class IndexDirectoryOnDisk<T> extends DirectoryOnDisk implements IndexDirectory<T, WritableOnDisk> {

  // Writables to be written into every directory created through the index
  private final Set<WritableFunction<WritableOnDisk>> metaWritables = new HashSet<>();
  private final IndexFunction<T> indexFunction;
  private final Formatter<T> indexFormatter;

  /**
   * Constructs a {@link IndexDirectoryOnDisk} instance that represents a directory, containing an index in the form of
   * sub directories.
   *
   * @param name           The name that this directory should have on disk.
   * @param indexFunction  An {@link IndexFunction} that calculates the index of this {@link IndexDirectoryOnDisk} from
   *                       a {@link ImmutableStack} of parent {@link IndexDirectoryOnDisk} indices. The top element of
   *                       the stack is from the closest {@link IndexDirectoryOnDisk} in the parent chain.
   * @param indexFormatter A {@link Formatter} used to format the directory name for each index element returned by the
   *                       {@link IndexDirectoryOnDisk#indexFunction}.
   */
  public IndexDirectoryOnDisk(String name, IndexFunction<T> indexFunction, Formatter<T> indexFormatter) {
    super(name);
    this.indexFunction = indexFunction;
    this.indexFormatter = indexFormatter;
  }

  @Override
  public Formatter<T> getIndexFormatter() {
    return this.indexFormatter;
  }

  @Override
  public Set<T> getIndex(ImmutableStack<Object> indices) {
    return this.indexFunction.apply(indices);
  }

  @Override
  public void addWritableToAll(WritableFunction<WritableOnDisk> writableFunction) {
    this.metaWritables.add(writableFunction);
  }

  /**
   * Creates a new subdirectory for every element of the {@link IndexDirectory#getIndex index} and writes all its
   * {@link IndexDirectory#addWritableToAll writables} to those. The respective element of the index will be pushed
   * onto the Stack for subsequent {@link Writable#prepare} calls.
   *
   * @param indices A {@link ImmutableStack} of parameters from all {@link IndexDirectory IndexDirectories} further up
   *                in the hierarchy. The Stack may contain different types, depending on the types {@code T} of {@link
   *                IndexDirectory IndexDirectories} further up in the hierarchy.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    this.prepareIndex(indices);
  }

  private void prepareIndex(ImmutableStack<Object> indices) {
    this.getIndex(indices).forEach(currentIndex -> {
      ImmutableStack<Object> newIndices = indices.push(currentIndex);
      DirectoryOnDisk subDirectory = makeSubDirectory(currentIndex);
      prepareMetaWritables(newIndices, subDirectory);
    });
  }

  private DirectoryOnDisk makeSubDirectory(T index) {
    DirectoryOnDisk subDirectory = new DirectoryOnDisk(this.indexFormatter.apply(index).toString());
    this.addWritable(subDirectory);
    return subDirectory;
  }

  private void prepareMetaWritables(ImmutableStack<Object> indices, DirectoryOnDisk target) {
    this.metaWritables.forEach(metaWritableFunction -> {
      Writable<WritableOnDisk> newWritable = metaWritableFunction.apply(indices);
      target.addWritable(newWritable);
      newWritable.prepare(indices);
    });
  }
}
