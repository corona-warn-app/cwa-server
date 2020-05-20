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
import app.coronawarn.server.services.distribution.assembly.exposureconfig.structure.directory.decorator.ExposureConfigSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import java.util.Set;

/**
 * Creates the directory structure {@code /parameters/country/:country} and writes a file called {@code index}
 * containing {@link RiskScoreParameters} wrapped in a signed zip archive.
 */
public class ExposureConfigurationDirectory extends DirectoryOnDisk {

  private static final String PARAMETERS_DIRECTORY = "parameters";
  private static final String COUNTRY_DIRECTORY = "country";
  private static final String COUNTRY = "DE";
  private static final String INDEX_FILE_NAME = "index";

  /**
   * Constructor.
   *
   * @param exposureConfig The {@link RiskScoreParameters} to sign and write.
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public ExposureConfigurationDirectory(RiskScoreParameters exposureConfig,
      CryptoProvider cryptoProvider) {
    super(PARAMETERS_DIRECTORY);

    ArchiveOnDisk archive = new ArchiveOnDisk(INDEX_FILE_NAME);
    archive.addWritable(new FileOnDisk("export.bin", exposureConfig.toByteArray()));

    IndexDirectoryOnDisk<String> country =
        new IndexDirectoryOnDisk<>(COUNTRY_DIRECTORY, __ -> Set.of(COUNTRY), Object::toString);
    country.addWritableToAll(__ ->
        new ExposureConfigSigningDecorator(archive, cryptoProvider));

    this.addWritable(new IndexingDecoratorOnDisk<>(country));
  }
}
