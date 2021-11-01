package app.coronawarn.server.services.download;

import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.ERROR_WONT_RETRY;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.PROCESSED_WITH_ERROR;
import static app.coronawarn.server.common.persistence.domain.FederationBatchStatus.UNPROCESSED;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for downloading and storing batch information from the federation gateway.
 */
@Component
public class FederationBatchProcessor {

  private static final Logger logger = LoggerFactory.getLogger(FederationBatchProcessor.class);
  private static final String CH = "CH";
  private final FederationBatchInfoService batchInfoService;
  private final DiagnosisKeyService diagnosisKeyService;
  private final FederationGatewayDownloadService federationGatewayDownloadService;
  private final DownloadServiceConfig config;
  private final ValidFederationKeyFilter validFederationKeyFilter;

  /**
   * This is a potential memory-leak if there are very many batches. This is an intentional decision: We'd rather run
   * into a memory-leak if there are too many batches than run into an endless loop if a batch-tag repeats
   */
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
   *
   * @throws FatalFederationGatewayException triggers if error occurs in the federation gateway
   */
  public void prepareDownload() throws FatalFederationGatewayException {
    if (config.getEnforceDateBasedDownload()) {
      LocalDate downloadDate = LocalDate.now(ZoneOffset.UTC)
          .minus(Period.ofDays(config.getEnforceDownloadOffsetDays()));
      batchInfoService.deleteForDate(downloadDate, config.getSourceSystem());
      saveFirstBatchInfoForDate(downloadDate);
    }
  }

  /**
   * Stores the batch info for the specified date. Its status is set to {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @param date The date for which the first batch info is stored.
   * @throws FatalFederationGatewayException triggers if error occurs in the federation gateway.
   */
  protected void saveFirstBatchInfoForDate(LocalDate date) throws FatalFederationGatewayException {
    try {
      logger.info("Triggering download of first batch for date {}", date);
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(date);
      batchInfoService.save(new FederationBatchInfo(response.getBatchTag(), date, this.config.getSourceSystem()));
    } catch (FatalFederationGatewayException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Triggering download of first batch for date {} failed", date, e);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with the status
   * value {@link FederationBatchStatus#ERROR}.
   */
  public void processErrorFederationBatches() {
    List<FederationBatchInfo> federationBatchInfoWithError = batchInfoService
        .findByStatus(ERROR, this.config.getSourceSystem());
    logger.info("{} error federation batches for reprocessing found", federationBatchInfoWithError.size());
    federationBatchInfoWithError.forEach(this::retryProcessingBatch);
  }

  private void retryProcessingBatch(FederationBatchInfo federationBatchInfo) {
    try {
      processBatchAndReturnNextBatchId(federationBatchInfo, ERROR_WONT_RETRY)
          .ifPresent(nextBatchTag ->
              batchInfoService.save(new FederationBatchInfo(nextBatchTag, federationBatchInfo.getDate(), this.config
                  .getSourceSystem())));
    } catch (Exception e) {
      logger.error("Failed to save next " + federationBatchInfo.getSourceSystem()
          + " batch info for processing. Will not try again", e);
      batchInfoService.updateStatus(federationBatchInfo, ERROR_WONT_RETRY);
    }
  }

  /**
   * Downloads and processes all batches from the federation gateway that have previously been marked with status value
   * {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @throws FatalFederationGatewayException triggers if error occurs in the federation gateway
   */
  public void processUnprocessedFederationBatches() throws FatalFederationGatewayException {
    Deque<FederationBatchInfo> unprocessedBatches = new LinkedList<>(
        batchInfoService.findByStatus(UNPROCESSED, this.config
            .getSourceSystem()));
    logger.info("{} unprocessed {} batches found", unprocessedBatches.size(), config.getSourceSystem());

    while (!unprocessedBatches.isEmpty()) {
      FederationBatchInfo currentBatchInfo = unprocessedBatches.remove();
      seenBatches.add(currentBatchInfo.getBatchTag());
      processBatchAndReturnNextBatchId(currentBatchInfo, ERROR)
          .ifPresent(nextBatchTag -> {
            if (isEfgsEnforceDateBasedDownloadAndNotSeen(nextBatchTag)) {
              unprocessedBatches.add(new FederationBatchInfo(nextBatchTag, currentBatchInfo.getDate(), this.config
                  .getSourceSystem()));
            }
          });
    }
    logger.info("Processed {} total {} batches", seenBatches.size(), config.getSourceSystem());
  }

  private boolean isEfgsEnforceDateBasedDownloadAndNotSeen(String batchTag) {
    return config.getEnforceDateBasedDownload() && !seenBatches.contains(batchTag);
  }

  private Optional<String> processBatchAndReturnNextBatchId(
      FederationBatchInfo batchInfo, FederationBatchStatus errorStatus) throws FatalFederationGatewayException {
    LocalDate date = batchInfo.getDate();
    String batchTag = batchInfo.getBatchTag();
    logger.info("Processing '{}' batch for date '{}' and batchTag '{}'", batchInfo.getSourceSystem(), date, batchTag);
    AtomicReference<Optional<String>> nextBatchTag = new AtomicReference<>(Optional.empty());
    try {
      BatchDownloadResponse response = federationGatewayDownloadService.downloadBatch(batchTag, date);
      AtomicBoolean batchContainsInvalidKeys = new AtomicBoolean(false);
      nextBatchTag.set(response.getNextBatchTag());
      response.getDiagnosisKeyBatch().ifPresentOrElse(batch -> {
        logger.info("Downloaded {} '{}' keys for date '{}' and batchTag '{}'", batch.getKeysCount(),
            batchInfo.getSourceSystem(), date, batchTag);
        Map<String, Integer> countedKeysByOriginCountry = batch
            .getKeysList().stream().collect(Collectors.groupingBy(
                app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey::getOrigin))
            .entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey,
                e -> e.getValue().size()));
        if (config.getSourceSystem() == FederationBatchSourceSystem.EFGS) {
          countedKeysByOriginCountry.forEach((key, value) -> logger
              .info("Downloaded {} '{}' keys with origin country '{}'", value, batchInfo.getSourceSystem(), key));
        }
        if (isChgs()) {
          countedKeysByOriginCountry.entrySet().stream().filter(k -> !CH.equalsIgnoreCase(k.getKey()))
              .forEach(k -> logger
                  .warn("There are keys {} with origin country {} which is different to CH and therefore they will be "
                      + "dropped", k.getValue(), k.getKey()));
        }

        if (config.isBatchAuditEnabled()) {
          federationGatewayDownloadService.auditBatch(batchTag, date);
        }
        List<DiagnosisKey> validDiagnosisKeys = extractValidDiagnosisKeysFromBatch(batch);
        int numOfInvalidKeys = batch.getKeysCount() - validDiagnosisKeys.size();
        if (numOfInvalidKeys > 0) {
          batchContainsInvalidKeys.set(true);
          logger.info("{} {} keys failed validation and were skipped", batchInfo.getSourceSystem(), numOfInvalidKeys);
        }
        int insertedKeys = diagnosisKeyService.saveDiagnosisKeys(validDiagnosisKeys);
        logger.info("Successfully inserted {} {} keys for date {} and batchTag {}", batchInfo.getSourceSystem(),
            insertedKeys, date, batchTag);
      }, () -> logger.info("{} batch for date {} and batchTag {} did not contain any keys", batchInfo.getSourceSystem(),
          date, batchTag));
      batchInfoService.updateStatus(batchInfo, batchContainsInvalidKeys.get() ? PROCESSED_WITH_ERROR : PROCESSED);
      return nextBatchTag.get();
    } catch (FatalFederationGatewayException e) {
      throw e;
    } catch (Exception e) {
      logger.error(batchInfo.getSourceSystem() + " batch processing for date " + date + " and batchTag " + batchTag
          + " failed. Status set to " + errorStatus.name(), e);
      batchInfoService.updateStatus(batchInfo, errorStatus);
      return nextBatchTag.get();
    }
  }

  private List<DiagnosisKey> extractValidDiagnosisKeysFromBatch(DiagnosisKeyBatch diagnosisKeyBatch) {
    Stream<app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey> partialKeys
        = diagnosisKeyBatch.getKeysList().stream().filter(validFederationKeyFilter::isValid);
    if (isChgs()) {
      partialKeys = partialKeys.filter(key -> CH.equalsIgnoreCase(key.getOrigin()));
    }
    return partialKeys
        .map(this::convertFederationDiagnosisKeyToDiagnosisKey)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toList());
  }

  private boolean isChgs() {
    return config.getSourceSystem() == FederationBatchSourceSystem.CHGS;
  }

  private Optional<DiagnosisKey> convertFederationDiagnosisKeyToDiagnosisKey(
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey diagnosisKey) {
    try {
      return Optional.of(DiagnosisKey.builder().fromFederationDiagnosisKey(diagnosisKey)
          .withReportType(ReportType.CONFIRMED_TEST)
          .withFieldNormalization(new FederationKeyNormalizer(config))
          .build());
    } catch (Exception e) {
      logger.warn(
          "Building diagnosis key from federation diagnosis key failed. The key's origin country is: " + diagnosisKey
              .getOrigin(), e);
      return Optional.empty();
    }
  }
}
