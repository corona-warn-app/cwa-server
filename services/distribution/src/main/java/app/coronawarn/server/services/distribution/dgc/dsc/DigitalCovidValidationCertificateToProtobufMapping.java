package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static app.coronawarn.server.common.shared.util.SerializationUtils.validateJsonSchema;

import app.coronawarn.server.common.protocols.internal.dgc.ServiceProviderAllowlistItem;
import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlist;
import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlistItem;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.common.shared.util.HashUtils.Algorithms;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList.CertificateAllowList;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList.ServiceProvider;
import app.coronawarn.server.services.distribution.dgc.dsc.errors.InvalidContentResponseException;
import app.coronawarn.server.services.distribution.dgc.dsc.errors.InvalidFingerprintException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.util.encoders.Hex;
import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DigitalCovidValidationCertificateToProtobufMapping {

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class ServiceProviderDto {

    @JsonProperty("providers")
    private List<String> providers;

    public List<String> getProviders() {
      return providers;
    }

    public void setProviders(List<String> providers) {
      this.providers = providers;
    }
  }

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DigitalCovidValidationCertificateToProtobufMapping.class);

  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = "dgc/dcc-validation-service-allowlist-rule.json";

  private final DistributionServiceConfig distributionServiceConfig;
  private final ResourceLoader resourceLoader;

  /**
   * I'm responsible for mapping certificate allow lists to their corresponding protobuf definition.
   *
   * @param distributionServiceConfig service config.
   * @param resourceLoader            resource loader.
   */
  public DigitalCovidValidationCertificateToProtobufMapping(
      DistributionServiceConfig distributionServiceConfig, ResourceLoader resourceLoader) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.resourceLoader = resourceLoader;
  }

  /**
   * Validates an object (JSON) based on a provided schema containing validation rules.
   *
   * @param allowList - object to be validated
   * @throws JsonProcessingException - if object to be validated fails on JSON processing
   * @throws ValidationException     - if the validation of the object based on validation schema fails.
   */
  public boolean validateSchema(AllowList allowList) {
    try (final InputStream in = resourceLoader.getResource(DCC_VALIDATION_RULE_JSON_CLASSPATH).getInputStream()) {
      validateJsonSchema(allowList, in);
      return true;
    } catch (ValidationException e) {
      LOGGER.error("Json schema validation failed", e);
      return false;
    } catch (Exception e) {
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
    List<ServiceProviderAllowlistItem> serviceProviderAllowlistItems = new ArrayList<>();
    List<ValidationServiceAllowlistItem> validationServiceAllowlistItemList = new ArrayList<>();
    final ObjectMapper objectMapper = new ObjectMapper();

    for (ServiceProvider serviceProvider : allowList.getServiceProviders()) {
      // 1. Fetch corresponding endpoint for retrieving providers
      final String serviceProviderAllowlistEndpoint = serviceProvider
          .getServiceProviderAllowlistEndpoint();

      // 2. Validate certificate fingerprint with fingerprint of leaf certificate of server
      final ServiceProviderDto serviceProviderDto = validateFingerprint(
          serviceProviderAllowlistEndpoint,
          serviceProvider.getFingerprint256(),
          objectMapper);

      // 2.1. Map each of the fetched providers to a ServiceProviderAllowListItem and add to total List.
      final List<ServiceProviderAllowlistItem> serviceProviderItems = serviceProviderDto.getProviders()
          .stream()
          .map(provider -> ServiceProviderAllowlistItem
              .newBuilder()
              .setServiceIdentityHash(
                  ByteString.copyFrom(Hex.decode(provider)))
              .build()).collect(Collectors.toList());
      serviceProviderAllowlistItems.addAll(serviceProviderItems);
    }

    for (CertificateAllowList certificateAllowList : allowList.getCertificates()) {
      // 3. Map certificates to ValidationServiceAllowlistItem
      validationServiceAllowlistItemList.add(
          ValidationServiceAllowlistItem.newBuilder()
              .setHostname(certificateAllowList.getHostname())
              .setServiceProvider(certificateAllowList.getServiceProvider())
              .setFingerprint256(ByteString.copyFrom(Hex.decode(certificateAllowList.getFingerprint256())))
              .build());
    }

    // 4. Add total list of ValidationServiceAllowlistItem's and ServiceProviderAllowListItem's to final protobuf
    return Optional.of(ValidationServiceAllowlist.newBuilder()
        .addAllCertificates(validationServiceAllowlistItemList)
        .addAllServiceProviders(serviceProviderAllowlistItems)
        .build());
  }

  private ServiceProviderDto validateFingerprint(String serviceProviderAllowlistEndpoint,
      String fingerPrintToCompare, final ObjectMapper objectMapper) {
    try (CloseableHttpClient httpClient = HttpClients.custom()
        .setSSLHostnameVerifier((hostname, session) -> validateHostname(
            session,
            fingerPrintToCompare))
        .build()) {
      HttpGet getMethod = new HttpGet(serviceProviderAllowlistEndpoint);
      final HttpEntity httpEntity = executeRequest(httpClient, getMethod);
      return buildServiceProviderDto(objectMapper, httpEntity);
    } catch (Exception e) {
      LOGGER.warn(e.getMessage(), e);
      ServiceProviderDto dto = new ServiceProviderDto();
      dto.setProviders(Collections.emptyList());
      return dto;
    }
  }

  private ServiceProviderDto buildServiceProviderDto(ObjectMapper objectMapper, HttpEntity response)
      throws InvalidFingerprintException {
    try {
      ServiceProviderDto serviceProviderDto = objectMapper.readValue(
          response.getContent(),
          ServiceProviderDto.class);
      if (serviceProviderDto.getProviders() == null) {
        throw new InvalidContentResponseException();
      }
      return serviceProviderDto;
    } catch (Exception e) {
      LOGGER.error("Failed to build Service Provider: Could not extract providers from response");
      throw new InvalidFingerprintException();
    }
  }

  private boolean validateHostname(final SSLSession session, final String fingerPrintToCompare) {
    Optional<Certificate> peerCertificate = Optional.empty();
    try {
      final Certificate[] peerCertificates = session.getPeerCertificates();
      if (peerCertificates.length <= 0) {
        return false;
      }
      peerCertificate = Optional.ofNullable(peerCertificates[0]);
    } catch (SSLPeerUnverifiedException e) {
      LOGGER.error(
          "Constructing ValidationServiceAllowlistItem failed: "
              + "certificate fingerprint {} does not match fingerprint of leaf certificate of validation server",
          fingerPrintToCompare, e);
    }
    return peerCertificate.map(it -> matches(it, fingerPrintToCompare)).orElse(false);
  }

  private boolean matches(final Certificate cert, final String fingerPrintToCompare) {
    Optional<String> fingerprint = Optional.empty();
    try {
      fingerprint = Optional.of(Hex.toHexString(HashUtils.byteStringDigest(cert.getEncoded(), Algorithms.SHA_256))
          .toLowerCase());
    } catch (CertificateEncodingException e) {
      LOGGER.error("Certificate Pinning failed: certificate could not be encoded.");
    }
    return fingerprint.map(it -> it.equals(fingerPrintToCompare.toLowerCase())).orElse(false);
  }

  private HttpEntity executeRequest(CloseableHttpClient httpClient, HttpGet getMethod)
      throws InvalidFingerprintException {
    try {
      final CloseableHttpResponse response = httpClient.execute(getMethod);
      return response.getEntity();
    } catch (Exception e) {
      LOGGER.warn("Request to obtain the service providers failed: ", e);
      throw new InvalidFingerprintException();
    }
  }
}
