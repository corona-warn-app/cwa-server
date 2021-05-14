package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static app.coronawarn.server.services.distribution.common.Helpers.getExpectedDateAndHourFiles;
import static java.io.File.separator;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"no-hour-retention"})
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class,
    KeySharingPoliciesChecker.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class DiagnosisKeysDateDirectoryTest {

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
    TimeUtils.setNow(Instant.EPOCH.plus(10, ChronoUnit.DAYS));
  }

  @AfterEach
  void tearDown() {
    TimeUtils.setNow(null);
  }

  private void runDateDistribution(Collection<DiagnosisKey> diagnosisKeys, LocalDateTime distributionTime) {
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig, sharingPoliciesChecker);
    bundler
        .setDiagnosisKeys(diagnosisKeys, distributionTime);
    DiagnosisKeysDateDirectory dateDirectory = new DiagnosisKeysDateDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    Directory<WritableOnDisk> outputDirectory = new DirectoryOnDisk(outputFile);
    outputDirectory.addWritable(dateDirectory);
    dateDirectory.prepare(new ImmutableStack<>()
        .push("version-directory")
        .push("country-directory")
        .push("DE"));
    outputDirectory.write();
  }

  @Test
  void testCreatesCorrectDirectoryStructureForMultipleDates() {
    Collection<DiagnosisKey> diagnosisKeys = IntStream.range(0, 3)
        .mapToObj(currentDate -> IntStream.range(0, 5)
            .mapToObj(currentHour ->
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
            .flatMap(List::stream)
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runDateDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 6, 0, 0));
    Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
    Set<String> expectedDateAndHourFiles = getExpectedDateAndHourFiles(Map.of(
        "1970-01-03", listOfHoursAsStrings(0, 23),
        "1970-01-04", listOfHoursAsStrings(0, 23),
        "1970-01-05", listOfHoursAsStrings(0, 23)),
        "1970-01-06");
    assertThat(actualFiles).isEqualTo(expectedDateAndHourFiles);
  }

  @Test
  void testDoesNotIncludeCurrentDateInDirectoryStructure() {
    Collection<DiagnosisKey> diagnosisKeys = IntStream.range(0, 3)
        .mapToObj(currentDate -> IntStream.range(0, 5)
            .mapToObj(currentHour ->
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
            .flatMap(List::stream)
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runDateDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 12, 0));
    Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
    Set<String> expectedDateAndHourFiles = getExpectedDateAndHourFiles(Map.of(
        "1970-01-03", listOfHoursAsStrings(0, 23),
        "1970-01-04", listOfHoursAsStrings(0, 23),
        "1970-01-05", listOfHoursAsStrings(0, 11)), "1970-01-05");
    assertThat(actualFiles).isEqualTo(expectedDateAndHourFiles);
  }

  @Test
  void testIncludesEmptyDatesInDirectoryStructure() {
    Collection<DiagnosisKey> diagnosisKeys = IntStream.range(0, 3)
        .filter(currentDate -> currentDate != 1)
        .mapToObj(currentDate -> IntStream.range(0, 5)
            .mapToObj(currentHour ->
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
            .flatMap(List::stream)
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .collect(Collectors.toList());

    runDateDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 12, 0));
    Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
    Set<String> expectedDateAndHourFiles = getExpectedDateAndHourFiles(Map.of(
        "1970-01-03", listOfHoursAsStrings(0, 23),
        "1970-01-04", listOfHoursAsStrings(0, 23),
        "1970-01-05", listOfHoursAsStrings(0, 11)), "1970-01-05");
    assertThat(actualFiles).isEqualTo(expectedDateAndHourFiles);
  }

  @Test
  void testIncludesDatesWithFewerKeysThanThresholdInDirectoryStructure() {
    Collection<DiagnosisKey> diagnosisKeys = List.of(
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 1, 0), 4),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 5, 1, 0), 5))
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runDateDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 6, 12, 0));
    Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
    Set<String> expectedDateAndHourFiles = getExpectedDateAndHourFiles(Map.of(
        "1970-01-03", listOfHoursAsStrings(1, 23),
        "1970-01-04", listOfHoursAsStrings(0, 23),
        "1970-01-05", listOfHoursAsStrings(0, 23),
        "1970-01-06", listOfHoursAsStrings(0, 11)), "1970-01-06");
    assertThat(actualFiles).isEqualTo(expectedDateAndHourFiles);
  }

  @Test
  void testDoesNotIncludeDatesInTheFuture() {
    Collection<DiagnosisKey> diagnosisKeys = List.of(
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 1, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 5, 1, 0), 5))
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runDateDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 4, 12, 0));
    Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
    Set<String> expectedDateAndHourFiles = getExpectedDateAndHourFiles(Map.of(
        "1970-01-03", listOfHoursAsStrings(1, 23),
        "1970-01-04", listOfHoursAsStrings(0, 11)), "1970-01-04");
    assertThat(actualFiles).isEqualTo(expectedDateAndHourFiles);
  }

  @Test
  void testWhenDemoProfileIsActiveItDoesIncludeCurrentDateInDirectoryStructure() {
    // set the incomplete days configuration for this particular test but revert before test ends
    // such that other tests are
    Boolean currentIncompleteDaysConfig = distributionServiceConfig.getIncludeIncompleteDays();
    distributionServiceConfig.setIncludeIncompleteDays(true);
    Collection<DiagnosisKey> diagnosisKeys = IntStream.range(0, 3)
        .mapToObj(currentDate -> IntStream.range(0, 5)
            .mapToObj(currentHour ->
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
            .flatMap(List::stream)
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    runDateDistribution(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 12, 0));
    Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());
    Set<String> expectedDateAndHourFiles = getExpectedDateAndHourFiles(Map.of(
        "1970-01-03", listOfHoursAsStrings(0, 23),
        "1970-01-04", listOfHoursAsStrings(0, 23),
        "1970-01-05", listOfHoursAsStrings(0, 11)), "1970-01-05");
    expectedDateAndHourFiles.addAll(Set.of(
        String.join(separator, "date", "1970-01-05", "index"),
        String.join(separator, "date", "1970-01-05", "index.checksum")));

    distributionServiceConfig.setIncludeIncompleteDays(currentIncompleteDaysConfig);
    assertThat(actualFiles).isEqualTo(expectedDateAndHourFiles);
  }

  private static List<String> listOfHoursAsStrings(int from, int until) {
    return IntStream.range(from, until + 1).mapToObj(String::valueOf).collect(Collectors.toList());
  }

}
