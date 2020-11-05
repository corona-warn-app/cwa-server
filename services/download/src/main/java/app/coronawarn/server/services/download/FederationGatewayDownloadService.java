

package app.coronawarn.server.services.download;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import feign.FeignException;
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
  public BatchDownloadResponse downloadBatch(LocalDate date) throws FatalFederationGatewayException {
    try {
      logger.info("Downloading first batch for date {}", date);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(date.format(ISO_LOCAL_DATE));
      return parseResponseEntity(response);
    } catch (FeignException.Forbidden feignException) {
      throw new FatalFederationGatewayException(
          "Downloading batch for date " + date.format(ISO_LOCAL_DATE) + " failed due to invalid client certificate.");
    } catch (FeignException feignException) {
      logger.error("Downloading first batch for date {} failed.", date);
      throw new BatchDownloadException("Downloading batch for date " + date.format(ISO_LOCAL_DATE) + " failed.",
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
  public BatchDownloadResponse downloadBatch(String batchTag, LocalDate date) throws FatalFederationGatewayException {
    String dateString = date.format(ISO_LOCAL_DATE);
    try {
      logger.info("Downloading batch for date {} and batchTag {}.", dateString, batchTag);
      ResponseEntity<DiagnosisKeyBatch> response = federationGatewayClient
          .getDiagnosisKeys(batchTag, dateString);
      return parseResponseEntity(response);
    } catch (FeignException.Forbidden feignException) {
      throw new FatalFederationGatewayException(
          "Downloading batch " + batchTag + " for date " + date.format(ISO_LOCAL_DATE)
              + " failed due to invalid client certificate.");
    } catch (FeignException exception) {
      logger.error("Downloading batch for date {} and batchTag {} failed.", batchTag, dateString);
      throw new BatchDownloadException("Downloading batch " + batchTag + " for date " + date + " failed.",
          exception);
    }
  }

  private BatchDownloadResponse parseResponseEntity(ResponseEntity<DiagnosisKeyBatch> response) {
    String batchTag = getHeader(response, HEADER_BATCH_TAG)
        .orElseThrow(() -> new BatchDownloadException("Missing " + HEADER_BATCH_TAG + " header."));
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
