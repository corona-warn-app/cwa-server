

package app.coronawarn.server.common.persistence.repository;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class FederationBatchInfoRepositoryTest {

  private static final String batchTag1 = "11111";
  private static final String batchTag2 = "22222";
  private static final String batchTag3 = "33333";
  private static final LocalDate date1 = LocalDate.parse("2020-08-15");
  private static final LocalDate date2 = LocalDate.parse("2020-08-16");
  private static final String statusError = FederationBatchStatus.ERROR.name();
  private static final String statusProcessed = FederationBatchStatus.PROCESSED.name();
  private static final String statusUnprocessed = FederationBatchStatus.UNPROCESSED.name();
  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @AfterEach
  void tearDown() {
    federationBatchInfoRepository.deleteAll();
  }

  @Test
  void testStatusIsReturnedCorrectly() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus(statusProcessed))
        .isEqualTo(singletonList(new FederationBatchInfo(batchTag1, date1, FederationBatchStatus.PROCESSED)));
  }

  @Test
  void testReturnsEmptyIfStatusDoesNotMatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed)).isEmpty();
  }

  @Test
  void testDoesNothingOnConflict() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date2, statusError);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusError);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed))
        .isEqualTo(singletonList(new FederationBatchInfo(batchTag1, date1, FederationBatchStatus.UNPROCESSED)));
  }

  @Test
  void testReturnsEmptyListIfNoUnprocessedBatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed)).isEmpty();
  }

  @Test
  void testCountForDate() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusUnprocessed);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag3, date2, statusUnprocessed);
    assertThat(federationBatchInfoRepository.countForDate(date1)).isEqualTo(1);
    assertThat(federationBatchInfoRepository.countForDate(date2)).isEqualTo(2);
  }

  @Test
  void testDeleteForDate() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusUnprocessed);
    assertThat(federationBatchInfoRepository.countForDate(date1)).isEqualTo(1);
    assertThat(federationBatchInfoRepository.countForDate(date2)).isEqualTo(1);
    federationBatchInfoRepository.deleteForDate(date1);
    assertThat(federationBatchInfoRepository.countForDate(date1)).isEqualTo(0);
    assertThat(federationBatchInfoRepository.countForDate(date2)).isEqualTo(1);
  }
}
