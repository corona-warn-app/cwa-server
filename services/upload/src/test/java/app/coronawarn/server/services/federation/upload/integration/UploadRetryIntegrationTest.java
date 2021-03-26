package app.coronawarn.server.services.federation.upload.integration;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.repository.EfgsUploadKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.services.federation.upload.client.TestFederationUploadClient;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import app.coronawarn.server.services.federation.upload.runner.TestDataGeneration;
import app.coronawarn.server.services.federation.upload.runner.Upload;

@EnableConfigurationProperties(value = UploadServiceConfig.class)
@ActiveProfiles({"testdata", "connect-efgs", "fake-client"})
@DirtiesContext
@SpringBootTest
public class UploadRetryIntegrationTest {

  @MockBean
  TestFederationUploadClient mockClient;

  @Autowired
  FederationUploadKeyRepository uploadKeyRepository;

  @Autowired
  TestDataGeneration testDataGeneration;

  @Autowired
  Upload uploadRunner;

  private List<FederationUploadKey> conflictKeys = new ArrayList<FederationUploadKey>();
  private List<FederationUploadKey> errorKeys = new ArrayList<FederationUploadKey>();

  @BeforeEach
  public void setup() {
    when(mockClient.postBatchUpload(any())).thenAnswer(ans -> {
      UploadPayload payload = ans.getArgument(0);
      List<FederationUploadKey> originalKeys = payload.getOriginalKeys();
      conflictKeys.addAll(originalKeys.subList(0, 1000));
      errorKeys.addAll(originalKeys.subList(1000, 2000));
      return Optional.of(new BatchUploadResponse(
          IntStream.range(0, 10).boxed().map(String::valueOf).collect(Collectors.toList()),
          IntStream.range(10, 20).boxed().map(String::valueOf).collect(Collectors.toList()),
          Collections.emptyList()));
    });
  }

  @Test
  void keysShouldBeMarkedCorrectlyInCaseTheyAreRejected() throws Exception {
    ((EfgsUploadKeyRepository) uploadKeyRepository).deleteAll();
    testDataGeneration.run(null);
    uploadRunner.run(null);
    Iterable<FederationUploadKey> allUploadKeys = uploadKeyRepository.findAll();
    for (FederationUploadKey key : allUploadKeys) {
      boolean wasConflictKey = conflictKeys.stream()
          .filter(k -> Arrays.areEqual(key.getKeyData(), k.getKeyData())).findFirst().isPresent();
      if (wasConflictKey) {
        assertTrue("EFGS multi status conflict key was not marked with batch tag",
            key.getBatchTag() != null && !key.getBatchTag().isEmpty());
      }
      boolean wasErrorKey = errorKeys.stream()
          .filter(k -> Arrays.areEqual(key.getKeyData(), k.getKeyData())).findFirst().isPresent();
      // if (wasErrorKey) {
      // assertTrue("EFGS multi status error key was marked incorrectly with batch tag",
      // key.getBatchTag().isEmpty());
      // }
    }
  }
}
