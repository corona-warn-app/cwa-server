package app.coronawarn.server.services.federation.upload.client;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-client")
public class ProdFederationUploadClient implements FederationUploadClient {

  private static final Logger logger = LoggerFactory
      .getLogger(ProdFederationUploadClient.class);

  private final FederationGatewayClient federationGatewayClient;

  public ProdFederationUploadClient(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  @Override
  public void postBatchUpload(UploadPayload uploadPayload) {
    var result = federationGatewayClient.postBatchUpload(
        uploadPayload.getBatch().toByteArray(),
        "application/json; version=1.0",
        "abc",
        "C=DE",
        uploadPayload.getBatchTag(),
        uploadPayload.getBatchSignature());
    logger.info("Response from EFGS: {}", result);
  }
}
