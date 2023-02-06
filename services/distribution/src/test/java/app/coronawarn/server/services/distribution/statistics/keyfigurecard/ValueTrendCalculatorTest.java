package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import org.junit.jupiter.api.Test;

class ValueTrendCalculatorTest {

  @Test
  void testPositiveGrowthSemantic() {
    isPositive(ValueTrendCalculator.getPositiveTrendGrowth(Trend.INCREASING));
    isNegative(ValueTrendCalculator.getPositiveTrendGrowth(Trend.DECREASING));
    isNeutral(ValueTrendCalculator.getPositiveTrendGrowth(Trend.STABLE));
  }

  @Test
  void testNegativeGrowthSemantic() {
    isPositive(ValueTrendCalculator.getNegativeTrendGrowth(Trend.DECREASING));
    isNegative(ValueTrendCalculator.getNegativeTrendGrowth(Trend.INCREASING));
    isNeutral(ValueTrendCalculator.getNegativeTrendGrowth(Trend.STABLE));
  }

  @Test
  void testTrendCalculationMap() {
    isStable(ValueTrendCalculator.from(0));
    isIncreasing(ValueTrendCalculator.from(1));
    isDecreasing(ValueTrendCalculator.from(-1));
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
