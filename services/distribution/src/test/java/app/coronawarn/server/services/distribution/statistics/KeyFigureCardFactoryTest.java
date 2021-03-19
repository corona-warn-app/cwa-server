package app.coronawarn.server.services.distribution.statistics;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.*;
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
    statisticsJsonStringObject.setEffectiveDate("2020-11-05");

    statisticsJsonStringObject.setInfectionsReportedDaily(70200);
    statisticsJsonStringObject.setInfectionsReported7daysAvg(1234.0);
    statisticsJsonStringObject.setInfectionsReported7daysGrowthrate(1.15);
    statisticsJsonStringObject.setInfectionsReportedCumulated(123456);
    statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(1);

    statisticsJsonStringObject.setSevenDayIncidence(168.5);
    statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(1);

    statisticsJsonStringObject.setPersonsWhoSharedKeysDaily(2717);
    statisticsJsonStringObject.setPersonWhoSharedKeys7daysAvg(123.0);
    statisticsJsonStringObject.setPersonsWhoSharedKeys7daysGrowthrate(1.05);
    statisticsJsonStringObject.setPersonsWhoSharedKeysCumulated(4321);
    statisticsJsonStringObject.setPersonsWhoSharedKeys7daysTrend5percent(1);

    statisticsJsonStringObject.setSevenDayRvaluepublishedDaily(100.63);
    statisticsJsonStringObject.setSevenDayRvaluePublishedTrend1percent(1);
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
      statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testInfectionsReportedTrendIncreasing() {
      statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testInfectionsReportedTrendStable() {
      statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(0);
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
    void shouldThrowAnExceptionIfMandatoryPropertyIsEqualToZero() {
      statisticsJsonStringObject.setInfectionsReportedCumulated(0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyLessThanZero() {
      statisticsJsonStringObject.setInfectionsReportedCumulated(-1);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1))
          .isInstanceOf(MissingPropertyException.class);
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
      statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertThat(result.getKeyFigures(0))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testIncidenceTrendIncreasing() {
      statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertThat(result.getKeyFigures(0))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testIncidenceTrendStable() {
      statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID);
      assertThat(result.getKeyFigures(0))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.STABLE, TrendSemantic.NEUTRAL);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsEqualToZero() {
      statisticsJsonStringObject.setSevenDayIncidence(-0.0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsLessThanZero() {
      statisticsJsonStringObject.setSevenDayIncidence(-0.0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD_ID))
          .isInstanceOf(MissingPropertyException.class);
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
      assertKeyFigure(result.getKeyFigures(1), 123, Rank.SECONDARY, Trend.INCREASING,
          TrendSemantic.NEUTRAL, 0);
      assertKeyFigure(result.getKeyFigures(2), 4321, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testIncidenceTrendSemanticShouldAlwaysBeStable() {
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysTrend5percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD_ID);
      assertThat(result.getKeyFigures(1))
          .extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.NEUTRAL);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsEqualToZero() {
      statisticsJsonStringObject.setPersonWhoSharedKeys7daysAvg(0.0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD_ID))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsLessThanZero() {
      statisticsJsonStringObject.setPersonWhoSharedKeys7daysAvg(-1.0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD_ID))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @ExtendWith(SpringExtension.class)
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {KeyFigureCardFactory.class},
      initializers = ConfigFileApplicationContextInitializer.class)
  class ReproductionNumberCardTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 4);
      assertKeyFigure(result.getKeyFigures(0), 100.63, Rank.PRIMARY, Trend.INCREASING,
          TrendSemantic.NEGATIVE, 2);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsEqualToZero() {
      statisticsJsonStringObject.setSevenDayRvaluepublishedDaily(0.0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, REPRODUCTION_NUMBER_CARD))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyLessThanZero() {
      statisticsJsonStringObject.setSevenDayRvaluepublishedDaily(-1.0);
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, REPRODUCTION_NUMBER_CARD))
          .isInstanceOf(MissingPropertyException.class);
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
