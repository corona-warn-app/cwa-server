

package app.coronawarn.server.common.federation.client;

import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Declarative web service client for the Federation Gateway API.
 *
 * <p>Any application that wants to uses it must make sure the required configuration
 * beans in this module are registered (scan root package of the module). There is also a constraint imposed on
 * application properties, such that values for the following structure must be declared:
 * <li> federation-gateway.base-url
 * <li> federation-gateway.ssl.key-store-path
 * <li> federation-gateway.ssl.key-store-pass
 * <li> federation-gateway.ssl.certificate-type
 */
@FeignClient(name = "federation-server", url = "${federation-gateway.base-url}")
public interface FederationGatewayClient {

  @GetMapping(value = "/diagnosiskeys/download/{date}",
      headers = {"Accept=application/protobuf; version=1.0",
          "X-SSL-Client-SHA256=${federation-gateway.ssl.certificate-sha}",
          "X-SSL-Client-DN=${federation-gateway.ssl.certificate-dn}"})
  ResponseEntity<DiagnosisKeyBatch> getDiagnosisKeys(@PathVariable("date") String date);

  @GetMapping(value = "/diagnosiskeys/download/{date}",
      headers = {"Accept=application/protobuf; version=1.0",
          "X-SSL-Client-SHA256=${federation-gateway.ssl.certificate-sha}",
          "X-SSL-Client-DN=${federation-gateway.ssl.certificate-dn}"})
  ResponseEntity<DiagnosisKeyBatch> getDiagnosisKeys(@RequestHeader("batchTag") String batchTag,
      @PathVariable("date") String date);

  /**
   * HTTP POST request federation gateway endpoint /diagnosiskyes/upload.
   *
   * @param raw            Payload body. This property contains a raw byte array with the encoded protobuf
   *                       DiagnosisKeyBatch.
   * @param batchTag       Unique batchTag to be identified by EFGS.
   * @param batchSignature Batch Signature as per PKCS#7 spec using Authorized Signing Certificate.
   */
  @PostMapping(value = "/diagnosiskeys/upload",
      consumes = "application/protobuf; version=1.0",
      headers = {"Accept=application/json; version=1.0",
          "X-SSL-Client-SHA256=${federation-gateway.ssl.certificate-sha}",
          "X-SSL-Client-DN=${federation-gateway.ssl.certificate-dn}"})
  ResponseEntity<BatchUploadResponse> postBatchUpload(
      byte[] raw,
      @RequestHeader("batchTag") String batchTag,
      @RequestHeader("batchSignature") String batchSignature);


  /**
   * HTTP GET request to federation gateway endpoint /diagnosiskeys/audit/download to get audit information about the
   * requested {@code batchtag} on the specific {@code date}. The EFGS audit interface can return the following
   * statuses: 200 - Returns the audit information for the {@code batch tag}. 400 - Invalid or missing request header.
   * 403 - Invalid or missing client certificate. 404 - The batch tag is not found or no data exists. 406 - Data format
   * or content is not valid. 410 - The date is expired or no more exists.
   *
   * @param date     The date for which the batch should be audited.
   * @param batchTag The batchTag of the batch that should be audited.
   * @return Response of the EFGS audit interface as string.
   */
  @GetMapping(value = "/diagnosiskeys/audit/download/{date}/{batchTag}",
      headers = {"Accept=application/json; version=1.0",
          "X-SSL-Client-SHA256=${federation-gateway.ssl.certificate-sha}",
          "X-SSL-Client-DN=${federation-gateway.ssl.certificate-dn}"})
  ResponseEntity<String> getAuditInformation(@PathVariable("date") String date,
      @PathVariable("batchTag") String batchTag);
}
