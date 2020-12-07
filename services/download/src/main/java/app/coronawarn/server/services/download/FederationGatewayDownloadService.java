package app.coronawarn.server.services.download;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import feign.FeignException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Responsible for downloading and storing batch information from the federation gateway.
 */
@Component
public class FederationGatewayDownloadService {

  public static final String HEADER_BATCH_TAG = "batchTag";
  public static final String HEADER_NEXT_BATCH_TAG = "nextBatchTag";
  public static final String EMPTY_HEADER = "null";
  private static final Logger logger = LoggerFactory.getLogger(FederationGatewayDownloadService.class);
  private final FederationGatewayClient federationGatewayClient;


  /**
   * Constructor.
   *
   * @param federationGatewayClient A {@link FederationGatewayClient} for retrieving federation diagnosis key batches.
   */
  public FederationGatewayDownloadService(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  /**
   * Download the first batch from the EFGS for the given date.
   *
   * @param date The date for which the batch should be downloaded.
   * @return The {@link BatchDownloadResponse} containing the downloaded batch, batchTag and nextBatchTag.
   */
  public BatchDownloadResponse downloadBatch(LocalDate date)
      throws FatalFederationGatewayException, BatchDownloadException {
    try {
      logger.info("Downloading first batch for date {}", date);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(getDateAsString(date));
      return parseResponseEntity(response);
    } catch (FeignException.Forbidden feignException) {
      throw new FatalFederationGatewayException(
          "Downloading batch for date " + getDateAsString(date) + " failed due to invalid client certificate.");
    } catch (FeignException | IllegalResponseException feignException) {
      logger.error("Downloading first batch for date {} failed.", date);
      throw new BatchDownloadException(date, feignException);
    }
  }

  /**
   * Download the batch from the EFGS with the given batchTag for the given date.
   *
   * @param batchTag The batchTag of the batch that should be downloaded.
   * @param date     The date for which the batch should be downloaded.
   * @return The {@link BatchDownloadResponse} containing the downloaded batch, batchTag and nextBatchTag.
   */
  public BatchDownloadResponse downloadBatch(String batchTag, LocalDate date)
      throws FatalFederationGatewayException, BatchDownloadException {
    String dateString = getDateAsString(date);
    try {
      logger.info("Downloading batch for date {} and batchTag {}.", dateString, batchTag);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(batchTag, dateString);
      return parseResponseEntity(response);
    } catch (FeignException.Forbidden feignException) {
      throw new FatalFederationGatewayException(
          "Downloading batch " + batchTag + " for date " + getDateAsString(date)
              + " failed due to invalid client certificate.");
    } catch (FeignException | IllegalResponseException exception) {
      logger.error("Downloading batch for date {} and batchTag {} failed. Reason: {}", batchTag, dateString,
          exception.getMessage());
      throw new BatchDownloadException(batchTag, date, exception);
    }
  }

  /**
   * Audit the batch from the EFGS for the given date.
   *
   * @param batchTag The batchTag of the batch that should be audited.
   * @param date     The date for which the batch should be audited.
   */
  public void auditBatch(String batchTag, LocalDate date) {
    try {
      logger.info("Auditing batch for date {} and batchTag {}.", getDateAsString(date), batchTag);
      ResponseEntity<String> auditInformation = federationGatewayClient
          .getAuditInformation(getDateAsString(date), batchTag);
      logger.debug("Retrieved audit response from EFGS: {}", auditInformation);
    } catch (FeignException.BadRequest | FeignException.Forbidden | FeignException.NotAcceptable
        | FeignException.Gone | FeignException.NotFound clientError) {
      logger.error("Auditing batch {} for date {} failed due to: {}", batchTag, getDateAsString(date),
          clientError.getMessage());
      throw new BatchAuditException(
          String.format("Auditing batch %s for date %s failed due to: %s", batchTag, date, clientError.getMessage()),
          clientError);
    } catch (FeignException e) {
      logger.error("Auditing batch {} for date {} failed due to uncommon reason: {}", batchTag,
          getDateAsString(date), e.getMessage());
      throw new BatchAuditException(String
          .format("Auditing batch %s  for date %s failed due to uncommon reason: %s", batchTag, date, e.getMessage()),
          e);
    }
  }

  private String getDateAsString(LocalDate date) {
    return date.format(ISO_LOCAL_DATE);
  }

  private BatchDownloadResponse parseResponseEntity(ResponseEntity<DiagnosisKeyBatch> response)
      throws IllegalResponseException {
    String batchTag = getHeader(response, HEADER_BATCH_TAG)
        .orElseThrow(() -> new IllegalResponseException("Missing " + HEADER_BATCH_TAG + " header."));
    Optional<String> nextBatchTag = getHeader(response, HEADER_NEXT_BATCH_TAG);
    return new BatchDownloadResponse(batchTag, Optional.ofNullable(response.getBody()), nextBatchTag);
  }

  private Optional<String> getHeader(ResponseEntity<DiagnosisKeyBatch> response, String header) {
    String headerString = response.getHeaders().getFirst(header);
    return (!EMPTY_HEADER.equals(headerString))
        ? Optional.ofNullable(headerString)
        : Optional.empty();
  }

  static class IllegalResponseException extends IOException {

    private static final long serialVersionUID = 3175572275651367015L;

    IllegalResponseException(String message) {
      super(message);
    }
  }
}
