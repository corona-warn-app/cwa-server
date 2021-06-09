package app.coronawarn.server.services.submission.validation;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.checkins.EventCheckinDataValidator;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Constraint(validatedBy = ValidSubmissionPayload.SubmissionPayloadValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidSubmissionPayload {

  /**
   * Error message.
   *
   * @return the error message
   */
  String message() default "Invalid diagnosis key submission payload.";

  /**
   * Groups.
   *
   * @return generic array
   */
  Class<?>[] groups() default {};

  /**
   * Payload.
   *
   * @return generic array that extends {@link Payload}
   */
  Class<? extends Payload>[] payload() default {};

  class SubmissionPayloadValidator implements ConstraintValidator<ValidSubmissionPayload, SubmissionPayload> {

    private final int maxNumberOfKeys;
    private final int minRollingPeriod;
    private final int maxRollingPeriod;
    private final Collection<String> supportedCountries;
    private final String defaultOriginCountry;
    private final EventCheckinDataValidator eventCheckinValidator;
    private static final Logger logger = LoggerFactory.getLogger(SubmissionPayloadValidator.class);

    public SubmissionPayloadValidator(SubmissionServiceConfig submissionServiceConfig,
        EventCheckinDataValidator checkinDataValidator) {
      maxNumberOfKeys = submissionServiceConfig.getMaxNumberOfKeys();
      maxRollingPeriod = submissionServiceConfig.getMaxRollingPeriod();
      minRollingPeriod = submissionServiceConfig.getMinRollingPeriod();
      supportedCountries = List.of(submissionServiceConfig.getSupportedCountries());
      defaultOriginCountry = submissionServiceConfig.getDefaultOriginCountry();
      eventCheckinValidator = checkinDataValidator;
    }

    /**
     * Validates the following constraints.
     * <ul>
     * <li>StartIntervalNumber values are always at midnight</li>
     * <li>There must not be more than allowed maximum number of keys in a payload (see
     * application.yaml/max-number-of-keys)
     * <li>The origin country can be missing or the provided value must be of the supported countries (see
     * application.yaml).</li>
     * <li>The visited countries can be missing or the provided values must be part of the supported countries.</li>
     * <li>Either a value of accepted Transmission Risk Level or an accepted Days Since Onset Of Symptoms must be
     * present. If one value is missing, the other one can be derived (see {@link DiagnosisKeyNormalizer}</li>
     * </ul>
     */
    @Override
    public boolean isValid(SubmissionPayload submissionPayload, ConstraintValidatorContext validatorContext) {
      List<TemporaryExposureKey> exposureKeys = submissionPayload.getKeysList();
      validatorContext.disableDefaultConstraintViolation();

      boolean isValidPayload = checkStartIntervalNumberIsAtMidNight(exposureKeys, validatorContext)
          && checkKeyCollectionSize(exposureKeys, validatorContext)
          && checkOriginCountryIsValid(submissionPayload, validatorContext)
          && checkVisitedCountriesAreValid(submissionPayload, validatorContext)
          && checkRequiredFieldsNotMissing(exposureKeys, validatorContext)
          && checkTransmissionRiskLevelIsAcceptable(exposureKeys, validatorContext)
          && checkDaysSinceOnsetOfSymptomsIsInRange(exposureKeys, validatorContext)
          && eventCheckinValidator.verify(submissionPayload, validatorContext)
          && checkRollingPeriodIsInRange(exposureKeys, validatorContext);

      if (!isValidPayload) {
        PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
        logger.error("Errors caused by invalid payload {}", printableSubmissionPayload);
      }
      return isValidPayload;
    }

    private void addViolation(ConstraintValidatorContext validatorContext, String message) {
      validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean checkKeyCollectionSize(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      if (exposureKeys.isEmpty() || exposureKeys.size() > maxNumberOfKeys) {
        addViolation(validatorContext,
            String.format("Number of keys must be between 1 and %s, but is %s.", maxNumberOfKeys, exposureKeys.size()));
        return false;
      }
      return true;
    }

    private boolean checkStartIntervalNumberIsAtMidNight(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      boolean isNotMidNight00Utc = exposureKeys.stream()
          .anyMatch(exposureKey -> exposureKey.getRollingStartIntervalNumber() % maxRollingPeriod > 0);

      if (isNotMidNight00Utc) {
        addViolation(validatorContext, "Start Interval Number must be at midnight ( 00:00 UTC )");
        return false;
      }

      return true;
    }

    /**
     * Verify if payload contains invalid or unaccepted origin country.
     *
     * @return false if the originCountry field of the given payload does not contain a country code from the
     * configured* <code>application.yml/supported-countries</code>
     */
    private boolean checkOriginCountryIsValid(SubmissionPayload submissionPayload,
        ConstraintValidatorContext validatorContext) {
      String originCountry = submissionPayload.getOrigin();
      if (submissionPayload.hasOrigin() && !StringUtils.isEmpty(originCountry)
          && !originCountry.equals(defaultOriginCountry)) {
        addViolation(validatorContext,
            String.format("Origin country %s is not part of the supported countries list", originCountry));
        return false;
      }
      return true;
    }

    private boolean checkVisitedCountriesAreValid(SubmissionPayload submissionPayload,
        ConstraintValidatorContext validatorContext) {
      if (submissionPayload.getVisitedCountriesList().isEmpty()) {
        return true;
      }
      Collection<String> invalidVisitedCountries = submissionPayload.getVisitedCountriesList().stream()
          .filter(not(supportedCountries::contains)).collect(toList());

      if (!invalidVisitedCountries.isEmpty()) {
        invalidVisitedCountries.forEach(country -> addViolation(validatorContext,
            "[" + country + "]: Visited country is not part of the supported countries list"));
      }
      return invalidVisitedCountries.isEmpty();
    }

    private boolean checkDaysSinceOnsetOfSymptomsIsInRange(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      // check if days since onset of symptoms is in the acceptable range
      return addViolationForInvalidTek(exposureKeys,
          tekStream -> tekStream.filter(TemporaryExposureKey::hasDaysSinceOnsetOfSymptoms)
              .filter(this::hasInvalidDaysSinceSymptoms),
          validatorContext, invalidTek -> "'" + invalidTek.getDaysSinceOnsetOfSymptoms()
              + "' is not a valid daysSinceOnsetOfSymptoms value.");
    }

    private boolean checkTransmissionRiskLevelIsAcceptable(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      // check if transmission risk level is in the acceptable range
      return addViolationForInvalidTek(exposureKeys,
          tekStream -> tekStream.filter(TemporaryExposureKey::hasTransmissionRiskLevel)
              .filter(this::hasInvalidTransmissionRiskLevel),
          validatorContext,
          invalidTek -> "'" + invalidTek.getTransmissionRiskLevel() + "' is not a valid transmissionRiskLevel value.");
    }

    private boolean checkRequiredFieldsNotMissing(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      // we check for DSOS and TRL. They are optional fields, but it is expected to receive either one of them.
      return addViolationForInvalidTek(exposureKeys,
          tekStream -> tekStream.filter(key -> !key.hasTransmissionRiskLevel())
              .filter(key -> !key.hasDaysSinceOnsetOfSymptoms()),
          validatorContext, invalidTek -> "A key was found which is missing both 'transmissionRiskLevel' "
              + "and 'daysSinceOnsetOfSymptoms.'");
    }

    private boolean hasInvalidDaysSinceSymptoms(TemporaryExposureKey key) {
      int dsos = key.getDaysSinceOnsetOfSymptoms();
      return dsos < DiagnosisKey.MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS
          || dsos > DiagnosisKey.MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS;
    }

    private boolean hasInvalidTransmissionRiskLevel(TemporaryExposureKey key) {
      int trl = key.getTransmissionRiskLevel();
      return trl < DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL || trl > DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL;
    }

    /**
     * Add a violation to the validation context, in case a key is found that matches the filtering function.
     *
     * @return True if an invalid key was found.
     */
    private boolean addViolationForInvalidTek(List<TemporaryExposureKey> exposureKeys,
        UnaryOperator<Stream<TemporaryExposureKey>> filterFunction, ConstraintValidatorContext validatorContext,
        Function<TemporaryExposureKey, String> messageConstructor) {
      AtomicBoolean foundInvalid = new AtomicBoolean(true);
      filterFunction.apply(exposureKeys.stream()).findFirst().ifPresent(invalidTek -> {
        foundInvalid.set(false);
        addViolation(validatorContext, messageConstructor.apply(invalidTek));
      });
      return foundInvalid.get();
    }

    private boolean checkRollingPeriodIsInRange(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      for (int i = 0; i < exposureKeys.size(); i++) {
        if (exposureKeys.get(i).getRollingPeriod() < minRollingPeriod
            || exposureKeys.get(i).getRollingPeriod() > maxRollingPeriod) {
          addViolation(validatorContext, "The rolling period is not in range.");
          return false;
        }
      }
      return true;
    }
  }
}
