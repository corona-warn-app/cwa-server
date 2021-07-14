package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.common.shared.util.TimeUtils.getCurrentUtcHour;
import static app.coronawarn.server.common.shared.util.TimeUtils.getUtcDate;
import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static app.coronawarn.server.services.distribution.common.Helpers.getExpectedHourFiles;
import static app.coronawarn.server.services.distribution.common.Helpers.getFilePaths;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.junit.DisabledAroundMidnight;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
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
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
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
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class, KeySharingPoliciesChecker.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class DiagnosisKeysHourDirectoryTest {

  @Rule
  private final TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  KeySharingPoliciesChecker sharingPoliciesChecker;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  private File outputFile;

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
  }

  private void runHourDistribution(Collection<DiagnosisKey> diagnosisKeys, LocalDateTime distributionTime,
      LocalDate keysSubmissionDate) {
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig, sharingPoliciesChecker);
    bundler.setDiagnosisKeys(diagnosisKeys, distributionTime);
    DiagnosisKeysHourDirectory hourDirectory = new DiagnosisKeysHourDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    Directory<WritableOnDisk> outputDirectory = new DirectoryOnDisk(outputFile);
    outputDirectory.addWritable(hourDirectory);
    hourDirectory.prepare(new ImmutableStack<>()
            .push("version-directory")
            .push("country-directory")
            .push("DE")
            .push(keysSubmissionDate)
        // date-directory
    );
    outputDirectory.write();
  }

  @Test
  void testCreatesCorrectStructureForMultipleHours() {
    Collection<DiagnosisKey> diagnosisKeys = IntStream.range(0, 5)
        .mapToObj(currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runHourDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 4, 0, 0),
        LocalDate.of(1970, 1, 3));
    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());
    assertThat(actualFiles).isEqualTo(getExpectedHourFiles(
        IntStream.range(0, 24).mapToObj(String::valueOf).collect(Collectors.toSet())));
  }

  @Test
  void testDoesNotIncludeCurrentHour() {
    Collection<DiagnosisKey> diagnosisKeys = IntStream.range(0, 5)
        .mapToObj(currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runHourDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 4, 0),
        LocalDate.of(1970, 1, 3));
    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());
    assertThat(actualFiles).isEqualTo(getExpectedHourFiles(Set.of("0", "1", "2", "3")));
  }

  @Test
  void testDoesNotIncludeHoursInTheFuture() {
    Collection<DiagnosisKey> diagnosisKeys = List.of(
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 2, 0), 5))
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runHourDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 1, 0),
        LocalDate.of(1970, 1, 3));
    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());
    assertThat(actualFiles).isEqualTo(getExpectedHourFiles(Set.of("0")));
  }

  @Test
  @DisabledAroundMidnight(offsetInMinutes = 3 * 60 + 1)
  void testDistributionTimeIsNowItDoesIncludeCurrentHour() {
    final LocalDateTime nowUtc = getCurrentUtcHour();
    Collection<DiagnosisKey> diagnosisKeys = List.of(
        buildDiagnosisKeys(6, nowUtc.minusHours(3), 5),
        buildDiagnosisKeys(6, nowUtc.minusHours(2), 5),
        buildDiagnosisKeys(6, nowUtc.minusHours(1), 5))
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runHourDistribution(diagnosisKeys, nowUtc, getUtcDate());
    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());
    assertThat(actualFiles).isEqualTo(getExpectedHourFiles(Set.of(
        String.valueOf(nowUtc.minusHours(3).getHour()),
        String.valueOf(nowUtc.minusHours(2).getHour()),
        String.valueOf(nowUtc.minusHours(1).getHour())
    )));
  }
}
