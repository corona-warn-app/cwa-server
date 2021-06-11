package app.coronawarn.server.services.distribution.statistics.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.LocalStatisticsDownloaded;
import app.coronawarn.server.common.persistence.service.LocalStatisticsDownloadService;
import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.RegionMappingConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticType;
import app.coronawarn.server.services.distribution.statistics.file.MockStatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.file.StatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class LocalStatisticsJsonProcessingTest {

  @EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
  @ExtendWith(SpringExtension.class)
  @Nested
  @DisplayName("Mocked Loader")
  @ContextConfiguration(classes = {LocalStatisticsJsonProcessingTest.class}, initializers = ConfigDataApplicationContextInitializer.class)
  class StatisticsJsonMockLoaderTest {
    @MockBean
    LocalStatisticsDownloadService service;

    @MockBean
    StatisticJsonFileLoader mockLoader;

    @Autowired
    RegionMappingConfig regionMappingConfig;

    @Test
    void shouldNotGenerateStatisticsIfEtagNotUpdated() {
      when(service.getMostRecentDownload()).thenReturn(Optional.of(new LocalStatisticsDownloaded(1, 1234, "latest-etag")));
      when(mockLoader.getFileIfUpdated(eq(StatisticType.LOCAL), eq("latest-etag"))).thenReturn(Optional.empty());
      var statisticsToProtobufMapping = new LocalStatisticsToProtobufMapping(mockLoader, service, regionMappingConfig);
      var statistics = statisticsToProtobufMapping.constructProtobufLocalStatistics();
      assertThat(statistics.isEmpty()).isTrue();
      verify(mockLoader, times(1)).getFileIfUpdated(eq(StatisticType.LOCAL), eq("latest-etag"));
    }
  }

  @EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
  @ExtendWith(SpringExtension.class)
  @ActiveProfiles({"local-json-stats"})
  @Nested
  @DisplayName("General Tests")
  @ContextConfiguration(classes = {LocalStatisticsJsonProcessingTest.class,
      LocalStatisticsToProtobufMapping.class,
      MockStatisticJsonFileLoader.class
  }, initializers = ConfigDataApplicationContextInitializer.class)
  class StatisticsJsonParsingTest {
    @MockBean
    LocalStatisticsDownloadService service;

    @Autowired
    LocalStatisticsToProtobufMapping localStatisticsToProtobufMapping;

    @Test
    void convertFromJsonToObjectTest() throws IOException {
      String content = FileUtils.readFileToString(
          new File("./src/test/resources/stats/local_statistic_data_processing_test.json"), StandardCharsets.UTF_8);
      List<LocalStatisticsJsonStringObject> statsDTO = SerializationUtils.deserializeJson(content, typeFactory -> typeFactory
          .constructCollectionType(List.class, LocalStatisticsJsonStringObject.class));

      assertThat(statisticsObjectContainsFields(statsDTO, "2021-05-15")).isTrue();
      assertThat(statisticsObjectContainsFields(statsDTO, "2021-05-18")).isTrue();
    }

    private boolean statisticsObjectContainsFields(List<LocalStatisticsJsonStringObject> statsDTO, String timestamp) {
      return statsDTO.stream().anyMatch(stat -> stat.getEffectiveDate().compareTo(timestamp) == 0);
    }

    private boolean statisticsObjectNullDate(List<LocalStatisticsJsonStringObject> statsDTO) {
      return statsDTO.stream().anyMatch(stat -> stat.getEffectiveDate() == null);
    }



    @Test
    void testEffectiveDateValidation() throws IOException {
      StatisticsJsonValidator<LocalStatisticsJsonStringObject> statisticsJsonValidator = new StatisticsJsonValidator<>();
      String content = FileUtils.readFileToString(
          new File("./src/test/resources/stats/local_statistic_data_processing_test.json"), StandardCharsets.UTF_8);
      List<LocalStatisticsJsonStringObject> statsDTO = SerializationUtils.deserializeJson(content, typeFactory -> typeFactory
          .constructCollectionType(List.class, LocalStatisticsJsonStringObject.class));
      statsDTO = new ArrayList<>(statisticsJsonValidator.validate(statsDTO));

      assertThat(statisticsObjectContainsFields(statsDTO, "2021-05-15")).isTrue();
      //The json object that has the effective_date set on null should not be anymore present after the validation
      assertThat(statisticsObjectNullDate(statsDTO)).isFalse();
      //The json object that has the effective_date set on invalid format date should not be anymore present after the validation
      assertThat(statisticsObjectContainsFields(statsDTO, "2021-15-05")).isFalse();
    }
  }
}
