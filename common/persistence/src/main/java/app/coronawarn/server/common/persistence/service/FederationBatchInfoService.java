

package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;

import static app.coronawarn.server.common.persistence.service.common.LogMessages.DELETING_BATCH_INFOS_WITH_DATE_OLDER;
import static app.coronawarn.server.common.persistence.service.common.LogMessages.MARKET_BATCH_WITH_STATUS;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FederationBatchInfoService {

  private static final Logger logger = LoggerFactory.getLogger(FederationBatchInfoService.class);
  private final FederationBatchInfoRepository federationBatchInfoRepository;

  public FederationBatchInfoService(FederationBatchInfoRepository federationBatchInfoRepository) {
    this.federationBatchInfoRepository = federationBatchInfoRepository;
  }

  /**
   * Persists the {@link FederationBatchInfo} instance. If the data of a particular federation batch already exists in
   * the database, this federation batch is not persisted.
   *
   * @param federationBatchInfo must not contain {@literal null}.
   */
  @Transactional
  public void save(FederationBatchInfo federationBatchInfo) {
    federationBatchInfoRepository
        .saveDoNothingOnConflict(federationBatchInfo.getBatchTag(), federationBatchInfo.getDate(),
            federationBatchInfo.getStatus().name());
  }

  /**
   * Sets the status of the provided federation batch.
   */
  public void updateStatus(FederationBatchInfo federationBatchInfo, FederationBatchStatus status) {
    String statusValue = status.name();
    federationBatchInfoRepository
        .saveDoUpdateStatusOnConflict(federationBatchInfo.getBatchTag(), federationBatchInfo.getDate(), statusValue);
    logger.info(MARKET_BATCH_WITH_STATUS, federationBatchInfo.getBatchTag(), statusValue);
  }

  /**
   * Returns all batch information entries with a given status.
   *
   * @param federationBatchStatus the status the batch information entries should have.
   * @return the list of batch information entries with the given status.
   */
  public List<FederationBatchInfo> findByStatus(FederationBatchStatus federationBatchStatus) {
    return federationBatchInfoRepository.findByStatus(federationBatchStatus.name());
  }

  /**
   * Deletes all federation batch information entries which have a date that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which batch information will be retained.
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  @Transactional
  public void applyRetentionPolicy(int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }

    LocalDate threshold = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(daysToRetain));
    int numberOfDeletions = federationBatchInfoRepository.countOlderThan(threshold);
    logger.info(DELETING_BATCH_INFOS_WITH_DATE_OLDER, numberOfDeletions, daysToRetain);
    federationBatchInfoRepository.deleteOlderThan(threshold);
  }

  /**
   * Deletes all federation batch information entries which have a specific date.
   *
   * @param date The date for which the batch information entries should be deleted.
   */
  @Transactional
  public void deleteForDate(LocalDate date) {
    int numberOfDeletions = federationBatchInfoRepository.countForDate(date);
    logger.info("Deleting {} batch info(s) for date {}.",
        numberOfDeletions, date);
    federationBatchInfoRepository.deleteForDate(date);
  }
}
