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

import static app.coronawarn.server.services.distribution.assembly.structure.util.functional.CheckedFunction.uncheckedFunction;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.archive.decorator.singing.DiagnosisKeySigningDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.TemporaryExposureKeyExportFile;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.DirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link DirectoryDecorator} that will bundle hour aggregates into date aggregates and sign them.
 */
public class DateAggregatingDecorator extends IndexDirectoryDecorator<LocalDate, WritableOnDisk> {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates a new DateAggregatingDecorator.
   */
  public DateAggregatingDecorator(IndexDirectory<LocalDate, WritableOnDisk> directory, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory);
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    Set<Directory<WritableOnDisk>> dayDirectories = this.getWritables().stream()
        .filter(writable -> writable instanceof DirectoryOnDisk)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());
    if (dayDirectories.isEmpty()) {
      return;
    }

    Set<String> dates = this.getIndex(indices).stream()
        .map(this.getIndexFormatter())
        .map(Object::toString)
        .collect(Collectors.toSet());

    dayDirectories.stream()
        .filter(dayDirectory -> dates.contains(dayDirectory.getName()))
        .forEach(currentDirectory -> Stream.of(currentDirectory)
            .map(this::getSubSubDirectoryArchives)
            .map(this::getTemporaryExposureKeyExportFilesFromArchives)
            .map(this::parseTemporaryExposureKeyExportsFromFiles)
            .map(this::reduceTemporaryExposureKeyExportsToNewFile)
            .map(temporaryExposureKeyExportFile -> {
              Archive<WritableOnDisk> aggregate = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
              aggregate.addWritable(temporaryExposureKeyExportFile);
              return aggregate;
            })
            .map(file -> new DiagnosisKeySigningDecorator(file, cryptoProvider, distributionServiceConfig))
            .forEach(aggregate -> {
              currentDirectory.addWritable(aggregate);
              aggregate.prepare(indices);
            })
        );
  }

  /**
   * Returns all archives that are 3 levels down from the root directory.
   */
  private Set<Archive<WritableOnDisk>> getSubSubDirectoryArchives(Directory<WritableOnDisk> rootDirectory) {
    return getWritablesInDirectory(rootDirectory, 3).stream()
        .filter(Writable::isArchive)
        .map(archive -> (Archive<WritableOnDisk>) archive)
        .collect(Collectors.toSet());
  }

  /**
   * Traverses a directory {@code depth} levels deep and returns a flattened list of all writables at that depth. A
   * {@code depth} of 0 or less returns a set only containing the root directory. A depth of 1 returns a set of
   * writables in the root directory. A depth of 2 returns a set of all writables in all directories in the root
   * directory, and so on.
   *
   * @param rootDirectory The directory in which to start traversal.
   * @param depth         The depth to traverse.
   * @return All writables that are {@code depth} levels down.
   */
  private Set<Writable<WritableOnDisk>> getWritablesInDirectory(Directory<WritableOnDisk> rootDirectory, int depth) {
    if (depth <= 0) {
      return Set.of(rootDirectory);
    } else if (depth == 1) {
      return rootDirectory.getWritables();
    } else {
      return rootDirectory.getWritables().stream()
          .filter(Writable::isDirectory)
          .flatMap(directory -> getWritablesInDirectory((Directory<WritableOnDisk>) directory, depth - 1).stream())
          .collect(Collectors.toSet());
    }
  }

  private Set<TemporaryExposureKeyExportFile> getTemporaryExposureKeyExportFilesFromArchives(
      Set<Archive<WritableOnDisk>> hourArchives) {
    return hourArchives.stream()
        .map(Directory::getWritables)
        .map(writables -> writables.stream()
            .filter(writable -> writable.getName().equals("export.bin")))
        .map(Stream::findFirst)
        .map(Optional::orElseThrow)
        .filter(writable -> writable instanceof File)
        .map(file -> (TemporaryExposureKeyExportFile) file)
        .collect(Collectors.toSet());
  }

  private Set<TemporaryExposureKeyExport> parseTemporaryExposureKeyExportsFromFiles(
      Set<TemporaryExposureKeyExportFile> temporaryExposureKeyExportFiles) {
    return temporaryExposureKeyExportFiles.stream()
        .map(TemporaryExposureKeyExportFile::getBytesWithoutHeader)
        .map(uncheckedFunction(TemporaryExposureKeyExport::parseFrom))
        .collect(Collectors.toSet());
  }

  private TemporaryExposureKeyExportFile reduceTemporaryExposureKeyExportsToNewFile(
      Set<TemporaryExposureKeyExport> temporaryExposureKeyExports) {
    return TemporaryExposureKeyExportFile.fromTemporaryExposureKeys(
        getTemporaryExposureKeys(temporaryExposureKeyExports),
        getRegion(temporaryExposureKeyExports),
        getStartTimestamp(temporaryExposureKeyExports),
        getEndTimestamp(temporaryExposureKeyExports),
        distributionServiceConfig
    );
  }

  private static Set<TemporaryExposureKey> getTemporaryExposureKeys(
      Set<TemporaryExposureKeyExport> temporaryExposureKeyExports) {
    return temporaryExposureKeyExports.stream()
        .map(TemporaryExposureKeyExport::getKeysList)
        .flatMap(List::stream)
        .collect(Collectors.toSet());
  }

  private static String getRegion(Set<TemporaryExposureKeyExport> temporaryExposureKeyExports) {
    return temporaryExposureKeyExports.stream()
        .map(TemporaryExposureKeyExport::getRegion)
        .findAny()
        .orElseThrow(NoSuchElementException::new);
  }

  private static long getStartTimestamp(
      Set<TemporaryExposureKeyExport> temporaryExposureKeyExports) {
    return temporaryExposureKeyExports.stream()
        .mapToLong(TemporaryExposureKeyExport::getStartTimestamp)
        .min()
        .orElseThrow(NoSuchElementException::new);
  }

  private static long getEndTimestamp(Set<TemporaryExposureKeyExport> temporaryExposureKeyExports) {
    return temporaryExposureKeyExports.stream()
        .mapToLong(TemporaryExposureKeyExport::getEndTimestamp)
        .max()
        .orElseThrow(NoSuchElementException::new);
  }
}
