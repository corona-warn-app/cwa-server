
package app.coronawarn.server.services.federation.upload.runner;

import static app.coronawarn.server.services.federation.upload.UploadLogMessages.ALL_KEYS_PROCESSED_SUCCESSFULLY;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.EXECUTING_BATCH_REQUEST;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.EXECUTING_BATCH_REQUESTS;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.GENERATING_UPLOAD_PAYLOAD;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.KEYS_NOT_PROCESSED_CORRECTLY;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.MARKING_OF_DIAGNOSIS_KEYS_WITH_BATCH_TAG_ID_FAILED;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.NR_KEYS_CONFLICT;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.NR_KEYS_RETRY;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.NR_KEYS_SUCCESSFUL;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.RUNNING_UPLOAD_JOB;
import static app.coronawarn.server.services.federation.upload.UploadLogMessages.UPLOAD_DIAGNOSIS_KEY_DATA_FAILED;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.services.federation.upload.Application;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyLoader;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Upload.class);

  private final FederationUploadClient federationUploadClient;
  private final PayloadFactory payloadFactory;
  private final DiagnosisKeyLoader diagnosisKeyLoader;
  private final ApplicationContext applicationContext;
  private final FederationUploadKeyService uploadKeyService;

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
      DiagnosisKeyLoader diagnosisKeyLoader,
      ApplicationContext applicationContext,
      FederationUploadKeyService uploadKeyService) {
    this.federationUploadClient = federationUploadClient;
    this.payloadFactory = payloadFactory;
    this.diagnosisKeyLoader = diagnosisKeyLoader;
    this.applicationContext = applicationContext;
    this.uploadKeyService = uploadKeyService;
  }

  private List<FederationUploadKey> getRetryKeysFromResponseBody(BatchUploadResponse body, UploadPayload payload) {
    return body.getStatus500()
        .stream()
        .map(Integer::parseInt)
        .map(index -> payload.getOriginalKeys().stream()
            .sorted(Comparator.comparing(diagnosisKey ->
                ByteString.copyFrom(diagnosisKey.getKeyData()).toStringUtf8()))
            .collect(Collectors.toList()).get(index))
        .collect(Collectors.toList());
  }

  private List<FederationUploadKey> executeUploadAndCollectErrors(UploadPayload payload) {
    logger.info(EXECUTING_BATCH_REQUESTS, payload.getBatchTag());
    var result = this.federationUploadClient.postBatchUpload(payload);
    List<FederationUploadKey> retryKeys = Collections.emptyList();
    if (result.isPresent()) {
      var body = result.get();
      retryKeys = this.getRetryKeysFromResponseBody(body, payload);
      logger.info(KEYS_NOT_PROCESSED_CORRECTLY);
      logger.info(NR_KEYS_SUCCESSFUL, body.getStatus201().size());
      logger.info(NR_KEYS_CONFLICT, body.getStatus409().size());
      logger.info(NR_KEYS_RETRY, body.getStatus500().size());
    } else {
      logger.info(ALL_KEYS_PROCESSED_SUCCESSFULLY);
    }

    return retryKeys;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info(RUNNING_UPLOAD_JOB);
    try {
      List<FederationUploadKey> diagnosisKeys = this.diagnosisKeyLoader.loadDiagnosisKeys();
      logger.info(GENERATING_UPLOAD_PAYLOAD, diagnosisKeys.size());
      List<UploadPayload> requests = this.payloadFactory.makePayloadList(diagnosisKeys);
      logger.info(EXECUTING_BATCH_REQUEST, requests.size());
      requests.forEach(payload -> {
        List<FederationUploadKey> retryKeys = this.executeUploadAndCollectErrors(payload);
        this.markSuccessfullyUploadedKeys(payload, retryKeys);
      });
    } catch (Exception e) {
      logger.error(UPLOAD_DIAGNOSIS_KEY_DATA_FAILED, e);
      Application.killApplication(applicationContext);
    }
  }

  private void markSuccessfullyUploadedKeys(UploadPayload payload, List<FederationUploadKey> retryKeys) {
    try {
      if (!retryKeys.isEmpty()) {
        payload.getOriginalKeys().removeIf(
            originalKey ->
                retryKeys.stream().anyMatch(retryKey ->
                    Arrays.equals(retryKey.getKeyData(), originalKey.getKeyData())));
      }
      uploadKeyService.updateBatchTagForKeys(payload.getOriginalKeys(), payload.getBatchTag());
    } catch (Exception ex) {
      // in case of an error with marking, try to move forward to the next upload batch if any unprocessed
      logger.error(MARKING_OF_DIAGNOSIS_KEYS_WITH_BATCH_TAG_ID_FAILED, ex);
    }
  }
}
