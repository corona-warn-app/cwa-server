

package app.coronawarn.server.services.federation.upload.runner;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Upload.class);
  private static final String CH = "CH";
  private static final String EU = "EU";

  private final FederationUploadClient federationUploadClient;
  private final PayloadFactory payloadFactory;
  private final DiagnosisKeyLoader diagnosisKeyLoader;
  private final ApplicationContext applicationContext;
  private final FederationUploadKeyService uploadKeyService;
  private final Environment env;

  /**
   * Creates an upload runner instance that reads Upload keys and send them to the Federation Gateway.
   *
   * @param federationUploadClient {@link FederationUploadClient} instance to call the EFGS API.
   * @param payloadFactory         {@link PayloadFactory} to generate the Payload Objects with proper batching and
   *                               signing.
   * @param diagnosisKeyLoader     {@link DiagnosisKeyLoader} to load DiagnosisKeys from the Upload table.
   * @param applicationContext     {@link ApplicationContext} app context.
   * @param uploadKeyService       {@link FederationUploadKeyService} upload service.
   * @param env                    Spring Application Context
   */
  public Upload(
      FederationUploadClient federationUploadClient,
      PayloadFactory payloadFactory,
      DiagnosisKeyLoader diagnosisKeyLoader,
      ApplicationContext applicationContext,
      FederationUploadKeyService uploadKeyService, Environment env) {
    this.federationUploadClient = federationUploadClient;
    this.payloadFactory = payloadFactory;
    this.diagnosisKeyLoader = diagnosisKeyLoader;
    this.applicationContext = applicationContext;
    this.uploadKeyService = uploadKeyService;
    this.env = env;
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
    logger.info("Executing batch request(s): {}", payload.getBatchTag());
    var result = this.federationUploadClient.postBatchUpload(payload);
    List<FederationUploadKey> retryKeys = Collections.emptyList();
    if (result.isPresent()) {
      var body = result.get();
      retryKeys = this.getRetryKeysFromResponseBody(body, payload);
      logger.info("Some keys were not processed correctly");
      logger.info("{} keys marked with status 201 (Successful)", body.getStatus201().size());
      logger.info("{} keys marked with status 409 (Conflict)", body.getStatus409().size());
      logger.info("{} keys marked with status 500 (Retry)", body.getStatus500().size());
    }  else {
      logger.info("All keys processed successfully");
    }

    return retryKeys;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Running Upload Job");
    try {
      List<FederationUploadKey> diagnosisKeys = this.diagnosisKeyLoader.loadDiagnosisKeys();
      logger.info("Generating Upload Payload for {} keys for {} system.", diagnosisKeys.size(), getSystem());
      List<UploadPayload> requests = this.payloadFactory.makePayloadList(diagnosisKeys);
      logger.info("Executing {} batch request", requests.size());
      requests.forEach(payload -> {
        List<FederationUploadKey> retryKeys = this.executeUploadAndCollectErrors(payload);
        this.markSuccessfullyUploadedKeys(payload, retryKeys);
      });
    } catch (Exception e) {
      logger.error("Upload diagnosis key data failed.", e);
      Application.killApplication(applicationContext);
    }
  }

  private String getSystem() {
    if (Arrays.asList(env.getActiveProfiles()).contains("connect-chgs")) {
      return CH;
    } else {
      return EU;
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
      logger.error("Post-upload marking of diagnosis keys with batch tag id failed", ex);
    }
  }
}
