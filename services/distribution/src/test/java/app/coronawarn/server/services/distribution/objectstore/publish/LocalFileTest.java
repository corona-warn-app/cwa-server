

package app.coronawarn.server.services.distribution.objectstore.publish;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LocalFileTest {
  @ParameterizedTest
  @ValueSource(strings = { 
      "version", 
      "version/v1/configuration/country", 
      "version/v1/diagnosis-keys/country",
      "version/v1/diagnosis-keys/country/DE/date", 
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour" })
  void testGetContentTypeJson(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertEquals("application/json", test.getContentType());
  }

  @ParameterizedTest
  @ValueSource(strings = { 
      "version/v1/configuration/country/DE/app_config", 
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11", 
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/13" })
  void testGetContentTypeZip(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertEquals("application/zip", test.getContentType());
  }

  @ParameterizedTest
  @ValueSource(strings = { 
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/13" })
  void testIsKeyFile(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertTrue(test.isKeyFile());
  }

  @ParameterizedTest
  @ValueSource(strings = { 
      "version",
      "version/v1/configuration/country",
      "version/v1/configuration/country/DE/app_config", 
      "version/v1/diagnosis-keys/country",
      "version/v1/diagnosis-keys/country/DE/date", 
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour" })
  void testIsNotKeyFile(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertFalse(test.isKeyFile());
  }
}
