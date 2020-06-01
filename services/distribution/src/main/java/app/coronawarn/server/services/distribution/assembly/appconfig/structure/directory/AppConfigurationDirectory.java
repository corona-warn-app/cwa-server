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

package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.archive.decorator.signing.AppConfigurationSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ApplicationConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
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

  private final IndexDirectoryOnDisk<String> countryDirectory;

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an {@link AppConfigurationDirectory} for the exposure configuration and risk score classification.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public AppConfigurationDirectory(CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getParametersPath());
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;

    countryDirectory = new IndexDirectoryOnDisk<>(distributionServiceConfig.getApi().getCountryPath(),
        __ -> Set.of(distributionServiceConfig.getApi().getCountryGermany()), Object::toString);

    addApplicationConfigurationIfValid();

    this.addWritable(new IndexingDecoratorOnDisk<>(countryDirectory, distributionServiceConfig.getOutputFileName()));
  }

  private void addApplicationConfigurationIfValid() {
    try {
      ApplicationConfiguration appConfig = ApplicationConfigurationProvider.readMasterFile();
      ConfigurationValidator validator = new ApplicationConfigurationValidator(appConfig);
      addArchiveIfMessageValid(distributionServiceConfig.getApi().getAppConfigFileName(),
          appConfig, validator);
    } catch (UnableToLoadFileException e) {
      logger.error("Exposure configuration will not be published! Unable to read configuration file from disk.", e);
    }
  }

  /**
   * If validation of the {@link Message} succeeds, it is written into a file, put into an archive with the specified
   * name and added to the specified parent directory.
   */
  private void addArchiveIfMessageValid(String archiveName, Message message, ConfigurationValidator validator) {
    ValidationResult validationResult = validator.validate();

    if (validationResult.hasErrors()) {
      logger.error("App configuration file creation failed. Validation failed for {}./n{}",
          archiveName, validationResult);
    }

    ArchiveOnDisk appConfigurationFile = new ArchiveOnDisk(archiveName);
    appConfigurationFile.addWritable(new FileOnDisk("export.bin", message.toByteArray()));
    countryDirectory.addWritableToAll(__ -> new AppConfigurationSigningDecorator(appConfigurationFile, cryptoProvider,
        distributionServiceConfig));
  }
}
