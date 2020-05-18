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
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.exception.InvalidPayloadException;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/version/v1")
public class SubmissionController {
  private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private TanVerifier tanVerifier;

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

  private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
  private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

  /**
   * Handles diagnosis key submission requests.
   *
   * @param exposureKeys The unmarshalled protocol buffers submission payload.
   * @param fake         A header flag, marking fake requests.
   * @param tan          A tan for diagnosis verification.
   * @return An empty response body.
   */
  @PostMapping(SUBMISSION_ROUTE)
  public DeferredResult<ResponseEntity<Void>> submitDiagnosisKey(
      @RequestBody SubmissionPayload exposureKeys,
      @RequestHeader(value = "cwa-fake") Integer fake,
      @RequestHeader(value = "cwa-authorization") String tan) {
    final DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();
    if (fake != 0) {
      setFakeDeferredResult(deferredResult);
    } else {
      setRealDeferredResult(deferredResult, exposureKeys, tan);
    }
    return deferredResult;
  }

  private void setFakeDeferredResult(DeferredResult<ResponseEntity<Void>> deferredResult) {
    long delay = new PoissonDistribution(fakeDelay).sample();
    scheduledExecutor.schedule(() -> deferredResult.setResult(buildSuccessResponseEntity()),
        delay, TimeUnit.MILLISECONDS);
  }

  private void setRealDeferredResult(DeferredResult<ResponseEntity<Void>> deferredResult,
      SubmissionPayload exposureKeys, String tan) {
    forkJoinPool.submit(() -> {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      if (!this.tanVerifier.verifyTan(tan)) {
        deferredResult.setResult(buildTanInvalidResponseEntity());
      } else {
        try {
          persistDiagnosisKeysPayload(exposureKeys);
          deferredResult.setResult(buildSuccessResponseEntity());
        } catch (Exception e) {
          deferredResult.setErrorResult(e);
        }
      }
      stopWatch.stop();
      updateFakeDelay(stopWatch.getTotalTimeMillis());
    });
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
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void persistDiagnosisKeysPayload(SubmissionPayload protoBufDiagnosisKeys) {
    List<Key> protoBufferKeysList = protoBufDiagnosisKeys.getKeysList();
    validatePayload(protoBufferKeysList);

    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();
    for (Key protoBufferKey : protoBufferKeysList) {
      DiagnosisKey diagnosisKey = DiagnosisKey.builder().fromProtoBuf(protoBufferKey).build();
      if (diagnosisKey.isYoungerThanRetentionThreshold(retentionDays)) {
        diagnosisKeys.add(diagnosisKey);
      } else {
        logger.debug("Not persisting diagnosis key {}, as it is outdated beyond retention threshold.", diagnosisKey);
      }
    }

    diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);
  }

  private void validatePayload(List<Key> protoBufKeysList) {
    if (protoBufKeysList.isEmpty() || protoBufKeysList.size() > maxNumberOfKeys) {
      throw new InvalidPayloadException(
          String.format("Number of keys must be between 1 and %s, but is %s.", maxNumberOfKeys, protoBufKeysList));
    }
  }

  private synchronized void updateFakeDelay(long realRequestDuration) {
    fakeDelay = fakeDelay + (1 / fakeDelayMovingAverageSamples) * (realRequestDuration - fakeDelay);
  }

}
