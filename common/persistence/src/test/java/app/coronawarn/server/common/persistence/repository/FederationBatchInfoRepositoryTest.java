

package app.coronawarn.server.common.persistence.repository;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.domain.FederationBatchTarget;
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
  private static final FederationBatchTarget efgsTarget = FederationBatchTarget.EFGS;
  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @AfterEach
  void tearDown() {
    federationBatchInfoRepository.deleteAll();
  }

  @Test
  void testStatusIsReturnedCorrectly() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed, efgsTarget);
    assertThat(federationBatchInfoRepository.findByStatus(statusProcessed))
        .isEqualTo(singletonList(
            new FederationBatchInfo(batchTag1, date1, FederationBatchStatus.PROCESSED, FederationBatchTarget.EFGS)));
  }

  @Test
  void testReturnsEmptyIfStatusDoesNotMatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed, efgsTarget);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed)).isEmpty();
  }

  @Test
  void testDoesNothingOnConflict() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed, efgsTarget);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date2, statusError, efgsTarget);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusError, efgsTarget);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed))
        .isEqualTo(singletonList(
            new FederationBatchInfo(batchTag1, date1, FederationBatchStatus.UNPROCESSED, FederationBatchTarget.EFGS)));
  }

  @Test
  void testReturnsEmptyListIfNoUnprocessedBatch() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusProcessed, efgsTarget);
    assertThat(federationBatchInfoRepository.findByStatus(statusUnprocessed)).isEmpty();
  }

  @Test
  void testCountForDate() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed, efgsTarget);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusUnprocessed, efgsTarget);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag3, date2, statusUnprocessed, efgsTarget);
    assertThat(federationBatchInfoRepository.countForDate(date1)).isEqualTo(1);
    assertThat(federationBatchInfoRepository.countForDate(date2)).isEqualTo(2);
  }

  @Test
  void testDeleteForDate() {
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag1, date1, statusUnprocessed, efgsTarget);
    federationBatchInfoRepository.saveDoNothingOnConflict(batchTag2, date2, statusUnprocessed, efgsTarget);
    assertThat(federationBatchInfoRepository.countForDate(date1)).isEqualTo(1);
    assertThat(federationBatchInfoRepository.countForDate(date2)).isEqualTo(1);
    federationBatchInfoRepository.deleteForDate(date1);
    assertThat(federationBatchInfoRepository.countForDate(date1)).isZero();
    assertThat(federationBatchInfoRepository.countForDate(date2)).isEqualTo(1);
  }
}
