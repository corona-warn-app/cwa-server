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

package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory;

import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.appconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.RiskScoreClassificationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.decorator.AppConfigurationSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.AppConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ExposureConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import com.google.protobuf.Message;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the directory structure {@code /parameters/country/:country} and writes two files. One containing {@link
 * RiskScoreParameters} and the another containing the {@link RiskScoreClassification}, wrapped in a signed zip
 * archive.
 */
public class AppConfigurationDirectory extends DirectoryOnDisk {

  private static final Logger logger = LoggerFactory.getLogger(AppConfigurationDirectory.class);
  private static final String PARAMETERS_DIRECTORY = "configuration";
  private static final String COUNTRY_DIRECTORY = "country";
  private static final String COUNTRY = "DE";
  private static final String EXPOSURE_CONFIGURATION_FILE_NAME = "exposure_configuration";
  private static final String RISK_SCORE_CLASSIFICATION_FILE_NAME = "risk_score_classification";

  private final IndexDirectoryOnDisk<String> countryDirectory =
      new IndexDirectoryOnDisk<>(COUNTRY_DIRECTORY, __ -> Set.of(COUNTRY), Object::toString);

  private final CryptoProvider cryptoProvider;

  /**
   * Creates an {@link AppConfigurationDirectory} for the exposure configuration and risk score classification.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public AppConfigurationDirectory(CryptoProvider cryptoProvider) {
    super(PARAMETERS_DIRECTORY);

    this.cryptoProvider = cryptoProvider;
    addExposureConfigurationIfValid();
    addRiskScoreClassificationIfValid();

    this.addWritable(new IndexingDecoratorOnDisk<>(countryDirectory));
  }

  private void addExposureConfigurationIfValid() {
    try {
      RiskScoreParameters exposureConfig = ExposureConfigurationProvider.readMasterFile();
      AppConfigurationValidator validator = new ExposureConfigurationValidator(exposureConfig);
      addArchiveIfMessageValid(EXPOSURE_CONFIGURATION_FILE_NAME, exposureConfig, validator);
    } catch (UnableToLoadFileException e) {
      logger.error("Exposure configuration will not be published! Unable to read configuration file from disk.");
    }
  }

  private void addRiskScoreClassificationIfValid() {
    try {
      RiskScoreClassification riskScoreClassification = RiskScoreClassificationProvider.readMasterFile();
      AppConfigurationValidator validator = new RiskScoreClassificationValidator(riskScoreClassification);
      addArchiveIfMessageValid(RISK_SCORE_CLASSIFICATION_FILE_NAME, riskScoreClassification, validator);
    } catch (UnableToLoadFileException e) {
      logger.error("Risk score classification will not be published! Unable to read configuration file from disk.");
    }
  }

  /**
   * If validation of the {@link Message} succeeds, it is written into a file, put into an archive with the specified
   * name and added to the specified parent directory.
   */
  private void addArchiveIfMessageValid(String archiveName, Message message, AppConfigurationValidator validator) {
    ValidationResult validationResult = validator.validate();

    if (validationResult.hasErrors()) {
      logger.error("App configuration file creation failed. Validation failed for {}./n{}",
          archiveName, validationResult);
    }

    ArchiveOnDisk appConfigurationFile = new ArchiveOnDisk(archiveName);
    appConfigurationFile.addWritable(new FileOnDisk("export.bin", message.toByteArray()));
    countryDirectory.addWritableToAll(__ -> new AppConfigurationSigningDecorator(appConfigurationFile, cryptoProvider));
  }
}
