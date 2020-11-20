package app.coronawarn.server.services.distribution.statistics;

import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.LocalStatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import org.assertj.core.api.ObjectAssert;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats"})
@ContextConfiguration(classes = {StatisticsJsonToProtobufTest.class,
    StatisticsToProtobufMapping.class, KeyFigureCardFactory.class,
    LocalStatisticJsonFileLoader.class
}, initializers = ConfigFileApplicationContextInitializer.class)
class StatisticsJsonToProtobufTest {

  @Autowired
  StatisticsToProtobufMapping statisticsToProtobufMapping;

  @Test
  void conversionTest() throws IOException, ParseException {
    String content = FileUtils.readFileToString(
        new File("./src/test/resources/stats/statistic_data.json"), StandardCharsets.UTF_8);
    List<StatisticsJsonStringObject> statsDTO = SerializationUtils.deserializeJson(content, typeFactory -> typeFactory
        .constructCollectionType(List.class, StatisticsJsonStringObject.class));
  }

  @Test
  void testGetCardIdSequenceFromConfig() throws IOException {
    Statistics stats = statisticsToProtobufMapping.constructProtobufStatistics();

    assertThat(stats.getCardIdSequenceList().size()).isEqualTo(4);
  }


  @Test
  void testKeyFigureCardContainsHeader() throws IOException {
    Statistics stats = statisticsToProtobufMapping.constructProtobufStatistics();

    assertThat(stats.getKeyFigureCardsCount()).isEqualTo(4);
    stats.getKeyFigureCardsList().forEach(keyFigureCard -> {
          assertThat(keyFigureCard.getHeader()).isNotNull();
          assertThat(keyFigureCard.getHeader().getUpdatedAt()).isPositive();
        }
    );
  }

  @Test
  void testKeyFigureCardBasedOnHeaderCardId() throws IOException {
    Statistics stats = statisticsToProtobufMapping.constructProtobufStatistics();

    KeyFigureCard infectionsCard = getKeyFigureCardForId(stats, 1);
    KeyFigureCard incidenceCard = getKeyFigureCardForId(stats, 2);
    KeyFigureCard keySubmissionsCard = getKeyFigureCardForId(stats, 3);

    assertThat(infectionsCard.getKeyFiguresCount()).isEqualTo(3);
    assertThat(incidenceCard.getKeyFiguresCount()).isEqualTo(1);
    assertThat(keySubmissionsCard.getKeyFiguresCount()).isEqualTo(3);


  }

  private KeyFigureCard getKeyFigureCardForId(Statistics stats, Integer id) {
    return stats.getKeyFigureCardsList()
        .stream()
        .filter(keyFigureCard -> keyFigureCard.getHeader().getCardId() == id)
        .findFirst().get();
  }

  @EnableConfigurationProperties(value = DistributionServiceConfig.class)
  @ExtendWith(SpringExtension.class)
  @ActiveProfiles({"local-json-stats", "processing-test"})
  @Nested
  @DisplayName("Value Processing Test")
  @ContextConfiguration(classes = {StatisticsJsonToProtobufTest.class,
      StatisticsToProtobufMapping.class, KeyFigureCardFactory.class,
      LocalStatisticJsonFileLoader.class
  }, initializers = ConfigFileApplicationContextInitializer.class)
  class StatisticsJsonProcessingTest {

    @Autowired
    StatisticsToProtobufMapping mapping;

    Statistics result;
    KeyFigureCard infections, incidence, keySubmission;

    @BeforeEach
    void setup() throws IOException {
      result = mapping.constructProtobufStatistics();
      infections = result.getKeyFigureCards(0);
      incidence = result.getKeyFigureCards(1);
      keySubmission = result.getKeyFigureCards(2);
    }

    private long dateToTimestamp(LocalDate date) {
      return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    @Test
    void testInfectionsCard() {
      assertThat(infections.getHeader())
          .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
          .containsExactly(INFECTIONS_CARD_ID, dateToTimestamp(LocalDate.of(2020, 11, 5)));
      assertThat(infections.getKeyFigures(1))
          .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(2895.0, Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testIncidenceCard() {
      assertThat(incidence.getHeader())
          .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
          .containsExactly(INCIDENCE_CARD_ID, dateToTimestamp(LocalDate.of(2020, 11, 7)));
      assertThat(incidence.getKeyFigures(0))
          .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(168.5, Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testKeySubmissionCard() {
      assertThat(keySubmission.getHeader())
          .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
          .containsExactly(KEY_SUBMISSION_CARD_ID, dateToTimestamp(LocalDate.of(2020, 11, 6)));
      assertThat(keySubmission.getKeyFigures(1))
          .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
          .containsExactly(123.0, Trend.INCREASING, TrendSemantic.POSITIVE);
    }

  }
}

