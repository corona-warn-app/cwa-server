

package app.coronawarn.server.services.distribution.runner;

import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler.ONE_HOUR_INTERVAL_SECONDS;
import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler.TEN_MINUTES_INTERVAL_SECONDS;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.TestData;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Generates random diagnosis keys for the time frame between the last diagnosis key in the database and now and writes
 * them to the database. If there are no diagnosis keys in the database yet, then random diagnosis keys for the time
 * frame between current hour and the beginning of the retention period (14 days ago) will be generated. The average
 * number of exposure keys to be generated per hour is configurable in the application properties (profile = {@code
 * testdata}).
 */
@Component
@Order(-1)
@Profile("testdata")
public class TestDataGeneration implements ApplicationRunner {

  private final Logger logger = LoggerFactory.getLogger(TestDataGeneration.class);

  private final Integer retentionDays;

  private final TestData config;

  private final DiagnosisKeyService diagnosisKeyService;

  private final RandomGenerator random = new JDKRandomGenerator();

  private final Set<String> supportedCountries;

  private static final int POISSON_MAX_ITERATIONS = 10_000_000;
  private static final double POISSON_EPSILON = 1e-12;

  /**
   * Creates a new TestDataGeneration runner.
   */
  TestDataGeneration(DiagnosisKeyService diagnosisKeyService,
      DistributionServiceConfig distributionServiceConfig) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.retentionDays = distributionServiceConfig.getRetentionDays();
    this.config = distributionServiceConfig.getTestData();
    this.supportedCountries = Set.of(distributionServiceConfig.getSupportedCountries());
  }

  /**
   * See {@link TestDataGeneration} class documentation.
   */
  @Override
  public void run(ApplicationArguments args) {
    writeTestData();
  }

  /**
   * See {@link TestDataGeneration} class documentation.
   */
  private void writeTestData() {
    supportedCountries.forEach(country -> {
      logger.debug("Querying diagnosis keys [{}] from the database...", country);
      List<DiagnosisKey> existingDiagnosisKeys = diagnosisKeyService.getDiagnosisKeys().stream()
          .filter(diagnosisKey -> diagnosisKey.getOriginCountry().equals(country))
          .collect(Collectors.toList());

      // Timestamps in hours since epoch. Test data generation starts one hour after the latest diagnosis key in the
      // database and ends one hour before the current one.
      long startTimestamp = getGeneratorStartTimestamp(existingDiagnosisKeys); // Inclusive
      long endTimestamp = getGeneratorEndTimestamp(); // Inclusive

      // Add the startTimestamp to the seed. Otherwise we would generate the same data every hour.
      random.setSeed(this.config.getSeed() + startTimestamp + country.hashCode());
      PoissonDistribution poisson =
          new PoissonDistribution(random, this.config.getExposuresPerHour(), POISSON_EPSILON, POISSON_MAX_ITERATIONS);

      if (startTimestamp > endTimestamp) {
        logger.debug("Skipping test data generation, latest diagnosis keys are still up-to-date.");
        return;
      }
      logger.debug("Generating diagnosis keys  [{}] between {} and {}...", country, startTimestamp, endTimestamp);
      List<DiagnosisKey> newDiagnosisKeys = LongStream.rangeClosed(startTimestamp, endTimestamp)
          .mapToObj(submissionTimestamp -> IntStream.range(0, poisson.sample())
              .mapToObj(ignoredValue -> generateDiagnosisKey(submissionTimestamp, country))
              .collect(Collectors.toList()))
          .flatMap(List::stream)
          .collect(Collectors.toList());

      logger.debug("Writing {} new diagnosis keys [{}] to the database...", newDiagnosisKeys.size(), country);
      diagnosisKeyService.saveDiagnosisKeys(newDiagnosisKeys);

      logger.debug("Test data generation finished successfully.");
    });
  }

  /**
   * Returns the submission timestamp (in 1 hour intervals since epoch) of the last diagnosis key in the database (or
   * the result of {@link TestDataGeneration#getRetentionStartTimestamp} if there are no diagnosis keys in the database
   * yet.
   */
  private long getGeneratorStartTimestamp(List<DiagnosisKey> diagnosisKeys) {
    if (diagnosisKeys.isEmpty()) {
      return getRetentionStartTimestamp();
    } else {
      DiagnosisKey latestDiagnosisKey = diagnosisKeys.get(diagnosisKeys.size() - 1);
      return latestDiagnosisKey.getSubmissionTimestamp() + 1;
    }
  }

  /**
   * Returns the timestamp (in 1 hour intervals since epoch) of the current hour. Example: If called at 15:38 UTC, this
   * function would return the timestamp for today 15:00 UTC.
   */
  private long getGeneratorEndTimestamp() {
    return (TimeUtils.getNow().getEpochSecond() / ONE_HOUR_INTERVAL_SECONDS);
  }

  /**
   * Returns the timestamp (in 1 hour intervals since epoch) at which the retention period starts. Example: If the
   * retention period in the application properties is set to 14 days, then this function would return the timestamp for
   * 14 days ago (from now) at 00:00 UTC.
   */
  private long getRetentionStartTimestamp() {
    return TimeUtils.getNow().truncatedTo(ChronoUnit.DAYS).minus(retentionDays, ChronoUnit.DAYS).getEpochSecond()
        / ONE_HOUR_INTERVAL_SECONDS;
  }

  /**
   * Either returns the list of all possible visited countries or only current distribution This ensure that when test
   * generation runs for a country (e.g. FR) all keys in the distribution will contain FR in the vistied_countries
   * array.
   *
   * @return
   */
  private Set<String> generateSetOfVisitedCountries(String distributionCountry) {
    if (random.nextBoolean()) {
      return supportedCountries;
    } else {
      return Set.of(distributionCountry);
    }
  }

  /**
   * Returns a random diagnosis key with a specific submission timestamp.
   */
  private DiagnosisKey generateDiagnosisKey(long submissionTimestamp, String country) {
    return DiagnosisKey.builder()
        .withKeyData(generateDiagnosisKeyBytes())
        .withRollingStartIntervalNumber(generateRollingStartIntervalNumber(submissionTimestamp))
        .withTransmissionRiskLevel(generateTransmissionRiskLevel())
        .withSubmissionTimestamp(submissionTimestamp)
        .withCountryCode(country)
        .withVisitedCountries(generateSetOfVisitedCountries(country))
        .withReportType(ReportType.CONFIRMED_TEST)
        .withConsentToFederation(this.config.getDistributionTestdataConsentToFederation())
        .build();
  }

  /**
   * Returns 16 random bytes.
   */
  private byte[] generateDiagnosisKeyBytes() {
    byte[] exposureKey = new byte[16];
    random.nextBytes(exposureKey);
    return exposureKey;
  }

  /**
   * Returns a random rolling start interval number (timestamp since when a key was active, represented by a 10 minute
   * interval counter) between a specific submission timestamp and the beginning of the retention period.
   */
  private int generateRollingStartIntervalNumber(long submissionTimestamp) {
    LocalDateTime time = LocalDateTime
        .ofEpochSecond(submissionTimestamp * ONE_HOUR_INTERVAL_SECONDS, 0, ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.DAYS);

    long maxRollingStartIntervalNumber = time.toEpochSecond(ZoneOffset.UTC) / TEN_MINUTES_INTERVAL_SECONDS;
    return Math.toIntExact(maxRollingStartIntervalNumber - TimeUnit.DAYS
        .toSeconds(getRandomBetween(0, retentionDays) / TEN_MINUTES_INTERVAL_SECONDS));
  }

  /**
   * Returns a random number between {@link RiskLevel#RISK_LEVEL_LOWEST_VALUE} and {@link
   * RiskLevel#RISK_LEVEL_HIGHEST_VALUE}.
   */
  private int generateTransmissionRiskLevel() {
    return Math.toIntExact(
        getRandomBetween(RiskLevel.RISK_LEVEL_LOWEST_VALUE, RiskLevel.RISK_LEVEL_HIGHEST_VALUE));
  }

  /**
   * Returns a random number between {@code minIncluding} and {@code maxIncluding}.
   */
  private long getRandomBetween(long minIncluding, long maxIncluding) {
    return minIncluding + Math.round(random.nextDouble() * (maxIncluding - minIncluding));
  }
}
