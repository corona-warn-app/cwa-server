package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeserializedPlausibleDeniabilityParametersTest {

  @Test
  void testCreateDeserializedPlausibleDeniabilityParametersObjectFromYaml() throws UnableToLoadFileException {
    final String PLAUSIBLE_DENIABILITY_PARAMETERS_FILE = "main-config/v2/plausible-deniability-parameters.yaml";

    DeserializedPlausibleDeniabilityParameters plausibleDeniabilityParameters = YamlLoader
        .loadYamlIntoClass(PLAUSIBLE_DENIABILITY_PARAMETERS_FILE, DeserializedPlausibleDeniabilityParameters.class);

    assertThat(plausibleDeniabilityParameters.getCheckInSizesInBytes()).containsAll(List.of(46));
  }
}
