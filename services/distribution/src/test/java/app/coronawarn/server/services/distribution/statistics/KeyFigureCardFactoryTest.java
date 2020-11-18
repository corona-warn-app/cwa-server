package app.coronawarn.server.services.distribution.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KeyFigureCardFactoryTest {

  @Nested
    class InfectionCardsTest {

    KeyFigureCardFactory figureCardFactory;
    StatisticsJsonStringObject statisticsJsonStringObject;

    @BeforeEach
    public void setup() {
      this.figureCardFactory = new KeyFigureCardFactory(new ValueProcessor());
      this.statisticsJsonStringObject = new StatisticsJsonStringObject();

      statisticsJsonStringObject.setInfectionsReportedDaily(70200);
      statisticsJsonStringObject.setEffectiveDate("2020-11-05");
      statisticsJsonStringObject.setInfectionsReported7daysAvg(1234);
      statisticsJsonStringObject.setInfectionsReported7daysGrowthrate(1.15);
      statisticsJsonStringObject.setInfectionsReportedCumulated(123456);

      statisticsJsonStringObject.setSevenDayIncidence(168.5);
      statisticsJsonStringObject.setSevenDayIncidenceGrowthrate(1.12);

      statisticsJsonStringObject.setPersonsWhoSharedKeysDaily(2717);
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysSum(123);
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysGrowthrate(1.05);
      statisticsJsonStringObject.setPersonsWhoSharedKeysCumulated(4321);
    }

    @Test
    void testFirstCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertKeyFigure(result.getKeyFigures(0), 70200, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 1234, Rank.SECONDARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 0);
      assertKeyFigure(result.getKeyFigures(2), 123456, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testSecondCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 2);
      assertKeyFigure(result.getKeyFigures(0), 168.5, Rank.PRIMARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 1);
    }

    @Test
    void testThirdCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 3);
      assertKeyFigure(result.getKeyFigures(0), 2717, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 123, Rank.SECONDARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 0);
      assertKeyFigure(result.getKeyFigures(2), 4321, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

  }

  private void assertKeyFigure(KeyFigure result, double value, Rank rank, Trend trend, TrendSemantic trendSemantic,
      Integer decimals) {
    assertThat(result).extracting(
        KeyFigure::getValue,
        KeyFigure::getRank,
        KeyFigure::getTrend,
        KeyFigure::getTrendSemantic,
        KeyFigure::getDecimals)
        .containsExactly(value, rank, trend, trendSemantic, decimals);
  }

}
