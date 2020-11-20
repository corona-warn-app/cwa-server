package app.coronawarn.server.services.distribution.statistics;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.KEY_SUBMISSION_CARD_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.MissingPropertyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ContextConfiguration(classes = {KeyFigureCardFactory.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class KeyFigureCardFactoryTest {

  @Autowired
  KeyFigureCardFactory figureCardFactory;

  StatisticsJsonStringObject statisticsJsonStringObject;

  @BeforeEach
  public void setup() {
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

  @Nested
  class InfectionCardsTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertKeyFigure(result.getKeyFigures(0), 70200, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 1234, Rank.SECONDARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 0);
      assertKeyFigure(result.getKeyFigures(2), 123456, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testInfectionsReportedTrendDecreasing() {
      statisticsJsonStringObject.setInfectionsReported7daysGrowthrate(0.0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testInfectionsReportedTrendIncreasing() {
      statisticsJsonStringObject.setInfectionsReported7daysGrowthrate(1.06);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testInfectionsReportedTrendStable() {
      statisticsJsonStringObject.setInfectionsReported7daysGrowthrate(0.96);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.STABLE, TrendSemantic.NEUTRAL);
    }

    @Test
    void shouldThrowAnExceptionIfAnyPropertyIsMissing() {
      var missingPropertyObject = new StatisticsJsonStringObject();
      missingPropertyObject.setEffectiveDate("2020-01-01");
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(missingPropertyObject, 1))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldNamePropertyMissingInException() {
      var missingPropertyObject = new StatisticsJsonStringObject();
      missingPropertyObject.setEffectiveDate("2020-01-01");
      missingPropertyObject.setInfectionsReportedDaily(1234);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(missingPropertyObject, 1))
          .isInstanceOf(MissingPropertyException.class)
          .hasMessageContaining("infections_reported_7days_avg")
          .hasMessageContaining("infections_reported_7days_growthrate")
          .hasMessageContaining("infections_reported_cumulated")
          .hasMessageNotContaining("infections_reported_daily");
    }

  }

  @Nested
  @ExtendWith(SpringExtension.class)
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {KeyFigureCardFactory.class},
      initializers = ConfigFileApplicationContextInitializer.class)
  class IncidenceCardTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertKeyFigure(result.getKeyFigures(0), 168.5, Rank.PRIMARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 1);
    }

    @Test
    void testIncidenceTrendDecreasing() {
      statisticsJsonStringObject.setSevenDayIncidenceGrowthrate(0.0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertThat(result.getKeyFigures(0))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testIncidenceTrendIncreasing() {
      statisticsJsonStringObject.setSevenDayIncidenceGrowthrate(1.5);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertThat(result.getKeyFigures(0))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testIncidenceTrendStable() {
      statisticsJsonStringObject.setSevenDayIncidenceGrowthrate(0.96);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertThat(result.getKeyFigures(0))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.STABLE, TrendSemantic.NEUTRAL);
    }
  }

  @Nested
  @ExtendWith(SpringExtension.class)
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {KeyFigureCardFactory.class},
      initializers = ConfigFileApplicationContextInitializer.class)
  class KeySubmissionCard {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 3);
      assertKeyFigure(result.getKeyFigures(0), 2717, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 123, Rank.SECONDARY, Trend.STABLE,
          TrendSemantic.NEUTRAL, 0);
      assertKeyFigure(result.getKeyFigures(2), 4321, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testPersonsWhoSharedKeysTrendDecreasing() {
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysGrowthrate(0.0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD_ID);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testIncidenceTrendIncreasing() {
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysGrowthrate(1.5);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD_ID);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testIncidenceTrendStable() {
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysGrowthrate(1.0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD_ID);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.STABLE, TrendSemantic.NEUTRAL);
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
