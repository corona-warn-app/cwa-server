package app.coronawarn.server.services.submission.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
public class SubmissionController {

  @Autowired
  private DiagnosisKeyService exposureKeyService;

  @Autowired
  private TanVerifier tanVerifier;

  @GetMapping(value = "")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Diagnosis Key Submission Endpoint v1");
  }

  // TODO update protoSpec and endpoint to Collection<TemporaryExposureKey>
  @PostMapping(value = "/diagnosis-keys")
  public ResponseEntity<Void> submitDiagnosisKey(
      @RequestBody SubmissionPayload exposureKeys,
      @RequestHeader(value = "cwa-fake") Integer fake,
      @RequestHeader(value = "cwa-authorization") String tan) {
    if (fake != 0) {
      //TODO consider sleep or similar
      return buildSuccessResponseEntity();
    }
    if (!this.tanVerifier.verifyTan(tan)) {
      return buildTanInvalidResponseEntity();
    }

    persistDiagnosisKeysPayload(exposureKeys);

    return buildSuccessResponseEntity();
  }

  /**
   * @return A response that indicates that an invalid TAN was specified in the request.
   */
  private ResponseEntity<Void> buildTanInvalidResponseEntity() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  /**
   * @return A response that indicates successful request processing.
   */
  private ResponseEntity<Void> buildSuccessResponseEntity() {
    return ResponseEntity.ok().build();
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload.
   *
   * @param protoBufDiagnosisKeys Diagnosis keys that were specified in the request.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  private void persistDiagnosisKeysPayload(SubmissionPayload protoBufDiagnosisKeys) {
    Collection<DiagnosisKey> diagnosisKeys = protoBufDiagnosisKeys.getKeysList().stream()
        .map(aProtoBufKey -> DiagnosisKey.builder().fromProtoBuf(aProtoBufKey).build())
        .collect(Collectors.toList());

    this.exposureKeyService.saveDiagnosisKeys(diagnosisKeys);
  }
}
