package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.PoissonDistribution;
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

  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";

  @Value("${services.submission.fake_delay_moving_average_samples}")
  private Double fakeDelayMovingAverageSamples;

  // Exponential moving average of the last N real request durations (in ms), where
  // N = fakeDelayMovingAverageSamples.
  @Value("${services.submission.initial_fake_delay_milliseconds}")
  private Double fakeDelay;

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private TanVerifier tanVerifier;

  private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
  private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

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
        persistDiagnosisKeysPayload(exposureKeys);
        deferredResult.setResult(buildSuccessResponseEntity());
      }
      stopWatch.stop();
      updateFakeDelay(stopWatch.getTotalTimeMillis());
    });
  }

  /**
   * Returns a response that indicates that an invalid TAN was specified in the request.
   */
  private ResponseEntity<Void> buildTanInvalidResponseEntity() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  /**
   * Returns a response that indicates successful request processing.
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

    this.diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);
  }

  private synchronized void updateFakeDelay(long realRequestDuration) {
    fakeDelay = fakeDelay + (1 / fakeDelayMovingAverageSamples) * (realRequestDuration - fakeDelay);
  }
}
