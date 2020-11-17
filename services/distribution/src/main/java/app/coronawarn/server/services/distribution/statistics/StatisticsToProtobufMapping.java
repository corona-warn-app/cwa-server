package app.coronawarn.server.services.distribution.statistics;

import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(StatisticsToProtobufMapping.class);


  private final DistributionServiceConfig distributionServiceConfig;

  public StatisticsToProtobufMapping(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }


  public Statistics constructProtobufStatistics() throws IOException {
    String content = FileUtils
        .readFileToString(new File("./src/test/resources/stats/statistic_data.json"), StandardCharsets.UTF_8);
    List<StatisticsJsonStringObject> jsonStringObjects = SerializationUtils
        .deserializeJson(content, typeFactory -> typeFactory
            .constructCollectionType(List.class, StatisticsJsonStringObject.class));

    return Statistics.newBuilder()
        .addAllCardIdSequence(getAllCardIdSequence())
        //.addAllKeyFigureCards(buildAllKeyFigureCards(jsonStringObject))
        .build();
  }

  private List<Integer> getAllCardIdSequence() {
    List<Integer> idSequence = new ArrayList<>();
    String[] idSequenceArray = distributionServiceConfig.getCardIdSequence().
        replace("[", "").replace("]", "")
        .split(",");
    List<Integer> idIntegerSequence = new ArrayList<>();
    for (String id : idSequenceArray) {
      idIntegerSequence.add(Integer.parseInt(id));
    }
    return idIntegerSequence;
  }

  private List<KeyFigureCard> buildAllKeyFigureCards(List<StatisticsJsonStringObject> jsonStringObjects) {
    List<KeyFigureCard> figureCards = new ArrayList<>();
    jsonStringObjects.forEach(jsonObject -> {
      getAllCardIdSequence().forEach(id -> {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(jsonObject.getUpdateTimestamp(), formatter);
        CardHeader cardHeader = CardHeader.newBuilder()
            .setCardId(id)
            .build();
      });
    });

    return null;
  }
}
