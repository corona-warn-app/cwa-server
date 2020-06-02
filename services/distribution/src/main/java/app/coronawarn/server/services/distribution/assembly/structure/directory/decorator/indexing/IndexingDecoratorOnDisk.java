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

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;

public class IndexingDecoratorOnDisk<T> extends AbstractIndexingDecorator<T, WritableOnDisk>
    implements IndexingDecorator<T, WritableOnDisk> {

  public IndexingDecoratorOnDisk(IndexDirectory<T, WritableOnDisk> directory, String indexFileName) {
    super(directory, indexFileName);
  }

  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    Set<T> index = this.getIndex(indices);
    JSONArray array = new JSONArray();
    List<?> elements = index.stream()
        .map(this.getIndexFormatter())
        .collect(Collectors.toList());
    array.addAll(elements);
    return new FileOnDisk(indexFileName, array.toJSONString().getBytes());
  }
}
