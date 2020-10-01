

package app.coronawarn.server.services.distribution.objectstore.publish;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class S3ObjectTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/13" })
  void testIsKeyFile(String key) {
    S3Object test = new S3Object(key);
    assertTrue(test.isDiagnosisKeyFile());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/configuration/country/DE/app_config",
      "version/v1/configuration/country",
      "version/v1/diagnosis-keys/country/DE/date",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour" })
  void testIsNotKeyFile(String key) {
    S3Object test = new S3Object(key);
    assertFalse(test.isDiagnosisKeyFile());
  }
}
