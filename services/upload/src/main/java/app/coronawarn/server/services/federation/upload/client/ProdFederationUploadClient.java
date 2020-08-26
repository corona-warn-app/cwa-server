package app.coronawarn.server.services.federation.upload.client;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-client")
public class ProdFederationUploadClient implements FederationUploadClient {

  private final FederationGatewayClient federationGatewayClient;

  public ProdFederationUploadClient(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  @Override
  public void postBatchUpload(UploadPayload uploadPayload) {
    federationGatewayClient.postBatchUpload(
        uploadPayload.getBatch().toByteArray(),
        uploadPayload.getBatchTag(),
        uploadPayload.getBatchSignature());
  }
}
