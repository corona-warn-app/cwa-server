

package app.coronawarn.server.services.federation.upload.client;

import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("fake-client")
public class TestFederationUploadClient implements FederationUploadClient {

  private static final Logger logger = LoggerFactory
      .getLogger(TestFederationUploadClient.class);

  @Override
  public Optional<BatchUploadResponse> postBatchUpload(UploadPayload uploadPayload) {
    logger.info("Calling fake batch upload with: \n\tkeys:{}\n\tbatchTag:{}\n\tbatchSignature:{}",
        uploadPayload.getBatch().getKeysCount(),
        uploadPayload.getBatchTag(),
        uploadPayload.getBatchSignature());
    return Optional.of(new BatchUploadResponse(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList()
    ));
  }
}
