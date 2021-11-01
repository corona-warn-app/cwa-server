package app.coronawarn.server.services.federation.upload.integration;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import com.google.protobuf.ByteString;
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

  private Set<String> conflictKeys = new HashSet<>();
  private Set<String> errorKeys = new HashSet<>();
  private Set<String> validKeysSkipped = new HashSet<>();

  @BeforeEach
  public void setup() {
    // The test uses the testdata profile which loads 8000 keys, hence the
    // specific indexes below.
    when(mockClient.postBatchUpload(any())).thenAnswer(ans -> {
      UploadPayload payload = ans.getArgument(0);
      List<FederationUploadKey> originalKeys = payload.getOriginalKeys();
      conflictKeys.addAll(originalKeys.subList(0, 1000)
          .stream()
          .map(k -> getKeyDataString(k))
          .collect(Collectors.toList()));
      errorKeys.addAll(originalKeys.subList(1000, 2000)
          .stream()
          .map(k -> getKeyDataString(k))
          .collect(Collectors.toList()));
      // These values are skipped because the batch contains error Keys
      validKeysSkipped.addAll(originalKeys.subList(2000, 4000)
          .stream()
          .map(k -> getKeyDataString(k))
          .collect(Collectors.toList()));
      return Optional.of(new BatchUploadResponse(
          IntStream.range(0, 1000).boxed().map(String::valueOf).collect(Collectors.toList()),
          IntStream.range(1000, 2000).boxed().map(String::valueOf).collect(Collectors.toList()),
          IntStream.range(2000, 4000).boxed().map(String::valueOf).collect(Collectors.toList())));
    });
  }

  private String getKeyDataString(FederationUploadKey k) {
    return ByteString.copyFrom(k.getKeyData()).toStringUtf8();
  }

  /**
   * Test ensures that keys which are part not part of an error 500 list in a
   * Gateway multi-status response are correctly marked with the batch tag id.
   * 201 and 409 keys are updated with the batch tag, while the 500 ones are left empty,
   * which makes them candidates for a retry in a subsequent upload run.
   */
  @Test
  void keysShouldBeMarkedCorrectlyInCaseTheyAreRejected() throws Exception {
    // The test must remove any upload keys that were created when the container
    // starts and calls the ApplicationRunners. Unfortunately there is no easy way
    // to disable the Spring runners if we want to have a full app-context (@SpringBootTest)
    // created for the integration testing.
    ((EfgsUploadKeyRepository) uploadKeyRepository).deleteAll();
    testDataGeneration.run(null);
    uploadRunner.run(null);
    Iterable<FederationUploadKey> allUploadKeys = uploadKeyRepository.findAll();
    for (FederationUploadKey key : allUploadKeys) {
      String keyData = getKeyDataString(key);
      boolean wasConflictKey = conflictKeys.contains(keyData);
      if (wasConflictKey) {
        assertTrue("EFGS multi status conflict key was not marked with batch tag",
            key.getBatchTag() != null && !key.getBatchTag().isEmpty());
      }
      boolean wasErrorKey = errorKeys.contains(keyData);
      if (wasErrorKey) {
        assertTrue("EFGS multi status error key was marked incorrectly with batch tag",
            key.getBatchTag() == null || key.getBatchTag().isEmpty());
      }
      boolean wasOkKey = validKeysSkipped.contains(keyData);
      if (wasOkKey) {
        assertTrue("EFGS multi status created key was not marked with batch tag",
            key.getBatchTag() == null || key.getBatchTag().isEmpty());
      }
    }
  }
}
