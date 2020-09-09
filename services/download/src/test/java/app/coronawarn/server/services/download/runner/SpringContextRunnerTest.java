package app.coronawarn.server.services.download.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.FederationBatchUtils;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
public class SpringContextRunnerTest {

  @MockBean
  private FederationGatewayClient federationGatewayClient;

  @MockBean
  private FederationBatchInfoService federationBatchInfoService;

  @Test
  @DirtiesContext
  void testDownloadRunSuccessfully() {
    BatchDownloadResponse serverResponse = FederationBatchUtils.createBatchDownloadResponse("abc", Optional.empty());
    when(federationGatewayClient.getDiagnosisKeys(anyString())).thenReturn(serverResponse);

    verify(federationGatewayClient, times(1)).getDiagnosisKeys(anyString());
    verify(federationGatewayClient, never()).getDiagnosisKeys(anyString(), anyString());
  }

  @Test
  @DirtiesContext
  void testDownloadFailsDueToExceptionInBatchInfoService() {
    BatchDownloadResponse serverResponse = FederationBatchUtils.createBatchDownloadResponse("abc", Optional.empty());
    when(federationGatewayClient.getDiagnosisKeys(anyString())).thenReturn(serverResponse);
    doThrow(RuntimeException.class).when(federationBatchInfoService).save(any());

    verify(federationGatewayClient, times(1)).getDiagnosisKeys(anyString());
  }

  @Test
  @DirtiesContext
  void testRetentionRunSuccessfully() {
    verify(federationBatchInfoService, times(1)).applyRetentionPolicy(anyInt());
  }

  @Test
  @DirtiesContext
  void testRetentionFailsDueToExceptionInBatchInfoService() {
    doThrow(RuntimeException.class).when(federationBatchInfoService).applyRetentionPolicy(anyInt());

    verify(federationBatchInfoService, times(1)).applyRetentionPolicy(anyInt());
  }
}
