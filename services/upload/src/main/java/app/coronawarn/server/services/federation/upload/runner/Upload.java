package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyLoader;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(Upload.class);
  private final FederationUploadClient federationUploadClient;
  private final PayloadFactory payloadFactory;
  private final DiagnosisKeyLoader diagnosisKeyLoader;

  /**
   * Creates an upload runner instance that reads Upload keys and send them to the Federation Gateway.
   *
   * @param federationUploadClient {@link FederationUploadClient} instance to call the EFGS API.
   * @param payloadFactory         {@link PayloadFactory} to generate the Payload Objects with proper batching and
   *                               signing.
   * @param diagnosisKeyLoader     {@link DiagnosisKeyLoader} to load DiagnosisKeys from the Upload table.
   */
  public Upload(
      FederationUploadClient federationUploadClient,
      PayloadFactory payloadFactory,
      DiagnosisKeyLoader diagnosisKeyLoader) {
    this.federationUploadClient = federationUploadClient;
    this.payloadFactory = payloadFactory;
    this.diagnosisKeyLoader = diagnosisKeyLoader;
  }

  private void executeFederationUpload(UploadPayload payload) {
    logger.info("Executing batch request(s): {}", payload.getBatchTag());
    this.federationUploadClient.postBatchUpload(payload);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Running Upload Job");
    List<DiagnosisKey> diagnosisKeys = this.diagnosisKeyLoader.loadDiagnosisKeys();
    logger.info("Generating Upload Payload for {} keys", diagnosisKeys.size());
    List<UploadPayload> requests = this.payloadFactory.makePayloadList(diagnosisKeys);
    logger.info("Executing {} batch request", requests.size());
    requests.forEach(this::executeFederationUpload);
  }
}
