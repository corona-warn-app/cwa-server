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
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class DiagnosisKeysDateDirectoryImpl extends IndexDirectoryImpl<LocalDate> {

  private static final String DATE_DIRECTORY = "date";
  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final Collection<DiagnosisKey> diagnosisKeys;
  private final CryptoProvider cryptoProvider;

  /**
   * Constructs a {@link DiagnosisKeysDateDirectoryImpl} instance associated with the specified {@link DiagnosisKey}
   * collection. Payload signing is be performed according to the specified {@link CryptoProvider}.
   *
   * @param diagnosisKeys  The diagnosis keys processed in the contained directories.
   * @param cryptoProvider The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysDateDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys,
      CryptoProvider cryptoProvider) {
    super(DATE_DIRECTORY, __ -> DateTime.getDates(diagnosisKeys), ISO8601::format);
    this.cryptoProvider = cryptoProvider;
    this.diagnosisKeys = diagnosisKeys;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addDirectoryToAll(currentIndices -> {
      LocalDate currentDate = (LocalDate) currentIndices.peek();
      IndexDirectory<LocalDateTime> hourDirectory = new DiagnosisKeysHourDirectoryImpl(
          diagnosisKeys, currentDate, cryptoProvider);
      return decorateHourDirectory(hourDirectory);
    });
    super.prepare(indices);
  }

  private Directory decorateHourDirectory(IndexDirectory<LocalDateTime> hourDirectory) {
    return new IndexingDecorator<>(hourDirectory);
  }
}
