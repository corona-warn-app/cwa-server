package app.coronawarn.server.services.distribution.statistics;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.FIRST_VACCINATION_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.FULLY_VACCINATED_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.HOSPITALIZATION_INCIDENCE_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INCIDENCE_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INFECTIONS_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INTENSIVE_CARE_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.KEY_SUBMISSION_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.REPRODUCTION_NUMBER_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.VACCINATION_DOSES_CARD;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ContextConfiguration(classes = {
    KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
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

    statisticsJsonStringObject.setAdministeredDosesDaily(843978);
    statisticsJsonStringObject.setAdministeredDoses7daysAvg(656704.8571428572);
    statisticsJsonStringObject.setAdministeredDosesCumulated(41517849);
    statisticsJsonStringObject.setAdministeredDoses7daysAvgGrowthrate(0.8608);
    statisticsJsonStringObject.setAdministeredDoses7daysAvgTrend5percent(1);
    statisticsJsonStringObject.setPersonsWithFirstDoseCumulated(31678786);
    statisticsJsonStringObject.setPersonsWithFirstDoseRatio(0.381);
    statisticsJsonStringObject.setPersonsFullyVaccinatedCumulated(9901626);
    statisticsJsonStringObject.setPersonsFullyVaccinatedRatio(0.11900000274181366);

    statisticsJsonStringObject.setSevenDayHospitalizationReportedDaily(3.21);
    statisticsJsonStringObject.setSevenDayHospitalizationReportedTrend1percent(1);
    statisticsJsonStringObject.setOccupiedIntensiveCareBedsReportedDailyRatio(4.43);
    statisticsJsonStringObject.setOccupiedIntensiveCareBedsReportedDaily1percent(1);

  }

  @Nested
  class InfectionCardsTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertKeyFigure(result.getKeyFigures(0), 70200, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 1234, Rank.SECONDARY, Trend.INCREASING, TrendSemantic.NEGATIVE, 0);
      assertKeyFigure(result.getKeyFigures(2), 123456, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testInfectionsReportedTrendDecreasing() {
      statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testInfectionsReportedTrendIncreasing() {
      statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testInfectionsReportedTrendStable() {
      statisticsJsonStringObject.setInfectionsReported7daysTrend5percent(0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 1);
      assertThat(result.getKeyFigures(1)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.STABLE, TrendSemantic.NEUTRAL);
    }

    @Test
    void shouldThrowAnExceptionIfAnyPropertyIsMissing() {
      var missingPropertyObject = new StatisticsJsonStringObject();
      missingPropertyObject.setEffectiveDate("2020-01-01");
      assertThatThrownBy(() -> figureCardFactory.createKeyFigureCard(missingPropertyObject, 1))
          .isInstanceOf(MissingPropertyException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldFailIfInfectionCardIsNotValid(Integer value) {
      statisticsJsonStringObject.setInfectionsReportedCumulated(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INFECTIONS_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class IncidenceCardTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 168.5, Rank.PRIMARY, Trend.INCREASING, TrendSemantic.NEGATIVE, 1);
    }

    @Test
    void testIncidenceTrendDecreasing() {
      statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD.ordinal());
      assertThat(result.getKeyFigures(0)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testIncidenceTrendIncreasing() {
      statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD.ordinal());
      assertThat(result.getKeyFigures(0)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testIncidenceTrendStable() {
      statisticsJsonStringObject.setSevenDayIncidenceTrend1percent(0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD.ordinal());
      assertThat(result.getKeyFigures(0)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.STABLE, TrendSemantic.NEUTRAL);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsEqualToZero() {
      statisticsJsonStringObject.setSevenDayIncidence(-0.0);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsLessThanZero() {
      statisticsJsonStringObject.setSevenDayIncidence(-0.0);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INCIDENCE_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class KeySubmissionCard {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 3);
      assertKeyFigure(result.getKeyFigures(0), 2717, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 123, Rank.SECONDARY, Trend.INCREASING, TrendSemantic.NEUTRAL, 0);
      assertKeyFigure(result.getKeyFigures(2), 4321, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testIncidenceTrendSemanticShouldAlwaysBeStable() {
      statisticsJsonStringObject.setPersonsWhoSharedKeys7daysTrend5percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD.ordinal());
      assertThat(result.getKeyFigures(1)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.NEUTRAL);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void shouldFailIfKeySubmissionCardIsNotValid(Double value) {
      statisticsJsonStringObject.setPersonWhoSharedKeys7daysAvg(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, KEY_SUBMISSION_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class ReproductionNumberCardTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 4);
      assertKeyFigure(result.getKeyFigures(0), 100.63, Rank.PRIMARY, Trend.INCREASING, TrendSemantic.NEGATIVE, 2);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void shouldFailIfReproductionNumberCardIsNotValid(Double value) {
      statisticsJsonStringObject.setSevenDayRvaluepublishedDaily(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, REPRODUCTION_NUMBER_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class VaccinationDosesCardFactoryTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, 7);

      assertKeyFigure(result.getKeyFigures(0), 843978, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
      assertKeyFigure(result.getKeyFigures(1), 656704.8571428572, Rank.SECONDARY, Trend.INCREASING,
          TrendSemantic.POSITIVE, 0);
      assertKeyFigure(result.getKeyFigures(2), 41517849, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @Test
    void testAdministeredDosesDecreasing() {
      statisticsJsonStringObject.setAdministeredDoses7daysAvgTrend5percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, VACCINATION_DOSES_CARD.ordinal());
      assertThat(result.getKeyFigures(1)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.DECREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testAdministeredDosesIncreasing() {
      statisticsJsonStringObject.setAdministeredDoses7daysAvgTrend5percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, VACCINATION_DOSES_CARD.ordinal());
      assertThat(result.getKeyFigures(1)).extracting(KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(Trend.INCREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyIsEqualToZero() {
      statisticsJsonStringObject.setAdministeredDosesDaily(0);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, VACCINATION_DOSES_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }

    @Test
    void shouldThrowAnExceptionIfMandatoryPropertyLessThanZero() {
      statisticsJsonStringObject.setAdministeredDosesDaily(-1);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, VACCINATION_DOSES_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class FullyVaccinatedCardFactoryTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, FULLY_VACCINATED_CARD.ordinal());

      assertKeyFigure(result.getKeyFigures(0), 0.11900000274181366, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 1);
      assertKeyFigure(result.getKeyFigures(1), 9901626, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void shouldFailIfFullyVaccinatedCardIsNotValid(Double value) {
      statisticsJsonStringObject.setPersonsFullyVaccinatedRatio(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, FULLY_VACCINATED_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class FirstVaccinationCardFactoryTest {

    @Test
    void testCardHasCorrectKeyFigures() {
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, FIRST_VACCINATION_CARD.ordinal());

      assertKeyFigure(result.getKeyFigures(0), 0.381, Rank.PRIMARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 1);
      assertKeyFigure(result.getKeyFigures(1), 31678786, Rank.TERTIARY, Trend.UNSPECIFIED_TREND,
          TrendSemantic.UNSPECIFIED_TREND_SEMANTIC, 0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void shouldFailIfFirstVaccinationCardIsNotValid(Double value) {
      statisticsJsonStringObject.setPersonsWithFirstDoseRatio(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, FIRST_VACCINATION_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class HospitalizationIncidenceCardTest {

    @Test
    void testSevenDayHospitalizationTrendIncreasing() {
      statisticsJsonStringObject.setSevenDayHospitalizationReportedTrend1percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject,
          HOSPITALIZATION_INCIDENCE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 3.21, Rank.PRIMARY, Trend.INCREASING, TrendSemantic.NEGATIVE, 1);
    }

    @Test
    void testSevenDayHospitalizationTrendDecreasing() {
      statisticsJsonStringObject.setSevenDayHospitalizationReportedTrend1percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject,
          HOSPITALIZATION_INCIDENCE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 3.21, Rank.PRIMARY, Trend.DECREASING, TrendSemantic.POSITIVE, 1);
    }

    @Test
    void testSevenDayHospitalizationTrendStable() {
      statisticsJsonStringObject.setSevenDayHospitalizationReportedTrend1percent(0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject,
          HOSPITALIZATION_INCIDENCE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 3.21, Rank.PRIMARY, Trend.STABLE, TrendSemantic.NEUTRAL, 1);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void shouldFailIfHospitalizationIncidenceCardIsNotValid(Double value) {
      statisticsJsonStringObject.setSevenDayHospitalizationReportedDaily(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, HOSPITALIZATION_INCIDENCE_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  @Nested
  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ContextConfiguration(classes = {
      KeyFigureCardFactory.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class IntensiveCareCardTest {

    @Test
    void testOccupiedIntensiveCareBedsReportedDailyRatioTrendIncreasing() {
      statisticsJsonStringObject.setOccupiedIntensiveCareBedsReportedDaily1percent(1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject,
          INTENSIVE_CARE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 4.43, Rank.PRIMARY, Trend.INCREASING, TrendSemantic.NEGATIVE, 1);
    }

    @Test
    void testOccupiedIntensiveCareBedsReportedDailyRatioTrendDecreasing() {
      statisticsJsonStringObject.setOccupiedIntensiveCareBedsReportedDaily1percent(-1);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject,
          INTENSIVE_CARE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 4.43, Rank.PRIMARY, Trend.DECREASING, TrendSemantic.POSITIVE, 1);
    }

    @Test
    void testOccupiedIntensiveCareBedsReportedDailyRatioTrendStable() {
      statisticsJsonStringObject.setOccupiedIntensiveCareBedsReportedDaily1percent(0);
      var result = figureCardFactory.createKeyFigureCard(statisticsJsonStringObject,
          INTENSIVE_CARE_CARD.ordinal());
      assertKeyFigure(result.getKeyFigures(0), 4.43, Rank.PRIMARY, Trend.STABLE, TrendSemantic.NEUTRAL, 1);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void shouldFailIfIntensiveCareCardIsNotValid(Double value) {
      statisticsJsonStringObject.setOccupiedIntensiveCareBedsReportedDailyRatio(value);
      assertThatThrownBy(
          () -> figureCardFactory.createKeyFigureCard(statisticsJsonStringObject, INTENSIVE_CARE_CARD.ordinal()))
          .isInstanceOf(MissingPropertyException.class);
    }
  }

  private void assertKeyFigure(KeyFigure result, double value, Rank rank, Trend trend, TrendSemantic trendSemantic,
      Integer decimals) {
    assertThat(result).extracting(KeyFigure::getValue, KeyFigure::getRank, KeyFigure::getTrend,
        KeyFigure::getTrendSemantic, KeyFigure::getDecimals)
        .containsExactly(value, rank, trend, trendSemantic, decimals);
  }
}
