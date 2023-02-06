package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static app.coronawarn.server.common.shared.util.SerializationUtils.deserializeJson;

import app.coronawarn.server.common.protocols.internal.dgc.ServiceProviderAllowlistItem;
import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlist;
import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlistItem;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.common.shared.util.HashUtils.Algorithms;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList.CertificateAllowList;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList.ServiceProvider;
import app.coronawarn.server.services.distribution.dgc.client.JsonValidationService;
import app.coronawarn.server.services.distribution.dgc.dsc.errors.InvalidContentResponseException;
import app.coronawarn.server.services.distribution.dgc.dsc.errors.InvalidFingerprintException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
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
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.bouncycastle.util.encoders.Hex;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile("!revocation")
public class DigitalCovidValidationCertificateToProtobufMapping {

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class ServiceProviderDto {

    @JsonProperty("providers")
    private List<String> providers;

    public List<String> getProviders() {
      return providers;
    }

    public void setProviders(final List<String> providers) {
      this.providers = providers;
    }
  }

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DigitalCovidValidationCertificateToProtobufMapping.class);

  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = "dgc/dcc-validation-service-allowlist-rule.json";

  private final DistributionServiceConfig distributionServiceConfig;
  private final ResourceLoader resourceLoader;

  private final JsonValidationService jsonValidationService;

  /**
   * I'm responsible for mapping certificate allow lists to their corresponding protobuf definition.
   *
   * @param distributionServiceConfig service config.
   * @param resourceLoader            resource loader.
   */
  public DigitalCovidValidationCertificateToProtobufMapping(
      final DistributionServiceConfig distributionServiceConfig, final ResourceLoader resourceLoader,
      final JsonValidationService jsonValidationService) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.resourceLoader = resourceLoader;
    this.jsonValidationService = jsonValidationService;
  }

  private ServiceProviderDto buildServiceProviderDto(final CloseableHttpClient httpClient, final HttpGet getMethod,
      final ObjectMapper objectMapper) throws InvalidFingerprintException {
    try (CloseableHttpResponse response = httpClient.execute(getMethod)) {
      return buildServiceProviderDto(objectMapper, response.getEntity());
    } catch (final Exception e) {
      LOGGER.warn("Request to obtain the service providers failed: ", e);
      throw new InvalidFingerprintException();
    }
  }

  private ServiceProviderDto buildServiceProviderDto(final ObjectMapper objectMapper, final HttpEntity response)
      throws InvalidFingerprintException {
    try {
      final ServiceProviderDto serviceProviderDto = objectMapper.readValue(
          response.getContent(),
          ServiceProviderDto.class);
      if (serviceProviderDto.getProviders() == null) {
        throw new InvalidContentResponseException();
      }
      return serviceProviderDto;
    } catch (final Exception e) {
      LOGGER.error("Failed to build Service Provider: Could not extract providers from response");
      throw new InvalidFingerprintException();
    }
  }

  /**
   * Create the Protobuf from JSON.
   *
   * @return the protobuf filled with values from JSON.
   */
  public Optional<ValidationServiceAllowlist> constructProtobufMapping() {
    final String allowListJson = distributionServiceConfig.getDigitalGreenCertificate().getAllowListAsString();

    if (!validateSchema(allowListJson) || !validateCertificate()) {
      return Optional.empty();
    }

    final AllowList allowList = deserializeJson(allowListJson,
        typeFactory -> typeFactory.constructType(AllowList.class));

    final List<ServiceProviderAllowlistItem> serviceProviderAllowlistItems = new ArrayList<>();
    final List<ValidationServiceAllowlistItem> validationServiceAllowlistItemList = new ArrayList<>();
    final ObjectMapper objectMapper = new ObjectMapper();

    for (final ServiceProvider serviceProvider : allowList.getServiceProviders()) {
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
              .build())
          .collect(Collectors.toList());
      serviceProviderAllowlistItems.addAll(serviceProviderItems);
    }

    for (final CertificateAllowList certificateAllowList : allowList.getCertificates()) {
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

  private HttpClientConnectionManager getConnectionManager(final String fingerPrintToCompare) {
    return PoolingHttpClientConnectionManagerBuilder.create()
        .setSSLSocketFactory(getSslSocketFactory(fingerPrintToCompare))
        .build();
  }

  private LayeredConnectionSocketFactory getSslSocketFactory(final String fingerPrintToCompare) {
    final SSLConnectionSocketFactoryBuilder builder = SSLConnectionSocketFactoryBuilder.create()
        .setTlsVersions(TLS.V_1_3, TLS.V_1_2);
    // builder.setSslContext(getSslContext(getKeyStore(), getKeyStorePassword()));
    builder.setHostnameVerifier((hostname, session) -> validateHostname(session, fingerPrintToCompare));
    return builder.build();
  }

  private boolean matches(final Certificate cert, final String fingerPrintToCompare) {
    Optional<String> fingerprint = Optional.empty();
    try {
      fingerprint = Optional.of(Hex.toHexString(HashUtils.byteStringDigest(cert.getEncoded(), Algorithms.SHA_256)));
    } catch (final CertificateEncodingException e) {
      LOGGER.error("Certificate Pinning failed: certificate could not be encoded.");
    }
    return fingerprint.map(it -> it.equalsIgnoreCase(fingerPrintToCompare)).orElse(false);
  }

  /**
   * Validates an object (JSON) based on a provided schema containing validation rules.
   */
  public boolean validateCertificate() {
    final String content = distributionServiceConfig.getDigitalGreenCertificate().getAllowListAsString();
    final byte[] signature = distributionServiceConfig.getDigitalGreenCertificate().getAllowListSignature();

    try {
      final PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDigitalGreenCertificate().getAllowListCertificate());
      ecdsaSignatureVerification(
          signature,
          publicKey,
          content.getBytes(StandardCharsets.UTF_8));
      return true;
    } catch (final Exception e) {
      LOGGER.error("Failed to validate certificate", e);
      return false;
    }
  }

  private ServiceProviderDto validateFingerprint(final String serviceProviderAllowlistEndpoint,
      final String fingerPrintToCompare, final ObjectMapper objectMapper) {
    try (CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(getConnectionManager(fingerPrintToCompare))
        .build()) {
      final HttpGet getMethod = new HttpGet(serviceProviderAllowlistEndpoint);
      return buildServiceProviderDto(httpClient, getMethod, objectMapper);
    } catch (final Exception e) {
      LOGGER.warn(e.getMessage(), e);
      final ServiceProviderDto dto = new ServiceProviderDto();
      dto.setProviders(Collections.emptyList());
      return dto;
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
    } catch (final SSLPeerUnverifiedException e) {
      LOGGER.error(
          "Constructing ValidationServiceAllowlistItem failed: "
              + "certificate fingerprint {} does not match fingerprint of leaf certificate of validation server",
          fingerPrintToCompare, e);
    }
    return peerCertificate.map(it -> matches(it, fingerPrintToCompare)).orElse(false);
  }

  /**
   * Validates an object (JSON) based on a provided schema containing validation rules.
   *
   * @param allowList - object to be validated
   * @throws JsonProcessingException - if object to be validated fails on JSON processing
   * @throws ValidationException     - if the validation of the object based on validation schema fails.
   */
  public boolean validateSchema(final String allowList) {
    try (final InputStream allowListSchemaAsStream = resourceLoader.getResource(DCC_VALIDATION_RULE_JSON_CLASSPATH)
        .getInputStream()) {
      final InputStream allowListAsStream = new ByteArrayInputStream(allowList.getBytes());
      jsonValidationService.validateJsonAgainstSchema(allowListAsStream, allowListSchemaAsStream);
      return true;
    } catch (ValidationException | JSONException e) {
      LOGGER.error("Json schema validation failed", e);
      return false;
    } catch (final Exception e) {
      LOGGER.error("Could not read resource " + DCC_VALIDATION_RULE_JSON_CLASSPATH, e);
      return false;
    }
  }
}
