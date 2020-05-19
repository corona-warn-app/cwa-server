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

package app.coronawarn.server.services.distribution.assembly.exposureconfig.structure.directory;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveImpl;
import java.util.Set;

/**
 * Creates the directory structure {@code /parameters/country/:country} and writes a file called
 * {@code index} containing {@link RiskScoreParameters} wrapped in a {@link
 * app.coronawarn.server.common.protocols.internal.SignedPayload}.
 */
public class ExposureConfigurationDirectoryImpl extends DirectoryImpl {

  private static final String PARAMETERS_DIRECTORY = "parameters";
  private static final String COUNTRY_DIRECTORY = "country";
  private static final String COUNTRY = "DE";
  private static final String INDEX_FILE_NAME = "index";

  /**
   * Constructor.
   *
   * @param exposureConfig The {@link RiskScoreParameters} to sign and write.
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the {@link
   *                       app.coronawarn.server.common.protocols.internal.SignedPayload}. TODO
   */
  public ExposureConfigurationDirectoryImpl(RiskScoreParameters exposureConfig,
      CryptoProvider cryptoProvider) {
    super(PARAMETERS_DIRECTORY);

    // TODO Extract into config archive
    Archive archive = new ArchiveImpl(INDEX_FILE_NAME);
    archive.addWritable(new FileImpl("export.bin", exposureConfig.toByteArray()));

    IndexDirectoryImpl<String> country =
        new IndexDirectoryImpl<>(COUNTRY_DIRECTORY, __ -> Set.of(COUNTRY), Object::toString);
    country.addWritableToAll(__ ->
        new ExposureConfigSigningDecorator(archive, cryptoProvider));

    this.addWritable(new IndexingDecorator<>(country));
  }
}
