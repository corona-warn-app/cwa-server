/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import io.micrometer.core.annotation.Timed;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/version/v1")
@Validated
public class SubmissionController {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";

  private final SubmissionMonitor submissionMonitor;
  private final DiagnosisKeyService diagnosisKeyService;
  private final TanVerifier tanVerifier;
  private final Integer retentionDays;
  private final FakeDelayManager fakeDelayManager;

  SubmissionController(
      DiagnosisKeyService diagnosisKeyService, TanVerifier tanVerifier, FakeDelayManager fakeDelayManager,
      SubmissionServiceConfig submissionServiceConfig, SubmissionMonitor submissionMonitor) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.tanVerifier = tanVerifier;
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
    retentionDays = submissionServiceConfig.getRetentionDays();
  }

  /**
   * Handles diagnosis key submission requests.
   *
   * @param exposureKeys The unmarshalled protocol buffers submission payload.
   * @param tan          A tan for diagnosis verification.
   * @return An empty response body.
   */
  @PostMapping(value = SUBMISSION_ROUTE, headers = {"cwa-fake=0"})
  @Timed(description = "Time spent handling submission.")
  public DeferredResult<ResponseEntity<Void>> submitDiagnosisKey(
      @ValidSubmissionPayload @RequestBody SubmissionPayload exposureKeys,
      @RequestHeader("cwa-authorization") String tan) {
    submissionMonitor.incrementRequestCounter();
    submissionMonitor.incrementRealRequestCounter();
    return buildRealDeferredResult(exposureKeys, tan);
  }

  private DeferredResult<ResponseEntity<Void>> buildRealDeferredResult(SubmissionPayload exposureKeys, String tan) {
    DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      if (!this.tanVerifier.verifyTan(tan)) {
        submissionMonitor.incrementInvalidTanRequestCounter();
        deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
      } else {
        persistDiagnosisKeysPayload(exposureKeys);
        deferredResult.setResult(ResponseEntity.ok().build());
      }
    } catch (Exception e) {
      deferredResult.setErrorResult(e);
    } finally {
      stopWatch.stop();
      fakeDelayManager.updateFakeRequestDelay(stopWatch.getTotalTimeMillis());
    }

    return deferredResult;
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload.
   *
   * @param protoBufDiagnosisKeys Diagnosis keys that were specified in the request.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void persistDiagnosisKeysPayload(SubmissionPayload protoBufDiagnosisKeys) {
    List<TemporaryExposureKey> protoBufferKeysList = protoBufDiagnosisKeys.getKeysList();
    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();

    for (TemporaryExposureKey protoBufferKey : protoBufferKeysList) {
      DiagnosisKey diagnosisKey = DiagnosisKey.builder().fromProtoBuf(protoBufferKey).build();
      if (diagnosisKey.isYoungerThanRetentionThreshold(retentionDays)) {
        diagnosisKeys.add(diagnosisKey);
      } else {
        logger.info("Not persisting a diagnosis key, as it is outdated beyond retention threshold.");
      }
    }

    diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);
  }
}
