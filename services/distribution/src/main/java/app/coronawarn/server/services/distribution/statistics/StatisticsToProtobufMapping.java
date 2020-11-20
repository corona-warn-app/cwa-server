package app.coronawarn.server.services.distribution.statistics;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.StatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsToProtobufMapping {

  private final DistributionServiceConfig distributionServiceConfig;
  private final KeyFigureCardFactory keyFigureCardFactory;
  private final StatisticJsonFileLoader jsonFileLoader;

  /**
   * Process the JSON file provided by TSI and map the it to Statistics protobuf object.
   *
   * @param distributionServiceConfig The config properties
   * @param keyFigureCardFactory      KeyFigureCard structure provider
   * @param jsonFileLoader            Loader of the file from the system
   */
  public StatisticsToProtobufMapping(DistributionServiceConfig distributionServiceConfig,
      KeyFigureCardFactory keyFigureCardFactory,
      StatisticJsonFileLoader jsonFileLoader) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.keyFigureCardFactory = keyFigureCardFactory;
    this.jsonFileLoader = jsonFileLoader;
  }


  /**
   * Create protobuf statistic object from raw JSON statistics.
   *
   * @return Statistics protobuf statistics object.
   */
  @Bean
  public Statistics constructProtobufStatistics() {
    String content = this.jsonFileLoader.getContent();
    List<StatisticsJsonStringObject> jsonStringObjects = SerializationUtils
        .deserializeJson(content, typeFactory -> typeFactory
            .constructCollectionType(List.class, StatisticsJsonStringObject.class));

    StatisticsJsonValidator validator = new StatisticsJsonValidator();
    jsonStringObjects = new ArrayList<>(validator.validate(jsonStringObjects));

    return Statistics.newBuilder()
        .addAllCardIdSequence(getAllCardIdSequence())
        .addAllKeyFigureCards(buildAllKeyFigureCards(jsonStringObjects))
        .build();
  }

  private List<Integer> getAllCardIdSequence() {
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

      getAllCardIdSequence().forEach(id ->
          keyFigureCards.add(keyFigureCardFactory.createKeyFigureCard(jsonObject, id)));
      figureCardsMap.put(dateTime, keyFigureCards);
    });

    return figureCardsMap.values().stream().findFirst().isPresent() ?
        figureCardsMap.values().stream().findFirst().get() : Collections.emptyList();
  }
}
