package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.integration;

import static java.io.File.separator;
import static java.io.File.separatorChar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.component.TraceTimeIntervalWarningsStructureProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.vault.config.VaultAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {Application.class}, initializers = ConfigDataApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = VaultAutoConfiguration.class)
@ActiveProfiles("fake-dcc-client")
class TraceTimeIntervalWarningsDistributionIT {

  @Autowired
  private TraceTimeIntervalWarningService traceTimeIntervalWarningService;

  @Autowired
  private TraceTimeIntervalWarningsStructureProvider traceTimeIntervalWarningsStructureProvider;

  @MockBean
  private ObjectStoreAccess objectStoreAccess;

  @MockBean
  private OutputDirectoryProvider outputDirectoryProvider;

  @Rule
  private TemporaryFolder tempFolder = new TemporaryFolder();

  private static final String PARENT_DIRECTORY = "parent";
  private static final String VERSION = "version";

  @BeforeEach
  public void setup() throws Exception {
    tempFolder.create();
    File outputDirectory = tempFolder.newFolder(PARENT_DIRECTORY);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
  }

  @Test
  void testIndicesAreOldestAndLatestForMultipleSubmissions() throws Exception {
    // given
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer excludedCurrentHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));
    Integer oldestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(10).toEpochSecond(ZoneOffset.UTC));
    Integer latestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(1).toEpochSecond(ZoneOffset.UTC));

    List<CheckIn> checkIns = Helpers.buildCheckIns(5, 10, 30);
    List<CheckIn> additionalCheckIns = Helpers.buildCheckIns(5, 10, 30);
    List<CheckIn> anotherCheckIns = Helpers.buildCheckIns(5, 10, 30);

    traceTimeIntervalWarningService
        .saveCheckins(checkIns, excludedCurrentHour, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    traceTimeIntervalWarningService
        .saveCheckins(additionalCheckIns, oldestHour, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    traceTimeIntervalWarningService
        .saveCheckins(anotherCheckIns, latestHour, SubmissionType.SUBMISSION_TYPE_PCR_TEST);

    // when
    final Directory<WritableOnDisk> traceWarningsDirectory = traceTimeIntervalWarningsStructureProvider
        .getTraceWarningsDirectory();
    final Directory<WritableOnDisk> directory = outputDirectoryProvider.getDirectory();
    final Directory<WritableOnDisk> version = new DirectoryOnDisk("version");
    final Directory<WritableOnDisk> v1 = new DirectoryOnDisk("v1");
    directory.addWritable(version);
    version.addWritable(v1);
    v1.addWritable(traceWarningsDirectory);

    ImmutableStack<Object> indices = new ImmutableStack<>();
    indices = indices.push("v1");
    directory.prepare(indices);
    directory.write();

    // then
    Set<String> expectedPaths = new java.util.HashSet<>(
        Set.of(PARENT_DIRECTORY, StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION, "v1", "twp"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country", "DE"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country", "DE", "hour"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country", "index"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country", "index.checksum"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country", "DE", "hour", "index"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION,"v1", "twp", "country", "DE", "hour", "index.checksum")));
    IntStream.range(oldestHour, latestHour + 1).forEach(hour -> {
      expectedPaths.add(StringUtils.joinWith(separator, PARENT_DIRECTORY, VERSION, "v1", "twp", "country", "DE", "hour", hour));
    });
    Set<String> actualFiles = Helpers.getSubFoldersPaths(tempFolder.getRoot().getAbsolutePath(), PARENT_DIRECTORY);
    actualFiles.addAll(Helpers.getFilePaths(tempFolder.getRoot(), tempFolder.getRoot().getAbsolutePath()));
    final List<Integer> excludedCurrentHourEmptyList = actualFiles.stream()
        .filter(path -> hasSuffix(path, excludedCurrentHour))
        .map(this::extractSubmissionHour)
        .collect(Collectors.toList());
    List<Integer> oldestAndLatestTimeStamps = actualFiles.stream()
        .filter(path -> hasSuffix(path, latestHour, oldestHour))
        .map(this::extractSubmissionHour).collect(Collectors.toList());

    assertThat(excludedCurrentHourEmptyList).isEmpty();
    assertThat(oldestAndLatestTimeStamps).containsExactlyInAnyOrder(latestHour, oldestHour);
    assertThat(actualFiles).containsAll(expectedPaths);
  }

  @Test
  void testIndicesAreOldestAndLatestForMultipleEncryptedSubmissions() throws Exception {
    // given
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer excludedCurrentHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));
    Integer oldestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(10).toEpochSecond(ZoneOffset.UTC));
    Integer latestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(1).toEpochSecond(ZoneOffset.UTC));

    List<CheckInProtectedReport> checkIns = Helpers.buildCheckInProtectedReports(30);
    List<CheckInProtectedReport> additionalCheckIns = Helpers.buildCheckInProtectedReports(30);
    List<CheckInProtectedReport> anotherCheckIns = Helpers.buildCheckInProtectedReports(30);

    traceTimeIntervalWarningService
        .saveCheckInProtectedReports(checkIns, excludedCurrentHour);
    traceTimeIntervalWarningService
        .saveCheckInProtectedReports(additionalCheckIns, oldestHour);
    traceTimeIntervalWarningService
        .saveCheckInProtectedReports(anotherCheckIns, latestHour);

    // when
    // - create protected reports directory
    // - add all to the test parent folder
    final Directory<WritableOnDisk> traceWarningsDirectory = traceTimeIntervalWarningsStructureProvider
        .getCheckInProtectedReportsDirectory();
    final Directory<WritableOnDisk> directory = outputDirectoryProvider.getDirectory();
    directory.addWritable(traceWarningsDirectory);

    // Prepare indices.
    // In this case push the version ("v2" for encrypted checkins) onto the indices to indicate that the root starts with "v2".
    // This is needed for the correct handling of the version.
    ImmutableStack<Object> indices = new ImmutableStack<>();
    indices = indices.push("v2");
    directory.prepare(indices);
    directory.write();

    Set<String> expectedPaths = new java.util.HashSet<>(
        Set.of(PARENT_DIRECTORY, StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "DE"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "DE", "hour"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "index"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "index.checksum"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "DE", "hour", "index"),
            StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "DE", "hour", "index.checksum")));
    IntStream.range(oldestHour, latestHour + 1).forEach(hour -> {
      expectedPaths.add(StringUtils.joinWith(separator, PARENT_DIRECTORY, "twp", "country", "DE", "hour", hour));
    });
    Set<String> actualFiles = Helpers.getSubFoldersPaths(tempFolder.getRoot().getAbsolutePath(), PARENT_DIRECTORY);
    actualFiles.addAll(Helpers.getFilePaths(tempFolder.getRoot(), tempFolder.getRoot().getAbsolutePath()));
    final List<Integer> excludedCurrentHourEmptyList = actualFiles.stream()
        .filter(path -> hasSuffix(path, excludedCurrentHour))
        .map(this::extractSubmissionHour)
        .collect(Collectors.toList());
    List<Integer> oldestAndLatestTimeStamps = actualFiles.stream()
        .filter(path -> hasSuffix(path, latestHour, oldestHour))
        .map(this::extractSubmissionHour).collect(Collectors.toList());
    final List<Integer> actualHourlyFilesBetweenOldestAndLatest = actualFiles.stream()
        .filter(path -> Pattern.matches("^[a-zA-Z\\/]+\\d+$", path))
        .map(this::extractSubmissionHour)
        .filter(submissionTimestamp -> oldestHour <= submissionTimestamp && latestHour >= submissionTimestamp)
        .collect(Collectors.toList());

    assertThat(excludedCurrentHourEmptyList).isEmpty();
    assertThat(oldestAndLatestTimeStamps).containsExactlyInAnyOrder(latestHour, oldestHour);
    assertThat(actualFiles).containsAll(expectedPaths);
    Integer[] expectedHourlyFiles = IntStream.range(oldestHour, latestHour + 1).boxed().toArray(Integer[]::new);
    assertThat(actualHourlyFilesBetweenOldestAndLatest)
        .containsExactlyInAnyOrder(expectedHourlyFiles);
  }

  private int extractSubmissionHour(String path) {
    final String[] split = path.split(separatorChar == '\\' ? "\\" + separator : separator);
    return Integer.parseInt(split[split.length - 1]);
  }

  private boolean hasSuffix(String path, Integer... submissionHours) {
    return Arrays.stream(submissionHours)
        .map(it -> path
            .endsWith(it.toString())).reduce((a, b) -> a || b).orElse(Boolean.FALSE);
  }
}
