package app.coronawarn.server.services.distribution.assembly.appconfig;

import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

import app.coronawarn.server.common.protocols.internal.v2.CoronaTestParameters;
import app.coronawarn.server.common.protocols.internal.v2.RiskCalculationParameters;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedDailySummariesConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedDiagnosisKeysDataMapping;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedExposureConfiguration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class YamlLoaderTest {

  @Test
  void okFile() throws UnableToLoadFileException {
    var result = loadApplicationConfiguration("configtests/app-config_ok.yaml");
    assertThat(result).withFailMessage("File is null, indicating loading failed").isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "configtests/app-config_empty.yaml",
      "configtests/wrong_file.yaml",
      "configtests/app-config_broken_syntax.yaml",
      "configtests/naming_mismatch.yaml",
      "configtests/file_does_not_exist_anywhere.yaml"
  })
  void throwsLoadFailure(String fileName) {
    assertThatExceptionOfType(UnableToLoadFileException.class).isThrownBy(() -> loadApplicationConfiguration(fileName));
  }

  @ParameterizedTest
  @MethodSource("getYamlDeserializationExpectations")
  <T> void successWithDeserializingIntoClass(String yamlPath, Class<T> expectedClassType) throws UnableToLoadFileException {
    var result = YamlLoader.loadYamlIntoClass(yamlPath, expectedClassType);
    assertThat(result).isNotNull();
  }

  @Test
  void failWhenYamlNotAlligned() {
    assertThatExceptionOfType(UnableToLoadFileException.class).isThrownBy(() -> {
      YamlLoader.loadYamlIntoClass("configtests/daily-summaries-config-fail.yaml", DeserializedDailySummariesConfig.class);
    });
    assertThatExceptionOfType(UnableToLoadFileException.class).isThrownBy(() -> {
      YamlLoader.loadYamlIntoClass("configtests/not-existing.yaml", DeserializedDailySummariesConfig.class);
    });
  }

  private static Stream<Arguments> getYamlDeserializationExpectations() {
    return Stream.of(
        Arguments.of("configtests/daily-summaries-config-ok.yaml", DeserializedDailySummariesConfig.class),
        Arguments.of("configtests/exposure-configuration-v2-ok.yaml", DeserializedExposureConfiguration.class),
        Arguments.of("configtests/diagnosis-keys-data-mapping-ok.yaml", DeserializedDiagnosisKeysDataMapping.class)
    );
  }

  @Test
  void testDefaultYamlValues42() throws Exception {
    RiskCalculationParameters.Builder riskCalculationParameterBuilder = YamlLoader.loadYamlIntoProtobufBuilder(
        ApplicationConfigurationV2PublicationConfig.V1_RISK_PARAMETERS_FILE,
        RiskCalculationParameters.Builder.class);
    assertEquals(42, riskCalculationParameterBuilder.getMaxEncounterAgeInDays());

    CoronaTestParameters.Builder coronaTestParameters = YamlLoader.loadYamlIntoProtobufBuilder(
        ApplicationConfigurationV2PublicationConfig.CORONA_TEST_PARAMETERS_FILE,
        CoronaTestParameters.Builder.class);
    assertEquals(42,
        coronaTestParameters.getCoronaPCRTestParametersBuilder().getHoursSinceTestRegistrationToShowRiskCard());
    assertEquals(42, coronaTestParameters.getCoronaRapidAntigenTestParametersBuilder()
        .getHoursSinceSampleCollectionToShowRiskCard());
  }
}
