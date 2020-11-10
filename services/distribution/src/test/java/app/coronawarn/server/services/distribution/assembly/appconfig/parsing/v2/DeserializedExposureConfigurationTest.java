package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeserializedExposureConfigurationTest {

  @Test
  void testCreateDeserializedExposureConfigurationObjectFromYaml() throws UnableToLoadFileException {

    final String IOS_V2_EXPOSURE_CONFIGURATION_FILE = "master-config/v2/exposure-configuration.yaml";

    DeserializedExposureConfiguration exposureConfiguration = YamlLoader.loadYamlIntoClass(
        IOS_V2_EXPOSURE_CONFIGURATION_FILE, DeserializedExposureConfiguration.class);

    assertThat(exposureConfiguration.getAttenuationDurationThresholds()).containsAll(List.of(30,50,70));
    assertThat(exposureConfiguration.getInfectiousnessForDaysSinceOnsetOfSymptoms()).containsAllEntriesOf(
        Map.of(1, 1, 2, 2));
    assertThat(exposureConfiguration.getReportTypeNoneMap()).isZero();
    assertThat(exposureConfiguration.getImmediateDurationWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getMediumDurationWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getNearDurationWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getOtherDurationWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getDaysSinceLastExposureThreshold()).isZero();
    assertThat(exposureConfiguration.getInfectiousnessStandardWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getInfectiousnessHighWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getReportTypeConfirmedTestWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getReportTypeConfirmedClinicalDiagnosisWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getReportTypeSelfReportedWeight()).isEqualTo(1.0);
    assertThat(exposureConfiguration.getReportTypeRecursiveWeight()).isEqualTo(1.0);

  }

}
