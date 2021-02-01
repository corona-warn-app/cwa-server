package app.coronawarn.server.services.callback.registration;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner registers the callback service at the EFGS.
 */
@Component
@Order(1)
public class RegistrationRunner implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(RegistrationRunner.class);
  private CallbackServiceConfig serviceConfig;
  private FederationGatewayClient federationGatewayClient;

  public RegistrationRunner(CallbackServiceConfig serviceConfig, FederationGatewayClient federationGatewayClient) {
    this.serviceConfig = serviceConfig;
    this.federationGatewayClient = federationGatewayClient;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!serviceConfig.isRegisterOnStartup()) {
      logger.info("Callback registration on startup was disabled.");
      return;
    }

    String endpointUrl = serviceConfig.getEndpointUrl();
    String registrationId = computeSha256Hash(endpointUrl);

    boolean callbackUrlIsAlreadyRegistered = federationGatewayClient.getCallbackRegistrations().getBody().stream()
        .anyMatch(registrationResponse -> StringUtils.equals(registrationId, registrationResponse.getId()));
    if (callbackUrlIsAlreadyRegistered) {
      logger.info("Callback for id '" + registrationId + "' and URL '" + endpointUrl + "' was already registered.");
      return;
    }

    federationGatewayClient.putCallbackRegistration(registrationId, endpointUrl);
    logger.info("Callback for id '" + registrationId + "' and URL '" + endpointUrl + "' registered successfully.");
  }

  private String computeSha256Hash(String subject) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Hash algorithm not found.");
    }
    byte[] hash = digest.digest(
        subject.getBytes(StandardCharsets.UTF_8));
    return new String(Hex.encode(hash));
  }
}
