package app.coronawarn.server.services.distribution.assembly.exposureconfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import org.junit.jupiter.api.Test;

public class ExposureConfigurationStructureProviderProviderTest {

  @Test
  public void okFile() throws UnableToLoadFileException {
    RiskScoreParameters result =
        ExposureConfigurationProvider.readFile("parameters/all_ok.yaml");

    assertNotNull(result, "File is null, indicating loading failed");
  }

  @Test
  public void wrongFile() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("parameters/wrong_file.yaml"));
  }

  @Test
  public void brokenSyntax() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("parameters/broken_syntax.yaml"));
  }

  @Test
  public void doesNotExist() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("file_does_not_exist_anywhere.yaml"));
  }
}
