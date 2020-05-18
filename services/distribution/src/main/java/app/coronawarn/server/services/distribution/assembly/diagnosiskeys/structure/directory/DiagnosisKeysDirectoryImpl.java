/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Collection;

/**
 * A {@link Directory} containing the file and directory structure that mirrors the API defined in the OpenAPI
 * definition {@code /services/distribution/api_v1.json}. Available countries (endpoint {@code
 * /version/v1/diagnosis-keys/country}) are statically set to only {@code "DE"}. The dates and respective hours
 * (endpoint {@code /version/v1/diagnosis-keys/country/DE/date}) will be created based on the actual {@link DiagnosisKey
 * DiagnosisKeys} given to the {@link DiagnosisKeysDirectoryImpl#DiagnosisKeysDirectoryImpl constructor}.
 */
public class DiagnosisKeysDirectoryImpl extends DirectoryImpl {

  private static final String DIAGNOSIS_KEYS_DIRECTORY = "diagnosis-keys";
  private final Collection<DiagnosisKey> diagnosisKeys;
  private final CryptoProvider cryptoProvider;

  /**
   * Constructs a {@link DiagnosisKeysDirectoryImpl} based on the specified {@link DiagnosisKey} collection.
   * Cryptographic signing is performed using the specified {@link CryptoProvider}.
   *
   * @param diagnosisKeys  The diagnosis keys processed in the contained sub directories.
   * @param cryptoProvider The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys, CryptoProvider cryptoProvider) {
    super(DIAGNOSIS_KEYS_DIRECTORY);
    this.diagnosisKeys = diagnosisKeys;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addDirectory(decorateCountryDirectory(
        new DiagnosisKeysCountryDirectoryImpl(diagnosisKeys, cryptoProvider)));
    super.prepare(indices);
  }

  private Directory decorateCountryDirectory(IndexDirectory<String> countryDirectory) {
    return new IndexingDecorator<>(countryDirectory);
  }
}
