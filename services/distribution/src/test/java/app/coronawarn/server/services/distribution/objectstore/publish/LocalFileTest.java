

package app.coronawarn.server.services.distribution.objectstore.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LocalFileTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "version",
      "version/v1/configuration/country",
      "version/v1/diagnosis-keys/country",
      "version/v1/diagnosis-keys/country/DE/date",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour"})
  void testGetContentTypeJson(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertEquals("application/json", test.getContentType());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/stats",
      "version/v1/app_config_ios",
      "version/v1/app_config_android",
      "version/v1/configuration/country/DE/app_config",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/13",
      "version/v1/qr_code_poster_template_android",
      "version/v1/qr_code_poster_template_ios"})
  void testGetContentTypeZip(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertEquals("application/zip", test.getContentType());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/qr_code_poster_template_android",
      "version/v1/qr_code_poster_template_android.checksum",
      "version/v1/qr_code_poster_template_ios",
      "version/v1/qr_code_poster_template_ios.checksum"})
  void testGetContentTypeForQRCodeTemplate(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    assertEquals("application/zip", test.getContentType());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/13"})
  void testIsKeyFile(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    Assertions.assertTrue(test.isKeyFile());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version",
      "version/v1/configuration/country",
      "version/v1/configuration/country/DE/app_config",
      "version/v1/diagnosis-keys/country",
      "version/v1/diagnosis-keys/country/DE/date",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour"})
  void testIsNotKeyFile(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    Assertions.assertFalse(test.isKeyFile());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/qr_code_poster_template_android",
      "version/v1/qr_code_poster_template_android.checksum",
      "version/v1/qr_code_poster_template_ios",
      "version/v1/qr_code_poster_template_ios.checksum"
  })
  void testIsQrPosterTemplate(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    Assertions.assertTrue(test.isQrPosterTemplate());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version",
      "version/v1",
      "version/v1/app_config_ios",
      "version/v1/app_config_android"
  })
  void testIsNotQrPosterTemplate(String path) {
    LocalFile test = new LocalIndexFile(Path.of("/root", path, "/index"), Path.of("/root"));
    Assertions.assertFalse(test.isQrPosterTemplate());
  }
}
