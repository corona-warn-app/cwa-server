package app.coronawarn.server.services.distribution.statistics.local;

import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.persistence.service.LocalStatisticsDownloadService;
import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
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
        this.updateETag(file.get().getETag());

        List<LocalStatisticsJsonStringObject> onePerProvinceStatistics = deserializeAndValidate(file);

        onePerProvinceStatistics.forEach(localStatisticsJsonStringObject -> {
          if (localStatisticsJsonStringObject.getProvinceCode() != null) {
            int provinceCode = Integer.parseInt(localStatisticsJsonStringObject.getProvinceCode());

            if (isFederalState(localStatisticsJsonStringObject.getProvinceCode())) {
              fillLocalStatisticsFederalStatesGroupMap(
                  localStatisticsMap,
                  provinceCode,
                  localStatisticsJsonStringObject,
                  federalStateSupplier(provinceCode, localStatisticsJsonStringObject),
                  federalStateEnhancer(provinceCode, localStatisticsJsonStringObject)
              );
            } else {

              Optional<String> federalStateCodeOptional = findFederalStateByProvinceCode(
                  localStatisticsJsonStringObject.getProvinceCode());

              federalStateCodeOptional.ifPresent(code -> {
                fillLocalStatisticsFederalStatesGroupMap(
                    localStatisticsMap,
                    Integer.parseInt(federalStateCodeOptional.get()),
                    localStatisticsJsonStringObject,
                    administrativeUnitSupplier(provinceCode, localStatisticsJsonStringObject),
                    administrativeUnitEnhancer(provinceCode, localStatisticsJsonStringObject)
                );
              });
            }
          }
        });
      }
    } catch (BucketNotFoundException | ConnectionException | FilePathNotFoundException | IOException ex) {
      logger.error("Local statistics file not generated!", ex);
    }

    return localStatisticsMap;
  }

  private void fillLocalStatisticsFederalStatesGroupMap(Map<Integer, LocalStatistics> localStatisticsMap,
      int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject,
      Supplier<LocalStatistics> statisticsSupplier,
      Function<LocalStatistics, LocalStatistics> statisticsEnhancer) {

    Optional<Integer> federalStateGroupOptional = regionMappingConfig.getFederalStateGroup(federalStateCode);

    processLocalStatisticsMapEntry(
        localStatisticsMap,
        federalStateGroupOptional,
        statisticsSupplier,
        statisticsEnhancer
    );
  }

  private void processLocalStatisticsMapEntry(
      Map<Integer, LocalStatistics> localStatisticsMap,
      Optional<Integer> federalStateGroupOptional,
      Supplier<LocalStatistics> statisticsSupplier,
      Function<LocalStatistics, LocalStatistics> statisticsEnhancer) {

    if (federalStateGroupOptional.isPresent()) {
      if (localStatisticsMap.get(federalStateGroupOptional.get()) != null) {
        LocalStatistics regionGroupStatistics = localStatisticsMap.get(federalStateGroupOptional.get());

        localStatisticsMap.put(federalStateGroupOptional.get(), statisticsEnhancer.apply(regionGroupStatistics));
      } else {
        localStatisticsMap.put(federalStateGroupOptional.get(), statisticsSupplier.get());
      }
    }
  }

  private List<LocalStatisticsJsonStringObject> filterOncePerProvinceStatistics(
      List<LocalStatisticsJsonStringObject> jsonStringObjects) {
    List<LocalStatisticsJsonStringObject> onePerProvinceStatistics = new ArrayList<>();
    Map<String, List<LocalStatisticsJsonStringObject>> groupedByProvince = jsonStringObjects.stream()
        .collect(groupingBy(LocalStatisticsJsonStringObject::getProvinceCode, toList()));

    groupedByProvince.keySet().stream().forEach(key -> {
      List<LocalStatisticsJsonStringObject> sameProvinceStatistics = groupedByProvince.get(key);
      LocalStatisticsJsonStringObject mostRecentStatistic = null;

      for (LocalStatisticsJsonStringObject provinceStatistic : sameProvinceStatistics) {
        if (mostRecentStatistic == null) {
          mostRecentStatistic = provinceStatistic;
        } else {
          if (LocalDate.parse(mostRecentStatistic.getEffectiveDate())
              .isBefore(LocalDate.parse(provinceStatistic.getEffectiveDate()))) {
            mostRecentStatistic = provinceStatistic;
          }
        }
      }

      onePerProvinceStatistics.add(mostRecentStatistic);
    });

    return onePerProvinceStatistics;
  }

  private List<LocalStatisticsJsonStringObject> deserializeAndValidate(Optional<JsonFile> file) throws IOException {
    StatisticsJsonValidator<LocalStatisticsJsonStringObject> validator = new StatisticsJsonValidator<>();

    List<LocalStatisticsJsonStringObject> jsonStringObjects = validator.validate(
        SerializationUtils.deserializeJson(
            file.get().getContent(), typeFactory -> typeFactory
                .constructCollectionType(List.class, LocalStatisticsJsonStringObject.class)
        )
    );

    return filterOncePerProvinceStatistics(jsonStringObjects);
  }

  private boolean isFederalState(String provinceCode) {
    return provinceCode.length() >= 1 && provinceCode.length() <= 2;
  }


}
