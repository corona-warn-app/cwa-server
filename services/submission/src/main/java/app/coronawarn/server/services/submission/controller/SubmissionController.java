/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.exception.InvalidPayloadException;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
public class SubmissionController {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";

  private final DiagnosisKeyService diagnosisKeyService;

  private final TanVerifier tanVerifier;

  @Autowired
  SubmissionController(DiagnosisKeyService diagnosisKeyService, TanVerifier tanVerifier) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.tanVerifier = tanVerifier;
  }

  // Exponential moving average of the last N real request durations (in ms), where
  // N = fakeDelayMovingAverageSamples.
  @Value("${services.submission.initial_fake_delay_milliseconds}")
  private Double fakeDelay;

  @Value("${services.submission.fake_delay_moving_average_samples}")
  private Double fakeDelayMovingAverageSamples;

  @Value("${services.submission.retention-days}")
  private Integer retentionDays;

  @Value("${services.submission.payload.max-number-of-keys}")
  private Integer maxNumberOfKeys;


  /**
   * Handles diagnosis key submission requests.
   *
   * @param exposureKeys The unmarshalled protocol buffers submission payload.
   * @param fake         A header flag, marking fake requests.
   * @param tan          A tan for diagnosis verification.
   * @return An empty response body.
   */
  @PostMapping(SUBMISSION_ROUTE)
  public CompletableFuture<ResponseEntity<Void>> submitDiagnosisKey(
      @RequestBody SubmissionPayload exposureKeys,
      @RequestHeader("cwa-fake") Integer fake,
      @RequestHeader("cwa-authorization") String tan) {
    if (fake != 0) {
      return fakeResult();
    } else {
      return realResult(exposureKeys, tan);
    }
  }

  private CompletableFuture<ResponseEntity<Void>> fakeResult() {
    long delay = new PoissonDistribution(fakeDelay).sample();
    Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS);
    return CompletableFuture.supplyAsync(this::buildSuccessResponseEntity, delayedExecutor);
  }

  private CompletableFuture<ResponseEntity<Void>> realResult(SubmissionPayload exposureKeys, String tan) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    if (!this.tanVerifier.verifyTan(tan)) {
      return CompletableFuture.supplyAsync(this::buildTanInvalidResponseEntity)
          .whenComplete((v, t) -> {
            stopWatch.stop();
            updateFakeDelay(stopWatch.getTotalTimeMillis());
          });
    } else {
      return persistDiagnosisKeysPayload(exposureKeys)
          .thenApply(diagnosisKeys -> buildSuccessResponseEntity())
          .exceptionally(throwable -> {
            logger.error(throwable.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }).whenComplete((v, t) -> {
            stopWatch.stop();
            updateFakeDelay(stopWatch.getTotalTimeMillis());
          });
    }
  }

  /**
   * Returns a response that indicates successful request processing.
   */
  private ResponseEntity<Void> buildSuccessResponseEntity() {
    return ResponseEntity.ok().build();
  }

  /**
   * Returns a response that indicates that an invalid TAN was specified in the request.
   */
  private ResponseEntity<Void> buildTanInvalidResponseEntity() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload.
   *
   * @param protoBufDiagnosisKeys Diagnosis keys that were specified in the request.
   * @return A list of saved Diagnosis Keys.
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  @Async
  public CompletableFuture<List<DiagnosisKey>> persistDiagnosisKeysPayload(SubmissionPayload protoBufDiagnosisKeys) {
    List<TemporaryExposureKey> protoBufferKeysList = protoBufDiagnosisKeys.getKeysList();
    validatePayload(protoBufferKeysList);

    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();
    for (TemporaryExposureKey protoBufferKey : protoBufferKeysList) {
      DiagnosisKey diagnosisKey = DiagnosisKey.builder().fromProtoBuf(protoBufferKey).build();
      if (diagnosisKey.isYoungerThanRetentionThreshold(retentionDays)) {
        diagnosisKeys.add(diagnosisKey);
      } else {
        logger.info("Not persisting a diagnosis key, as it is outdated beyond retention threshold.");
      }
    }

    return diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);
  }

  private void validatePayload(List<TemporaryExposureKey> protoBufKeysList) {
    if (protoBufKeysList.isEmpty() || protoBufKeysList.size() > maxNumberOfKeys) {
      throw new InvalidPayloadException(
          String.format("Number of keys must be between 1 and %s, but is %s.", maxNumberOfKeys, protoBufKeysList));
    }
  }

  private synchronized void updateFakeDelay(long realRequestDuration) {
    fakeDelay = fakeDelay + (1 / fakeDelayMovingAverageSamples) * (realRequestDuration - fakeDelay);
  }

}
