package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeserializedDiagnosisKeysDataMappingTest {

  @Test
  void testCreateDeserializedDiagnosisKeysDataMappingObjectFromYaml() throws UnableToLoadFileException {
    final String ANDROID_V2_DATA_MAPPING_FILE = "master-config/v2/diagnosis-keys-data-mapping.yaml";

    DeserializedDiagnosisKeysDataMapping dataMapping = YamlLoader.loadYamlIntoClass(
        ANDROID_V2_DATA_MAPPING_FILE, DeserializedDiagnosisKeysDataMapping.class);

    assertThat(dataMapping.getDaysSinceOnsetToInfectiousness()).containsAllEntriesOf(Map.of(1,1,2,2));
    assertThat(dataMapping.getInfectiousnessWhenDaysSinceOnsetMissing()).isEqualTo(0);
    assertThat(dataMapping.getReportTypeWhenMissing()).isEqualTo(0);
  }
}
