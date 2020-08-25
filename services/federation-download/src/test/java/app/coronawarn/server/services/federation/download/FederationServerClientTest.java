package app.coronawarn.server.services.federation.download;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import app.coronawarn.server.services.federation.download.download.FederationGatewayClient;

@SpringBootTest
class FederationServerClientTest {

  @Autowired
  FederationGatewayClient federationGatewayClient;

  @Test
  @Disabled
  void testFederationClient() {
    final String diagnosisKeys = federationGatewayClient
        .getDiagnosisKeys("application/json; version=1.0", "abc", "C=DE", "2020-08-18");
    assertNotNull(diagnosisKeys);
    assertFalse(diagnosisKeys.isBlank());
  }
}
