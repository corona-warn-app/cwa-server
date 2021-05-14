package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeserializedDailySummariesConfigTest {

  @Test
  void testCreateDeserializedDailySummariesConfigObjectFromYaml() throws UnableToLoadFileException {
    final String ANDROID_V2_DAILY_SUMMARIES_FILE = "main-config/v2/daily-summaries-config.yaml";

    DeserializedDailySummariesConfig dailySummaries = YamlLoader
        .loadYamlIntoClass(ANDROID_V2_DAILY_SUMMARIES_FILE, DeserializedDailySummariesConfig.class);

    assertThat(dailySummaries.getAttenuationBucketThresholdDb()).containsAll(List.of(30, 50, 70));
    assertThat(dailySummaries.getAttenuationBucketWeights()).containsAll(List.of(1.0, 1.0, 1.0, 1.0));
    assertThat(dailySummaries.getDaysSinceExposureThreshold()).isZero();
    assertThat(dailySummaries.getInfectiousnessWeights()).containsAllEntriesOf(Map.of(1, 1.0, 2, 1.0));
    assertThat(dailySummaries.getMinimumWindowScore()).isZero();
    assertThat(dailySummaries.getReportTypeWeights()).containsAllEntriesOf(Map.of(1, 1.0, 2, 1.0, 3, 1.0, 4, 1.0));


  }
}
