package app.coronawarn.server.common.persistence.service.utils.checkins;

import java.util.function.LongFunction;

public final class CheckinsDateSpecification {

  /**
   * Derivation function that requires a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
   * since UNIX epoch.
   */
  public static final LongFunction<Integer> TEN_MINUTE_INTERVAL_DERIVATION = unixTimestamp -> {
    if (unixTimestamp < 0) {
      throw new IllegalArgumentException(
          "Ten minute interval cannot be computed from a negative timestamp");
    }
    return Math.toIntExact(unixTimestamp / 600L);
  };

  /**
   * Derivation function that requires a UNIX timestamp (in seconds) and returns the corresponding hour since UNIX
   * epoch.
   */
  public static final LongFunction<Integer> HOUR_SINCE_EPOCH_DERIVATION = unixTimestamp -> {
    if (unixTimestamp < 0) {
      throw new IllegalArgumentException(
          "Hour since epoch cannot be computed from a negative timestamp");
    }
    return Math.toIntExact(unixTimestamp / 3600L);
  };
}
