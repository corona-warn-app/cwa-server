package app.coronawarn.server.common.persistence.repository;


import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
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
public class FederationBatchInfoRepositoryTest {

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  private static final String batchTag1 = "11111";
  private static final String batchTag2 = "22222";
  private static final String batchTag3 = "33333";
  private static final String batchTag4 = "44444";

  private static final LocalDate date1 = LocalDate.parse("2020-08-15");
  private static final LocalDate date2 = LocalDate.parse("2020-08-16");
  private static final LocalDate date3 = LocalDate.parse("2020-08-17");
  private static final LocalDate date4 = LocalDate.parse("2020-08-18");

  private static final String statusError = FederationBatchStatus.ERROR.name();
  private static final String statusProcessed = FederationBatchStatus.PROCESSED.name();
  private static final String statusUnprocessed = FederationBatchStatus.UNPROCESSED.name();

  @AfterEach
  void tearDown() {
    federationBatchInfoRepository.deleteAll();
  }

  @Test
  void testReturnsEmptyListIfNoUnprocessedBatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus("UNPROCESSED")).isEmpty();
  }

  @Test
  void testDoesNothingOnConflict() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusUnprocessed);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date1, statusError);

    List<FederationBatchInfo> actBatches = federationBatchInfoRepository.findByStatus("UNPROCESSED");
    assertThat(actBatches).isEqualTo(singletonList(new FederationBatchInfo(batchTag2, date2)));
  }
}
