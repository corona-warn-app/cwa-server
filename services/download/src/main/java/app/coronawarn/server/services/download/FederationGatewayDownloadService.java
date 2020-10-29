

package app.coronawarn.server.services.download;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import feign.FeignException;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
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
  private ApplicationContext applicationContext;

  /**
   * Constructor.
   *
   * @param federationGatewayClient A {@link FederationGatewayClient} for retrieving federation diagnosis key batches.
   */
  public FederationGatewayDownloadService(FederationGatewayClient federationGatewayClient,
      ApplicationContext applicationContext) {
    this.federationGatewayClient = federationGatewayClient;
    this.applicationContext = applicationContext;
  }

  /**
   * Download the first batch from the EFGS for the given date.
   *
   * @param date The date for which the batch should be downloaded.
   * @return The {@link BatchDownloadResponse} containing the downloaded batch, batchTag and nextBatchTag.
   */
  public BatchDownloadResponse downloadBatch(LocalDate date) {
    try {
      logger.info("Downloading first batch for date {}", date);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(date.format(ISO_LOCAL_DATE));
      return parseResponseEntity(response);
    } catch (FeignException feignException) {
      exitAfterAuthenticationError(date, feignException);
      logger.error("Downloading first batch for date {} failed.", date);
      throw new FederationGatewayException("Downloading batch for date " + date.format(ISO_LOCAL_DATE) + " failed.",
          feignException);
    }
  }

  /**
   * Download the batch from the EFGS with the given batchTag for the given date.
   *
   * @param batchTag The batchTag of the batch that should be downloaded.
   * @param date     The date for which the batch should be downloaded.
   * @return The {@link BatchDownloadResponse} containing the downloaded batch, batchTag and nextBatchTag.
   */
  public BatchDownloadResponse downloadBatch(String batchTag, LocalDate date) {
    String dateString = date.format(ISO_LOCAL_DATE);
    try {
      logger.info("Downloading batch for date {} and batchTag {}.", dateString, batchTag);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(batchTag, dateString);
      return parseResponseEntity(response);
    } catch (FeignException feignException) {
      exitAfterAuthenticationError(date, feignException);
      logger.error("Downloading batch for date {} and batchTag {} failed.", batchTag, dateString);
      throw new FederationGatewayException("Downloading batch " + batchTag + " for date " + date + " failed.",
          feignException);
    }
  }

  private void exitAfterAuthenticationError(LocalDate date, FeignException feignException) {
    if (feignException.status() == HttpStatus.FORBIDDEN.value()) {
      throw new NotAuthenticatedException(
          "Downloading batch for date " + date.format(ISO_LOCAL_DATE) + " failed due to invalid client certificate.");
    }
  }


  private BatchDownloadResponse parseResponseEntity(ResponseEntity<DiagnosisKeyBatch> response) {
    String batchTag = getHeader(response, HEADER_BATCH_TAG)
        .orElseThrow(() -> new FederationGatewayException("Missing " + HEADER_BATCH_TAG + " header."));
    Optional<String> nextBatchTag = getHeader(response, HEADER_NEXT_BATCH_TAG);
    return new BatchDownloadResponse(batchTag, Optional.ofNullable(response.getBody()), nextBatchTag);
  }

  private Optional<String> getHeader(ResponseEntity<DiagnosisKeyBatch> response, String header) {
    String headerString = response.getHeaders().getFirst(header);
    return (!EMPTY_HEADER.equals(headerString))
        ? Optional.ofNullable(headerString)
        : Optional.empty();
  }
}
