package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.v2;

import static java.io.File.separator;
import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.config.PreDistributionTrlValueMappingProvider;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationV2PublicationConfig;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, ApplicationConfigurationV2PublicationConfig.class,
    PreDistributionTrlValueMappingProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class AppConfigurationV1StructureProviderTest {

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  private CryptoProvider cryptoProvider;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  @Qualifier("applicationConfigurationV1Android")
  private ApplicationConfigurationAndroid applicationConfigurationAndroid;

  @Autowired
  @Qualifier("applicationConfigurationV1Ios")
  private ApplicationConfigurationIOS applicationConfigurationIos;

  @Test
  void createsCorrectIosFiles() throws IOException {
    Set<String> expFiles =
        Set.of(join(separator, "app_config_ios"), join(separator, "app_config_ios.checksum"));
    Writable<WritableOnDisk> appConfigs =
        new AppConfigurationV2StructureProvider<ApplicationConfigurationIOS>(
            applicationConfigurationIos, cryptoProvider, distributionServiceConfig,
            distributionServiceConfig.getApi().getAppConfigV2IosFileName())
            .getConfigurationArchive();

    assertThat(writeDirectoryAndGetFiles(appConfigs)).isEqualTo(expFiles);
  }

  @Test
  void createsCorrectAndroidFiles() throws IOException {
    Set<String> expFiles = Set.of(join(separator, "app_config_android"),
        join(separator, "app_config_android.checksum"));
    Writable<WritableOnDisk> appConfigs =
        new AppConfigurationV2StructureProvider<ApplicationConfigurationAndroid>(
            applicationConfigurationAndroid, cryptoProvider, distributionServiceConfig,
            distributionServiceConfig.getApi().getAppConfigV2AndroidFileName())
            .getConfigurationArchive();

    assertThat(writeDirectoryAndGetFiles(appConfigs)).isEqualTo(expFiles);
  }

  private Set<String> writeDirectoryAndGetFiles(Writable<WritableOnDisk> configFile) throws IOException {
    outputFolder.create();
    File outputFile = outputFolder.newFolder();
    Directory<WritableOnDisk> parentDirectory = new DirectoryOnDisk(outputFile);
    parentDirectory.addWritable(configFile);
    parentDirectory.prepare(new ImmutableStack<>());
    parentDirectory.write();
    return Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
  }
}
