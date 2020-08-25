package app.coronawarn.server.common.federation.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FederationServerClientTest {

  @Autowired
  FederationServerClient federationServerClient;

  @Test
  void testApplicationLoads() {
  }

  @Test
  void testFederationClient() {
    final String diagnosisKeys = federationServerClient
        .getDiagnosisKeys("application/json; version=1.0", "abcd", "C=PL", "2020-08-18");
    assertNotNull(diagnosisKeys);
    assertFalse(diagnosisKeys.isBlank());
  }
}
