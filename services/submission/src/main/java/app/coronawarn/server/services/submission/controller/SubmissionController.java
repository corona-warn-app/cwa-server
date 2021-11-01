package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.config.TrlDerivations;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.submission.checkins.EventCheckinFacade;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.normalization.SubmissionKeyNormalizer;
import app.coronawarn.server.services.submission.validation.ValidSubmissionOnBehalfPayload;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload;
import app.coronawarn.server.services.submission.verification.EventTanVerifier;
import app.coronawarn.server.services.submission.verification.TanVerificationService;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import io.micrometer.core.annotation.Timed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
  public static final String SUBMISSION_ON_BEHALF_ROUTE = "/submission-on-behalf";
  private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
  public static final String CWA_FILTERED_CHECKINS_HEADER = "cwa-filtered-checkins";
  public static final String CWA_SAVED_CHECKINS_HEADER = "cwa-saved-checkins";

  private final SubmissionMonitor submissionMonitor;
  private final DiagnosisKeyService diagnosisKeyService;
  private final TanVerifier tanVerifier;
  private final EventTanVerifier eventTanVerifier;
  private final Integer retentionDays;
  private final Integer randomKeyPaddingMultiplier;
  private final FakeDelayManager fakeDelayManager;
  private final SubmissionServiceConfig submissionServiceConfig;
  private EventCheckinFacade eventCheckinFacade;
  private final TrlDerivations trlDerivations;

  SubmissionController(DiagnosisKeyService diagnosisKeyService, TanVerifier tanVerifier,
      EventTanVerifier eventTanVerifier, FakeDelayManager fakeDelayManager,
      SubmissionServiceConfig submissionServiceConfig, SubmissionMonitor submissionMonitor,
      EventCheckinFacade eventCheckinFacade) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.tanVerifier = tanVerifier;
    this.eventTanVerifier = eventTanVerifier;
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
    this.submissionServiceConfig = submissionServiceConfig;
    this.retentionDays = submissionServiceConfig.getRetentionDays();
    this.randomKeyPaddingMultiplier = submissionServiceConfig.getRandomKeyPaddingMultiplier();
    this.eventCheckinFacade = eventCheckinFacade;
    this.trlDerivations = submissionServiceConfig.getTrlDerivations();
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
    return buildRealDeferredResult(exposureKeys, tan, tanVerifier);
  }

  /**
   * Handles "submission on behalf" requests. The basic idea is, that public health departments should be enabled to
   * warn all participants of a certain event although the department didn't join the event - it's like: "warn on behalf
   * of ..."
   *
   * @param submissionPayload The unmarshalled protocol buffers submission payload.
   * @param tan               A tan for diagnosis verification.
   * @return An empty response body.
   */
  @PostMapping(value = SUBMISSION_ON_BEHALF_ROUTE, headers = {"cwa-fake=0"})
  @Timed(description = "Time spent handling submission.")
  public DeferredResult<ResponseEntity<Void>> submissionOnBehalf(
      @ValidSubmissionOnBehalfPayload @RequestBody SubmissionPayload submissionPayload,
      @RequestHeader("cwa-authorization") String tan) {
    submissionMonitor.incrementRequestCounter();
    submissionMonitor.incrementRealRequestCounter();
    submissionMonitor.incrementSubmissionOnBehalfCounter();
    return buildRealDeferredResult(submissionPayload, tan, eventTanVerifier);
  }

  /**
   * Saves the checkins and, if needed, filters them.
   *
   * @param submissionPayload Type protobuf.
   * @param tan               A tan for diagnosis verification.
   * @return DeferredResult.
   */
  private DeferredResult<ResponseEntity<Void>> buildRealDeferredResult(SubmissionPayload submissionPayload,
      String tan, TanVerificationService tanVerifier) {
    DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      if (!tanVerifier.verifyTan(tan)) {
        submissionMonitor.incrementInvalidTanRequestCounter();
        deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
      } else {
        extractAndStoreDiagnosisKeys(submissionPayload);
        CheckinsStorageResult checkinsStorageResult = eventCheckinFacade.extractAndStoreCheckins(submissionPayload);

        deferredResult.setResult(ResponseEntity.ok()
            .header(CWA_FILTERED_CHECKINS_HEADER, String.valueOf(checkinsStorageResult.getNumberOfFilteredCheckins()))
            .header(CWA_SAVED_CHECKINS_HEADER, String.valueOf(checkinsStorageResult.getNumberOfSavedCheckins()))
            .build());
      }
    } catch (Exception e) {
      deferredResult.setErrorResult(e);
    } finally {
      stopWatch.stop();
      fakeDelayManager.updateFakeRequestDelay(stopWatch.getTotalTimeMillis());
    }
    return deferredResult;
  }

  private void extractAndStoreDiagnosisKeys(SubmissionPayload submissionPayload) {
    List<DiagnosisKey> diagnosisKeys = extractValidDiagnosisKeysFromPayload(
        enhanceWithDefaultValuesIfMissing(submissionPayload));
    for (DiagnosisKey diagnosisKey : diagnosisKeys) {
      mapTrasmissionRiskValue(diagnosisKey);
    }
    diagnosisKeyService.saveDiagnosisKeys(padDiagnosisKeys(diagnosisKeys));
  }

  private void mapTrasmissionRiskValue(DiagnosisKey diagnosisKey) {
    diagnosisKey.setTransmissionRiskLevel(
        trlDerivations.mapFromTrlSubmittedToTrlToStore(diagnosisKey.getTransmissionRiskLevel()));
  }

  private List<DiagnosisKey> extractValidDiagnosisKeysFromPayload(SubmissionPayload submissionPayload) {
    List<TemporaryExposureKey> protoBufferKeys = submissionPayload.getKeysList();

    List<DiagnosisKey> diagnosisKeys = protoBufferKeys.stream()
        .map(protoBufferKey -> DiagnosisKey.builder()
            .fromTemporaryExposureKeyAndMetadata(
                protoBufferKey,
                submissionPayload.getSubmissionType(),
                submissionPayload.getVisitedCountriesList(),
                submissionPayload.getOrigin(),
                submissionPayload.getConsentToFederation())
            .withFieldNormalization(new SubmissionKeyNormalizer(submissionServiceConfig))
            .build()
        )
        .filter(diagnosisKey -> diagnosisKey.isYoungerThanRetentionThreshold(retentionDays))
        .collect(Collectors.toList());

    if (protoBufferKeys.size() > diagnosisKeys.size()) {
      logger.warn("Not persisting {} diagnosis key(s), as it is outdated beyond retention threshold.",
          protoBufferKeys.size() - diagnosisKeys.size());
    }
    return diagnosisKeys;
  }

  private SubmissionPayload enhanceWithDefaultValuesIfMissing(SubmissionPayload submissionPayload) {
    String originCountry = defaultIfEmptyOriginCountry(submissionPayload.getOrigin());

    return SubmissionPayload.newBuilder()
        .addAllKeys(submissionPayload.getKeysList())
        .setRequestPadding(submissionPayload.getRequestPadding())
        .addAllVisitedCountries(submissionPayload.getVisitedCountriesList())
        .setOrigin(originCountry)
        .setConsentToFederation(submissionPayload.getConsentToFederation())
        .setSubmissionType(submissionPayload.getSubmissionType())
        .build();
  }

  private String defaultIfEmptyOriginCountry(String originCountry) {
    return StringUtils.defaultIfBlank(originCountry, submissionServiceConfig.getDefaultOriginCountry());
  }

  private List<DiagnosisKey> padDiagnosisKeys(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> paddedDiagnosisKeys = new ArrayList<>();
    diagnosisKeys.forEach(diagnosisKey -> {
      paddedDiagnosisKeys.add(diagnosisKey);
      IntStream.range(1, randomKeyPaddingMultiplier)
          .mapToObj(index -> DiagnosisKey.builder()
              .withKeyDataAndSubmissionType(
                  HashUtils.generateSecureRandomByteArrayData(16), diagnosisKey.getSubmissionType())
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
