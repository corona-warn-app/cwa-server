package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ApplicationConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import org.junit.jupiter.api.Test;

class ApplicationConfigurationMasterFileTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Test
  void testMasterFile() throws UnableToLoadFileException {
    var config = ApplicationConfigurationProvider.readMasterFile();

    var validator = new ApplicationConfigurationValidator(config);
    ValidationResult result = validator.validate();

    assertThat(result).isEqualTo(SUCCESS);
  }
}
