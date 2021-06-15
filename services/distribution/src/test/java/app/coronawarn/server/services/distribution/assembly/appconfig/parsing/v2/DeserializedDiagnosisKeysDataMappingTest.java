package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeserializedDiagnosisKeysDataMappingTest {

  @Test
  void testCreateDeserializedDiagnosisKeysDataMappingObjectFromYaml() throws UnableToLoadFileException {
    final String ANDROID_V2_DATA_MAPPING_FILE = "main-config/v2/diagnosis-keys-data-mapping.yaml";

    DeserializedDiagnosisKeysDataMapping dataMapping = YamlLoader.loadYamlIntoClass(
        ANDROID_V2_DATA_MAPPING_FILE, DeserializedDiagnosisKeysDataMapping.class);

    assertThat(dataMapping.getDaysSinceOnsetToInfectiousness()).containsAllEntriesOf(Map.of(1,1,2,2));
    assertThat(dataMapping.getInfectiousnessWhenDaysSinceOnsetMissing()).isZero();
    assertThat(dataMapping.getReportTypeWhenMissing()).isEqualTo(1);
  }
}
