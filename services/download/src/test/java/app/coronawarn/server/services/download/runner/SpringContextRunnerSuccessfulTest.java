package app.coronawarn.server.services.download.runner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.FederationBatchUtils;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
public class SpringContextRunnerSuccessfulTest {

  @Autowired
  private FederationGatewayClient federationGatewayClient;

  @MockBean
  private FederationBatchInfoService federationBatchInfoService;

  @Nested
  @TestConfiguration
  public static class TestConfig {

    @Primary
    @Bean
    public FederationGatewayClient mockFederationGatewayClient() {
      FederationGatewayClient federationGatewayClient = mock(FederationGatewayClient.class);
      BatchDownloadResponse serverResponse = FederationBatchUtils.createBatchDownloadResponse("abc", Optional.empty());
      when(federationGatewayClient.getDiagnosisKeys(anyString())).thenReturn(serverResponse);
      return federationGatewayClient;
    }
  }

  @Test
  @DirtiesContext
  void testDownloadRunSuccessfully() {
    verify(federationGatewayClient, times(1)).getDiagnosisKeys(anyString());
    verify(federationGatewayClient, never()).getDiagnosisKeys(anyString(), anyString());
    verify(federationBatchInfoService, times(1)).applyRetentionPolicy(anyInt());
  }
}
