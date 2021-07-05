package app.coronawarn.server.services.distribution.statistics.local;

import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.administrativeUnitEnhancer;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.administrativeUnitSupplier;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.federalStateEnhancer;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.federalStateSupplier;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.findFederalStateByProvinceCode;
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
   * Process the JSON file provided by TSI and map the it to Local Statistics protobuf object.
   *
   * @param jsonFileLoader - Loader of the file from the system
   * @param localStatisticsDownloadService - Local Statistics Download Service for keeping track of ETags
   */
  public LocalStatisticsToProtobufMapping(StatisticJsonFileLoader jsonFileLoader,
      LocalStatisticsDownloadService localStatisticsDownloadService,
      RegionMappingConfig regionMappingConfig) {
    this.jsonFileLoader = jsonFileLoader;
    this.localStatisticsDownloadService = localStatisticsDownloadService;
    this.regionMappingConfig = regionMappingConfig;
  }

  /**
   * Create protobuf local statistic map from raw JSON local statistics.
   * Local statistics has to be uploaded on CDN in 7 packages. Each entry on the map represents 1 package.
   * Packages of local statistics contains data about 1 up to 4 federal states and all their administrative units.
   * For more info related to the grouping of the federal states into packages please look into
   * https://github.com/corona-warn-app/cwa-app-tech-spec/blob/42e9e4f3c588cd2fd283f904e9f0ccd53a2b83d0/
   * docs/spec/statistics.md#populate-local-statistical-data
   *
   * @return map containing local statistics grouped by archive id.
   */
  @Bean
  public Map<Integer, LocalStatistics> constructProtobufLocalStatistics() {
    Map<Integer, LocalStatistics> localStatisticsMap = new HashMap<>();

    try {
      Optional<JsonFile> optionalFile = this.getFile();

      if (optionalFile.isEmpty()) {
        logger.warn("Stats file is already updated to the latest version. Skipping generation.");
        return Collections.emptyMap();
      } else {
        JsonFile file = optionalFile.get();

        List<LocalStatisticsJsonStringObject> onePerProvinceStatistics = deserializeAndValidate(file);

        onePerProvinceStatistics.forEach(localStatisticsJsonStringObject -> {
          if (localStatisticsJsonStringObject.getProvinceCode() != null) {
            int provinceCode = Integer.parseInt(localStatisticsJsonStringObject.getProvinceCode());

            if (isFederalState(provinceCode)) {
              fillLocalStatisticsFederalStatesGroupMap(
                  localStatisticsMap,
                  provinceCode,
                  federalStateSupplier(provinceCode, localStatisticsJsonStringObject),
                  federalStateEnhancer(provinceCode, localStatisticsJsonStringObject)
              );
            } else {
              int federalStateCode = findFederalStateByProvinceCode(provinceCode);
              fillLocalStatisticsFederalStatesGroupMap(
                  localStatisticsMap,
                  federalStateCode,
                  administrativeUnitSupplier(provinceCode, localStatisticsJsonStringObject),
                  administrativeUnitEnhancer(localStatisticsJsonStringObject)
              );
            }
          }
        });
        this.updateETag(optionalFile.get().getETag());
      }
    } catch (Exception ex) {
      logger.error("Local statistics file not generated!", ex);
    }

    return localStatisticsMap;
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
   * Handles the addition or enhancing of Local Statistics objects in the map.
   * If the map already contains the CDN package number for the current {@code federalStateCode}, the Local
   * Statistics Object put on that key (CDN package number) will be enhanced using {@code statisticsEnhancer}
   * EX: Map contains group 3 put on key 3. Group 3 is already represented in the map by an {@link LocalStatistics}
   * object which contains data about BE and BB federal states.
   * The method is called with {@code federalStateCode} representing MV federal state this time.
   * In this case the data will be added on map value under key 3 ({@link LocalStatistics}) in the federal data list
   * of this object.
   * If the map does not contain the CDN package number for the current {@code federalStateCode},
   * the Local Statistics Object supplied by {@code statisticsSupplier} will be put to the map with the key
   * equal to the CDN package group in which the {@code federalStateCode} should stay.
   *
   * @param localStatisticsMap - local statistics map grouped by archive index.
   * @param federalStateCode - federal state code
   * @param statisticsSupplier - supplier which builds local statistics
   * @param statisticsEnhancer - supplier which enhance local statistics
   */
  private void fillLocalStatisticsFederalStatesGroupMap(Map<Integer, LocalStatistics> localStatisticsMap,
      int federalStateCode,
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
        .filter(LocalStatisticsJsonStringObject::isComplete)
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

  private List<LocalStatisticsJsonStringObject> deserializeAndValidate(JsonFile file) throws IOException {
    StatisticsJsonValidator<LocalStatisticsJsonStringObject> validator = new StatisticsJsonValidator<>();

    List<LocalStatisticsJsonStringObject> jsonStringObjects = validator.validate(
        SerializationUtils.deserializeJson(
            file.getContent(), typeFactory -> typeFactory
                .constructCollectionType(List.class, LocalStatisticsJsonStringObject.class)
        )
    );

    return filterOncePerProvinceStatistics(jsonStringObjects);
  }

  private boolean isFederalState(int provinceCode) {
    return provinceCode < 100;
  }

}
