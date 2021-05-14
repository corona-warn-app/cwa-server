package app.coronawarn.server.services.distribution.objectstore;

import static app.coronawarn.server.common.shared.util.TimeUtils.getCurrentUtcHour;
import static app.coronawarn.server.common.shared.util.TimeUtils.getUtcDate;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {S3RetentionPolicy.class, ObjectStore.class, FailedObjectStoreOperationsCounter.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class S3RetentionPolicyTest {

  @MockBean
  private ObjectStoreAccess objectStoreAccess;

  @MockBean
  private FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  @Autowired
  private S3RetentionPolicy s3RetentionPolicy;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  private LocalDateTime currentTime;

  @BeforeEach
  void setup() {
    currentTime = getCurrentUtcHour();
  }

  @Test
  void shouldDeleteDiagnosisKeyHourFiles() {
    var toBeKept = generateDiagnosisKeyHourFilesForDay(getUtcDate().minusDays(1), "DE");
    var toBeDeleted = generateDiagnosisKeyHourFilesForDay(getUtcDate().minusDays(2), "DE");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(Stream.concat(toBeDeleted.stream(), toBeKept.stream())
        .collect(Collectors.toUnmodifiableList()));

    when(objectStoreAccess.getObjectsWithPrefix(this.getDiagnosisKeyPrefix("DE"))).thenReturn(mockResponse);
    s3RetentionPolicy.applyDiagnosisKeyHourRetentionPolicy(2);

    toBeDeleted.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(f));
    toBeKept.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(f));
  }

  @Test
  void shouldDeleteDiagnosisKeyHourFilesFromAllBuckets() {
    var toBeKeptDE = generateDiagnosisKeyHourFilesForDay(getUtcDate().minusDays(1), "DE");
    var toBeKeptEUR = generateDiagnosisKeyHourFilesForDay(getUtcDate().minusDays(1), "EUR");

    var toBeDeletedDE = generateDiagnosisKeyHourFilesForDay(getUtcDate().minusDays(2), "DE");
    var toBeDeletedEUR = generateDiagnosisKeyHourFilesForDay(getUtcDate().minusDays(2), "EUR");

    List<S3Object> mockResponseDE = this
        .s3ObjectsFromFilenames(Stream.concat(toBeDeletedDE.stream(), toBeKeptDE.stream())
            .collect(Collectors.toUnmodifiableList()));
    when(objectStoreAccess.getObjectsWithPrefix(this.getDiagnosisKeyPrefix("DE"))).thenReturn(mockResponseDE);

    List<S3Object> mockResponseEUR = this
        .s3ObjectsFromFilenames(Stream.concat(toBeDeletedEUR.stream(), toBeKeptEUR.stream())
            .collect(Collectors.toUnmodifiableList()));
    when(objectStoreAccess.getObjectsWithPrefix(this.getDiagnosisKeyPrefix("EUR"))).thenReturn(mockResponseEUR);

    s3RetentionPolicy.applyDiagnosisKeyHourRetentionPolicy(2);

    toBeDeletedDE.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(f));
    toBeKeptDE.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(f));
    toBeDeletedEUR.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(f));
    toBeKeptEUR.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(f));
  }

  @Test
  void shouldRemoveDiagnosisKeyHourIndexFilesFromDEBucket() {
    var validHourFile = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(1), "DE");
    var invalidHourFile = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(2), "DE");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(list(validHourFile, invalidHourFile));

    when(objectStoreAccess.getObjectsWithPrefix(this.getDiagnosisKeyPrefix("DE"))).thenReturn(mockResponse);

    s3RetentionPolicy.applyDiagnosisKeyHourRetentionPolicy(2);

    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(invalidHourFile);
    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(validHourFile);
  }

  @Test
  void shouldRemoveDiagnosisKeyHourIndexFilesFromEURBucket() {
    var validHourFile = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(1), "EUR");
    var invalidHourFile = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(2), "EUR");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(list(validHourFile, invalidHourFile));

    when(objectStoreAccess.getObjectsWithPrefix(this.getDiagnosisKeyPrefix("EUR"))).thenReturn(mockResponse);

    s3RetentionPolicy.applyDiagnosisKeyHourRetentionPolicy(2);

    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(invalidHourFile);
    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(validHourFile);
  }

  @Test
  void shouldRemoveDiagnosisKeyIndexFilesOlderThanCutoffDate() {
    var validHourFile = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(1), "DE");
    var invalidHourFile1 = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(2), "DE");
    var invalidHourFile2 = generateDiagnosisKeyHourIndex(getUtcDate().minusDays(3), "DE");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(list(validHourFile, invalidHourFile1, invalidHourFile2));

    when(objectStoreAccess.getObjectsWithPrefix(this.getDiagnosisKeyPrefix("DE"))).thenReturn(mockResponse);

    s3RetentionPolicy.applyDiagnosisKeyHourRetentionPolicy(2);

    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(invalidHourFile1);
    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(invalidHourFile2);
    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(validHourFile);
  }

  @Test
  void shouldDeleteOldDiagnosisKeyFiles() {
    Collection<String> supportedCounties = asList(distributionServiceConfig.getSupportedCountries());

    Collection<String> expectedFilesToBeDeleted = supportedCounties.stream()
        .map(country -> generateDiagnosisKeyHourFilename(getUtcDate().minusDays(2), country))
        .collect(toList());

    List<S3Object> mockResponse = list(new S3Object("version/v1/configuration/country/DE/app_config"));
    mockResponse.addAll(
        supportedCounties.stream().map(country -> new S3Object(generateDiagnosisKeyHourFilename(getUtcDate(), country)))
            .collect(toList()));
    mockResponse.addAll(expectedFilesToBeDeleted.stream().map(S3Object::new).collect(toList()));

    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(mockResponse);

    s3RetentionPolicy.applyDiagnosisKeyDayRetentionPolicy(1);

    expectedFilesToBeDeleted.forEach(expectedFileToBeDeleted ->
        verify(objectStoreAccess, atLeastOnce()).deleteObjectsWithPrefix(expectedFileToBeDeleted));
  }

  @Test
  void shouldNotDeleteDiagnosisKeyFilesIfAllAreValid() {
    Collection<String> supportedCounties = asList(distributionServiceConfig.getSupportedCountries());

    List<S3Object> mockResponse = list(new S3Object("version/v1/configuration/country/DE/app_config"));
    mockResponse.addAll(
        supportedCounties.stream().map(country -> asList(
            new S3Object(generateDiagnosisKeyHourFilename(getUtcDate().minusDays(1), country)),
            new S3Object(generateDiagnosisKeyHourFilename(getUtcDate().plusDays(1), country)),
            new S3Object(generateDiagnosisKeyHourFilename(getUtcDate(), country)))).flatMap(List::stream)
            .collect(toList()));

    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(mockResponse);
    s3RetentionPolicy.applyDiagnosisKeyDayRetentionPolicy(1);

    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(any());
  }

  @Test
  void deleteDiagnosisKeysUpdatesFailedOperationCounter() {
    doThrow(ObjectStoreOperationFailedException.class).when(objectStoreAccess).deleteObjectsWithPrefix(any());

    s3RetentionPolicy.deleteS3Object(new S3Object("foo"));

    verify(failedObjectStoreOperationsCounter, times(1))
        .incrementAndCheckThreshold(any(ObjectStoreOperationFailedException.class));
  }

  @Test
  void shouldDeleteTraceTimeWarningFilesOlderThanCutoffDate() {
    var toBeKept = generateTraceTimeWarningFilenamesForRange(
        currentTime.minusHours(TimeUnit.DAYS.toHours(2)), currentTime);
    var toBeDeleted = generateTraceTimeWarningFilenamesForRange(
        currentTime.minusHours(TimeUnit.DAYS.toHours(5)), currentTime.minusHours(TimeUnit.DAYS.toHours(2)));

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(Stream.concat(toBeDeleted.stream(), toBeKept.stream())
        .collect(Collectors.toUnmodifiableList()));

    when(objectStoreAccess.getObjectsWithPrefix(this.getTraceTimeWarningPrefix("DE"))).thenReturn(mockResponse);
    s3RetentionPolicy.applyTraceTimeWarningHourRetentionPolicy(2);

    toBeDeleted.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(f));
    toBeKept.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(f));
  }

  private String getDiagnosisKeyPrefix(String country) {
    var api = distributionServiceConfig.getApi();
    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getDiagnosisKeysPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getDatePath() + "/";
  }

  private String getTraceTimeWarningPrefix(String country) {
    var api = distributionServiceConfig.getApi();
    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getTraceWarningsPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getHourPath() + "/";
  }

  private String generateDiagnosisKeyHourIndex(LocalDate date, String country) {
    var api = distributionServiceConfig.getApi();
    return this.getDiagnosisKeyPrefix(country) + date.toString() + "/" + api.getHourPath();
  }

  private String generateDiagnosisKeyHourFilename(LocalDate date, String country) {
    return generateDiagnosisKeyHourFilename(date, country, "0");
  }

  private String generateDiagnosisKeyHourFilename(LocalDate date, String country, String hour) {
    var api = distributionServiceConfig.getApi();
    return this.getDiagnosisKeyPrefix(country) + date.toString() + "/" + api.getHourPath() + "/" + hour;
  }

  private String generateTraceTimeWarningFilename(long epochHour, String country) {
    return this.getTraceTimeWarningPrefix(country) + epochHour;
  }

  private List<String> generateTraceTimeWarningFilenamesForRange(LocalDateTime startInclusive,
      LocalDateTime endExclusive) {
    return LongStream.range(TimeUnit.SECONDS.toHours(startInclusive.toEpochSecond(ZoneOffset.UTC)),
        TimeUnit.SECONDS.toHours(endExclusive.toEpochSecond(ZoneOffset.UTC)))
        .mapToObj(epochHour -> generateTraceTimeWarningFilename(epochHour, "DE"))
        .collect(Collectors.toList());
  }

  private List<String> generateDiagnosisKeyHourFilesForDay(LocalDate day, String country) {
    List<String> files = new ArrayList<>();
    for (var i = 0; i <= 24; i++) {
      files.add(generateDiagnosisKeyHourFilename(day, country, Integer.toString(i)));
    }
    return files;
  }

  private List<S3Object> s3ObjectsFromFilenames(List<String> filenames) {
    return filenames.stream().map(S3Object::new).collect(Collectors.toUnmodifiableList());
  }
}
