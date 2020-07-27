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
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.HourIndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DiagnosisKeysDateDirectory extends IndexDirectoryOnDisk<LocalDate> {

  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysDateDirectory} instance associated with the specified {@link DiagnosisKey}
   * collection. Payload signing is be performed according to the specified {@link CryptoProvider}.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysDateDirectory(DiagnosisKeyBundler diagnosisKeyBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getDatePath(),
        __ -> diagnosisKeyBundler.getDatesWithDistributableDiagnosisKeys(), ISO8601::format);
    this.cryptoProvider = cryptoProvider;
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(__ -> {
      DiagnosisKeysHourDirectory hourDirectory =
          new DiagnosisKeysHourDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig);
      return decorateHourDirectory(hourDirectory);
    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateHourDirectory(DiagnosisKeysHourDirectory hourDirectory) {
    return new HourIndexingDecorator(hourDirectory, distributionServiceConfig);
  }
}
