package app.coronawarn.server.services.distribution.diagnosiskeys.structure;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.File;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.Batch;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.file.decorator.SigningDecorator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

class HourDirectoryImpl extends IndexDirectoryImpl<LocalDateTime> {

  private static final String HOUR_DIRECTORY = "hour";

  private static final String INDEX_FILE_NAME = "index";

  public HourDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys, CryptoProvider cryptoProvider) {
    super(HOUR_DIRECTORY, indices -> {
      LocalDate currentDate = ((LocalDate) indices.peek());
      return DateTime.getHours(currentDate, diagnosisKeys);
    }, LocalDateTime::getHour);
    this.addFileToAll(indices -> {
      Stack<Object> indicesCopy = (Stack<Object>) indices.clone();
      LocalDateTime hour = (LocalDateTime) indicesCopy.pop();
      indicesCopy.pop();
      String region = (String) indicesCopy.pop();
      return new SigningDecorator(
          new FileImpl(INDEX_FILE_NAME, this.generateHourFile(diagnosisKeys, hour, region)),
          cryptoProvider);
    });
  }

  private byte[] generateHourFile(Collection<DiagnosisKey> diagnosisKeys, LocalDateTime currentHour,
      String region) {
    System.out.println(
        "Generating \t\t" + this.getFileOnDisk().getPath() + "/" + currentHour.getHour());
    return generateFileBucket(diagnosisKeys, currentHour, region).toByteArray();
  }

  private static FileBucket generateFileBucket(Collection<DiagnosisKey> diagnosisKeys,
      LocalDateTime currentHour, String region) {
    return FileBucket.newBuilder()
        .addAllFiles(generateFiles(diagnosisKeys, currentHour, region))
        .build();
  }

  private static List<File> generateFiles(Collection<DiagnosisKey> diagnosisKeys,
      LocalDateTime currentHour, String region) {
    Instant startTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC));
    Instant endTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC).plusHours(1));
    // TODO Remove random
    List<Key> keys = getKeys(diagnosisKeys, currentHour);
    return Batch.aggregateKeys(keys, startTimestamp, endTimestamp, region);
  }

  private static List<Key> getKeys(Collection<DiagnosisKey> diagnosisKeys,
      LocalDateTime currentHour) {
    return List.of();
    /*
    return IntStream.range(0, number)
        .mapToObj(__ -> Key.newBuilder()
            .setRollingStartNumber(generateRollingStartNumber(startDate, totalHours))
            .setRollingPeriod(generateRollingPeriod())
            // TODO Remove random
            .setTransmissionRiskLevel(generateRiskLevel(random).getNumber())
            .setKeyData(ByteString.copyFrom(generateDiagnosisKeyBytes()))
            .build())
        .collect(Collectors.toList());
      */
  }

  /*
  // Timestamp since when a key was active, represented by a 10 minute interval counter
  private static int generateRollingStartNumber(LocalDate startDate, int totalHours,
      RandomGenerator random) {
    // Calculate some random timestamp between the startDate (at 00:00 UTC) and totalHours
    // later. This will form the basis for our rollingStartNumber.
    LocalDateTime startDateTime = startDate.atStartOfDay();
    // TODO Remove random
    Instant randomTimestamp = Instant.ofEpochMilli(Random.getRandomBetween(
        startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
        startDateTime.plusHours(totalHours).toInstant(ZoneOffset.UTC).toEpochMilli(),
        random));
    return Math.toIntExact(Math.floorDiv(
        randomTimestamp.toEpochMilli(),
        TimeUnit.MINUTES.toMillis(10)));
  }

  // Number of 10 minute intervals that a key was active for.
  private static int generateRollingPeriod() {
    // We currently assume this to always be one day.
    return Math.toIntExact(Math.floorDiv(TimeUnit.DAYS.toMinutes(1), 10));
  }

  private static RiskLevel generateRiskLevel(RandomGenerator random) {
    return RiskLevel.forNumber(Random.getRandomBetween(
        RiskLevel.RISK_LEVEL_LOWEST_VALUE, RiskLevel.RISK_LEVEL_HIGHEST_VALUE, random));
  }

  private static byte[] generateDiagnosisKeyBytes(RandomGenerator random) {
    byte[] exposureKey = new byte[16];
    random.nextBytes(exposureKey);
    return exposureKey;
  }
  */
}
