/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 *  file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CountryIndexingDecorator<T> extends IndexDirectoryDecorator<T, WritableOnDisk> {

  private static final String NEW_LINE_SEPARATOR = "\r\n";

  private static final String FILE_NAME = "index";

  public CountryIndexingDecorator(IndexDirectory<T, WritableOnDisk> directory) {
    super(directory);
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);

    Collection<DirectoryOnDisk> directories = this.getWritables().stream()
        .filter(Writable::isDirectory)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());

    directories.forEach(this::writeIndexFileForCountry);
  }

  private void writeIndexFileForCountry(Directory<WritableOnDisk> directory) {
    var dateDirectory = (Directory<WritableOnDisk>) directory.getWritables()
        .stream()
        .filter(Writable::isDirectory)
        .findFirst()
        .orElseThrow();

    String content = CountryIndexingDecorator.getExposureKeyExportsIn(dateDirectory)
        .stream()
        .sorted()
        .collect(Collectors.joining(NEW_LINE_SEPARATOR));

    var indexFile =  new FileOnDisk(FILE_NAME, content.getBytes(StandardCharsets.UTF_8));
    directory.addWritable(indexFile);
  }

  private static Set<String> getExposureKeyExportsIn(Directory<WritableOnDisk> rootDirectory) {
    Collection<Directory<WritableOnDisk>> directories = rootDirectory.getWritables().stream()
        .filter(Writable::isDirectory)
        .filter(directory -> !(directory instanceof Archive))
        .map(directory -> (Directory<WritableOnDisk>) directory)
        .collect(Collectors.toSet());

    if (directories.isEmpty()) {
      return Set.of(rootDirectory.getName());
    } else {
      return directories.stream()
          .map(CountryIndexingDecorator::getExposureKeyExportsIn)
          .flatMap(Set::stream)
          .map(childName -> rootDirectory.getName() + "/" + childName)
          .collect(Collectors.toSet());
    }
  }
}
