package app.coronawarn.server.services.distribution.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.services.distribution.parameters.validation.ExposureConfigurationValidator;
import app.coronawarn.server.services.distribution.parameters.validation.ValidationResult;
import org.junit.jupiter.api.Test;

/**
 * This test will verify that the provided Exposure Configuration master file is syntactically
 * correct and according to spec.
 *
 * There should never be any deployment when this test is failing.
 */
public class ExposureConfigurationMasterFileTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Test
  public void testMasterFile() throws UnableToLoadFileException {
    var config = new ExposureConfigurationProvider().readMasterFile();

    var validator = new ExposureConfigurationValidator(config);
    var result =  validator.validate();

    assertEquals(SUCCESS, result);
  }
}
