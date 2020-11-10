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
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
  
  // This is a potential memory-leak if there are very many batches
  // This is an intentional decision: 
  // We'd rather run into a memory-leak if there are too many batches 
  // than run into an endless loop if a batch-tag repeats
  private final Set<String> seenBatches;

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
    this.seenBatches = new HashSet<>();
  }

  /**
   * Checks if the date-based download logic is enabled and prepares the FederationBatchInfo Repository accordingly. The
   * Federation Batch Info stores information about which batches have already been processed to not download them
   * again. If the date-based download is enabled, the entries for the specified date need to be removed. Stores the
   * first FederationBatchTag for the specified date as a starting point for further processing.
   */
  public void prepareDownload() throws FatalFederationGatewayException {
    if (config.getEfgsEnforceDateBasedDownload()) {
      LocalDate downloadDate = LocalDate.now(ZoneOffset.UTC)
          .minus(Period.ofDays(config.getEfgsEnforceDownloadOffsetDays()));
      batchInfoService.deleteForDate(downloadDate);
      saveFirstBatchInfoForDate(downloadDate);
    }
  }

  /**
   * Stores the batch info for the specified date. Its status is set to {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @param date The date for which the first batch info is stored.
   */
  protected void saveFirstBatchInfoForDate(LocalDate date) throws FatalFederationGatewayException {
    try {
      logger.info("Triggering download of first batch for date {}.", date);
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(date);
      batchInfoService.save(new FederationBatchInfo(response.getBatchTag(), date));
    } catch (BatchDownloadException e) {
      logger.error("Triggering download of first batch for date {} failed. Reason: {}.", date, e.getMessage());
    } catch (FatalFederationGatewayException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Triggering download of first batch for date {} failed.", date, e);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been
   * marked with the status value {@link FederationBatchStatus#ERROR}.
   */
  public void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfoWithError = batchInfoService.findByStatus(ERROR);
    logger.info("{} error federation batches for reprocessing found.", federationBatchInfoWithError.size());
    federationBatchInfoWithError.forEach(this::retryProcessingBatch);
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
  public void processUnprocessedFederationBatches() throws FatalFederationGatewayException {
    Deque<FederationBatchInfo> unprocessedBatches = new LinkedList<>(batchInfoService.findByStatus(UNPROCESSED));
    logger.info("{} unprocessed federation batches found.", unprocessedBatches.size());

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatchInfo = unprocessedBatches.remove();
      seenBatches.add(currentBatchInfo.getBatchTag());
      processBatchAndReturnNextBatchId(currentBatchInfo, ERROR)
          .ifPresent(nextBatchTag -> {
            if (isEfgsEnforceDateBasedDownloadAndNotSeen(nextBatchTag)) {
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatchInfo.getDate()));
            }
          });
    }
  }

  private boolean isEfgsEnforceDateBasedDownloadAndNotSeen(String batchTag) {
    return config.getEfgsEnforceDateBasedDownload() && !seenBatches.contains(batchTag);
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo batchInfo, FederationBatchStatus errorStatus) throws FatalFederationGatewayException {
    LocalDate date = batchInfo.getDate();
    String batchTag = batchInfo.getBatchTag();
    logger.info("Processing batch for date {} and batchTag {}.", date, batchTag);
    try {
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(batchTag, date);
      AtomicBoolean batchContainsInvalidKeys = new AtomicBoolean(false);
      response.getDiagnosisKeyBatch().ifPresentOrElse(batch -> {
        logger.info("Downloaded {} keys for date {} and batchTag {}.", batch.getKeysCount(), date, batchTag);
        List<DiagnosisKey> validDiagnosisKeys = extractValidDiagnosisKeysFromBatch(batch);
        int numOfInvalidKeys = batch.getKeysCount() - validDiagnosisKeys.size();
        if (numOfInvalidKeys > 0) {
          batchContainsInvalidKeys.set(true);
          logger.info("{} keys failed validation and were skipped.", numOfInvalidKeys);
        }
        int insertedKeys = diagnosisKeyService.saveDiagnosisKeys(validDiagnosisKeys);
        logger.info("Successfully inserted {} keys for date {} and batchTag {}.", insertedKeys, date, batchTag);
      }, () -> logger.info("Batch for date {} and batchTag {} did not contain any keys.", date, batchTag));
      batchInfoService.updateStatus(batchInfo, batchContainsInvalidKeys.get() ? PROCESSED_WITH_ERROR : PROCESSED);
      return response.getNextBatchTag();
    } catch (BatchDownloadException e) {
      logger.error("Federation batch processing for date {} and batchTag {} failed. Status set to {}. Reason: {}.",
          date, batchTag, errorStatus.name(), e.getMessage());
      batchInfoService.updateStatus(batchInfo, errorStatus);
      return Optional.empty();
    } catch (FatalFederationGatewayException e) {
      throw e;
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
    } catch (InvalidDiagnosisKeyException e) {
      logger.info("Building diagnosis key from federation diagnosis key failed. Reason: {}.", e.getMessage());
      return Optional.empty();
    } catch (Exception e) {
      logger.info("Building diagnosis key from federation diagnosis key failed.", e);
      return Optional.empty();
    }
  }
}
