package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.v2.PresenceTracingParameters;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import org.junit.jupiter.api.Test;

public class PresenceTracingParametersValidationTest {

  private static final String PRESENCE_TRACING_PARAMETERS_FILE = "configtests/presence-tracing-parameters.yaml";

  @Test
  void testIfTheYamlFileWasLoadedInTheObject() throws UnableToLoadFileException {
    PresenceTracingParameters.Builder presenceTracingParameters =
        YamlLoader.loadYamlIntoProtobufBuilder(PRESENCE_TRACING_PARAMETERS_FILE,
        PresenceTracingParameters.Builder.class);

    assertThat(presenceTracingParameters.getSubmissionParameters()).isNotNull();
    assertThat(presenceTracingParameters.getSubmissionParameters().getDurationFiltersCount()).isOne();
    assertThat(presenceTracingParameters.getSubmissionParameters().getAerosoleDecayLinearFunctionsCount()).isEqualTo(2);
    assertThat(presenceTracingParameters.getQrCodeErrorCorrectionLevelValue()).isZero();
    assertThat(presenceTracingParameters.getRiskCalculationParameters()).isNotNull();
    assertThat(presenceTracingParameters.getRiskCalculationParameters()
        .getTransmissionRiskValueMappingCount()).isEqualTo(8);
    assertThat(presenceTracingParameters.getRiskCalculationParameters()
        .getNormalizedTimePerCheckInToRiskLevelMappingCount()).isEqualTo(2);
    assertThat(presenceTracingParameters.getRiskCalculationParameters()
        .getNormalizedTimePerDayToRiskLevelMappingCount()).isEqualTo(2);
    assertThat(presenceTracingParameters.getRevokedTraceLocationVersionsList()).isEmpty();
    assertThat(presenceTracingParameters.getPlausibleDeniabilityParameters()).isNotNull();
    assertThat(presenceTracingParameters.getPlausibleDeniabilityParameters()
        .getCheckInSizesInBytesCount()).isEqualTo(0);
    assertThat(presenceTracingParameters.getPlausibleDeniabilityParameters()
        .getProbabilityToFakeCheckInsIfNoCheckIns()).isZero();
    assertThat(presenceTracingParameters.getPlausibleDeniabilityParameters()
        .getProbabilityToFakeCheckInsIfSomeCheckIns()).isEqualTo(0);
    assertThat(presenceTracingParameters.getPlausibleDeniabilityParameters()
        .getNumberOfFakeCheckInsFunctionParametersCount()).isOne();
    assertThat(presenceTracingParameters.getQrCodeDescriptorsCount()).isEqualTo(1);
  }
}
