package app.coronawarn.server.services.distribution.statistics;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INCIDENCE_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INFECTIONS_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.KEY_SUBMISSION_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.REPRODUCTION_NUMBER_CARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.MockStatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats", "processing-test", "debug"})
@ContextConfiguration(classes = {StatisticsJsonToProtobufTest.class,
    StatisticsToProtobufMapping.class, KeyFigureCardFactory.class,
    MockStatisticJsonFileLoader.class
}, initializers = ConfigDataApplicationContextInitializer.class)
class StatisticsJsonProcessingTest {

  @Autowired
  StatisticsToProtobufMapping mapping;

  @MockBean
  StatisticsDownloadService service;

  private long dateToTimestamp(LocalDate date) {
    return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
  }

  @Test
  void shouldRunParsing() throws IOException {
    var result = mapping.constructProtobufStatistics();
    when(service.getMostRecentDownload()).thenReturn(Optional.empty());

    // Assert Infections card
    assertThat(result.getKeyFigureCards(0).getHeader())
        .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
        .containsExactly(INFECTIONS_CARD.ordinal(), dateToTimestamp(LocalDate.of(2020, 11, 5)));
    assertThat(result.getKeyFigureCards(0).getKeyFigures(1))
        .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
        .containsExactly(2895.0, Trend.INCREASING, TrendSemantic.NEGATIVE);

    // Assert Incidence Card
    assertThat(result.getKeyFigureCards(1).getHeader())
        .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
        .containsExactly(INCIDENCE_CARD.ordinal(), dateToTimestamp(LocalDate.of(2020, 11, 7)));
    assertThat(result.getKeyFigureCards(1).getKeyFigures(0))
        .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
        .containsExactly(168.5, Trend.DECREASING, TrendSemantic.POSITIVE);

    // Assert Key Submissions Card
    assertThat(result.getKeyFigureCards(2).getHeader())
        .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
        .containsExactly(KEY_SUBMISSION_CARD.ordinal(), dateToTimestamp(LocalDate.of(2020, 11, 6)));
    assertThat(result.getKeyFigureCards(2).getKeyFigures(1))
        .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
        .containsExactly(11.428571428571429, Trend.STABLE, TrendSemantic.NEUTRAL);

    // Assert Reproduction Number Card
    assertThat(result.getKeyFigureCards(3).getHeader())
        .extracting(CardHeader::getCardId, CardHeader::getUpdatedAt)
        .containsExactly(REPRODUCTION_NUMBER_CARD.ordinal(), dateToTimestamp(LocalDate.of(2020, 11, 5)));
    assertThat(result.getKeyFigureCards(3).getKeyFigures(0))
        .extracting(KeyFigure::getValue, KeyFigure::getTrend, KeyFigure::getTrendSemantic)
        .containsExactly(1.67, Trend.INCREASING, TrendSemantic.NEGATIVE);
  }

}
