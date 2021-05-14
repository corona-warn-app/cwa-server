package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.common.Helpers.buildTraceTimeIntervalWarning;
import static app.coronawarn.server.services.distribution.common.Helpers.getFilePaths;
import static app.coronawarn.server.services.distribution.common.Helpers.getSubFoldersPaths;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.ProdTraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.transformation.EnfParameterAdapter;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(
    value = {DistributionServiceConfig.class, TransmissionRiskLevelEncoding.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CryptoProvider.class, DistributionServiceConfig.class,
        KeySharingPoliciesChecker.class, EnfParameterAdapter.class,},
    initializers = ConfigDataApplicationContextInitializer.class)
public class TraceTimeIntervalWarningsStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";
  private static final String SEPARATOR = File.separator;
  private static final String DS_STORE = ".DS_Store";

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Mock
  TraceTimeIntervalWarningService traceTimeWarningService;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  TraceTimeIntervalWarningsPackageBundler bundler;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    bundler = new ProdTraceTimeIntervalWarningsPackageBundler(distributionServiceConfig);

    // create a specific test folder for later assertions of structures.
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
  }

  @Test
  void should_create_correct_top_parent_folder_for_warnings_file_structure() {
    when(traceTimeWarningService.getTraceTimeIntervalWarnings()).thenReturn(Collections.emptyList());
    TraceTimeIntervalWarningsStructureProvider distributionStructureProvider =
        new TraceTimeIntervalWarningsStructureProvider(traceTimeWarningService, bundler,
            cryptoProvider, distributionServiceConfig);
    Directory<WritableOnDisk> traceWarnings = distributionStructureProvider.getTraceWarningsDirectory();
    assertEquals("twp", traceWarnings.getName());
  }

  @Test
  void should_create_hourly_packages_for_new_checkins_data() throws IOException {
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer oldestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(10).toEpochSecond(ZoneOffset.UTC));
    Integer newestNotCurrentHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(1).toEpochSecond(ZoneOffset.UTC));
    Integer currentHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));

    List<TraceTimeIntervalWarning> traceWarnings =
        buildTraceTimeIntervalWarning(5, 10, oldestHour, 30);
    traceWarnings.addAll(buildTraceTimeIntervalWarning(70, 100, newestNotCurrentHour, 30));
    traceWarnings.addAll(buildTraceTimeIntervalWarning(90, 160, currentHour, 30));

    writeDirectories(traceWarnings);

    Set<String> expectedPaths = Set.of(
        PARENT_TEST_FOLDER,
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
            oldestHour),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
            newestNotCurrentHour),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index.checksum"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index.checksum"));

    Set<String> actualFiles =
        getSubFoldersPaths(testOutputFolder.getRoot().getAbsolutePath(), PARENT_TEST_FOLDER);
    actualFiles.addAll(getFilePaths(testOutputFolder.getRoot(), testOutputFolder.getRoot().getAbsolutePath()));

    expectedPaths.stream().forEach(expected -> {
      assertTrue(actualFiles.contains(expected), "Should contain " + expected);
    });

    // Newest hour path should not be in the package structure since it is the current hour
    assertFalse(actualFiles.contains(StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
            currentHour)), "Should NOT contain current hour");
  }

  @Test
  void should_not_publish_packages_with_warnings_submitted_in_the_future() throws IOException {
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer oldestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(10).toEpochSecond(ZoneOffset.UTC));
    Integer middleHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(5).toEpochSecond(ZoneOffset.UTC));
    Integer futureHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.plusHours(2).toEpochSecond(ZoneOffset.UTC));

    List<TraceTimeIntervalWarning> traceWarnings =
        buildTraceTimeIntervalWarning(5, 10, oldestHour, 30);
    traceWarnings.addAll(buildTraceTimeIntervalWarning(70, 100, middleHour, 30));
    traceWarnings.addAll(buildTraceTimeIntervalWarning(90, 160, futureHour, 30));

    writeDirectories(traceWarnings);

    Set<String> expectedPaths = Set.of(
        PARENT_TEST_FOLDER,
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
            oldestHour),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
            middleHour),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index.checksum"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index.checksum"));

    Set<String> actualFiles =
        getSubFoldersPaths(testOutputFolder.getRoot().getAbsolutePath(), PARENT_TEST_FOLDER);
    actualFiles.addAll(getFilePaths(testOutputFolder.getRoot(), testOutputFolder.getRoot().getAbsolutePath()));

    expectedPaths.stream().forEach(expected -> {
      assertTrue(actualFiles.contains(expected));
    });

    assertFalse(actualFiles.contains(StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp",
        "country", "DE", "hour", futureHour)));
  }

  @Test
  void should_not_publish_packages_with_warnings_submitted_too_far_in_the_past() throws IOException {
    Integer threshold = distributionServiceConfig.getRetentionDays();
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer tooFarBackHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusDays(threshold).toEpochSecond(ZoneOffset.UTC));
    Integer newerHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(5).toEpochSecond(ZoneOffset.UTC));

    List<TraceTimeIntervalWarning> traceWarnings =
        buildTraceTimeIntervalWarning(5, 10, tooFarBackHour, 30);
    traceWarnings.addAll(buildTraceTimeIntervalWarning(70, 100, newerHour, 30));

    writeDirectories(traceWarnings);

    Set<String> expectedPaths = Set.of(
        PARENT_TEST_FOLDER,
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
            newerHour),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index.checksum"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index.checksum"));

    Set<String> actualFiles =
        getSubFoldersPaths(testOutputFolder.getRoot().getAbsolutePath(), PARENT_TEST_FOLDER);
    actualFiles.addAll(getFilePaths(testOutputFolder.getRoot(), testOutputFolder.getRoot().getAbsolutePath()));

    expectedPaths.stream().forEach(expected -> {
      assertTrue(actualFiles.contains(expected));
    });

    assertFalse(actualFiles.contains(StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp",
        "country", "DE", "hour", tooFarBackHour)));
  }

  /**
   * If there are no TraceTimeIntervalWarnings to be published and the package would be empty, index files (not a zip
   * file with signature!) shall be published on CDN instead with HTTP response header cwa-empty-pkg: 1 (to indicate
   * that the package is empty).
   */
  @Test
  void should_publish_only_index_files_when_no_available_checkins_data() throws IOException {
    writeDirectories(Collections.emptyList());

    Set<String> expectedPaths = Set.of(
        PARENT_TEST_FOLDER,
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index.checksum"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index.checksum"));

    Set<String> actualFiles =
        getSubFoldersPaths(testOutputFolder.getRoot().getAbsolutePath(), PARENT_TEST_FOLDER);
    actualFiles.addAll(getFilePaths(testOutputFolder.getRoot(), testOutputFolder.getRoot().getAbsolutePath()));

    String[] expected = expectedPaths.toArray(new String[expectedPaths.size()]);
    String[] actual = actualFiles.toArray(new String[expectedPaths.size()]);
    Arrays.sort(expected);
    Arrays.sort(actual);
    assertArrayEquals(expected, actual);
  }


  @Test
  void should_create_all_hourly_packages_for_new_checkins_data() throws IOException {
    int numberOfHourlyPackages = 10;
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer oldestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(numberOfHourlyPackages).toEpochSecond(ZoneOffset.UTC));
    Integer newestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));

    List<TraceTimeIntervalWarning> traceWarnings =
        buildTraceTimeIntervalWarning(5, 10, oldestHour, 30);
    traceWarnings.addAll(buildTraceTimeIntervalWarning(90, 160, newestHour, 30));

    writeDirectories(traceWarnings);

    Set<String> expectedHourlyPackagesPaths = new HashSet<>();
    IntStream.range(0, numberOfHourlyPackages + 1).forEach(hourlyPackage -> {
      final String hourDirectory = StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
          oldestHour + hourlyPackage);
      final String hourIndexDirectory = StringUtils
          .joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
              oldestHour + hourlyPackage, "index");
      final String hourIndexChecksumDirectory = StringUtils
          .joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour",
              oldestHour + hourlyPackage, "index.checksum");
      expectedHourlyPackagesPaths.addAll(Set.of(hourDirectory, hourIndexDirectory, hourIndexChecksumDirectory));
    });

    Set<String> expectedPaths = Stream.of(
        PARENT_TEST_FOLDER,
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "index.checksum"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index"),
        StringUtils.joinWith(SEPARATOR, PARENT_TEST_FOLDER, "twp", "country", "DE", "hour", "index.checksum")).collect(
        Collectors.toSet());
    expectedPaths.addAll(expectedHourlyPackagesPaths);

    Set<String> actualFiles =
        getSubFoldersPaths(testOutputFolder.getRoot().getAbsolutePath(), PARENT_TEST_FOLDER).stream()
            .filter(this::isNotDsStore).collect(
            Collectors.toSet());
    actualFiles.addAll(getFilePaths(testOutputFolder.getRoot(), testOutputFolder.getRoot().getAbsolutePath()));
    actualFiles.forEach(actual -> assertTrue(expectedPaths.contains(actual)));
  }

  private boolean isNotDsStore(String it) {
    return !it.endsWith(DS_STORE);
  }

  private void writeDirectories(List<TraceTimeIntervalWarning> traceWarnings) throws IOException {
    when(traceTimeWarningService.getTraceTimeIntervalWarnings()).thenReturn(traceWarnings);
    Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
    TraceTimeIntervalWarningsStructureProvider distributionStructureProvider =
        new TraceTimeIntervalWarningsStructureProvider(traceTimeWarningService, bundler,
            cryptoProvider, distributionServiceConfig);
    outputDirectory.addWritable(distributionStructureProvider.getTraceWarningsDirectory());
    outputDirectoryProvider.clear();
    outputDirectory.prepare(new ImmutableStack<>());
    outputDirectory.write();
  }
}
