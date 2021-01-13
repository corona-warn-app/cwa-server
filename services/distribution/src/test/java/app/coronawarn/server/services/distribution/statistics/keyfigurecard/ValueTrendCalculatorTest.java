package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ValueTrendCalculatorTest {

  private final ValueTrendCalculator NO_THRESHOLD = new ValueTrendCalculator(0.0);
  private final ValueTrendCalculator SMALL_THRESHOLD = new ValueTrendCalculator(0.05);

  @Test
  void shouldHaveIncreasingTrend() {
    isIncreasing(NO_THRESHOLD.getTrend(1.1));
  }

  @Test
  void shouldHaveDecreasingTrend() {
    isDecreasing(NO_THRESHOLD.getTrend(0.9));
  }

  @Test
  void shouldHaveStableTrend() {
    isStable(NO_THRESHOLD.getTrend(1.0));
  }

  @Test
  void shouldHaveStableTrendWithThreshold() {
    isStable(SMALL_THRESHOLD.getTrend(1.01));
    isStable(SMALL_THRESHOLD.getTrend(1.02));
    isStable(SMALL_THRESHOLD.getTrend(1.05));
    isStable(SMALL_THRESHOLD.getTrend(0.99));
    isStable(SMALL_THRESHOLD.getTrend(0.95));
  }

  @Test
  void shouldHaveIncreasingTrendWithThreshold() {
    isIncreasing(SMALL_THRESHOLD.getTrend(1.06));
  }

  @Test
  void shouldHaveDecreasingTrendWithThreshold() {
    isDecreasing(SMALL_THRESHOLD.getTrend(0.94));
  }

  @Test
  void testPositiveGrowthSemantic() {
    isPositive(ValueTrendCalculator.getPositiveTrendGrowth(Trend.INCREASING));
    isNegative(ValueTrendCalculator.getPositiveTrendGrowth(Trend.DECREASING));
    isNeutral(ValueTrendCalculator.getPositiveTrendGrowth(Trend.STABLE));
  }

  @Test
  void testNEGATIVEGrowthSemantic() {
    isPositive(ValueTrendCalculator.getNegativeTrendGrowth(Trend.DECREASING));
    isNegative(ValueTrendCalculator.getNegativeTrendGrowth(Trend.INCREASING));
    isNeutral(ValueTrendCalculator.getNegativeTrendGrowth(Trend.STABLE));
  }

  private static void isStable(Trend trend) {
    assertThat(trend).isEqualTo(Trend.STABLE);
  }

  private static void isIncreasing(Trend trend) {
    assertThat(trend).isEqualTo(Trend.INCREASING);
  }

  private static void isDecreasing(Trend trend) {
    assertThat(trend).isEqualTo(Trend.DECREASING);
  }

  private static void isPositive(TrendSemantic semantic) {
    assertThat(semantic).isEqualTo(TrendSemantic.POSITIVE);
  }

  private static void isNegative(TrendSemantic semantic) {
    assertThat(semantic).isEqualTo(TrendSemantic.NEGATIVE);
  }

  private static void isNeutral(TrendSemantic semantic) {
    assertThat(semantic).isEqualTo(TrendSemantic.NEUTRAL);
  }

}
