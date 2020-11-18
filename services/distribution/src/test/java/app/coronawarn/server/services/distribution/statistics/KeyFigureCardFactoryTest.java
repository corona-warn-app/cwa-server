package app.coronawarn.server.services.distribution.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KeyFigureCardFactoryTest {

  @Nested
  class InfectionCardsTest {

    @Test
    void testFirstKeyFigureHasCorrectValues() {
      KeyFigureCardFactory figureCardFactory = new KeyFigureCardFactory(new ValueProcessor());
      var statisticsJsonStringObject = new StatisticsJsonStringObject();

      statisticsJsonStringObject.setInfectionsReportedDaily(70200);
      statisticsJsonStringObject.setEffectiveDate("2020-11-05");
      statisticsJsonStringObject.setInfectionsReported7daysAvg(1234);
      statisticsJsonStringObject.setInfectionsReported7daysGrowthrate(1.15);
      statisticsJsonStringObject.setInfectionsReportedCumulated(123456);

      var result = figureCardFactory
          .createKeyFigureCard(statisticsJsonStringObject, 1);
      assertKeyFigure(result.getKeyFigures(0), 70200, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 1234, Rank.SECONDARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 0);
      assertKeyFigure(result.getKeyFigures(2), 123456, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);


    }
  }

  private void assertKeyFigure(KeyFigure result, double value, Rank rank, Trend trend, TrendSemantic trendSemantic,
      Integer decimals) {
    assertThat(result.getValue()).isEqualTo(value);
    assertThat(result.getRank()).isEqualTo(rank);
    assertThat(result.getTrend()).isEqualTo(trend);
    assertThat(result.getDecimals()).isEqualTo(decimals);
    assertThat(result.getTrendSemantic()).isEqualTo(trendSemantic);
  }
}
