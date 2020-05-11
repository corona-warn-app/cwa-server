package app.coronawarn.server.services.distribution.exposureconfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ExposureConfigurationProviderTest {

  @Test
  public void okFile() throws UnableToLoadFileException {
    var provider = new ExposureConfigurationProvider();
    var result = provider.readFile("parameters/all_ok.yaml");

    assertNotNull(result, "File is null, indicating loading failed");
  }

  @Test
  public void wrongFile() {
    assertThrows(UnableToLoadFileException.class, () ->
        new ExposureConfigurationProvider().readFile("parameters/wrong_file.yaml"));
  }

  @Test
  public void brokenSyntax() {
    assertThrows(UnableToLoadFileException.class, () ->
        new ExposureConfigurationProvider().readFile("parameters/broken_syntax.yaml"));
  }

  @Test
  public void doesNotExist() {
    assertThrows(UnableToLoadFileException.class, () ->
        new ExposureConfigurationProvider().readFile("file_does_not_exist_anywhere.yaml"));
  }
}
