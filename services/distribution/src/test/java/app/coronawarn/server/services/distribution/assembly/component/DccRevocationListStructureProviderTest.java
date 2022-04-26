package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationListToProtobufMapping;
import app.coronawarn.server.services.distribution.dcc.TestDccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = { DistributionServiceConfig.class })
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DccRevocationListStructureProvider.class,
    CryptoProvider.class, DistributionServiceConfig.class,
    TestDccRevocationClient.class,
    DccRevocationListToProtobufMapping.class,
    DccRevocationListDecoder.class,
    },
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({ "fake-dcc-revocation", "revocation" })
class DccRevocationListStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";

  @MockBean
  DccRevocationListService dccRevocationListService;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DccRevocationListToProtobufMapping dccRevocationListToProtobufMapping;

  @Autowired
  TestDccRevocationClient dccRevocationClient;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  @Autowired
  DccRevocationListStructureProvider underTest;

  @BeforeEach
  public void setup() throws IOException {
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
    when(dccRevocationListService.getRevocationListEntries()).thenReturn(
        List.of(
            new RevocationEntry("7805b250c759584".getBytes(), "0a".getBytes(), "MEyOtQZC1g6sMVle2FP5cA==".getBytes())));
  }

  @Test
  void shouldCreateCorrectFileStructureForValueSets() {
    Directory<WritableOnDisk> dccRevocationDirectory = underTest.getDccRevocationDirectory();
    dccRevocationDirectory.prepare(new ImmutableStack<>());

    assertEquals("version", dccRevocationDirectory.getName());
    DirectoryOnDisk v1Directory = (DirectoryOnDisk) dccRevocationDirectory.getWritables().stream()
        .filter(writableOnDisk -> writableOnDisk instanceof DirectoryOnDisk).iterator().next();
    assertEquals("v1", v1Directory.getName());
    DirectoryOnDisk dccDirectory = (DirectoryOnDisk) v1Directory.getWritables().stream()
        .filter(writableOnDisk -> writableOnDisk instanceof DirectoryOnDisk).iterator().next();
    assertEquals("dcc-rl", dccDirectory.getName());

    List<String> kidTypeDirectoriesName = dccRevocationDirectory.getWritables().stream()
        .map(Writable::getName).collect(Collectors.toList());
    assertNotNull(kidTypeDirectoriesName);

    dccDirectory.getWritables().stream()
        .filter(writableOnDisk -> writableOnDisk instanceof DirectoryOnDisk)
        // iterate through dcc-rl directory structure
        .map(directory -> ((DirectoryOnDisk) directory).getWritables().stream()
            .filter(writableOnDisk -> writableOnDisk instanceof DirectoryOnDisk).iterator().next())
        // iterate through xhash directory structure
        .map(xdirectory -> ((DirectoryOnDisk) xdirectory).getWritables().stream()
            .filter(writableOnDisk -> writableOnDisk instanceof DirectoryOnDisk).iterator().next())
        // iterate through yhash directory structure
        .map(ydirectory -> ((DirectoryOnDisk) ydirectory).getWritables().iterator().next())
        // verify archive name for yhash directory
        .forEach(archive -> {
          assertEquals("chunk", archive.getName());
          List<String> archiveContent = ((DistributionArchiveSigningDecorator) archive).getWritables().stream()
              .map(Writable::getName).collect(Collectors.toList());
          assertTrue((archiveContent).containsAll(Set.of("export.bin", "export.sig")));
        });
  }

  @Test
  void coverFetchDccRevocationList() {
    underTest.fetchDccRevocationList();
  }

  @Test
  void coverFetchDccRevocationListException() throws Exception {
    doThrow(RuntimeException.class).when(dccRevocationListService).store(anyList());
    underTest.fetchDccRevocationList();
  }
}
