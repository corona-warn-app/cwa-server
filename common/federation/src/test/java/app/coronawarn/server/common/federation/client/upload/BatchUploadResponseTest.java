
package app.coronawarn.server.common.federation.client.upload;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class BatchUploadResponseTest {

  @Test
  void checkEmptyBatchUploadResponse() {
    BatchUploadResponse batchUploadResponse = new BatchUploadResponse();

    assertThat(batchUploadResponse.getStatus201()).isEmpty();
    assertThat(batchUploadResponse.getStatus409()).isEmpty();
    assertThat(batchUploadResponse.getStatus500()).isEmpty();
  }
}
