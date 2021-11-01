package app.coronawarn.server.services.distribution.assembly.appconfig.structure;

import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static java.io.File.separator;
import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.AppConfigurationDirectory;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, ApplicationConfigurationPublicationConfig.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class AppConfigurationDirectoryTest {

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  private CryptoProvider cryptoProvider;

  @Autowired
  private DistributionServiceConfig distributionServiceConfigSpy;

  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  @Test
  void createsCorrectFiles() throws IOException {
    Set<String> expFiles = Set.of(
        join(separator, "configuration", "country", "index"),
        join(separator, "configuration", "country", "index.checksum"),
        join(separator, "configuration", "country", "DE", "app_config"),
        join(separator, "configuration", "country", "DE", "app_config.checksum")
        );

    assertThat(writeDirectoryAndGetFiles(applicationConfiguration)).isEqualTo(expFiles);
  }

  @Test
  void doesNotWriteAppConfigIfValidationFails() throws IOException, UnableToLoadFileException {
    ApplicationConfiguration invalidConfiguration =
        loadApplicationConfiguration("configtests/app-config_mrs_negative.yaml");

    Set<String> expFiles = Set.of(
        join(separator, "configuration", "country", "index"),
        join(separator, "configuration", "country", "index.checksum"));

    assertThat(writeDirectoryAndGetFiles(invalidConfiguration)).isEqualTo(expFiles);
  }

  private Set<String> writeDirectoryAndGetFiles(ApplicationConfiguration applicationConfiguration) throws IOException {
    outputFolder.create();
    File outputFile = outputFolder.newFolder();
    Directory<WritableOnDisk> parentDirectory = new DirectoryOnDisk(outputFile);
    AppConfigurationDirectory configurationDirectory =
        new AppConfigurationDirectory(applicationConfiguration, cryptoProvider, distributionServiceConfigSpy);
    parentDirectory.addWritable(configurationDirectory);

    configurationDirectory.prepare(new ImmutableStack<>());
    configurationDirectory.write();
    return Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
  }
}
