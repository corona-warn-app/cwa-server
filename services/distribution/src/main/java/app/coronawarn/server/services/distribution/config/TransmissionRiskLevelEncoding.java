package app.coronawarn.server.services.distribution.config;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.config.YamlPropertySourceFactory;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

/**
 * Wrapper over properties defined in <code>master-config/tranmission-risk-encoding.yaml</code>. It
 * provides convenience methods to derive properties from one another. Please see yaml file for more
 * details on the reasoning / intent of encoding this field. This class also validates its own
 * internal state at construction time as per Spring configuration class validation mechanisms.
 */
@Configuration
@Validated
@ConfigurationProperties(prefix = "transmission-risk-encoding")
@PropertySource(value = "classpath:master-config/transmission-risk-encoding.yaml",
    factory = YamlPropertySourceFactory.class)
public class TransmissionRiskLevelEncoding implements Validator {


  private Map<Integer, Integer> transmissionRiskToDaysSinceSymptoms;

  private Map<Integer, Integer> transmissionRiskToReportType;


  TransmissionRiskLevelEncoding() {
  }


  /**
   * Returns a mapped ENF v2 Days Since Symptoms value for the given Transmission Risk Level or
   * throws an exception if the TRL is not part of the mapping.
   */
  public Integer getDaysSinceSymptomsForTransmissionRiskLevel(Integer transmissionRiskLevel) {
    return getMappedValue(transmissionRiskLevel, transmissionRiskToDaysSinceSymptoms, "daysSinceOnsetSymptoms");
  }

  /**
   * Returns a mapped ENF v2 Report Type value for the given Transmission Risk Level or throws an
   * exception if the TRL is not part of the mapping.
   */
  public ReportType getReportTypeForTransmissionRiskLevel(Integer transmissionRiskLevel) {
    return ReportType.forNumber(getMappedValue(transmissionRiskLevel, transmissionRiskToReportType, "reportType"));
  }

  private Integer getMappedValue(Integer transmissionRiskLevel, Map<Integer, Integer> encodingMap,
      String propertyLogged) {
    if (!encodingMap.containsKey(transmissionRiskLevel)) {
      throw new IllegalArgumentException(
          "transmissionRiskLevel value " + transmissionRiskLevel + " is unknown and a "
              + "distributable " + propertyLogged + " value is not mapped to it.");
    }
    return encodingMap.get(transmissionRiskLevel);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return TransmissionRiskLevelEncoding.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "transmissionRiskToDaysSinceSymptoms", "trlToDsos.empty",
        "TRL to DSOS encoding map is null or empty");
    ValidationUtils.rejectIfEmpty(errors, "transmissionRiskToReportType", "trlToReportType.empty",
        "TRL to RT encoding map is null or empty");

    TransmissionRiskLevelEncoding encodingMappings = (TransmissionRiskLevelEncoding) target;

    if (trlKeysNotInRange(encodingMappings.getTransmissionRiskToDaysSinceSymptoms())) {
      errors.rejectValue("transmissionRiskToDaysSinceSymptoms",
          "transmissionRisk to daysSinceOnsetSymptoms map contains invalid TRL");
    }
    if (trlKeysNotInRange(encodingMappings.getTransmissionRiskToReportType())) {
      errors.rejectValue("transmissionRiskToReportType", "transmissionRisk to reportType map contains invalid TRL");
    }
  }

  private boolean trlKeysNotInRange(
      Map<Integer, Integer> transmissionRiskLevelToDaysSinceSymptomsMapping) {
    Set<Integer> trlKeys = transmissionRiskLevelToDaysSinceSymptomsMapping.keySet();

    return !IntStream.rangeClosed(DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL,
        DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL).allMatch(trlKeys::contains);
  }

  private void internalValidate() {
    BeanPropertyBindingResult errorsContainer =
        new BeanPropertyBindingResult(this, "transmissionRiskEncoding");
    this.validate(this, errorsContainer);
    if (errorsContainer.hasErrors()) {
      throw new IllegalStateException(
          "Errors while loading transmission risk level encoding configuration. Reason: "
              + StringUtils.join(errorsContainer.getAllErrors().stream().map(ObjectError::toString)
                  .collect(Collectors.toList()), ","));
    }
  }

  /**
   * Constructs the configuration class from the given mappings.
   */
  public static TransmissionRiskLevelEncoding from(
      Map<Integer, Integer> transmissionRiskLevelToDaysSinceSymptoms,
      Map<Integer, Integer> transmissionRiskLevelToReportType) {
    TransmissionRiskLevelEncoding transmissionRiskEncoding = new TransmissionRiskLevelEncoding();
    transmissionRiskEncoding.setTransmissionRiskToDaysSinceSymptoms(transmissionRiskLevelToDaysSinceSymptoms);
    transmissionRiskEncoding.setTransmissionRiskToReportType(transmissionRiskLevelToReportType);
    transmissionRiskEncoding.internalValidate();
    return transmissionRiskEncoding;
  }


  /*
   * Setters below are package protected and used by Spring to inject the maps loaded from the yaml.
   * For now this should be used because @ConstructorBinding does not work in the current setup.
   * Getters are needed for spring validation, but they return copies of the internal state.
   */

  void setTransmissionRiskToDaysSinceSymptoms(Map<Integer, Integer> transmissionRiskToDaysSinceSymptoms) {
    this.transmissionRiskToDaysSinceSymptoms = transmissionRiskToDaysSinceSymptoms;
  }

  void setTransmissionRiskToReportType(Map<Integer, Integer> transmissionRiskToReportType) {
    this.transmissionRiskToReportType = transmissionRiskToReportType;
  }

  public Map<Integer, Integer> getTransmissionRiskToDaysSinceSymptoms() {
    return new HashMap<Integer, Integer>(transmissionRiskToDaysSinceSymptoms);
  }

  public Map<Integer, Integer> getTransmissionRiskToReportType() {
    return new HashMap<Integer, Integer>(transmissionRiskToReportType);
  }
}
