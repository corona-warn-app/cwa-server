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

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

public abstract class AbstractIndexingDecorator<T, W extends Writable<W>> extends
    IndexDirectoryDecorator<T, W> implements IndexingDecorator<T, W> {

  private static final String INDEX_FILE_NAME = "index";

  final IndexDirectory<T, W> directory;

  public AbstractIndexingDecorator(IndexDirectory<T, W> directory) {
    super(directory);
    this.directory = directory;
  }

  /**
   * See {@link AbstractIndexingDecorator} class documentation.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritable(this.getIndexFile(INDEX_FILE_NAME, indices));
    super.prepare(indices);
  }
}
