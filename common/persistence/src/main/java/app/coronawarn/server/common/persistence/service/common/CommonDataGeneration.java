package app.coronawarn.server.common.persistence.service.common;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.boot.ApplicationRunner;

public abstract class CommonDataGeneration<T extends DiagnosisKey> implements ApplicationRunner {

  protected static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);
  protected static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);
  protected final Integer retentionDays;
  protected final RandomGenerator random;

  protected CommonDataGeneration(Integer retentionDays) {
    this.retentionDays = retentionDays;
    this.random = new JDKRandomGenerator();
  }

  /**
   * Returns 16 random bytes.
   */
  protected byte[] generateDiagnosisKeyBytes() {
    byte[] keyData = new byte[16];
    random.nextBytes(keyData);
    return keyData;
  }

  /**
   * Returns a random {@link SubmissionType}.
   */
  protected SubmissionType generateSubmissionType() {
    return SubmissionType.forNumber(Math.toIntExact(getRandomBetween(0, 1)));
  }

  /**
   * Returns a random diagnosis key or any of it's subtypes with a specific submission timestamp and country.
   */
  protected abstract T generateDiagnosisKey(long submissionTimestamp, String country);

  /**
   * Returns a random number between {@link RiskLevel#RISK_LEVEL_LOWEST_VALUE} and {@link
   * RiskLevel#RISK_LEVEL_HIGHEST_VALUE}.
   */
  protected int generateTransmissionRiskLevel() {
    return Math.toIntExact(
        getRandomBetween(RiskLevel.RISK_LEVEL_LOWEST_VALUE, RiskLevel.RISK_LEVEL_HIGHEST_VALUE));
  }

  /**
   * Returns a random number between {@code minIncluding} and {@code maxIncluding}.
   */
  protected long getRandomBetween(long minIncluding, long maxIncluding) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }

  /**
   * Returns a random rolling start interval number (timestamp since when a key was active, represented by a 10 minute
   * interval counter) between a specific submission timestamp and the beginning of the retention period.
   */
  protected int generateRollingStartIntervalNumber(long submissionTimestamp) {
    LocalDateTime time = LocalDateTime
        .ofEpochSecond(submissionTimestamp * ONE_HOUR_INTERVAL_SECONDS, 0, ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.DAYS);

    long maxRollingStartIntervalNumber = time.toEpochSecond(ZoneOffset.UTC) / TEN_MINUTES_INTERVAL_SECONDS;
    return Math.toIntExact(maxRollingStartIntervalNumber - TimeUnit.DAYS
        .toSeconds(getRandomBetween(0, retentionDays) / TEN_MINUTES_INTERVAL_SECONDS));
  }
}
