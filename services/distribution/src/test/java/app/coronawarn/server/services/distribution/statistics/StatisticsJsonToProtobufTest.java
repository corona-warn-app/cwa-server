package app.coronawarn.server.services.distribution.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.StatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StatisticsJsonToProtobufTest.class,
    StatisticsToProtobufMapping.class, KeyFigureCardFactory.class,
    StatisticJsonFileLoader.class
}, initializers = ConfigFileApplicationContextInitializer.class)
class StatisticsJsonToProtobufTest {

  @Autowired
  StatisticsToProtobufMapping statisticsToProtobufMapping;

  @Test
  void convertFromJsonToObjectTest() throws IOException {
    String content = FileUtils.readFileToString(
        new File("./src/test/resources/stats/statistic_data.json"), StandardCharsets.UTF_8);
    List<StatisticsJsonStringObject> statsDTO = SerializationUtils.deserializeJson(content, typeFactory -> typeFactory
        .constructCollectionType(List.class, StatisticsJsonStringObject.class));

    assertThat(statsDTO.size()).isEqualTo(3);

  }

  @Test
  void testGetCardIdSequenceFromConfig() {
    Statistics stats = statisticsToProtobufMapping.constructProtobufStatistics();

    assertThat(stats.getCardIdSequenceList().size()).isEqualTo(4);
  }


  @Test
  void testKeyFigureCardContainsHeader() {
    Statistics stats = statisticsToProtobufMapping.constructProtobufStatistics();

    assertThat(stats.getKeyFigureCardsCount()).isEqualTo(4);
    stats.getKeyFigureCardsList().forEach(keyFigureCard -> {
          assertThat(keyFigureCard.getHeader()).isNotNull();
          assertThat(keyFigureCard.getHeader().getUpdatedAt()).isPositive();
        }
    );
  }

  @Test
  void testKeyFigureCardBasedOnHeaderCardId() {
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
}

