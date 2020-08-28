package app.coronawarn.server.common.persistence.repository;


import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
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
  void testReturnsEmptyListIfNoUnprocessedBatch() {
    federationBatchRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchRepository.findUnprocessedFederationBatches()).isEmpty();
  }

  @Test
  void testDoesNothingOnConflict() {
    federationBatchRepository.saveDoNothingOnConflict(batchTag2, date2, null);
    federationBatchRepository.saveDoNothingOnConflict(batchTag2, date1, statusError);

    List<FederationBatch> actBatches = federationBatchRepository.findUnprocessedFederationBatches();
    assertThat(actBatches).isEqualTo(singletonList(new FederationBatch(batchTag2, date2)));
  }
}
