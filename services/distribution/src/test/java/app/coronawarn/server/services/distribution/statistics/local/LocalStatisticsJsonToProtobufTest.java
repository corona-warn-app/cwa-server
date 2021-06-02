package app.coronawarn.server.services.distribution.statistics.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.LocalStatisticsDownloadService;
import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.RegionMappingConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.file.JsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.file.MockStatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

class LocalStatisticsJsonToProtobufTest {

  @EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
  @ExtendWith(SpringExtension.class)
  @Nested
  @DisplayName("Mocked Loader")
  @ContextConfiguration(classes = {LocalStatisticsJsonToProtobufTest.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class StatisticsJsonMockLoaderTest {
    @MockBean
    LocalStatisticsDownloadService service;

    @MockBean
    JsonFileLoader mockLoader;

//    @Autowired
//    DistributionServiceConfig serviceConfig;

    @Test
    void shouldNotGenerateStatisticsIfEtagNotUpdated() {
//      when(service.getMostRecentDownload()).thenReturn(Optional.of(new StatisticsDownload(1, 1234, "latest-etag")));
//      when(mockLoader.getFileIfUpdated(eq("latest-etag"))).thenReturn(Optional.empty());
//      var statisticsToProtobufMapping = new StatisticsToProtobufMapping(serviceConfig, factory, mockLoader, service);
//      var statistics = statisticsToProtobufMapping.constructProtobufStatistics();
//      assertThat(statistics.getKeyFigureCardsList().isEmpty()).isTrue();
//      verify(mockLoader, times(1)).getFileIfUpdated(eq("latest-etag"));
    }
  }

  @EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
  @ExtendWith(SpringExtension.class)
  @ActiveProfiles({"local-json-stats"})
  @Nested
  @DisplayName("General Tests")
  @ContextConfiguration(classes = {LocalStatisticsJsonToProtobufTest.class,
      LocalStatisticsToProtobufMapping.class,
      MockStatisticJsonFileLoader.class
  }, initializers = ConfigDataApplicationContextInitializer.class)
  class StatisticsJsonParsingTest {
    @MockBean
    LocalStatisticsDownloadService service;

    @Autowired
    LocalStatisticsToProtobufMapping localStatisticsToProtobufMapping;

    @Test
    void convertFromJsonToObjectTest() throws IOException {
//      String content = FileUtils.readFileToString(
//          new File("./src/test/resources/stats/statistic_data.json"), StandardCharsets.UTF_8);
//      List<StatisticsJsonStringObject> statsDTO = SerializationUtils.deserializeJson(content, typeFactory -> typeFactory
//          .constructCollectionType(List.class, StatisticsJsonStringObject.class));
//
//      assertThat(statisticsObjectContainsFields(statsDTO, "2020-12-04T11:13:22,588")).isTrue();
    }

    private boolean statisticsObjectContainsFields(List<StatisticsJsonStringObject> statsDTO, String timestamp) {
      return statsDTO.stream().anyMatch(stat -> stat.getUpdateTimestamp().compareTo(timestamp) == 0);
    }

    @Test
    void testGetCardIdSequenceFromConfig() throws IOException {
//      LocalStatistics stats = localStatisticsToProtobufMapping.constructProtobufLocalStatistics();
//
//      assertThat(stats.getCardIdSequenceList().size()).isEqualTo(4);
    }


    @Test
    void testKeyFigureCardContainsHeader() throws IOException {
//      LocalStatistics stats = localStatisticsToProtobufMapping.constructProtobufLocalStatistics();

//      assertThat(stats.getKeyFigureCardsCount()).isEqualTo(4);
//      stats.getKeyFigureCardsList().forEach(keyFigureCard -> {
//            assertThat(keyFigureCard.getHeader()).isNotNull();
//            assertThat(keyFigureCard.getHeader().getUpdatedAt()).isPositive();
//          }
//      );
    }

    @Test
    void testKeyFigureCardBasedOnHeaderCardId() throws IOException {
//      LocalStatistics stats = localStatisticsToProtobufMapping.constructProtobufLocalStatistics();

//      KeyFigureCard infectionsCard = getKeyFigureCardForId(stats, 1);
//      KeyFigureCard incidenceCard = getKeyFigureCardForId(stats, 2);
//      KeyFigureCard keySubmissionsCard = getKeyFigureCardForId(stats, 3);
//
//      assertThat(infectionsCard.getKeyFiguresCount()).isEqualTo(3);
//      assertThat(incidenceCard.getKeyFiguresCount()).isEqualTo(1);
//      assertThat(keySubmissionsCard.getKeyFiguresCount()).isEqualTo(3);
    }

    @Test
    void testEffectiveDateValidation() throws IOException {
//      StatisticsJsonValidator<LocalStatisticsJsonStringObject> statisticsJsonValidator = new StatisticsJsonValidator<>();

//      String content = FileUtils.readFileToString(
//          new File("./src/test/resources/stats/statistic_data.json"), StandardCharsets.UTF_8);
//      List<StatisticsJsonStringObject> statsDTO = SerializationUtils.deserializeJson(content, typeFactory -> typeFactory
//          .constructCollectionType(List.class, StatisticsJsonStringObject.class));
//      statsDTO = new ArrayList<>(statisticsJsonValidator.validate(statsDTO));
//
//      assertThat(statisticsObjectContainsFields(statsDTO, "2020-12-04T11:13:22,588")).isTrue();
//      //The json object that has the effective_date set on null should not be anymore present after the validation
//      assertThat(statisticsObjectContainsFields(statsDTO, "2020-12-04T00:00:00,000")).isFalse();
//      //The json object that has the effective_date set on invalid format date should not be anymore present after the validation
//      assertThat(statisticsObjectContainsFields(statsDTO, "2020-12-05T00:01:00,000")).isFalse();
    }

//    private KeyFigureCard getKeyFigureCardForId(Statistics stats, Integer id) {
//      return stats.getKeyFigureCardsList()
//          .stream()
//          .filter(keyFigureCard -> keyFigureCard.getHeader().getCardId() == id)
//          .findFirst().get();
//    }
  }



  @EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
  @ExtendWith(SpringExtension.class)
  @ActiveProfiles({"local-json-stats", "wrong-json"})
  @Nested
  @DisplayName("Wrong JSON Properties Test")
  @ContextConfiguration(classes = {LocalStatisticsJsonToProtobufTest.class,
      LocalStatisticsToProtobufMapping.class,
      MockStatisticJsonFileLoader.class
  }, initializers = ConfigDataApplicationContextInitializer.class)
  class StatisticsWrongJsonTest {

    @MockBean
    LocalStatisticsDownloadService service;

    @Autowired
    LocalStatisticsToProtobufMapping statisticsToProtobufMapping;

    @Test
    void testGenerateStatsWithWrongJSON() throws IOException {
      when(service.getMostRecentDownload()).thenReturn(Optional.empty());
      var statsObject = statisticsToProtobufMapping.constructProtobufLocalStatistics();
//      var allEmpty = statsObject.getKeyFigureCardsList().stream()
//          .allMatch(c -> c.getHeader().getCardId() == KeyFigureCardSequenceConstants.EMPTY_CARD);
//      Assert.assertTrue("All key figure cards are empty: no properties in JSON to create cards", allEmpty);
    }

  }

  @EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
  @ExtendWith(SpringExtension.class)
  @ActiveProfiles({"local-json-stats", "processing-test"})
  @Nested
  @DisplayName("Value Processing Test")
  @ContextConfiguration(classes = {LocalStatisticsJsonToProtobufTest.class,
      LocalStatisticsToProtobufMapping.class,
      MockStatisticJsonFileLoader.class
  }, initializers = ConfigDataApplicationContextInitializer.class)

  class StatisticsJsonProcessingTest {

    @MockBean
    LocalStatisticsDownloadService service;

    @Autowired
    LocalStatisticsToProtobufMapping statisticsToProtobufMapping;

    LocalStatistics result;

//    KeyFigureCard infections, incidence, keySubmission;

    @BeforeEach
    void setup() throws IOException {
//      result = statisticsToProtobufMapping.constructProtobufLocalStatistics();
//      infections = result.getKeyFigureCards(0);
//      incidence = result.getKeyFigureCards(1);
//      keySubmission = result.getKeyFigureCards(2);
    }

    private long dateToTimestamp(LocalDate date) {
      return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    @Test
    void testInfectionsCard() {
//      assertThat(infections.getHeader())
//          .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
//          .containsExactly(INFECTIONS_CARD_ID, dateToTimestamp(LocalDate.of(2020, 11, 5)));
//      assertThat(infections.getKeyFigures(1))
//          .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
//          .containsExactly(2895.0, Trend.INCREASING, TrendSemantic.NEGATIVE);
    }

    @Test
    void testIncidenceCard() {
//      assertThat(incidence.getHeader())
//          .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
//          .containsExactly(INCIDENCE_CARD_ID, dateToTimestamp(LocalDate.of(2020, 11, 7)));
//      assertThat(incidence.getKeyFigures(0))
//          .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
//          .containsExactly(168.5, Trend.DECREASING, TrendSemantic.POSITIVE);
    }

    @Test
    void testKeySubmissionCard() {
//      assertThat(keySubmission.getHeader())
//          .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
//          .containsExactly(KEY_SUBMISSION_CARD_ID, dateToTimestamp(LocalDate.of(2020, 11, 6)));
//      assertThat(keySubmission.getKeyFigures(1))
//          .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
//          .containsExactly(11.428571428571429, Trend.STABLE, TrendSemantic.NEUTRAL);
    }

  }
}
