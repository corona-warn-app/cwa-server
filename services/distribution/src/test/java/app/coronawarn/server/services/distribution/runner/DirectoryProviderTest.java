package app.coronawarn.server.services.distribution.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.Assertions;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Assembly.class}, initializers = ConfigFileApplicationContextInitializer.class)
class DirectoryProviderTest {

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @MockBean
  CwaApiStructureProvider cwaApiStructureProvider;

  @Autowired
  Assembly assembly;

  private Directory<WritableOnDisk> parentDirectory;
  private Directory<WritableOnDisk> childDirectory;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  void setup() throws IOException {
    outputFolder.create();
    var outputDirectory = outputFolder.newFolder("parent");
    var outputSubDirectory = outputFolder.newFolder("parent/child");
    parentDirectory = new DirectoryOnDisk(outputDirectory);
    childDirectory = new DirectoryOnDisk(outputSubDirectory);
  }

  @Test
  void DirectoryproviderTest() throws IOException {
    Directory<WritableOnDisk> spyParentDirectory = spy(parentDirectory);

    when(outputDirectoryProvider.getDirectory()).thenReturn(spyParentDirectory);
    when(cwaApiStructureProvider.getDirectory()).thenReturn(childDirectory);

    assembly.run(null);

    verify(outputDirectoryProvider, times(1)).getDirectory();
    verify(outputDirectoryProvider, times(1)).clear();
    outputDirectoryProvider.clear();
    Assertions.assertNull(outputDirectoryProvider.getFileOnDisk());
  }
}
