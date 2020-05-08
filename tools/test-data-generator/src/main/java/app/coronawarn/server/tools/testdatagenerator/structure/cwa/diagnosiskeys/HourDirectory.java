package app.coronawarn.server.tools.testdatagenerator.structure.cwa.diagnosiskeys;

import app.coronawarn.server.common.protocols.external.exposurenotification.File;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.tools.testdatagenerator.structure.IndexDirectory;
import app.coronawarn.server.tools.testdatagenerator.structure.SigningDirectory;
import app.coronawarn.server.tools.testdatagenerator.util.Aggregator;
import app.coronawarn.server.tools.testdatagenerator.util.Common;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HourDirectory extends IndexDirectory<LocalDateTime> implements SigningDirectory {

  private final LocalDate startDate;
  private final int totalHours;
  private final int exposuresPerHour;
  private final Random random;
  private final Crypto crypto;

  @SuppressWarnings("unchecked")
  public HourDirectory(LocalDate startDate, int totalHours, int exposuresPerHour,
      Crypto crypto, Random random) {
    super("hour", indices -> {
      LocalDate currentDate = ((LocalDate) indices.peek());
      return Common.getHours(startDate, currentDate, totalHours);
    }, LocalDateTime::getHour);
    this.startDate = startDate;
    this.totalHours = totalHours;
    this.exposuresPerHour = exposuresPerHour;
    this.random = random;
    this.crypto = crypto;
    this.addFileToAll("index", indices -> {
      Stack<Object> indicesCopy = (Stack<Object>) indices.clone();
      LocalDateTime hour = (LocalDateTime) indicesCopy.pop();
      indicesCopy.pop();
      String region = (String) indicesCopy.pop();
      return this.generateHourFile(hour, region);
    });
  }

  private byte[] generateHourFile(LocalDateTime currentHour, String region) {
    return generateFileBucket(this.startDate, this.totalHours, currentHour, region,
        this.exposuresPerHour, this.random).toByteArray();
  }

  private static FileBucket generateFileBucket(LocalDate startDate, int totalHours,
      LocalDateTime currentHour, String region,
      int exposuresPerHour, Random random) {
    return FileBucket.newBuilder()
        .addAllFiles(
            generateFiles(startDate, totalHours, currentHour, region, exposuresPerHour, random))
        .build();
  }

  private static List<File> generateFiles(
      LocalDate startDate, int totalHours, LocalDateTime currentHour, String region,
      int exposuresPerHour,
      Random random) {
    Instant startTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC));
    Instant endTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC).plusHours(1));
    int numExposures = Common.nextPoisson(exposuresPerHour, random);
    List<Key> keys = generateKeys(numExposures, startDate, totalHours, random);
    return Aggregator.aggregateKeys(keys, startTimestamp, endTimestamp, region);
  }

  private static List<Key> generateKeys(int number, LocalDate startDate, int totalHours,
      Random random) {
    return IntStream.range(0, number)
        .mapToObj(__ -> Key.newBuilder()
            .setRollingStartNumber(generateRollingStartNumber(startDate, totalHours, random))
            .setRollingPeriod(generateRollingPeriod())
            .setTransmissionRiskLevel(generateRiskLevel(random).getNumber())
            .setKeyData(ByteString.copyFrom(generateDiagnosisKeyBytes(random)))
            .build()
        )
        .collect(Collectors.toList());
  }

  // Timestamp since when a key was active, represented by a 10 minute interval counter
  private static int generateRollingStartNumber(LocalDate startDate, int totalHours,
      Random random) {
    // Calculate some random timestamp between the startDate (at 00:00 UTC) and totalHours
    // later. This will form the basis for our rollingStartNumber.
    LocalDateTime startDateTime = startDate.atStartOfDay();
    Instant randomTimestamp = Instant.ofEpochMilli(Common.getRandomBetween(
        startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
        startDateTime.plusHours(totalHours).toInstant(ZoneOffset.UTC).toEpochMilli(),
        random
    ));
    return Math.toIntExact(Math.floorDiv(
        randomTimestamp.toEpochMilli(),
        TimeUnit.MINUTES.toMillis(10)
    ));
  }

  // Number of 10 minute intervals that a key was active for.
  private static int generateRollingPeriod() {
    // We currently assume this to always be one day.
    return Math.toIntExact(Math.floorDiv(TimeUnit.DAYS.toMinutes(1), 10));
  }

  private static RiskLevel generateRiskLevel(Random random) {
    return RiskLevel.forNumber(
        Common.getRandomBetween(
            RiskLevel.RISK_LEVEL_LOWEST_VALUE,
            RiskLevel.RISK_LEVEL_HIGHEST_VALUE,
            random
        )
    );
  }

  private static byte[] generateDiagnosisKeyBytes(Random random) {
    byte[] exposureKey = new byte[16];
    random.nextBytes(exposureKey);
    return exposureKey;
  }

  @Override
  public void sign() {
    Arrays.stream(Objects.requireNonNull(this.getFile().listFiles()))
        .forEach(file -> this.signFiles(file, this.crypto));
  }
}
