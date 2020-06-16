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

package app.coronawarn.server.services.distribution.assembly.appconfig.structure;

import static java.io.File.separator;
import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.AppConfigurationDirectory;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Signature;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class}, initializers = ConfigFileApplicationContextInitializer.class)
class AppConfigurationDirectoryTest {

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();
  private File outputFile;
  private AppConfigurationDirectory configurationDirectory;

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @BeforeEach
  void setup() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    configurationDirectory = new AppConfigurationDirectory(cryptoProvider, distributionServiceConfig);
    Directory<WritableOnDisk> parentDirectory = new DirectoryOnDisk(outputFile);
    parentDirectory.addWritable(configurationDirectory);
  }

  @Test
  void createsCorrectFiles() {
    configurationDirectory.prepare(new ImmutableStack<>());
    configurationDirectory.write();

    Set<String> expFiles = Set.of(
        join(separator, "configuration", "country", "index"),
        join(separator, "configuration", "country", "index.checksum"),
        join(separator, "configuration", "country", "DE", "app_config"),
        join(separator, "configuration", "country", "DE", "app_config.checksum"));

    Set<String> actFiles = Helpers.getFiles(outputFile, outputFile.getAbsolutePath());

    assertThat(actFiles).isEqualTo(expFiles);
  }
}
