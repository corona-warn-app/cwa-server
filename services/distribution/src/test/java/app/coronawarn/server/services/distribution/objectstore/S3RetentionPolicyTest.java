package app.coronawarn.server.services.distribution.objectstore;

import static app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils.getUtcDate;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  private List<String> generateHourFilesForDay(LocalDate day, String country) {
    List<String> files = new ArrayList<>();
    for (var i = 0; i <= 24; i++) {
      files.add(generateHourFilename(day, country, Integer.toString(i)));
    }
    return files;
  }

  private List<S3Object> s3ObjectsFromFilenames(List<String> filenames) {
    return filenames.stream().map(S3Object::new).collect(Collectors.toUnmodifiableList());
  }

  @Test
  void shouldDeleteHourFile() {
    var toBeKept = generateHourFilesForDay(getUtcDate().minusDays(1), "DE");
    var toBeDeleted = generateHourFilesForDay(getUtcDate().minusDays(2), "DE");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(Stream.concat(toBeDeleted.stream(), toBeKept.stream())
        .collect(Collectors.toUnmodifiableList()));

    when(objectStoreAccess.getObjectsWithPrefix(eq(this.getPrefix("DE")))).thenReturn(mockResponse);
    s3RetentionPolicy.applyHourFileRetentionPolicy(2);

    toBeDeleted.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(f)));
    toBeKept.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(eq(f)));
  }

  @Test
  void shouldDeleteHourFileFromAllBuckets() {
    var toBeKeptDE = generateHourFilesForDay(getUtcDate().minusDays(1), "DE");
    var toBeKeptEUR = generateHourFilesForDay(getUtcDate().minusDays(1), "EUR");

    var toBeDeletedDE = generateHourFilesForDay(getUtcDate().minusDays(2), "DE");
    var toBeDeletedEUR = generateHourFilesForDay(getUtcDate().minusDays(2), "EUR");


    List<S3Object> mockResponseDE = this.s3ObjectsFromFilenames(Stream.concat(toBeDeletedDE.stream(), toBeKeptDE.stream())
        .collect(Collectors.toUnmodifiableList()));
    when(objectStoreAccess.getObjectsWithPrefix(eq(this.getPrefix("DE")))).thenReturn(mockResponseDE);

    List<S3Object> mockResponseEUR = this.s3ObjectsFromFilenames(Stream.concat(toBeDeletedEUR.stream(), toBeKeptEUR.stream())
        .collect(Collectors.toUnmodifiableList()));
    when(objectStoreAccess.getObjectsWithPrefix(eq(this.getPrefix("EUR")))).thenReturn(mockResponseEUR);

    s3RetentionPolicy.applyHourFileRetentionPolicy(2);

    toBeDeletedDE.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(f)));
    toBeKeptDE.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(eq(f)));
    toBeDeletedEUR.forEach(f -> verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(f)));
    toBeKeptEUR.forEach(f -> verify(objectStoreAccess, never()).deleteObjectsWithPrefix(eq(f)));
  }

  @Test
  void shouldRemoveIndexFileFromDEBucket() {
    var validHourFile = generateHourIndex(getUtcDate().minusDays(1), "DE");
    var invalidHourFile = generateHourIndex(getUtcDate().minusDays(2), "DE");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(list(validHourFile, invalidHourFile));

    when(objectStoreAccess.getObjectsWithPrefix(eq(this.getPrefix("DE")))).thenReturn(mockResponse);

    s3RetentionPolicy.applyHourFileRetentionPolicy(2);

    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(invalidHourFile));
    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(eq(validHourFile));
  }

  @Test
  void shouldRemoveIndexFileFromEURBucket() {
    var validHourFile = generateHourIndex(getUtcDate().minusDays(1), "EUR");
    var invalidHourFile = generateHourIndex(getUtcDate().minusDays(2), "EUR");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(list(validHourFile, invalidHourFile));

    when(objectStoreAccess.getObjectsWithPrefix(eq(this.getPrefix("EUR")))).thenReturn(mockResponse);

    s3RetentionPolicy.applyHourFileRetentionPolicy(2);

    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(invalidHourFile));
    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(eq(validHourFile));
  }

  @Test
  void shouldRemoveFilesOlderThanCutoffDate() {
    var validHourFile = generateHourIndex(getUtcDate().minusDays(1), "DE");
    var invalidHourFile1 = generateHourIndex(getUtcDate().minusDays(2), "DE");
    var invalidHourFile2 = generateHourIndex(getUtcDate().minusDays(3), "DE");

    List<S3Object> mockResponse = this.s3ObjectsFromFilenames(list(validHourFile, invalidHourFile1, invalidHourFile2));

    when(objectStoreAccess.getObjectsWithPrefix(eq(this.getPrefix("DE")))).thenReturn(mockResponse);

    s3RetentionPolicy.applyHourFileRetentionPolicy(2);

    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(invalidHourFile1));
    verify(objectStoreAccess, times(1)).deleteObjectsWithPrefix(eq(invalidHourFile2));
    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(eq(validHourFile));
  }

  @Test
  void shouldDeleteOldFiles() {
    Collection<String> supportedCounties = asList(distributionServiceConfig.getSupportedCountries());

    Collection<String> expectedFilesToBeDeleted = supportedCounties.stream()
        .map(country -> generateHourFilename(getUtcDate().minusDays(2), country))
        .collect(toList());

    List<S3Object> mockResponse = list(new S3Object("version/v1/configuration/country/DE/app_config"));
    mockResponse.addAll(
        supportedCounties.stream().map(country -> new S3Object(generateHourFilename(getUtcDate(), country)))
            .collect(toList()));
    mockResponse.addAll(expectedFilesToBeDeleted.stream().map(S3Object::new).collect(toList()));

    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(mockResponse);

    s3RetentionPolicy.applyRetentionPolicy(1);

    expectedFilesToBeDeleted.forEach(expectedFileToBeDeleted ->
        verify(objectStoreAccess, atLeastOnce()).deleteObjectsWithPrefix(eq(expectedFileToBeDeleted)));
  }

  @Test
  void shouldNotDeleteFilesIfAllAreValid() {
    Collection<String> supportedCounties = asList(distributionServiceConfig.getSupportedCountries());

    List<S3Object> mockResponse = list(new S3Object("version/v1/configuration/country/DE/app_config"));
    mockResponse.addAll(
        supportedCounties.stream().map(country -> asList(
            new S3Object(generateHourFilename(getUtcDate().minusDays(1), country)),
            new S3Object(generateHourFilename(getUtcDate().plusDays(1), country)),
            new S3Object(generateHourFilename(getUtcDate(), country)))).flatMap(List::stream)
            .collect(toList()));

    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(mockResponse);
    s3RetentionPolicy.applyRetentionPolicy(1);

    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(any());
  }

  @Test
  void deleteDiagnosisKeysUpdatesFailedOperationCounter() {
    doThrow(ObjectStoreOperationFailedException.class).when(objectStoreAccess).deleteObjectsWithPrefix(any());

    s3RetentionPolicy.deleteDiagnosisKey(new S3Object("foo"));

    verify(failedObjectStoreOperationsCounter, times(1))
        .incrementAndCheckThreshold(any(ObjectStoreOperationFailedException.class));
  }

  private String getPrefix(String country) {
    var api = distributionServiceConfig.getApi();
    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getDiagnosisKeysPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getDatePath() + "/";
  }

  private String generateHourIndex(LocalDate date, String country) {
    var api = distributionServiceConfig.getApi();
    return this.getPrefix(country) + date.toString() + "/" + api.getHourPath();
  }

  private String generateHourFilename(LocalDate date, String country) {
    return generateHourFilename(date, country, "0");
  }

  private String generateHourFilename(LocalDate date, String country, String hour) {
    var api = distributionServiceConfig.getApi();
    return this.getPrefix(country) + date.toString() + "/" + api.getHourPath() + "/" + hour;
  }
}
