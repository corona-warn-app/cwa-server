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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import static app.coronawarn.server.services.distribution.assembly.structure.util.functional.CheckedFunction.uncheckedFunction;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
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
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DirectoryDecorator} that will TODO
 */
public class DateAggregatingDecorator extends IndexDirectoryDecorator<LocalDate, WritableOnDisk> {

  private static final Logger logger = LoggerFactory.getLogger(DateAggregatingDecorator.class);

  private final CryptoProvider cryptoProvider;

  private static final String AGGREGATE_FILE_NAME = "index";

  public DateAggregatingDecorator(IndexDirectory directory, CryptoProvider cryptoProvider) {
    super(directory);
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    logger.debug("Aggregating ..."); // TODO

    Set<Directory<WritableOnDisk>> dayDirectories = this.getWritables().stream()
        .filter(writable -> writable instanceof DirectoryOnDisk)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());
    if (dayDirectories.size() == 0) {
      return;
    }

    List<Directory<WritableOnDisk>> sortedDayDirectories = new ArrayList<>(dayDirectories);
    sortedDayDirectories.sort(Comparator.comparing(Writable::getName));

    // Exclude the last day
    sortedDayDirectories.subList(0, sortedDayDirectories.size() - 1).forEach(currentDirectory -> {
      Stream.of(currentDirectory)
          .map(this::getSubSubDirectoryArchives)
          .map(this::getTemporaryExposureKeyExportFilesFromArchives)
          .map(this::parseTemporaryExposureKeyExportsFromFiles)
          .map(this::reduceTemporaryExposureKeyExportsToNewFile)
          .map(temporaryExposureKeyExportFile -> {
            Archive<WritableOnDisk> aggregate = new ArchiveOnDisk(AGGREGATE_FILE_NAME);
            aggregate.addWritable(temporaryExposureKeyExportFile);
            return aggregate;
          })
          .map(file -> new DiagnosisKeySigningDecorator(file, cryptoProvider))
          .peek(currentDirectory::addWritable)
          .forEach(aggregate -> aggregate.prepare(indices));
    });
  }

  private Set<Directory<WritableOnDisk>> getSubSubDirectoryArchives(Directory<WritableOnDisk> rootDirectory) {
    // Get all archives 2 directory levels down
    return Stream.of(rootDirectory)
        .map(Directory::getWritables)
        .flatMap(Set::stream)
        .filter(Writable::isDirectory)
        .map(directory -> ((DirectoryOnDisk) directory).getWritables())
        .flatMap(Set::stream)
        .filter(Writable::isDirectory)
        .map(directory -> ((DirectoryOnDisk) directory).getWritables())
        .flatMap(Collection::stream)
        .filter(Writable::isDirectory)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());
  }

  private Set<TemporaryExposureKeyExportFile> getTemporaryExposureKeyExportFilesFromArchives(
      Set<Directory<WritableOnDisk>> hourArchives) {
    return hourArchives.stream()
        .map(Directory::getWritables)
        // TODO
        .map(a -> a.stream().filter(b -> b.getName().equals("export.bin")))
        .map(Stream::findFirst)
        .map(Optional::orElseThrow)
        .filter(writable -> writable instanceof File)
        .map(file -> (TemporaryExposureKeyExportFile) file)
        .collect(Collectors.toSet());
  }

  private Set<TemporaryExposureKeyExport> parseTemporaryExposureKeyExportsFromFiles(
      Set<TemporaryExposureKeyExportFile> temporaryExposureKeyExportFiles) {
    return temporaryExposureKeyExportFiles.stream()
        .map(FileOnDisk::getBytes)
        .map(uncheckedFunction(TemporaryExposureKeyExport::parseFrom))
        .collect(Collectors.toSet());
  }

  private TemporaryExposureKeyExportFile reduceTemporaryExposureKeyExportsToNewFile(
      Set<TemporaryExposureKeyExport> temporaryExposureKeyExports) {
    return TemporaryExposureKeyExportFile.fromTemporaryExposureKeys(
        getTemporaryExposureKeys(temporaryExposureKeyExports),
        getRegion(temporaryExposureKeyExports),
        getStartTimestamp(temporaryExposureKeyExports),
        getEndTimestamp(temporaryExposureKeyExports)
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
