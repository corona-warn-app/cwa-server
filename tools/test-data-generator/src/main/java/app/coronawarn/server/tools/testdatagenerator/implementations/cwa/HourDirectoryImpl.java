package app.coronawarn.server.tools.testdatagenerator.implementations.cwa;

import app.coronawarn.server.common.protocols.external.exposurenotification.File;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.tools.testdatagenerator.decorators.file.SigningDecorator;
import app.coronawarn.server.tools.testdatagenerator.implementations.FileImpl;
import app.coronawarn.server.tools.testdatagenerator.implementations.IndexDirectoryImpl;
import app.coronawarn.server.tools.testdatagenerator.util.Batch;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import app.coronawarn.server.tools.testdatagenerator.util.DateTime;
import app.coronawarn.server.tools.testdatagenerator.util.Random;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

class HourDirectoryImpl extends IndexDirectoryImpl<LocalDateTime> {

  private static final int POISSON_MAX_ITERATIONS = 10000000;
  private static final double POISSON_EPSILON = 1e-12;
  private final PoissonDistribution poisson;

  private final LocalDate startDate;
  private final int totalHours;
  private final RandomGenerator random;

  public HourDirectoryImpl(LocalDate startDate, int totalHours, int exposuresPerHour,
      Crypto crypto, RandomGenerator random) {
    super("hour", indices -> {
      LocalDate currentDate = ((LocalDate) indices.peek());
      return DateTime.getHours(startDate, currentDate, totalHours);
    }, LocalDateTime::getHour);
    this.startDate = startDate;
    this.totalHours = totalHours;
    this.random = random;
    this.poisson = new PoissonDistribution(random, exposuresPerHour, POISSON_EPSILON,
        POISSON_MAX_ITERATIONS);
    this.addFileToAll(indices -> {
      Stack<Object> indicesCopy = (Stack<Object>) indices.clone();
      LocalDateTime hour = (LocalDateTime) indicesCopy.pop();
      indicesCopy.pop();
      String region = (String) indicesCopy.pop();
      return new SigningDecorator(
          new FileImpl("index", this.generateHourFile(hour, region)), crypto);
    });
  }

  private byte[] generateHourFile(LocalDateTime currentHour, String region) {
    System.out.println(
        "Generating \t\t" + this.getFileOnDisk().getPath() + "/" + currentHour.getHour());
    return generateFileBucket(this.startDate, this.totalHours, currentHour, region, this.poisson,
        this.random).toByteArray();
  }

  private static FileBucket generateFileBucket(LocalDate startDate, int totalHours,
      LocalDateTime currentHour, String region, PoissonDistribution poisson,
      RandomGenerator random) {
    return FileBucket.newBuilder()
        .addAllFiles(generateFiles(startDate, totalHours, currentHour, region, poisson, random))
        .build();
  }

  private static List<File> generateFiles(LocalDate startDate, int totalHours,
      LocalDateTime currentHour, String region, PoissonDistribution poisson,
      RandomGenerator random) {
    Instant startTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC));
    Instant endTimestamp = Instant.from(currentHour.atOffset(ZoneOffset.UTC).plusHours(1));
    int numExposures = poisson.sample();
    List<Key> keys = generateKeys(numExposures, startDate, totalHours, random);
    return Batch.aggregateKeys(keys, startTimestamp, endTimestamp, region);
  }

  private static List<Key> generateKeys(int number, LocalDate startDate, int totalHours,
      RandomGenerator random) {
    return IntStream.range(0, number)
        .mapToObj(__ -> Key.newBuilder()
            .setRollingStartNumber(generateRollingStartNumber(startDate, totalHours, random))
            .setRollingPeriod(generateRollingPeriod())
            .setTransmissionRiskLevel(generateRiskLevel(random).getNumber())
            .setKeyData(ByteString.copyFrom(generateDiagnosisKeyBytes(random)))
            .build())
        .collect(Collectors.toList());
  }

  // Timestamp since when a key was active, represented by a 10 minute interval counter
  private static int generateRollingStartNumber(LocalDate startDate, int totalHours,
      RandomGenerator random) {
    // Calculate some random timestamp between the startDate (at 00:00 UTC) and totalHours
    // later. This will form the basis for our rollingStartNumber.
    LocalDateTime startDateTime = startDate.atStartOfDay();
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
}
