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

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.normalization.SubmissionKeyNormalizer;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import io.micrometer.core.annotation.Timed;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
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

  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";
  private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
  private final SubmissionMonitor submissionMonitor;
  private final DiagnosisKeyService diagnosisKeyService;
  private final TanVerifier tanVerifier;
  private final Integer retentionDays;
  private final Integer randomKeyPaddingMultiplier;
  private final FakeDelayManager fakeDelayManager;
  private final SubmissionServiceConfig submissionServiceConfig;

  SubmissionController(DiagnosisKeyService diagnosisKeyService, TanVerifier tanVerifier,
      FakeDelayManager fakeDelayManager, SubmissionServiceConfig submissionServiceConfig,
      SubmissionMonitor submissionMonitor) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.tanVerifier = tanVerifier;
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
    this.submissionServiceConfig = submissionServiceConfig;
    this.retentionDays = submissionServiceConfig.getRetentionDays();
    this.randomKeyPaddingMultiplier = submissionServiceConfig.getRandomKeyPaddingMultiplier();
  }

  private static byte[] generateRandomKeyData() {
    byte[] randomKeyData = new byte[16];
    new SecureRandom().nextBytes(randomKeyData);
    return randomKeyData;
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

  private DeferredResult<ResponseEntity<Void>> buildRealDeferredResult(SubmissionPayload submissionPayload,
      String tan) {
    DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      if (!this.tanVerifier.verifyTan(tan)) {
        submissionMonitor.incrementInvalidTanRequestCounter();
        deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
      } else {
        List<DiagnosisKey> diagnosisKeys = extractValidDiagnosisKeysFromPayload(submissionPayload);
        checkDiagnosisKeysStructure(diagnosisKeys);
        diagnosisKeyService.saveDiagnosisKeys(padDiagnosisKeys(diagnosisKeys));

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

  private List<DiagnosisKey> extractValidDiagnosisKeysFromPayload(SubmissionPayload submissionPayload) {
    List<TemporaryExposureKey> protoBufferKeys = submissionPayload.getKeysList();
    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();

    for (TemporaryExposureKey protoBufferKey : protoBufferKeys) {
      String originCountry = StringUtils.defaultIfBlank(submissionPayload.getOrigin(),
          submissionServiceConfig.getDefaultOriginCountry());

      DiagnosisKey diagnosisKey = DiagnosisKey.builder()
          .fromTemporaryExposureKey(protoBufferKey)
          .withVisitedCountries(submissionPayload.getVisitedCountriesList())
          .withCountryCode(originCountry)
          .withConsentToFederation(submissionPayload.getConsentToFederation())
          .withFieldNormalization(new SubmissionKeyNormalizer(submissionServiceConfig))
          .build();

      if (diagnosisKey.isYoungerThanRetentionThreshold(retentionDays)) {
        diagnosisKeys.add(diagnosisKey);
      } else {
        logger.warn("Not persisting a diagnosis key, as it is outdated beyond retention threshold.");
      }
    }

    return diagnosisKeys;
  }

  /**
   * Checks if a key with transmission risk level 6 is missing in the submitted diagnosis keys. If there is one, it
   * should not have a rolling start interval number of today midnight. In case of violations, these are logged.
   *
   * <p>The check is only done for the key with transmission risk level 6, since the number of keys to be submitted
   * depends on the time how long the app is installed on the phone. The key with transmission risk level 6 is the one
   * from the day before the submission and should always be present.
   *
   * @param diagnosisKeys The diagnosis keys to check.
   */
  private void checkDiagnosisKeysStructure(List<DiagnosisKey> diagnosisKeys) {
    diagnosisKeys.sort(Comparator.comparing(DiagnosisKey::getRollingStartIntervalNumber));
    String keysString = Arrays.toString(diagnosisKeys.toArray());
    Predicate<DiagnosisKey> hasRiskLevel6 = diagnosisKey -> diagnosisKey.getTransmissionRiskLevel() == 6;

    if (diagnosisKeys.stream().noneMatch(hasRiskLevel6)) {
      logger.warn("Submission payload was sent with missing key having transmission risk level 6. {}", keysString);
    } else {
      logger.debug("Submission payload was sent with key having transmission risk level 6. {}", keysString);
    }

    diagnosisKeys.stream().filter(hasRiskLevel6).findFirst().ifPresent(diagnosisKey -> {
      long todayMidnightUtc = LocalDate
          .ofInstant(Instant.now(), UTC)
          .atStartOfDay()
          .toEpochSecond(UTC) / (60 * 10);
      if (diagnosisKey.getRollingStartIntervalNumber() == todayMidnightUtc) {
        logger.warn("Submission payload was sent with a key having transmission risk level 6"
            + " and rolling start interval number of today midnight. {}", keysString);
      }
    });
  }

  private List<DiagnosisKey> padDiagnosisKeys(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> paddedDiagnosisKeys = new ArrayList<>();
    diagnosisKeys.forEach(diagnosisKey -> {
      paddedDiagnosisKeys.add(diagnosisKey);
      IntStream.range(1, randomKeyPaddingMultiplier)
          .mapToObj(index -> DiagnosisKey.builder()
              .withKeyData(generateRandomKeyData())
              .withRollingStartIntervalNumber(diagnosisKey.getRollingStartIntervalNumber())
              .withTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel())
              .withRollingPeriod(diagnosisKey.getRollingPeriod())
              .withVisitedCountries(diagnosisKey.getVisitedCountries())
              .withCountryCode(diagnosisKey.getOriginCountry())
              .withReportType(diagnosisKey.getReportType())
              .withConsentToFederation(diagnosisKey.isConsentToFederation())
              .withDaysSinceOnsetOfSymptoms(diagnosisKey.getDaysSinceOnsetOfSymptoms())
              .build())
          .forEach(paddedDiagnosisKeys::add);
    });
    return paddedDiagnosisKeys;
  }
}
