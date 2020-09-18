

package app.coronawarn.server.common.persistence.service.common;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Refers to the time that needs to pass after a key's rolling period has passed,
 * such that the key can be considered completely expired. This is a DPP policy enforced
 * upon processes which involve publishing/distributing/sharing keys with other external
 * systems.
 */
public final class ExpirationPolicy {

  private final long expirationTime;
  private final ChronoUnit timeUnit;

  private ExpirationPolicy(long expirationTime, ChronoUnit timeUnit) {
    this.expirationTime = expirationTime;
    this.timeUnit = timeUnit;
  }

  public long getExpirationTime() {
    return expirationTime;
  }

  public ChronoUnit getTimeUnit() {
    return timeUnit;
  }

  /**
   * Get an instance of an expiration policy.
   */
  public static ExpirationPolicy of(long timeValue, ChronoUnit timeUnit) {
    if (Objects.isNull(timeUnit)) {
      throw new IllegalArgumentException("Time unit parameter must not be null.");
    }
    return new ExpirationPolicy(timeValue, timeUnit);
  }
}
