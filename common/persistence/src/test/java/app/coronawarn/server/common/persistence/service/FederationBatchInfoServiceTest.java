

package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
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
import org.springframework.test.context.ActiveProfiles;

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
    var actKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED, EFGS);
    assertThat(actKeys).isEmpty();
  }

  @Test
  void testSaveAndRetrieve() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date, EFGS);
    federationBatchInfoService.save(federationBatchInfo);
    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS);
    assertThat(actualKeys.size()).isEqualTo(1);
    assertThat(actualKeys.get(0)).isEqualTo(federationBatchInfo);
  }

  @Test
  void testSaveAndRetrieveDifferentStatus() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date, EFGS);
    federationBatchInfoService.save(federationBatchInfo);
    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.ERROR,EFGS);
    assertThat(actualKeys).isEmpty();
  }

  @Test
  void testDoesNotPersistOnConflict() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date, EFGS);
    assertThat(federationBatchInfoService.save(federationBatchInfo)).isTrue();
    assertThat(federationBatchInfoService.save(federationBatchInfo)).isFalse();
  }

  @Test
  void testUpdateStatus() {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date,
        FederationBatchStatus.UNPROCESSED, EFGS);
    federationBatchInfoService.updateStatus(federationBatchInfo, FederationBatchStatus.PROCESSED);

    var actualUnprocessedKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS);
    assertThat(actualUnprocessedKeys).isEmpty();

    var actualProcessedKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.PROCESSED,EFGS);
    assertThat(actualProcessedKeys.size()).isEqualTo(1);
    assertThat(actualProcessedKeys.get(0))
        .isEqualTo(
            new FederationBatchInfo(batchTag, date, FederationBatchStatus.PROCESSED, EFGS));
  }

  @Test
  void testSaveAndRetrieveOnConflict() {
    FederationBatchInfo federationBatchInfo1 =
        new FederationBatchInfo(batchTag, date, FederationBatchStatus.UNPROCESSED, EFGS);
    federationBatchInfoService.save(federationBatchInfo1);

    FederationBatchInfo federationBatchInfo2 = new FederationBatchInfo(batchTag, date, FederationBatchStatus.ERROR,
        EFGS);
    federationBatchInfoService.save(federationBatchInfo2);

    var actualErrorKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.ERROR,EFGS);
    assertThat(actualErrorKeys).isEmpty();

    var actualKeys = federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS);
    assertThat(actualKeys.size()).isEqualTo(1);
    assertThat(actualKeys.get(0)).isEqualTo(federationBatchInfo1);
  }

  @ValueSource(ints = {0, 28})
  @ParameterizedTest
  void testApplyRetentionPolicyForValidNumberOfDays(int daysToRetain) {
    assertThatCode(() -> federationBatchInfoService.applyRetentionPolicy(daysToRetain, EFGS))
        .doesNotThrowAnyException();
  }

  @Test
  void testApplyRetentionPolicyForNegativeNumberOfDays() {
    assertThat(catchThrowable(() -> federationBatchInfoService.applyRetentionPolicy(-1, EFGS)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testApplyRetentionPolicyForOneNotApplicableEntry() {
    LocalDate date = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(2));
    FederationBatchInfo expectedBatchInfo = new FederationBatchInfo(batchTag, date, EFGS);

    federationBatchInfoService.save(expectedBatchInfo);
    federationBatchInfoService.applyRetentionPolicy(2, EFGS);
    List<FederationBatchInfo> actualBatchInfos =
        federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS);

    assertThat(actualBatchInfos.size()).isEqualTo(1);
    assertThat(actualBatchInfos.get(0)).isEqualTo(expectedBatchInfo);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    LocalDate date = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(2));
    FederationBatchInfo expectedBatchInfo = new FederationBatchInfo(batchTag, date, EFGS);

    federationBatchInfoService.save(expectedBatchInfo);
    federationBatchInfoService.applyRetentionPolicy(1, EFGS);
    List<FederationBatchInfo> actualBatchInfos =
        federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS);

    assertThat(actualBatchInfos).isEmpty();
  }

  @Test
  void testDeleteForDay() {
    LocalDate date = LocalDate.now(ZoneOffset.UTC);
    FederationBatchInfo expectedBatchInfo = new FederationBatchInfo(batchTag, date, EFGS);
    federationBatchInfoService.save(expectedBatchInfo);

    assertThat(federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS)).hasSize(1);
    federationBatchInfoService.deleteForDate(date, EFGS);
    assertThat(federationBatchInfoService.findByStatus(FederationBatchStatus.UNPROCESSED,EFGS)).isEmpty();
  }
}
