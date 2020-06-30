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
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

/**
 * A {@link Directory} containing the file and directory structure that mirrors the API defined in the OpenAPI
 * definition {@code /services/distribution/api_v1.json}. Available countries (endpoint {@code
 * /version/v1/diagnosis-keys/country}) are statically set to only {@code "DE"}. The dates and respective hours
 * (endpoint {@code /version/v1/diagnosis-keys/country/DE/date}) will be created based on the actual {@link DiagnosisKey
 * DiagnosisKeys} given to the {@link DiagnosisKeysDirectory#DiagnosisKeysDirectory constructor}.
 */
public class DiagnosisKeysDirectory extends DirectoryOnDisk {

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysDirectory} based on the specified {@link DiagnosisKey} collection. Cryptographic
   * signing is performed using the specified {@link CryptoProvider}.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysDirectory(DiagnosisKeyBundler diagnosisKeyBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getDiagnosisKeysPath());
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritable(decorateCountryDirectory(
        new DiagnosisKeysCountryDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig)));
    super.prepare(indices);
  }

  private IndexDirectory<String, WritableOnDisk> decorateCountryDirectory(
      IndexDirectoryOnDisk<String> countryDirectory) {
    return new IndexingDecoratorOnDisk<>(countryDirectory, distributionServiceConfig.getOutputFileName());
  }
}
