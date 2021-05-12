

package app.coronawarn.server.services.distribution.objectstore.integration;

import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.common.DiagnosisTestData;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import app.coronawarn.server.services.distribution.runner.Assembly;
import app.coronawarn.server.services.distribution.runner.RetentionPolicy;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"no-hour-retention", "local-json-stats"})
class ObjectStoreFilePreservationIT extends BaseS3IntegrationTest {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;
  @Autowired
  private TraceTimeIntervalWarningService traceTimeIntervalWarningService;
  @Autowired
  private Assembly fileAssembler;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private S3RetentionPolicy s3RetentionPolicy;
  @Autowired
  private ObjectStoreAccess objectStoreAccess;
  @Autowired
  private DistributionServiceConfig distributionServiceConfig;
  @Autowired
  private StatisticsDownloadService statisticsDownloadService;

  @MockBean
  private OutputDirectoryProvider distributionDirectoryProvider;

  @Rule
  private TemporaryFolder testOutputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    testOutputFolder.create();
    objectStoreAccess.deleteObjectsWithPrefix(distributionServiceConfig.getApi().getVersionPath());
  }

  /**
   * The test covers a behaviour that manifests itself when data retention and shifting policies cause the file
   * distribution logic to generate, in subsequent runs, different content for the same timeframes. Below the
   * distribution problem is described with a concerete daily scenario.
   * <p>
   * The test presumes there are 4 consecutive days with 80 keys submitted daily. Running a distribution in Day 4 would
   * result in:
   * <p>
   * Day 1   -> submission of 80 keys   -> 1 empty file distributed<br> Day 2   -> submission of 80 keys   -> 1
   * distributed containing 160 keys<br> Day 3   -> submission of 80 keys   -> 1 empty file distributed<br> Day 4   ->
   * submission of 80 keys   -> 1 distributed containing 160 keys<br>
   * <p>
   * All day & hour files already generated should not be changed/removed from S3 even after retention policies have
   * been applied and a second distribution is triggered.
   * <p>
   * If for example, data in Day 1 gets removed completely, then a second distribution run causes a shifting of keys in
   * different files compared to the previous run, the result being:
   * <p>
   * Day 2   -> submission of 80 keys   -> 1 empty file generated (different than what is currently on S3)<br> Day 3 ->
   * submission of 80 keys   -> 1 distributed containing 160 keys (different than 1 empty file on S3)<br> Day 4   ->
   * submission of 80 keys   -> 1 empty file (different than 1 empty file on S3)<br>
   */
  @Test
  void files_once_published_to_objectstore_should_not_be_overriden_because_of_retention_or_shifting_policies()
      throws IOException {

    // keep data in the past for this test
    LocalDate testStartDate = LocalDate.now().minusDays(10);
    LocalDate testEndDate = LocalDate.now().minusDays(6);

    // setup the 80 keys per day scenario
    createDiagnosisKeyTestData(testStartDate, testEndDate, 80);

    assembleAndDistribute(testOutputFolder.newFolder("output-before-retention"));
    List<S3Object> filesBeforeRetention = getPublishedFiles();

    triggerRetentionPolicy(testStartDate);

    // Trigger second distribution after data retention policies were applied
    assembleAndDistribute(testOutputFolder.newFolder("output-after-retention"));
    List<S3Object> filesAfterRetention = getPublishedFiles();

    assertPreviouslyPublishedKeyFilesAreTheSame(filesBeforeRetention, filesAfterRetention);
  }

  private List<S3Object> getPublishedFiles() {
    return objectStoreAccess.getObjectsWithPrefix(distributionServiceConfig.getApi().getVersionPath());
  }

  private void assertPreviouslyPublishedKeyFilesAreTheSame(List<S3Object> filesBeforeRetention,
      List<S3Object> filesAfterRetention) {

    Map<String, S3Object> beforeRetentionFileMap = filesBeforeRetention
        .stream()
        .filter(S3Object::isDiagnosisKeyFile)
        .collect(Collectors.toMap(S3Object::getObjectName, s3object -> s3object));

    filesAfterRetention
        .stream()
        .filter(S3Object::isDiagnosisKeyFile)
        .forEach(secondVersion -> {
          S3Object previouslyPublished = beforeRetentionFileMap.get(secondVersion.getObjectName());

          if (filesAreDifferent(previouslyPublished, secondVersion)) {
            throw new AssertionError("Files have been changed on object store "
                + "due to retention policy. Before: " + previouslyPublished.getObjectName()
                + "-" + previouslyPublished.getCwaHash()
                + "| After:" + secondVersion.getObjectName()
                + "-" + secondVersion.getCwaHash());
          }
        });
  }

  private boolean filesAreDifferent(S3Object previouslyPublished, S3Object newVerion) {
    return previouslyPublished == null ||
        !newVerion.getCwaHash().equals(previouslyPublished.getCwaHash());
  }

  /**
   * Remove test data inserted for the given date
   */
  private void triggerRetentionPolicy(LocalDate fromDate) {
    DistributionServiceConfig mockDistributionConfig = new DistributionServiceConfig();
    mockDistributionConfig.setRetentionDays(numberOfDaysSince(fromDate));
    mockDistributionConfig.setObjectStore(distributionServiceConfig.getObjectStore());
    new RetentionPolicy(diagnosisKeyService, traceTimeIntervalWarningService, applicationContext,
        mockDistributionConfig, s3RetentionPolicy, statisticsDownloadService).run(null);
  }

  private Integer numberOfDaysSince(LocalDate testStartDate) {
    return (int) ChronoUnit.DAYS.between(testStartDate, LocalDate.now()) - 1;
  }

  private void createDiagnosisKeyTestData(LocalDate fromDay, LocalDate untilDay, int casesPerDay) {
    DiagnosisTestData testData = DiagnosisTestData.of(fromDay, untilDay, casesPerDay);
    diagnosisKeyService.saveDiagnosisKeys(testData.getDiagnosisKeys());
  }

  private void assembleAndDistribute(File output) throws IOException {
    when(distributionDirectoryProvider.getDirectory()).thenReturn(new DirectoryOnDisk(output));
    when(distributionDirectoryProvider.getFileOnDisk()).thenReturn(output);

    fileAssembler.run(null);

    S3Publisher s3Publisher = new S3Publisher(objectStoreAccess,
        new FailedObjectStoreOperationsCounter(distributionServiceConfig),
        newAsyncExecutor(), distributionServiceConfig);
    s3Publisher.publish(distributionDirectoryProvider.getFileOnDisk().toPath().toAbsolutePath());
  }

  private ThreadPoolTaskExecutor newAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(11);
    executor.initialize();
    return executor;
  }
}
