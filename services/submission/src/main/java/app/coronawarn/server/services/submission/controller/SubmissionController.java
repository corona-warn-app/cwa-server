package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING_VALUE;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_SRS_OTHER_VALUE;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST_VALUE;
import static java.lang.String.valueOf;
import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.util.ObjectUtils.isEmpty;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.config.TrlDerivations;
import app.coronawarn.server.common.persistence.domain.validation.ValidRollingStartIntervalNumberValidator;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.submission.checkins.EventCheckinFacade;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.normalization.SubmissionKeyNormalizer;
import app.coronawarn.server.services.submission.validation.PrintableSubmissionPayload;
import app.coronawarn.server.services.submission.validation.ValidSubmissionOnBehalfPayload;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload;
import app.coronawarn.server.services.submission.verification.EventTanVerifier;
import app.coronawarn.server.services.submission.verification.SrsOtpVerifier;
import app.coronawarn.server.services.submission.verification.TanVerificationService;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import feign.FeignException;
import feign.RetryableException;
import io.micrometer.core.annotation.Timed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
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
  public static final String CWA_KEYS_TRUNCATED_HEADER = "cwa-keys-truncated";
  public static final Marker SECURITY = MarkerFactory.getMarker("SECURITY");

  private final SubmissionMonitor submissionMonitor;
  private final DiagnosisKeyService diagnosisKeyService;
  private final TanVerifier tanVerifier;
  private final EventTanVerifier eventTanVerifier;
  private final SrsOtpVerifier srsOtpVerifier;
  private final Integer retentionDays;
  private final int srsDays;
  private final Integer randomKeyPaddingMultiplier;
  private final FakeDelayManager fakeDelayManager;
  private final SubmissionServiceConfig submissionServiceConfig;
  private EventCheckinFacade eventCheckinFacade;
  private final TrlDerivations trlDerivations;
  private final ValidRollingStartIntervalNumberValidator rollingStartIntervalNumberValidator;

  SubmissionController(DiagnosisKeyService diagnosisKeyService, TanVerifier tanVerifier,
      EventTanVerifier eventTanVerifier, final SrsOtpVerifier srsOtpVerifier, FakeDelayManager fakeDelayManager,
      SubmissionServiceConfig submissionServiceConfig, SubmissionMonitor submissionMonitor,
      EventCheckinFacade eventCheckinFacade) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.tanVerifier = tanVerifier;
    this.eventTanVerifier = eventTanVerifier;
    this.srsOtpVerifier = srsOtpVerifier;
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
    this.submissionServiceConfig = submissionServiceConfig;
    this.retentionDays = submissionServiceConfig.getRetentionDays();
    this.srsDays = submissionServiceConfig.getSrsDays();
    this.randomKeyPaddingMultiplier = submissionServiceConfig.getRandomKeyPaddingMultiplier();
    this.eventCheckinFacade = eventCheckinFacade;
    this.trlDerivations = submissionServiceConfig.getTrlDerivations();
    rollingStartIntervalNumberValidator = new ValidRollingStartIntervalNumberValidator();
  }

  /**
   * Handles diagnosis key submission requests.
   *
   * @param exposureKeys The unmarshalled protocol buffers submission payload.
   * @param tan          A tan for diagnosis verification.
   * @return An empty response body.
   */
  @PostMapping(value = SUBMISSION_ROUTE, headers = { "cwa-fake=0" })
  @Timed(description = "Time spent handling submission.")
  public DeferredResult<ResponseEntity<Void>> submitDiagnosisKey(
      @ValidSubmissionPayload @RequestBody SubmissionPayload exposureKeys,
      @RequestHeader(name = "cwa-authorization", required = false) String tan,
      @RequestHeader(name = "cwa-otp", required = false) String otp) {

    if (isEmpty(tan) && isEmpty(otp)) {
      logger.warn("'cwa-authorization' and 'cwa-otp' header missing!");
      return badRequest();
    }
    if (!isEmpty(tan) && !isEmpty(otp)) {
      logger.warn("'cwa-authorization' and 'cwa-otp' header provided!");
      return badRequest();
    }

    if (!isEmpty(otp)) {
      if (!isSelfReport(exposureKeys) || !isUuid(otp)) {
        return badRequest();
      }
      if (diagnosisKeyService.countTodaysDiagnosisKeys() >= submissionServiceConfig.getMaxKeysPerDay()) {
        logger.warn("We reached the maximum number ({}) of allowed Self-Report-Submissions for today ({})!",
            submissionServiceConfig.getMaxKeysPerDay(), now(UTC));
        return tooManyRequests();
      }
      submissionMonitor.incrementRequestCounter();
      submissionMonitor.incrementRealRequestCounter();
      submissionMonitor.incrementSelfReportSubmissions();
      return buildRealDeferredResult(exposureKeys, otp, srsOtpVerifier);
    }

    if (!validSubmissionType(exposureKeys)) {
      return badRequest();
    }

    submissionMonitor.incrementRequestCounter();
    submissionMonitor.incrementRealRequestCounter();
    return buildRealDeferredResult(exposureKeys, tan, tanVerifier);
  }

  /**
   * Valid regular submission types: SUBMISSION_TYPE_PCR_TEST_VALUE, SUBMISSION_TYPE_RAPID_TEST_VALUE,
   * SUBMISSION_TYPE_HOST_WARNING_VALUE.
   * 
   * @param payload uses {@link SubmissionPayload#getSubmissionType()} int value of the enum
   * @return <code>true</code> if it's ok.
   * 
   * @see SubmissionType
   */
  public static boolean validSubmissionType(final SubmissionPayload payload) {
    return 0 <= payload.getSubmissionType().getNumber()
        && payload.getSubmissionType().getNumber() <= SUBMISSION_TYPE_HOST_WARNING_VALUE;
  }

  /**
   * Valid SRS types: SUBMISSION_TYPE_SRS_SELF_TEST, SUBMISSION_TYPE_SRS_RAT, SUBMISSION_TYPE_SRS_REGISTERED_PCR,
   * SUBMISSION_TYPE_SRS_UNREGISTERED_PCR, SUBMISSION_TYPE_SRS_RAPID_PCR, SUBMISSION_TYPE_SRS_OTHER.
   * 
   * @param payload uses {@link SubmissionPayload#getSubmissionType()} int value of the enum
   * @return <code>true</code> if it's ok.
   * 
   * @see SubmissionType
   */
  public static boolean isSelfReport(final SubmissionPayload payload) {
    return SUBMISSION_TYPE_SRS_SELF_TEST_VALUE <= payload.getSubmissionType().getNumber()
        && payload.getSubmissionType().getNumber() <= SUBMISSION_TYPE_SRS_OTHER_VALUE;
  }

  /**
   * {@link UUID} syntax check of given string.
   * 
   * @param otp to be checked for valid {@link UUID} syntax.
   * @return <code>true</code> if and only if {@link UUID#fromString(String)} doesn't throw
   *         {@link IllegalArgumentException} for the given otp.
   */
  public static boolean isUuid(final String otp) {
    try {
      UUID.fromString(otp);
      return true;
    } catch (final IllegalArgumentException e) {
      logger.warn(SECURITY, "OTP error ({})", e.getMessage());
      return false;
    }
  }

  /**
   * {@link ResponseEntity#badRequest()} wrapped into {@link DeferredResult}.
   * 
   * @return {@link HttpStatus#BAD_REQUEST}
   */
  public static DeferredResult<ResponseEntity<Void>> badRequest() {
    return new DeferredResult<>(null, () -> ResponseEntity.badRequest().build());
  }

  public static DeferredResult<ResponseEntity<Void>> tooManyRequests() {
    return new DeferredResult<>(null, () -> ResponseEntity.status(TOO_MANY_REQUESTS).build());
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
  @PostMapping(value = SUBMISSION_ON_BEHALF_ROUTE, headers = { "cwa-fake=0" })
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
   * @param payload Type protobuf.
   * @param tan               A tan for diagnosis verification.
   * @return DeferredResult.
   */
  private DeferredResult<ResponseEntity<Void>> buildRealDeferredResult(final SubmissionPayload payload,
      final String tan, final TanVerificationService tanVerifier) {
    final DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      final BodyBuilder response = ResponseEntity.ok();
      final Collection<DiagnosisKey> diagnosisKeys = extractValidDiagnosisKeysFromPayload(
          enhanceWithDefaultValuesIfMissing(payload), response);

      if (isSelfReport(payload) && diagnosisKeyService.exists(diagnosisKeys)) {
        logger.warn(SECURITY, "Self-Report contains already persisted keys - {}",
            new PrintableSubmissionPayload(payload));
        deferredResult
            .setResult(ResponseEntity.status(BAD_REQUEST).header("cwa-error-code", "KEYS_ALREADY_EXIST").build());
      }

      if (!tanVerifier.verifyTan(tan)) {
        submissionMonitor.incrementInvalidTanRequestCounter();
        deferredResult.setResult(ResponseEntity.status(FORBIDDEN).build());
      } else {
        saveDiagnosisKeys(diagnosisKeys);

        CheckinsStorageResult checkinsStorageResult = eventCheckinFacade.extractAndStoreCheckins(payload);

        if (isSelfReport(payload)) {
          diagnosisKeyService.recordSrs(payload.getSubmissionType());
        }

        response.header(CWA_FILTERED_CHECKINS_HEADER, valueOf(checkinsStorageResult.getNumberOfFilteredCheckins()))
            .header(CWA_SAVED_CHECKINS_HEADER, valueOf(checkinsStorageResult.getNumberOfSavedCheckins()));
        deferredResult.setResult(response.build());
      }
    } catch (final RetryableException e) {
      logger.error("Verification Service could not be reached after retry mechanism.", e);
      deferredResult.setErrorResult(e);
    } catch (final FeignException e) {
      logger.error("Verification Service could not be reached.", e);
      deferredResult.setErrorResult(e);
    } catch (final Exception e) {
      logger.error(e.getLocalizedMessage(), e);
      deferredResult.setErrorResult(e);
    } finally {
      stopWatch.stop();
      fakeDelayManager.updateFakeRequestDelay(stopWatch.getTotalTimeMillis());
    }
    return deferredResult;
  }

  private void saveDiagnosisKeys(final Collection<DiagnosisKey> diagnosisKeys) {
    for (final DiagnosisKey key : diagnosisKeys) {
      // TRL mapping
      key.setTransmissionRiskLevel(trlDerivations.mapFromTrlSubmittedToTrlToStore(key.getTransmissionRiskLevel()));
    }
    diagnosisKeyService.saveDiagnosisKeys(padDiagnosisKeys(diagnosisKeys));
  }

  private Collection<DiagnosisKey> extractValidDiagnosisKeysFromPayload(final SubmissionPayload submissionPayload,
      final BodyBuilder response) {
    final Collection<TemporaryExposureKey> protoBufferKeys = submissionPayload.getKeysList();

    Collection<DiagnosisKey> diagnosisKeys = protoBufferKeys.stream()
        .filter(protoBufferKey -> rollingStartIntervalNumberValidator
            .isValid(protoBufferKey.getRollingStartIntervalNumber(), null))
        .map(protoBufferKey -> DiagnosisKey.builder()
            .fromTemporaryExposureKeyAndMetadata(
                protoBufferKey,
                submissionPayload.getSubmissionType(),
                submissionPayload.getVisitedCountriesList(),
                submissionPayload.getOrigin(),
                submissionPayload.getConsentToFederation())
            .withFieldNormalization(new SubmissionKeyNormalizer(submissionServiceConfig))
            .build())
        .filter(diagnosisKey -> diagnosisKey.isYoungerThanRetentionThreshold(retentionDays))
        .toList();

    if (isSelfReport(submissionPayload)) {
      final Collection<DiagnosisKey> keys = diagnosisKeys.stream()
          .filter(diagnosisKey -> diagnosisKey.isYoungerThanRetentionThreshold(srsDays)).toList();
      if (keys.size() < diagnosisKeys.size()) {
        response.header(CWA_KEYS_TRUNCATED_HEADER, valueOf(srsDays));
        logger.warn(
            "Not persisting '{}' self reported key(s), because they are older than '{}' days.",
            diagnosisKeys.size() - keys.size(), srsDays);
        diagnosisKeys = keys;
      }
    }

    if (protoBufferKeys.size() > diagnosisKeys.size()) {
      logger.warn("Not persisting {} one or more diagnosis key(s), as it is outdated beyond retention threshold or the "
          + "RollingStartIntervalNumber is in the future. Payload: {}",
          protoBufferKeys.size() - diagnosisKeys.size(),
          new PrintableSubmissionPayload(submissionPayload));
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

  private Collection<DiagnosisKey> padDiagnosisKeys(final Collection<DiagnosisKey> diagnosisKeys) {
    if (randomKeyPaddingMultiplier <= 1) {
      // no padding required
      return diagnosisKeys;
    }
    final Collection<DiagnosisKey> paddedDiagnosisKeys = new ArrayList<>(
        diagnosisKeys.size() * randomKeyPaddingMultiplier);
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
