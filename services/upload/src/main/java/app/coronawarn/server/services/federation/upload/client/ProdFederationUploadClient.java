
package app.coronawarn.server.services.federation.upload.client;

import static app.coronawarn.server.services.federation.upload.UploadLogMessages.RESPONSE_STATUS_FROM_EFGS;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-client")
public class ProdFederationUploadClient implements FederationUploadClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdFederationUploadClient.class);

  private final FederationGatewayClient federationGatewayClient;

  public ProdFederationUploadClient(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  @Override
  public Optional<BatchUploadResponse> postBatchUpload(UploadPayload uploadPayload) {
    var result = federationGatewayClient.postBatchUpload(
        uploadPayload.getBatch().toByteArray(),
        uploadPayload.getBatchTag(),
        uploadPayload.getBatchSignature());
    logger.info(RESPONSE_STATUS_FROM_EFGS, result.getStatusCode());
    return Optional.ofNullable(result.getBody());
  }
}
