package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.exception.InvalidPayloadException;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
public class SubmissionController {

  @Value("${services.submission.retention-days}")
  private Integer retentionDays;

  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private TanVerifier tanVerifier;

  @Value("${services.submission.payload.max-number-of-keys}")
  private Integer maxNumberOfKeys;

  @PostMapping(SUBMISSION_ROUTE)
  public ResponseEntity<Void> submitDiagnosisKey(
      @RequestBody SubmissionPayload exposureKeysPayload,
      @RequestHeader(value = "cwa-fake") Integer fake,
      @RequestHeader(value = "cwa-authorization") String tan) throws InvalidDiagnosisKeyException, InvalidPayloadException {
    if (fake != 0) {
      return ResponseEntity.ok().build();
    }

    if (!this.tanVerifier.verifyTan(tan)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    saveDiagnosisKeysPayload(exposureKeysPayload);
    return ResponseEntity.ok().build();
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload.
   *
   * @param protoBufDiagnosisKeys Diagnosis keys that were specified in the request.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void saveDiagnosisKeysPayload(SubmissionPayload protoBufDiagnosisKeys) throws InvalidDiagnosisKeyException, InvalidPayloadException {
    List<Key> protoBufKeysList = protoBufDiagnosisKeys.getKeysList();
    validatePayload(protoBufKeysList);

    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();
    for (Key aProtoBufKey : protoBufKeysList) {
      DiagnosisKey diagnosisKey = DiagnosisKey.builder().fromProtoBuf(aProtoBufKey).build();
      if (diagnosisKey.isYoungerThanRetentionPeriod(retentionDays)) {
        diagnosisKeys.add(diagnosisKey);
      }
    }

    diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);
  }

  private void validatePayload(List<Key> protoBufKeysList) throws InvalidPayloadException {
    if (protoBufKeysList.isEmpty() || protoBufKeysList.size() > maxNumberOfKeys) {
      throw new InvalidPayloadException(
          String.format("Number of keys must be between 1 and %s, but is %s.", maxNumberOfKeys, protoBufKeysList));
    }
  }

}
