package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.FederationBatchUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SpringContextRunnerFailureTest {

  @MockBean
  private FederationGatewayClient federationGatewayClient;

  @Autowired
  private FederationBatchInfoService federationBatchInfoService;

  @Nested
  @TestConfiguration
  public static class TestConfig {

    @Primary
    @Bean
    public FederationBatchInfoService mockFederationBatchInfoService() {
      FederationBatchInfoService federationBatchInfoService = mock(FederationBatchInfoService.class);
      doThrow(RuntimeException.class).when(federationBatchInfoService).applyRetentionPolicy(anyInt());
      return federationBatchInfoService;
    }
  }

  @Test
  @DirtiesContext
  void testDownloadRunSuccessfully() {
    verify(federationGatewayClient, never()).getDiagnosisKeys(anyString());
    verify(federationGatewayClient, never()).getDiagnosisKeys(anyString(), anyString());
    verify(federationBatchInfoService, never()).applyRetentionPolicy(anyInt());
  }
}
