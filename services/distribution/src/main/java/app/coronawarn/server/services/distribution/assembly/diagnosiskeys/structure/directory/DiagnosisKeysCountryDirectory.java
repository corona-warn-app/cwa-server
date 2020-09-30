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
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DateIndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DiagnosisKeysCountryDirectory extends IndexDirectoryOnDisk<String> {

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysCountryDirectory} instance that represents the {@code .../country/:country/...}
   * portion of the diagnosis key directory structure.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysCountryDirectory(DiagnosisKeyBundler diagnosisKeyBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getCountryPath(), ignoredValue -> {
      Set<String> countries = new HashSet<>(Set.of(distributionServiceConfig.getSupportedCountries()));
      countries.add(distributionServiceConfig.getEuPackageName());
      return countries;
    }, Object::toString);
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> Optional.of(decorateDateDirectory(
        new DiagnosisKeysDateDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig))));
    super.prepare(indices);
  }

  private IndexDirectory<LocalDate, WritableOnDisk> decorateDateDirectory(DiagnosisKeysDateDirectory dateDirectory) {
    return new DateIndexingDecorator(dateDirectory, distributionServiceConfig);
  }
}
