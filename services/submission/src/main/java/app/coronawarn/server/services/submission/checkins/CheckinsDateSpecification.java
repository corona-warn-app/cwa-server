package app.coronawarn.server.services.submission.checkins;

import java.util.function.Function;

public final class CheckinsDateSpecification {

  /**
   * Derivation function that requires a UNIX timestamp (in seconds) and returns the corresponding
   * 10-minute interval since UNIX epoch.
   */
  public static final Function<Long, Integer> TEN_MINUTE_INTERVAL_DERIVATION = (unixTimestamp) -> {
    if (unixTimestamp < 0)
      throw new IllegalArgumentException(
          "Ten minute interval cannot be computed from a negative timestamp");
    return Math.toIntExact(unixTimestamp / 600L);
  };
}
