package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.v2.CoronaTestParameters;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import org.junit.jupiter.api.Test;

public class CoronaTestParametersTest {

  private static final String CORONA_TEST_PARAMETERS_FILE = "configtests/corona-test-parameters.yaml";


  @Test
  void testLoadingOfCoronaTestParametersFile() throws UnableToLoadFileException {
    CoronaTestParameters.Builder coronaTestParameters =
        YamlLoader.loadYamlIntoProtobufBuilder(CORONA_TEST_PARAMETERS_FILE,
            CoronaTestParameters.Builder.class);

    assertThat(coronaTestParameters.getCoronaRapidAntigenTestParameters()).isNotNull();
    assertThat(coronaTestParameters.getCoronaRapidAntigenTestParameters().getHoursToDeemTestOutdated()).isEqualTo(48);
  }
}
