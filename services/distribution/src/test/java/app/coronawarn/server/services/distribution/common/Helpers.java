package app.coronawarn.server.services.distribution.common;

import static app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader.loadYamlIntoProtobufBuilder;
import static java.io.File.separator;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Helpers {

  public static void prepareAndWrite(Directory<WritableOnDisk> directory) {
    directory.prepare(new ImmutableStack<>());
    directory.write();
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp) {
    return DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(new byte[16], SubmissionType.SUBMISSION_TYPE_PCR_TEST)
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(submissionTimeStamp)
        .withCountryCode("DE")
        .withVisitedCountries(Set.of("DE"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .withDaysSinceOnsetOfSymptoms(1)
        .build();
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(LocalDateTime dateTime) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond(ZoneOffset.UTC) / 3600);
  }

  public static List<DiagnosisKey> buildDiagnosisKeys(
      int startIntervalNumber, LocalDateTime submissionTimestamp, int number) {
    long timestamp = submissionTimestamp.toEpochSecond(ZoneOffset.UTC) / 3600;
    return buildDiagnosisKeys(startIntervalNumber, timestamp, number);
  }

  public static List<DiagnosisKey> buildDiagnosisKeys(
      int startIntervalNumber, LocalDateTime submissionTimestamp, int number, String originCountry,
      Set<String> visitedCountries,
      ReportType reportType,
      int daysSinceOnsetOfSymptoms) {
    long timestamp = submissionTimestamp.toEpochSecond(ZoneOffset.UTC) / 3600;
    return buildDiagnosisKeys(startIntervalNumber, timestamp, number, originCountry, visitedCountries, reportType,
        daysSinceOnsetOfSymptoms);
  }

  public static List<DiagnosisKey> buildDiagnosisKeys(int startIntervalNumber, long submissionTimestamp, int number) {
    return buildDiagnosisKeys(startIntervalNumber, submissionTimestamp, number,
        "DE", Set.of("DE"), ReportType.CONFIRMED_TEST, 1);
  }

  public static List<DiagnosisKey> buildDiagnosisKeys(int startIntervalNumber, long submissionTimestamp,
      int number, Set<String> visitedCountries) {
    return buildDiagnosisKeys(startIntervalNumber, submissionTimestamp, number,
        "DE", visitedCountries, ReportType.CONFIRMED_TEST, 1);
  }

  public static List<DiagnosisKey> buildDiagnosisKeys(int startIntervalNumber,
      long submissionTimestamp,
      int number,
      String originCountry,
      Set<String> visitedCountries,
      ReportType reportType,
      int daysSinceOnsetOfSymptoms) {

    return buildDiagnosisKeys(startIntervalNumber, submissionTimestamp, number, originCountry,
        visitedCountries, reportType, daysSinceOnsetOfSymptoms, 2);
  }

  public static List<DiagnosisKey> buildDiagnosisKeys(int startIntervalNumber,
      long submissionTimestamp,
      int number,
      String originCountry,
      Set<String> visitedCountries,
      ReportType reportType,
      int daysSinceOnsetOfSymptoms,
      int transmissionRiskLevel) {

    return IntStream.range(0, number)
        .mapToObj(ignoredValue ->
        {
          byte[] keyData = new byte[16];
          Random random = new Random();
          random.nextBytes(keyData);

          return DiagnosisKey.builder()
              .withKeyDataAndSubmissionType(keyData, SubmissionType.SUBMISSION_TYPE_PCR_TEST)
              .withRollingStartIntervalNumber(startIntervalNumber)
              .withTransmissionRiskLevel(transmissionRiskLevel)
              .withSubmissionTimestamp(submissionTimestamp)
              .withCountryCode(originCountry)
              .withVisitedCountries(visitedCountries)
              .withReportType(reportType)
              .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
              .build();
        })
        .collect(Collectors.toList()
        );
  }

  public static List<DiagnosisKey> buildDiagnosisKeysWithFlexibleRollingPeriod(
      int startIntervalNumber, long submissionTimestamp, int number, int rollingPeriod) {
    return IntStream.range(0, number)
        .mapToObj(ignoredValue ->
        {
          byte[] keyData = new byte[16];
          Random random = new Random();
          random.nextBytes(keyData);

          return DiagnosisKey.builder()
              .withKeyDataAndSubmissionType(keyData, SubmissionType.SUBMISSION_TYPE_PCR_TEST)
              .withRollingStartIntervalNumber(startIntervalNumber)
              .withTransmissionRiskLevel(2)
              .withSubmissionTimestamp(submissionTimestamp)
              .withRollingPeriod(rollingPeriod)
              .withVisitedCountries(Set.of("DE"))
              .withCountryCode("DE")
              .build();
        })
        .collect(Collectors.toList()
        );
  }

  public static Set<String> getFilePaths(java.io.File root, String basePath) {
    Set<String> files = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isFile)
        .map(File::getAbsolutePath)
        .map(path -> path.substring(basePath.length() + 1))
        .collect(Collectors.toSet());

    Set<java.io.File> directories = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isDirectory)
        .collect(Collectors.toSet());

    Set<String> subFiles = directories.stream()
        .map(subDirectory -> getFilePaths(subDirectory, basePath))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    files.addAll(subFiles);
    return files;
  }

  public static Set<String> getSubFoldersPaths(String basePath, String apiFolder) throws IOException {
    return Files.walk(Paths.get(basePath))
        .filter(Files::isDirectory)
        .map(path -> path.toAbsolutePath().toString())
        .filter(path -> path.contains(apiFolder))
        .map(path -> path.substring(path.indexOf(apiFolder)))
        .collect(Collectors.toSet());
  }

  public static ApplicationConfiguration loadApplicationConfiguration(String path) throws UnableToLoadFileException {
    return loadYamlIntoProtobufBuilder(path, ApplicationConfiguration.Builder.class).build();
  }

  public static Set<String> getExpectedHourFiles(Collection<String> hours) {
    return hours.stream()
        .map(hour -> Set.of(
            String.join(separator, "hour", hour, "index"),
            String.join(separator, "hour", hour, "index.checksum")))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  public static Set<String> getExpectedDateAndHourFiles(Map<String, List<String>> datesAndHours, String currentDate) {
    Set<String> expectedFiles = new HashSet<>();

    datesAndHours.forEach((date, hours) -> {
      if (!date.equals(currentDate)) {
        expectedFiles.add(String.join(separator, "date", date, "index"));
        expectedFiles.add(String.join(separator, "date", date, "index.checksum"));
      }

      expectedFiles.add(String.join(separator, "date", date, "hour", "index"));
      expectedFiles.add(String.join(separator, "date", date, "hour", "index.checksum"));

      hours.forEach(hour -> {
        expectedFiles.add(String.join(separator, "date", date, "hour", hour, "index"));
        expectedFiles.add(String.join(separator, "date", date, "hour", hour, "index.checksum"));
      });
    });

    return expectedFiles;
  }

  public static TraceTimeIntervalWarning buildTraceTimeIntervalWarning(int startIntervalNumber,
      int endIntervalNumber, int submissionHourSinceEpoch) {
    final byte[] guid = UUID.randomUUID().toString().getBytes();
    final int transmissionRiskLevel = 5;
    return new TraceTimeIntervalWarning(guid, startIntervalNumber, endIntervalNumber,
        transmissionRiskLevel, submissionHourSinceEpoch, SubmissionType.SUBMISSION_TYPE_PCR_TEST) {
      @Override
      public Long getId() {
        return Long.valueOf(Arrays.hashCode(guid));
      }
    };
  }

  public static List<TraceTimeIntervalWarning> buildTraceTimeIntervalWarning(
      int startIntervalNumber, int endIntervalNumber, int submissionHourSinceEpoch, int numberOfWarnings) {
    return IntStream.range(0, numberOfWarnings)
        .mapToObj(ignoredValue -> buildTraceTimeIntervalWarning(startIntervalNumber, endIntervalNumber,
            submissionHourSinceEpoch))
        .collect(Collectors.toList());
  }

  public static List<CheckIn> buildCheckIns(int startIntervalNumber, int endIntervalNumber, int numberOfCheckIns) {
    return IntStream.range(0, numberOfCheckIns).mapToObj(
        index -> buildCheckIn(startIntervalNumber, endIntervalNumber)).collect(Collectors.toList());
  }

  public static CheckIn buildCheckIn(int startIntervalNumber, int endIntervalNumber) {
    final byte[] guid = UUID.randomUUID().toString().getBytes();
    return CheckIn.newBuilder()
        .setStartIntervalNumber(startIntervalNumber)
        .setEndIntervalNumber(endIntervalNumber)
        .setLocationId(ByteString.copyFrom(guid))
        .setTransmissionRiskLevel(5)
        .build();
  }
}
