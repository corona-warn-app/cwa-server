package app.coronawarn.server.services.distribution.statistics;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.*;

import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.exceptions.BucketNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.ConnectionException;
import app.coronawarn.server.services.distribution.statistics.exceptions.FilePathNotFoundException;
import app.coronawarn.server.services.distribution.statistics.file.JsonFile;
import app.coronawarn.server.services.distribution.statistics.file.JsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.MissingPropertyException;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private final JsonFileLoader jsonFileLoader;
  private final StatisticsDownloadService statisticsDownloadService;

  /**
   * Process the JSON file provided by TSI and map the it to Statistics protobuf object.
   * @param distributionServiceConfig The config properties
   * @param keyFigureCardFactory      KeyFigureCard structure provider
   * @param jsonFileLoader            Loader of the file from the system
   * @param statisticsDownloadService Statistics Download Service for keeping track of ETags
   */
  public StatisticsToProtobufMapping(DistributionServiceConfig distributionServiceConfig,
      KeyFigureCardFactory keyFigureCardFactory,
      JsonFileLoader jsonFileLoader,
      StatisticsDownloadService statisticsDownloadService) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.keyFigureCardFactory = keyFigureCardFactory;
    this.jsonFileLoader = jsonFileLoader;
    this.statisticsDownloadService = statisticsDownloadService;
  }

  private Optional<JsonFile> getFile() {
    var mostRecent = this.statisticsDownloadService.getMostRecentDownload();
    if (mostRecent.isPresent()) {
      return this.jsonFileLoader.getFileIfUpdated(mostRecent.get().getEtag());
    } else {
      return Optional.of(this.jsonFileLoader.getFile());
    }
  }

  private void updateETag(String newETag) {
    var currentTimestamp = TimeUtils.getCurrentUtcHour().toEpochSecond(ZoneOffset.UTC);
    this.statisticsDownloadService.store(currentTimestamp, newETag);
  }

  /**
   * Create protobuf statistic object from raw JSON statistics.
   *
   * @return Statistics protobuf statistics object.
   */
  @Bean
  public Statistics constructProtobufStatistics() {
    try {
      Optional<JsonFile> file = this.getFile();
      if (file.isEmpty()) {
        logger.warn("Stats file is already updated to the latest version. Skipping generation.");
        return Statistics.newBuilder().build();
      } else {
        List<StatisticsJsonStringObject> jsonStringObjects = SerializationUtils
            .deserializeJson(file.get().getContent(), typeFactory -> typeFactory
                .constructCollectionType(List.class, StatisticsJsonStringObject.class));

        StatisticsJsonValidator validator = new StatisticsJsonValidator();
        jsonStringObjects = new ArrayList<>(validator.validate(jsonStringObjects));

        this.updateETag(file.get().getETag());
        return Statistics.newBuilder()
            .addAllCardIdSequence(getAllCardIdSequence())
            .addAllKeyFigureCards(buildAllKeyFigureCards(jsonStringObjects))
            .build();
      }
    } catch (BucketNotFoundException | ConnectionException | FilePathNotFoundException ex) {
      logger.error("Statistics file not generated!", ex);
      return Statistics.newBuilder().build();
    }
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

  private LocalDate effectiveDateStringToLocalDate(String effectiveDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return LocalDate.parse(effectiveDate, formatter);
  }

  @SuppressWarnings("checkstyle:Indentation")
  private List<KeyFigureCard> buildAllKeyFigureCards(List<StatisticsJsonStringObject> jsonStringObjects) {
    Map<Integer, Optional<KeyFigureCard>> figureCardMap = new HashMap<>();

    figureCardMap.put(INFECTIONS_CARD_ID, Optional.empty());
    figureCardMap.put(INCIDENCE_CARD_ID, Optional.empty());
    figureCardMap.put(KEY_SUBMISSION_CARD_ID, Optional.empty());
    figureCardMap.put(REPRODUCTION_NUMBER_CARD, Optional.empty());

    List<StatisticsJsonStringObject> orderedList = jsonStringObjects.stream()
        .sorted(Comparator.comparing(a -> effectiveDateStringToLocalDate(a.getEffectiveDate())))
        .collect(Collectors.toList());
    Collections.reverse(orderedList);

    List<StatisticsJsonStringObject> collectedJsonObjects = new ArrayList<>();

    for (var stat : orderedList) {
      collectedJsonObjects.add(stat);
      getAllCardIdSequence().forEach(id -> {
        if (figureCardMap.get(id).isEmpty()) {
          KeyFigureCard card;
          try {
            card = keyFigureCardFactory.createKeyFigureCard(stat, id);
            logger.info("[{}] {} successfully created", stat.getEffectiveDate(), toCardName(id));
            figureCardMap.put(id, Optional.of(card));
          } catch (MissingPropertyException ex) {
            logger.warn("[{}] {}", stat.getEffectiveDate(), ex.getMessage());
          }
        }
      });

      if (figureCardMap.values().stream().allMatch(Optional::isPresent)) {
        break;
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("The following statistics JSON entries were used to create the cards. Null values are omitted.");
      for (var stat: collectedJsonObjects) {
        var jsonString = SerializationUtils.stringifyObject(stat);
        logger.debug("[{}] {}", stat.getEffectiveDate(), jsonString);
      }
    }

    var emptyCard = keyFigureCardFactory.createKeyFigureCard(jsonStringObjects.get(0), EMPTY_CARD);
    return List.of(
        figureCardMap.get(INFECTIONS_CARD_ID).orElse(emptyCard),
        figureCardMap.get(INCIDENCE_CARD_ID).orElse(emptyCard),
        figureCardMap.get(KEY_SUBMISSION_CARD_ID).orElse(emptyCard),
        figureCardMap.get(REPRODUCTION_NUMBER_CARD).orElse(emptyCard)
    );
  }
}
