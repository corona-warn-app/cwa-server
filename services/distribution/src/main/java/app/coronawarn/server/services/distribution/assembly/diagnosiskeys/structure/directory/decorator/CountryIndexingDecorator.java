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
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This decorator creates the central index file for a country, e.g. DE.
 */
public class CountryIndexingDecorator<T> extends IndexDirectoryDecorator<T, WritableOnDisk> {

  /**
   * Separate each entry in the index file with new line.
   */
  private static final String NEW_LINE_SEPARATOR = "\r\n";

  /**
   * the name of this index file.
   */
  private final String fileName;

  /**
   * Creates a new decorator instance for the given directory.
   *
   * @param directory                 The target country directory.
   * @param distributionServiceConfig The config.
   */
  public CountryIndexingDecorator(IndexDirectory<T, WritableOnDisk> directory,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory);
    this.fileName = distributionServiceConfig.getOutputFileName();
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);

    Collection<DirectoryOnDisk> countryDirectories = this.getWritables().stream()
        .filter(Writable::isDirectory)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());

    countryDirectories.forEach(this::writeIndexFileForCountry);
  }

  private void writeIndexFileForCountry(Directory<WritableOnDisk> directory) {
    var dateDirectory = (Directory<WritableOnDisk>) directory.getWritables()
        .stream()
        .filter(Writable::isDirectory)
        .findFirst()
        .orElseThrow();

    String resourcePaths = CountryIndexingDecorator.getExposureKeyExportPaths(dateDirectory)
        .stream()
        .sorted()
        .collect(Collectors.joining(NEW_LINE_SEPARATOR));

    directory.addWritable(new FileOnDisk(fileName, resourcePaths.getBytes(StandardCharsets.UTF_8)));
  }

  private static Set<String> getExposureKeyExportPaths(Directory<WritableOnDisk> rootDirectory) {
    Collection<Directory<WritableOnDisk>> directories = rootDirectory.getWritables()
        .stream()
        .filter(Writable::isDirectory)
        .filter(directory -> !(directory instanceof Archive))
        .map(directory -> (Directory<WritableOnDisk>) directory)
        .collect(Collectors.toSet());

    if (directories.isEmpty()) {
      return Set.of(rootDirectory.getName());
    } else {
      return directories.stream()
          .map(CountryIndexingDecorator::getExposureKeyExportPaths)
          .flatMap(Set::stream)
          .map(childName -> rootDirectory.getName() + "/" + childName)
          .collect(Collectors.toSet());
    }
  }
}
