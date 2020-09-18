

package app.coronawarn.server.services.download;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.download.BatchDownloadResponse;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import java.time.LocalDate;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
  private final FederationGatewayClient federationGatewayClient;

  /**
   * Constructor.
   *
   * @param batchInfoService        A {@link FederationBatchInfoService} for accessing diagnosis key batch information.
   * @param diagnosisKeyService     A {@link DiagnosisKeyService} for storing retrieved diagnosis keys.
   * @param federationGatewayClient A {@link FederationGatewayClient} for retrieving federation diagnosis key batches.
   */
  public FederationBatchProcessor(FederationBatchInfoService batchInfoService,
      DiagnosisKeyService diagnosisKeyService, FederationGatewayClient federationGatewayClient) {
    this.batchInfoService = batchInfoService;
    this.diagnosisKeyService = diagnosisKeyService;
    this.federationGatewayClient = federationGatewayClient;
  }

  /**
   * Stores the batch info for the specified date. Its status is set to {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @param date The date for which the first batch info is stored.
   */
  public void saveFirstBatchInfoForDate(LocalDate date) {
    try {
      logger.info("Downloading first batch for date {}", date);
      BatchDownloadResponse response =
          federationGatewayClient.getDiagnosisKeys(date.format(ISO_LOCAL_DATE)).orElseThrow();
      batchInfoService.save(new FederationBatchInfo(response.getBatchTag(), date));
    } catch (NoSuchElementException e) {
      logger.error("Batch for date {} was empty.", date);
    } catch (Exception e) {
      logger.error("Downloading batch for date {} failed.", date, e);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with the status
   * value {@link FederationBatchStatus#ERROR}.
   */
  public void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfosWithError = batchInfoService.findByStatus(ERROR);
    logger.info("{} error federation batches for reprocessing found", federationBatchInfosWithError.size());
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
    logger.info("{} unprocessed federation batches found", unprocessedBatches.size());

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatch = unprocessedBatches.remove();
      processBatchAndReturnNextBatchId(currentBatch, ERROR)
          .ifPresent(nextBatchTag ->
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatch.getDate())));
    }
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo batchInfo, FederationBatchStatus errorStatus) {
    String date = batchInfo.getDate().format(ISO_LOCAL_DATE);
    String batchTag = batchInfo.getBatchTag();
    logger.info("Processing batch for date {} and batchTag {}", date, batchTag);
    try {
      BatchDownloadResponse response = federationGatewayClient.getDiagnosisKeys(batchTag, date).orElseThrow();
      logger
          .info("Downloaded {} keys for date {} and batchTag {}", response.getDiagnosisKeyBatch().getKeysCount(), date,
              batchTag);
      int insertedKeys = diagnosisKeyService.saveDiagnosisKeys(convertDiagnosisKeys(response));
      logger.info("Successfully inserted {} keys for date {} and batchTag {}", insertedKeys, date, batchTag);
      batchInfoService.updateStatus(batchInfo, PROCESSED);
      return response.getNextBatchTag();
    } catch (Exception e) {
      logger.error("Federation batch processing for date {} and batchTag {} failed. Status set to {}",
          date, batchTag, errorStatus.name(), e);
      batchInfoService.updateStatus(batchInfo, errorStatus);
      return Optional.empty();
    }
  }

  private List<DiagnosisKey> convertDiagnosisKeys(BatchDownloadResponse batchDownloadResponse) {
    return batchDownloadResponse.getDiagnosisKeyBatch().getKeysList()
        .stream()
        .map(diagnosisKey -> DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey).build())
        .collect(toList());
  }
}
