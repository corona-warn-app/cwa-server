package app.coronawarn.server.services.distribution.statistics.local;

import app.coronawarn.server.common.persistence.service.LocalStatisticsDownloadService;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData.FederalState;
import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.common.protocols.internal.stats.SevenDayIncidenceData;
import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.RegionMappingConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticType;
import app.coronawarn.server.services.distribution.statistics.exceptions.BucketNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.ConnectionException;
import app.coronawarn.server.services.distribution.statistics.exceptions.FilePathNotFoundException;
import app.coronawarn.server.services.distribution.statistics.file.JsonFile;
import app.coronawarn.server.services.distribution.statistics.file.StatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalStatisticsToProtobufMapping {

  private static final Logger logger = LoggerFactory.getLogger(LocalStatisticsToProtobufMapping.class);

  private final StatisticJsonFileLoader jsonFileLoader;
  private final LocalStatisticsDownloadService localStatisticsDownloadService;
  private final RegionMappingConfig regionMappingConfig;

  /**
   * Process the JSON file provided by TSI and map the it to Statistics protobuf object.
   *
   * @param jsonFileLoader                 Loader of the file from the system
   * @param localStatisticsDownloadService Statistics Download Service for keeping track of ETags
   */
  public LocalStatisticsToProtobufMapping(StatisticJsonFileLoader jsonFileLoader,
      LocalStatisticsDownloadService localStatisticsDownloadService,
      RegionMappingConfig regionMappingConfig) {
    this.jsonFileLoader = jsonFileLoader;
    this.localStatisticsDownloadService = localStatisticsDownloadService;
    this.regionMappingConfig = regionMappingConfig;
  }

  private Optional<JsonFile> getFile() {
    var mostRecent = this.localStatisticsDownloadService.getMostRecentDownload();
    if (mostRecent.isPresent()) {
      return this.jsonFileLoader.getFileIfUpdated(StatisticType.LOCAL, mostRecent.get().getEtag());
    } else {
      return Optional.of(this.jsonFileLoader.getFile(StatisticType.LOCAL));
    }
  }

  private void updateETag(String newETag) {
    var currentTimestamp = TimeUtils.getCurrentUtcHour().toEpochSecond(ZoneOffset.UTC);
    this.localStatisticsDownloadService.store(currentTimestamp, newETag);
  }

  /**
   * Create protobuf statistic object from raw JSON statistics.
   *
   * @return Statistics protobuf statistics object.
   */
  @Bean
  public Map<Integer, LocalStatistics> constructProtobufLocalStatistics() {
    Map<Integer, LocalStatistics> localStatisticsMap = new HashMap<>();

    try {
      Optional<JsonFile> file = this.getFile();

      if (file.isEmpty()) {
        logger.warn("Stats file is already updated to the latest version. Skipping generation.");
        return Collections.emptyMap();
      } else {
        StatisticsJsonValidator<LocalStatisticsJsonStringObject> validator = new StatisticsJsonValidator<>();

        List<LocalStatisticsJsonStringObject> jsonStringObjects = validator.validate(
            SerializationUtils.deserializeJson(
                file.get().getContent(), typeFactory -> typeFactory
                    .constructCollectionType(List.class, LocalStatisticsJsonStringObject.class)
            )
        );

        this.updateETag(file.get().getETag());

        jsonStringObjects.forEach(localStatisticsJsonStringObject -> {
          Optional<String> federalStateCodeOptional = provinceToFederalStateMapping(
              localStatisticsJsonStringObject.getProvinceCode());

          if (federalStateCodeOptional.isPresent()) {
            int federalStateCode = Integer.parseInt(federalStateCodeOptional.get());
            Optional<Integer> federalStateGroupOptional = regionMappingConfig.getFederalStateGroup(federalStateCode);

            if (federalStateGroupOptional.isPresent()) {
              if (localStatisticsMap.get(federalStateGroupOptional.get()) != null) {
                LocalStatistics regionGroupStatistics = localStatisticsMap.get(federalStateGroupOptional.get());

                localStatisticsMap.put(federalStateGroupOptional.get(),
                    addFederalStateData(regionGroupStatistics, federalStateCode, localStatisticsJsonStringObject));
              } else {
                localStatisticsMap.put(federalStateGroupOptional.get(),
                    buildLocalStatistics(federalStateCode, localStatisticsJsonStringObject));
              }
            }
          }
        });
      }
    } catch (BucketNotFoundException | ConnectionException | FilePathNotFoundException | IOException ex) {
      logger.error("Local statistics file not generated!", ex);
    }

    return localStatisticsMap;
  }

  private SevenDayIncidenceData buildSevenDaysIncidence(
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return SevenDayIncidenceData.newBuilder()
        .setValue(localStatisticsJsonStringObject.getSevenDayIncidence1stReportedDaily())
        .build();
  }

  private FederalStateData buildFederalStateData(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return FederalStateData.newBuilder()
        .setFederalState(FederalState.forNumber(federalStateCode))
        .setSevenDayIncidence(buildSevenDaysIncidence(localStatisticsJsonStringObject))
        .build();
  }

  private LocalStatistics buildLocalStatistics(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return LocalStatistics.newBuilder()
        .addFederalStateData(buildFederalStateData(federalStateCode, localStatisticsJsonStringObject))
        .build();
  }

  private LocalStatistics addFederalStateData(LocalStatistics localStatistics,
      int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return localStatistics.toBuilder()
        .addFederalStateData(buildFederalStateData(federalStateCode, localStatisticsJsonStringObject))
        .build();
  }

  private Optional<String> provinceToFederalStateMapping(String provinceCode) {
    switch (provinceCode.length()) {
      case 1:
      case 2:
        return Optional.of(provinceCode);
      case 4:
        return Optional.of(provinceCode.substring(0, 1));
      case 5:
        return Optional.of(provinceCode.substring(0, 2));
      default:
        return Optional.empty();
    }
  }
}
