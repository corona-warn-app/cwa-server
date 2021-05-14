package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import org.junit.jupiter.api.Test;

class DeserializedRevokedTraceLocationVersionsTest {

  @Test
  void testCreateRevokedTraceLocationVersionsObjectFromYaml() throws UnableToLoadFileException {
    final String REVOKED_TRACE_LOCATION_VERSIONS_FILE = "main-config/v2/revoked-trace-location-versions.yaml";

    DeserializedRevokedTraceLocationVersions deserializedRevokedTraceLocationVersions = YamlLoader
        .loadYamlIntoClass(REVOKED_TRACE_LOCATION_VERSIONS_FILE, DeserializedRevokedTraceLocationVersions.class);

    assertThat(deserializedRevokedTraceLocationVersions.getRevokedTraceLocationVersions()).isEmpty();

  }
}
