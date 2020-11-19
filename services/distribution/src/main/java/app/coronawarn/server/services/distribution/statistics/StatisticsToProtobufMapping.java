package app.coronawarn.server.services.distribution.statistics;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class StatisticsToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(StatisticsToProtobufMapping.class);


  private final DistributionServiceConfig distributionServiceConfig;

  private final KeyFigureCardFactory keyFigureCardFactory;

  public StatisticsToProtobufMapping(DistributionServiceConfig distributionServiceConfig,
      KeyFigureCardFactory keyFigureCardFactory) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.keyFigureCardFactory = keyFigureCardFactory;
  }


  /**
   * Create protobuf statistic object from raw JSON statistics.
   *
   * @return Statistics protobuf statistics object.
   * @throws IOException .
   */
  @Bean
  public Statistics constructProtobufStatistics() throws IOException {
    String content = FileUtils
        .readFileToString(new File(
                "/Users/i353910/Work/cwa/cwa-server/services/distribution/src/main/resources/stats/statistic_data.json"),
            StandardCharsets.UTF_8);
    List<StatisticsJsonStringObject> jsonStringObjects = SerializationUtils
        .deserializeJson(content, typeFactory -> typeFactory
            .constructCollectionType(List.class, StatisticsJsonStringObject.class));

    return Statistics.newBuilder()
        .addAllCardIdSequence(getAllCardIdSequence())
        .addAllKeyFigureCards(buildAllKeyFigureCards(jsonStringObjects))
        .build();
  }

  private List<Integer> getAllCardIdSequence() {
    List<Integer> idSequence = new ArrayList<>();
    String[] idSequenceArray = distributionServiceConfig.getCardIdSequence()
        .replace("[", "").replace("]", "")
        .split(",");
    List<Integer> idIntegerSequence = new ArrayList<>();
    for (String id : idSequenceArray) {
      idIntegerSequence.add(Integer.parseInt(id));
    }
    return idIntegerSequence;
  }

  private List<KeyFigureCard> buildAllKeyFigureCards(List<StatisticsJsonStringObject> jsonStringObjects) {
    Map<LocalDate, List<KeyFigureCard>> figureCardsMap = new HashMap<>();

    jsonStringObjects.forEach(jsonObject -> {
      List<KeyFigureCard> keyFigureCards = new ArrayList<>();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate dateTime = LocalDate.parse(jsonObject.getEffectiveDate(), formatter);

      getAllCardIdSequence().forEach(id -> {
        keyFigureCards.add(keyFigureCardFactory.createKeyFigureCard(jsonObject, id));
      });
      figureCardsMap.put(dateTime, keyFigureCards);
    });

    return figureCardsMap.values().stream().findFirst().get();
  }
}
