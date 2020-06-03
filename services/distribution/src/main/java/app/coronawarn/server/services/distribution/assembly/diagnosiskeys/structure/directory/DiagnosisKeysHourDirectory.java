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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.archive.decorator.singing.DiagnosisKeySigningDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.TemporaryExposureKeyExportFile;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class DiagnosisKeysHourDirectory extends IndexDirectoryOnDisk<LocalDateTime> {

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysHourDirectory} instance for the specified date.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for cryptographic signing.
   */
  public DiagnosisKeysHourDirectory(DiagnosisKeyBundler diagnosisKeyBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getHourPath(),
        indices -> diagnosisKeyBundler.getHoursWithDistributableDiagnosisKeys(((LocalDate) indices.peek())),
        LocalDateTime::getHour);
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      LocalDateTime currentHour = (LocalDateTime) currentIndices.peek();
      // The LocalDateTime currentHour already contains both the date and the hour information, so
      // we can throw away the LocalDate that's the second item on the stack from the "/date"
      // IndexDirectory.
      String region = (String) currentIndices.pop().pop().peek();

      List<DiagnosisKey> diagnosisKeysForCurrentHour =
          this.diagnosisKeyBundler.getDiagnosisKeysForHour(currentHour);

      long startTimestamp = currentHour.toEpochSecond(ZoneOffset.UTC);
      long endTimestamp = currentHour.plusHours(1).toEpochSecond(ZoneOffset.UTC);
      File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
          diagnosisKeysForCurrentHour, region, startTimestamp, endTimestamp, distributionServiceConfig);

      Archive<WritableOnDisk> hourArchive = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
      hourArchive.addWritable(temporaryExposureKeyExportFile);

      return decorateDiagnosisKeyArchive(hourArchive);
    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DiagnosisKeySigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
