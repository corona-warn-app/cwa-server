package app.coronawarn.server.common.persistence.repository;


import static org.assertj.core.api.Assertions.assertThat;


import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith({SpringExtension.class})
@AutoConfigureWebTestClient
public class FederationBatchRepositoryTest {

  @Autowired
  private FederationBatchRepository federationBatchRepository;

  private static final String batchTag1 = "11111";
  private static final String batchTag2 = "22222";
  private static final String batchTag3 = "33333";
  private static final String batchTag4 = "44444";

  private static final LocalDate date1 = LocalDate.parse("2020-08-15");
  private static final LocalDate date2 = LocalDate.parse("2020-08-16");
  private static final LocalDate date3 = LocalDate.parse("2020-08-17");
  private static final LocalDate date4 = LocalDate.parse("2020-08-18");

  private static final FederationBatchStatus statusError = FederationBatchStatus.ERROR;
  private static final FederationBatchStatus statusProcessed = FederationBatchStatus.PROCESSED;
  private static final FederationBatchStatus statusNull = null;

  private static Stream<Arguments> getUnprocessedBatchArgumentsSortedByDateDescending() {
    return Stream.of(
        Arguments.of(batchTag4, date4, statusError),
        Arguments.of(batchTag3, date3, statusNull),
        Arguments.of(batchTag2, date2, statusError),
        Arguments.of(batchTag1, date1, statusError)
    );
  }

  @AfterEach
  void tearDown() {
    federationBatchRepository.deleteAll();
  }

  @Test
  void testReturnsNullIfNoUnprocessedBatch() {
    federationBatchRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchRepository.findOldestUnprocessedFederationBatch()).isNull();
  }

  @ParameterizedTest
  @MethodSource("getUnprocessedBatchArgumentsSortedByDateDescending")
  void testOnlyOldestBatchIsReturned(String batchTag, LocalDate date, FederationBatchStatus status) {
    federationBatchRepository.saveDoNothingOnConflict(batchTag, date, status);
    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(batchTag, date, status)).isTrue();
  }

  @Test
  void testProcessedBatchDoesNotOverwriteUnprocessedBatch() {
    federationBatchRepository.saveDoNothingOnConflict(batchTag2, date2, statusError);
    federationBatchRepository.saveDoNothingOnConflict(batchTag3, date3, statusError);
    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(batchTag2, date2, statusError)).isTrue();
    federationBatchRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(batchTag2, date2, statusError)).isTrue();
  }

  @Test
  void testDoesNothingOnConflict() {
    federationBatchRepository.saveDoNothingOnConflict(batchTag2, date2, statusError);
    federationBatchRepository.saveDoNothingOnConflict(batchTag2, date1, statusError);

    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(batchTag2, date2, statusError)).isTrue();
  }

  private boolean validateBatchPropertiesOfOldestUnprocessedBatch(String batchTag, LocalDate date,
      FederationBatchStatus status) {
    FederationBatch federationBatch = federationBatchRepository.findOldestUnprocessedFederationBatch();
    return Objects.equals(federationBatch.getBatchTag(), batchTag)
        && Objects.equals(federationBatch.getDate(), date)
        && Objects.equals(federationBatch.getStatus(), status);
  }
}
