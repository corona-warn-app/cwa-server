package app.coronawarn.server.services.distribution.diagnosiskeys.structure.file;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.File;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.Batch;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HourFileImpl extends FileImpl {

  private static final Logger logger = LoggerFactory.getLogger(HourFileImpl.class);

  private static final String INDEX_FILE_NAME = "index";

  private final LocalDateTime currentHour;
  private final String region;
  private final Collection<DiagnosisKey> diagnosisKeys;

  public HourFileImpl(LocalDateTime currentHour, String region,
      Collection<DiagnosisKey> diagnosisKeys) {
    super(INDEX_FILE_NAME, new byte[0]);
    this.currentHour = currentHour;
    this.region = region;
    this.diagnosisKeys = diagnosisKeys;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    this.setBytes(createHourBytes());
    super.prepare(indices);
  }

  private byte[] createHourBytes() {
    logger.debug("Creating hour file for {}", currentHour);
    return FileBucket.newBuilder()
        .addAllFiles(generateFiles(diagnosisKeys, currentHour, region))
        .build()
        .toByteArray();
  }

  private static List<File> generateFiles(Collection<DiagnosisKey> diagnosisKeys,
      LocalDateTime currentHour, String region) {
    Instant startTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC));
    Instant endTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC).plusHours(1));
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
