package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static java.io.File.separator;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class DiagnosisKeysHourDirectoryTest {

  @Rule
  private final TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  private File outputFile;

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysAndExpectedFiles")
  void checkBuildsTheCorrectHourDirectoryStructure(TestTuple testTuple) {

    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);

    bundler.setDiagnosisKeys(testTuple.diagnosisKeys,
        testTuple.distributionRun);

    DiagnosisKeysHourDirectory hourDirectory = new DiagnosisKeysHourDirectory(bundler, cryptoProvider,
        distributionServiceConfig);

    Directory<WritableOnDisk> outputDirectory = new DirectoryOnDisk(outputFile);

    outputDirectory.addWritable(hourDirectory);

    hourDirectory.prepare(new ImmutableStack<>()
        .push("version-directory")
        .push("country-directory")
        .push(LocalDate.of(1970, 1, 3))//date-directory
    );
    outputDirectory.write();

    final Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());

    Assertions.assertThat(actualFiles).isEqualTo(testTuple.expectedFiles);
  }

  private static Stream<Arguments> createDiagnosisKeysAndExpectedFiles() {
    return Stream.of(new TestTuple(
            IntStream.range(0, 5)
                .mapToObj(
                    currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
                .flatMap(List::stream)
                .collect(Collectors.toList()),
            getExpectedFiles(Set.of("0", "1", "2", "3", "4")),
            LocalDateTime.of(1970, 1, 5, 0, 0)
        ),
        new TestTuple(
            IntStream.range(0, 5)
                .filter(currentHour -> currentHour != 3)
                .mapToObj(
                    currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
                .flatMap(List::stream)
                .collect(Collectors.toList()),
            getExpectedFiles(Set.of("0", "1", "2", "4")),
            LocalDateTime.of(1970, 1, 5, 0, 0)
        ),
        new TestTuple(
            IntStream.range(0, 5)
                .mapToObj(
                    currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
                .flatMap(List::stream)
                .collect(Collectors.toList()),
            getExpectedFiles(Set.of("0", "1", "2", "3")),
            LocalDateTime.of(1970, 1, 3, 4, 0)
        )
    )
        .map(Arguments::of);
  }

  private static Set<String> getExpectedFiles(Collection<String> hours) {

    return hours.stream()
        .map(hour -> Set.of(
            String.join(separator, "hour", hour, "index"),
            String.join(separator, "hour", hour, "index.checksum")))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }


  private static class TestTuple {

    Collection<DiagnosisKey> diagnosisKeys;
    Collection<String> expectedFiles;
    LocalDateTime distributionRun;

    public TestTuple(Collection<DiagnosisKey> diagnosisKeys, Collection<String> expectedFiles,
        LocalDateTime distributionRun) {
      this.diagnosisKeys = diagnosisKeys;
      this.expectedFiles = expectedFiles;
      this.distributionRun = distributionRun;
    }

  }

}
