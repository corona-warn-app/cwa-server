

package app.coronawarn.server.common.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class FederationBatchInfoServiceTest {

  LocalDate date = LocalDate.of(2020, 9, 1);
  String batchTag = "91e810c19729de860ea";

  @Autowired
  private FederationBatchInfoService federationBatchInfoService;

  @Autowired
  private FederationBatchInfoRepository federationBatchInfoRepository;

  @AfterEach
  public void tearDown() {
    federationBatchInfoRepository.deleteAll();
  }

  @Test
  void testRetrievalForEmptyDb() {
    var actKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actKeys).isEmpty();
  }

  @Test
  void testSaveAndRetrieve() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date);
    federationBatchInfoService.save(federationBatchInfo);
    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actualKeys.size()).isEqualTo(1);
    assertThat(actualKeys.get(0)).isEqualTo(federationBatchInfo);
  }

  @Test
  void testSaveAndRetrieveDifferentStatus() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date);
    federationBatchInfoService.save(federationBatchInfo);
    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.ERROR);
    assertThat(actualKeys).isEmpty();
  }

  @Test
  void testUpdateStatus() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date,
        FederationBatchStatus.UNPROCESSED);
    federationBatchInfoService.updateStatus(federationBatchInfo, FederationBatchStatus.PROCESSED);

    var actualUnprocessedKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actualUnprocessedKeys).isEmpty();

    var actualProcessedKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.PROCESSED);
    assertThat(actualProcessedKeys.size()).isEqualTo(1);
    assertThat(actualProcessedKeys.get(0))
        .isEqualTo(new FederationBatchInfo(batchTag, date, FederationBatchStatus.PROCESSED));
  }

  @Test
  void testSaveAndRetrieveOnConflict() {
    FederationBatchInfo federationBatchInfo1 =
        new FederationBatchInfo(batchTag, date, FederationBatchStatus.UNPROCESSED);
    federationBatchInfoService.save(federationBatchInfo1);

    FederationBatchInfo federationBatchInfo2 = new FederationBatchInfo(batchTag, date, FederationBatchStatus.ERROR);
    federationBatchInfoService.save(federationBatchInfo2);

    var actualErrorKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.ERROR);
    assertThat(actualErrorKeys).isEmpty();

    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);
    assertThat(actualKeys.size()).isEqualTo(1);
    assertThat(actualKeys.get(0)).isEqualTo(federationBatchInfo1);
  }

  @ValueSource(ints = {0, 28})
  @ParameterizedTest
  void testApplyRetentionPolicyForValidNumberOfDays(int daysToRetain) {
    assertThatCode(() -> federationBatchInfoService.applyRetentionPolicy(daysToRetain))
        .doesNotThrowAnyException();
  }

  @Test
  void testApplyRetentionPolicyForNegativeNumberOfDays() {
    assertThat(catchThrowable(() -> federationBatchInfoService.applyRetentionPolicy(-1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testApplyRetentionPolicyForOneNotApplicableEntry() {
    LocalDate date = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(2));
    FederationBatchInfo expectedBatchInfo = new FederationBatchInfo(batchTag, date);

    federationBatchInfoService.save(expectedBatchInfo);
    federationBatchInfoService.applyRetentionPolicy(2);
    List<FederationBatchInfo> actualBatchInfos =
        federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);

    assertThat(actualBatchInfos.size()).isEqualTo(1);
    assertThat(actualBatchInfos.get(0)).isEqualTo(expectedBatchInfo);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    LocalDate date = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(2));
    FederationBatchInfo expectedBatchInfo = new FederationBatchInfo(batchTag, date);

    federationBatchInfoService.save(expectedBatchInfo);
    federationBatchInfoService.applyRetentionPolicy(1);
    List<FederationBatchInfo> actualBatchInfos =
        federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED);

    assertThat(actualBatchInfos).isEmpty();
  }

  @Test
  void testDeleteForDay() {
    LocalDate date = LocalDate.now(ZoneOffset.UTC);
    FederationBatchInfo expectedBatchInfo = new FederationBatchInfo(batchTag, date);
    federationBatchInfoService.save(expectedBatchInfo);

    assertThat(federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED)).hasSize(1);
    federationBatchInfoService.deleteForDate(date);
    assertThat(federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED)).isEmpty();
  }
}
