package app.coronawarn.server.services.download;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED_WITH_ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.normalization.FederationKeyNormalizer;
import app.coronawarn.server.services.download.validation.ValidFederationKeyFilter;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for downloading and storing batch information from the federation gateway.
 */
@Component
public class FederationBatchProcessor {

  private static final Logger logger = LoggerFactory.getLogger(FederationBatchProcessor.class);
  private final FederationBatchInfoService batchInfoService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final FederationGatewayDownloadService federationGatewayDownloadService;
  private final DownloadServiceConfig config;
  private final ValidFederationKeyFilter validFederationKeyFilter;

  /**
   * Constructor.
   *
   * @param batchInfoService                 A {@link FederationBatchInfoService} for accessing diagnosis key batch
   *                                         information.
   * @param diagnosisKeyService              A {@link DiagnosisKeyService} for storing retrieved diagnosis keys.
   * @param federationGatewayDownloadService A {@link FederationGatewayDownloadService} for retrieving federation
   *                                         diagnosis key batches.
   * @param config                           A {@link DownloadServiceConfig} for retrieving federation configuration.
   * @param federationKeyValidator           A {@link ValidFederationKeyFilter} for validating keys in the downloaded
   *                                         batches
   */
  public FederationBatchProcessor(FederationBatchInfoService batchInfoService,
      DiagnosisKeyService diagnosisKeyService, FederationGatewayDownloadService federationGatewayDownloadService,
      DownloadServiceConfig config, ValidFederationKeyFilter federationKeyValidator) {
    this.batchInfoService = batchInfoService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.federationGatewayDownloadService = federationGatewayDownloadService;
    this.config = config;
    this.validFederationKeyFilter = federationKeyValidator;
  }

  /**
   * Stores the batch info for the specified date. Its status is set to {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @param date The date for which the first batch info is stored.
   */
  public void saveFirstBatchInfoForDate(LocalDate date) {
    checkIfDownloadShouldBeForced(date);
    try {
      logger.info("Triggering download of first batch for date {}.", date);
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(date);
      batchInfoService.save(new FederationBatchInfo(response.getBatchTag(), date));
    } catch (Exception e) {
      logger.error("Triggering download of first batch for date {} failed.", date, e);
    }
  }

  /**
   * The Federation Batch Info stores information about which batches have already been processed to not download them
   * again. The batches for the current day might change constantly when national backends upload keys, hence there is
   * the need to download the batches for today again. Hence, the entries in federation batch info with the current day
   * need to be removed. There is a parameter 'efgs-repeat-download-offset-days' with default 0 for
   * that.
   *
   * @param date The date the download was triggered for
   */
  public void checkIfDownloadShouldBeForced(LocalDate date) {
    LocalDate downloadAgainDate = LocalDate.now(ZoneOffset.UTC)
        .minus(Period.ofDays(config.getEfgsEnforceDownloadOffsetDays()));
    if (downloadAgainDate.equals(date)) {
      logger.info("Preparing database to enforce download of batches for day {} again.", date);
      batchInfoService.deleteForDate(downloadAgainDate);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with the status
   * value {@link FederationBatchStatus#ERROR}.
   */
  public void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfosWithError = batchInfoService.findByStatus(ERROR);
    logger.info("{} error federation batches for reprocessing found.", federationBatchInfosWithError.size());
    federationBatchInfosWithError.forEach(this::retryProcessingBatch);
  }

  private void retryProcessingBatch(FederationBatchInfo federationBatchInfo) {
    try {
      processBatchAndReturnNextBatchId(federationBatchInfo, ERROR_WONT_RETRY)
          .ifPresent(nextBatchTag ->
              batchInfoService.save(new FederationBatchInfo(nextBatchTag, federationBatchInfo.getDate())));
    } catch (Exception e) {
      logger.error("Failed to save next federation batch info for processing. Will not try again.", e);
      batchInfoService.updateStatus(federationBatchInfo, ERROR_WONT_RETRY);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with status value
   * {@link FederationBatchStatus#UNPROCESSED}.
   */
  public void processUnprocessedFederationBatches() {
    Deque<FederationBatchInfo> unprocessedBatches = new LinkedList<>(batchInfoService.findByStatus(UNPROCESSED));
    logger.info("{} unprocessed federation batches found.", unprocessedBatches.size());

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatch = unprocessedBatches.remove();
      processBatchAndReturnNextBatchId(currentBatch, ERROR)
          .ifPresent(nextBatchTag ->
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatch.getDate())));
    }
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo batchInfo, FederationBatchStatus errorStatus) {
    LocalDate date = batchInfo.getDate();
    String batchTag = batchInfo.getBatchTag();
    logger.info("Processing batch for date {} and batchTag {}.", date, batchTag);
    try {
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(batchTag, date);
      AtomicBoolean batchContainsInvalidKeys = new AtomicBoolean(false);
      response.getDiagnosisKeyBatch().ifPresent(batch -> {
        logger.info("Downloaded {} keys for date {} and batchTag {}.", batch.getKeysCount(), date, batchTag);
        List<DiagnosisKey> validDiagnosisKeys = extractValidDiagnosisKeysFromBatch(batch);
        int numOfInvalidKeys = batch.getKeysCount() - validDiagnosisKeys.size();
        if (numOfInvalidKeys > 0) {
          batchContainsInvalidKeys.set(true);
          logger.info("{} keys failed validation and were skipped.", numOfInvalidKeys);
        }
        int insertedKeys = diagnosisKeyService.saveDiagnosisKeys(validDiagnosisKeys);
        logger.info("Successfully inserted {} keys for date {} and batchTag {}.", insertedKeys, date, batchTag);
      });
      batchInfoService.updateStatus(batchInfo, batchContainsInvalidKeys.get() ? PROCESSED_WITH_ERROR : PROCESSED);
      return response.getNextBatchTag();
    } catch (Exception e) {
      logger.error("Federation batch processing for date {} and batchTag {} failed. Status set to {}.",
          date, batchTag, errorStatus.name(), e);
      batchInfoService.updateStatus(batchInfo, errorStatus);
      return Optional.empty();
    }
  }

  private List<DiagnosisKey> extractValidDiagnosisKeysFromBatch(DiagnosisKeyBatch diagnosisKeyBatch) {
    return diagnosisKeyBatch.getKeysList()
        .stream()
        .filter(validFederationKeyFilter::isValid)
        .map(this::convertFederationDiagnosisKeyToDiagnosisKey)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toList());
  }

  private Optional<DiagnosisKey> convertFederationDiagnosisKeyToDiagnosisKey(
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey diagnosisKey) {
    try {
      return Optional.of(DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey)
          .withReportType(ReportType.CONFIRMED_TEST)
          .withFieldNormalization(new FederationKeyNormalizer(config))
          .build());
    } catch (Exception ex) {
      logger.info("Building diagnosis key from federation diagnosis key failed.", ex);
      return Optional.empty();
    }
  }
}
