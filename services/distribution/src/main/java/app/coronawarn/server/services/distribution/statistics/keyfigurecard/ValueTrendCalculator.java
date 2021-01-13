package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import java.util.HashMap;
import java.util.Map;

public class ValueTrendCalculator {

  static Map<Integer, Trend> TREND_MAP = new HashMap<>() {
    {
      put(-1, Trend.DECREASING);
      put(0, Trend.STABLE);
      put(1, Trend.INCREASING);
    }
  };

  public static Trend from(Integer trendNumber) {
    return TREND_MAP.getOrDefault(trendNumber, Trend.UNRECOGNIZED);
  }

  private final double threshold;

  public ValueTrendCalculator(double threshold) {
    this.threshold = threshold;
  }

  /**
   * Based on the {@param value} and {@param threshold} determine the {@link Trend} of the data point. Trend follows
   * these rules:
   * <li>`INCREASING` if `trend >= (1 + threshold)`</li>
   * <li>`DECREASING` if `trend <= (1 - threshold)`</li>
   * <li>`STABLE` if `(1 - threshold) < trend < (1 + threshold)`</li>
   *
   * @return Trend.
   */
  public Trend getTrend(double value) {
    if (value > (1 + threshold)) {
      return Trend.INCREASING;
    } else if (value < (1 - threshold)) {
      return Trend.DECREASING;
    } else {
      return Trend.STABLE;
    }
  }

  /**
   * Returns the Trend Semantic based on a negative semantic growth, maps {@link Trend} values to {@link
   * TrendSemantic}.
   * <li>`INCREASING -> NEGATIVE`</li>
   * <li>`STABLE -> NEUTRAL`</li>
   * <li>`DECREASING -> POSITIVE`</li>
   *
   * @param trend value.
   * @return TrendSemantic.
   */
  public static TrendSemantic getNegativeTrendGrowth(Trend trend) {
    if (trend.equals(Trend.INCREASING)) {
      return TrendSemantic.NEGATIVE;
    } else if (trend.equals(Trend.DECREASING)) {
      return TrendSemantic.POSITIVE;
    } else {
      return TrendSemantic.NEUTRAL;
    }
  }

  /**
   * Returns the Trend Semantic based on a POSITIVE semantic growth, maps {@link Trend} values to {@link
   * TrendSemantic}.
   * <li>`DECREASING -> NEGATIVE`</li>
   * <li>`STABLE -> NEUTRAL`</li>
   * <li>`INCREASING -> POSITIVE`</li>
   *
   * @param trend value.
   * @return TrendSemantic.
   */
  public static TrendSemantic getPositiveTrendGrowth(Trend trend) {
    if (trend.equals(Trend.DECREASING)) {
      return TrendSemantic.NEGATIVE;
    } else if (trend.equals(Trend.INCREASING)) {
      return TrendSemantic.POSITIVE;
    } else {
      return TrendSemantic.NEUTRAL;
    }
  }
}
