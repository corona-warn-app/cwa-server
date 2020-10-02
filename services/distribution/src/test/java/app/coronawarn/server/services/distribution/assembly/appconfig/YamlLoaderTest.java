

package app.coronawarn.server.services.distribution.assembly.appconfig;

import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
}
