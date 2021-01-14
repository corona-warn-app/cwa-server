package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import java.util.Map;

public class ValueTrendCalculator {

  static Map<Integer, Trend> trend_map = Map.of(-1, Trend.DECREASING, 0, Trend.STABLE, 1, Trend.INCREASING);

  /**
   * Return {@link Trend} based on static map.
   * <li>1: `INCREASING`</li>
   * <li>-1: `DECREASING`</li>
   * <li>0: `STABLE`</li>
   * @param trendNumber the number to be used in the map.
   * @return Trend.
   */
  public static Trend from(Integer trendNumber) {
    return trend_map.getOrDefault(trendNumber, Trend.UNRECOGNIZED);
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
