package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeserializedDailySummariesConfigTest {

  @Test
  void testCreateDeserializedDailySummariesConfigObjectFromYaml() throws UnableToLoadFileException {
    final String ANDROID_V2_DAILY_SUMMARIES_FILE = "master-config/v2/daily-summaries-config.yaml";

    DeserializedDailySummariesConfig dailySummaries = YamlLoader
        .loadYamlIntoClass(ANDROID_V2_DAILY_SUMMARIES_FILE, DeserializedDailySummariesConfig.class);

    assertThat(dailySummaries.getAttenuationBucketThresholdDb()).containsAll(List.of(30, 50, 70));
    assertThat(dailySummaries.getAttenuationBucketWeights()).containsAll(List.of(1.0, 1.0, 1.0, 1.0));
    assertThat(dailySummaries.getDaysSinceExposureThreshold()).isEqualTo(0);
    assertThat(dailySummaries.getInfectiousnessWeights()).containsAllEntriesOf(Map.of(1, 1.0, 2, 1.0));
    assertThat(dailySummaries.getMinimumWindowScore()).isEqualTo(0);
    assertThat(dailySummaries.getReportTypeWeights()).containsAllEntriesOf(Map.of(1, 1.0, 2, 1.0, 3, 1.0, 4, 1.0));





  }
}
