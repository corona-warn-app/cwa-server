package org.ena.server.services.upload.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;
import org.ena.server.services.common.persistence.domain.DiagnosisKey;
import org.ena.server.services.common.persistence.service.DiagnosisKeyService;
import org.ena.server.services.upload.verification.TanVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
public class UploadController {

  @Autowired
  private DiagnosisKeyService exposureKeyService;

  @Autowired
  private TanVerifier tanVerifier;

  @GetMapping(value = "")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Upload Endpoint v1");
  }

  @PostMapping(value = "/diagnosis-keys/country/{country}")
  public ResponseEntity<String> submitDiagnosisKey(
      @PathVariable String country,
      @RequestBody TemporaryExposureKey exposureKeys,
      @RequestHeader(value = "cwa-fake") Integer fake,
      @RequestHeader(value = "cwa-authorization") String tan) {

    if (fake != 0) {
      //TODO consider sleep or similar
      return buildSuccessResponseEntity();
    }

    if (!this.tanVerifier.verifyTan(tan)) {
      return buildTanInvalidResponseEntity();
    }

    persistDiagnosisKeysPayload(Collections.singleton(exposureKeys));

    return buildSuccessResponseEntity();
  }

  /**
   * @return A response that indicates that an invalid TAN was specified in the request.
   */
  private ResponseEntity<String> buildTanInvalidResponseEntity() {
    // TODO implement
    return null;
  }

  /**
   * @return A response that indicates successful request processing.
   */
  private ResponseEntity<String> buildSuccessResponseEntity() {
    return ResponseEntity.ok().build();
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload and returns the
   * persisted {@link DiagnosisKey} instances.
   *
   * @param protBufDiagnosisKeys Diagnosis keys that were specified in the request.
   * @return {@link DiagnosisKey} instances that were successfully persisted.
   */
  private Collection<DiagnosisKey> persistDiagnosisKeysPayload(
      Collection<TemporaryExposureKey> protBufDiagnosisKeys) {
    Collection<DiagnosisKey> diagnosisKeys = protBufDiagnosisKeys.stream()
        .map((aProtoBufKey) -> DiagnosisKey.builder().fromProtoBuf(aProtoBufKey).build())
        .collect(Collectors.toList());

    return this.exposureKeyService.saveDiagnosisKeys(diagnosisKeys);
  }
}
