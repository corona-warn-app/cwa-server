package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static app.coronawarn.server.common.shared.util.SerializationUtils.validateJsonSchema;

import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlist;
import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlistItem;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList.CertificateAllowList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DigitalCovidValidationCertificateToProtobufMapping {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DigitalCovidValidationCertificateToProtobufMapping.class);

  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = "dgc/dcc-validation-service-allowlist-rule.json";

  private final DistributionServiceConfig distributionServiceConfig;
  private final ResourceLoader resourceLoader;

  public DigitalCovidValidationCertificateToProtobufMapping(
      DistributionServiceConfig distributionServiceConfig, ResourceLoader resourceLoader) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.resourceLoader = resourceLoader;
  }

  /**
   * Validates an object (JSON) based on a provided schema containing validation rules.
   * @param allowList - object to be validated
   * @throws JsonProcessingException - if object to be validated fails on JSON processing
   * @throws ValidationException - if the validation of the object based on validation schema fails.
   */
  public boolean validateSchema(AllowList allowList) {
    try (final InputStream in = resourceLoader.getResource(DCC_VALIDATION_RULE_JSON_CLASSPATH).getInputStream()) {
      validateJsonSchema(allowList, in);
      return true;
    } catch (ValidationException e) {
      LOGGER.error("Json schema validation failed", e);
      return false;
    } catch (IOException e) {
      LOGGER.error("Could not read resource " + DCC_VALIDATION_RULE_JSON_CLASSPATH, e);
      return false;
    }
  }

  /**
   * Validates an object (JSON) based on a provided schema containing validation rules.
   */
  public boolean validateCertificate() {
    String content = distributionServiceConfig.getDigitalGreenCertificate().getAllowListAsString();
    byte[] signature = distributionServiceConfig.getDigitalGreenCertificate().getAllowListSignature();

    try {
      PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDigitalGreenCertificate().getAllowListCertificate());
      ecdsaSignatureVerification(
          signature,
          publicKey,
          content.getBytes(StandardCharsets.UTF_8));
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to validate certificate", e);
      return false;
    }
  }

  /**
   * Create the Protobuf from JSON.
   *
   * @return the protobuf filled with values from JSON.
   */
  public Optional<ValidationServiceAllowlist> constructProtobufMapping() {
    AllowList allowList = distributionServiceConfig.getDigitalGreenCertificate().getAllowList();
    if (!validateSchema(allowList) || !validateCertificate()) {
      return Optional.empty();
    }
    List<ValidationServiceAllowlistItem> validationServiceAllowlistItemList = new ArrayList<>();
    for (CertificateAllowList certificateAllowList : allowList.getCertificates()) {
      validationServiceAllowlistItemList.add(
          ValidationServiceAllowlistItem.newBuilder()
              .setHostname(certificateAllowList.getHostname())
              .setServiceProvider(certificateAllowList.getServiceProvider())
              .setFingerprint256(ByteString.copyFrom(certificateAllowList.getFingerprint256(), StandardCharsets.UTF_8))
              .build());
    }
    return Optional.of(ValidationServiceAllowlist.newBuilder()
        .addAllCertificates(validationServiceAllowlistItemList)
        .build());
  }
}
