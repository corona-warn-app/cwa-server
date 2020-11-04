package app.coronawarn.server.services.distribution.config;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.config.YamlPropertySourceFactory;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Wrapper over properties defined in <code>master-config/tranmission-risk-encoding.yaml</code>. It
 * provides convenience methods to derive properties from one another. Please see yaml file for more
 * details on the reasoning / intent of encoding this field. This class also validates its own
 * internal state at construction time as per Spring configuration class validation mechanisms.
 */
@Configuration
@ConfigurationProperties(prefix = "transmission-risk-encoding")
@PropertySource(value = "classpath:master-config/transmission-risk-encoding.yaml",
    factory = YamlPropertySourceFactory.class)
public class TransmissionRiskLevelEncoding implements Validator {


  /* 'transmissionRiskLevel' to 'daysSinceOnsetOfSymptoms' */
  @NotNull
  private Map<Integer, Integer> trlToDsos;

  /* 'transmissionRiskLevel' to 'reportType' */
  @NotNull
  private Map<Integer, Integer> trlToReportType;


  TransmissionRiskLevelEncoding() {
  }


  /**
   * Returns a mapped ENF v2 Days Since Symptoms value for the given Transmission Risk Level or
   * throws an exception if the TRL is not part of the mapping.
   */
  public Integer getDaysSinceSymptomsForTransmissionRiskLevel(Integer transmissionRiskLevel) {
    return getMappedValue(transmissionRiskLevel, trlToDsos, "daysSinceOnsetSymptoms");
  }

  /**
   * Returns a mapped ENF v2 Report Type value for the given Transmission Risk Level or throws an
   * exception if the TRL is not part of the mapping.
   */
  public ReportType getReportTypeForTransmissionRiskLevel(Integer transmissionRiskLevel) {
    return ReportType.forNumber(getMappedValue(transmissionRiskLevel, trlToReportType, "reportType"));
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
    return clazz == TransmissionRiskLevelEncoding.class;
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "trlToDsos", "trlToDsos.empty",
        "TRL to DSOS encoding map is null or empty");
    ValidationUtils.rejectIfEmpty(errors, "trlToReportType", "trlToReportType.empty",
        "TRL to RT encoding map is null or empty");

    TransmissionRiskLevelEncoding encodingMappings = (TransmissionRiskLevelEncoding) target;

    if (trlKeysNotInRange(encodingMappings.getTrlToDsos())) {
      errors.rejectValue("trlToDsos",
          "transmissionRisk to daysSinceOnsetSymptoms map contains invalid TRL");
    }
    if (trlKeysNotInRange(encodingMappings.getTrlToReportType())) {
      errors.rejectValue("trlToReportType", "transmissionRisk to reportType map contains invalid TRL");
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
    transmissionRiskEncoding.setTrlToDsos(transmissionRiskLevelToDaysSinceSymptoms);
    transmissionRiskEncoding.setTrlToReportType(transmissionRiskLevelToReportType);
    transmissionRiskEncoding.internalValidate();
    return transmissionRiskEncoding;
  }


  /*
   * Setters below are package protected and used by Spring to inject the maps loaded from the yaml.
   * For now this should be used because @ConstructorBinding does not work in the current setup.
   * Getters are needed for spring validation, but they return copies of the internal state.
   */

  void setTrlToDsos(Map<Integer, Integer> trlToDsos) {
    this.trlToDsos = trlToDsos;
  }

  void setTrlToReportType(Map<Integer, Integer> trlToRt) {
    this.trlToReportType = trlToRt;
  }

  public Map<Integer, Integer> getTrlToDsos() {
    return new HashMap<Integer, Integer>(trlToDsos);
  }

  public Map<Integer, Integer> getTrlToReportType() {
    return new HashMap<Integer, Integer>(trlToReportType);
  }
}
