package app.coronawarn.server.services.distribution.statistics;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.FOURTH_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INFECTIONS_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.KEY_SUBMISSION_CARD_ID;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.StatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.MissingPropertyException;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(StatisticsToProtobufMapping.class);

  private final DistributionServiceConfig distributionServiceConfig;
  private final KeyFigureCardFactory keyFigureCardFactory;
  private final StatisticJsonFileLoader jsonFileLoader;

  /**
   * Process the JSON file provided by TSI and map the it to Statistics probobuf object.
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
   * @throws IOException .
   */
  @Bean
  public Statistics constructProtobufStatistics() throws IOException {
    String content = this.jsonFileLoader.getContent();
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

  private LocalDate effectiveDateStringToLocalDate(String effectiveDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return LocalDate.parse(effectiveDate, formatter);
  }

  @SuppressWarnings("checkstyle:Indentation")
  private List<KeyFigureCard> buildAllKeyFigureCards(List<StatisticsJsonStringObject> jsonStringObjects) {
    Map<Integer, KeyFigureCard> figureCardMap = new HashMap<>();

    figureCardMap.put(INFECTIONS_CARD_ID, null);
    figureCardMap.put(INCIDENCE_CARD_ID, null);
    figureCardMap.put(KEY_SUBMISSION_CARD_ID, null);
    figureCardMap.put(FOURTH_CARD_ID, null);

    List<StatisticsJsonStringObject> orderedList = jsonStringObjects.stream()
        .sorted(Comparator.comparing(a -> effectiveDateStringToLocalDate(a.getEffectiveDate())))
        .collect(Collectors.toList());
    Collections.reverse(orderedList);

    for (var stat : orderedList) {
      getAllCardIdSequence().forEach(id -> {
        if (figureCardMap.get(id) == null) {
          KeyFigureCard card;
          try {
            card = keyFigureCardFactory.createKeyFigureCard(stat, id);
            figureCardMap.put(id, card);
          } catch (MissingPropertyException ex) {
            logger.warn(ex.getMessage());
          }
        }
      });

      if (figureCardMap.values().stream().allMatch(Objects::nonNull)) {
        break;
      }
    }

    return List.of(
        figureCardMap.get(INFECTIONS_CARD_ID),
        figureCardMap.get(INCIDENCE_CARD_ID),
        figureCardMap.get(KEY_SUBMISSION_CARD_ID),
        figureCardMap.get(FOURTH_CARD_ID)
    );
  }
}
