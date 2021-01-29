package app.coronawarn.server.services.callback.registration;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
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

  private CallbackServiceConfig serviceConfig;
  private FederationGatewayClient federationGatewayClient;

  public RegistrationRunner(CallbackServiceConfig serviceConfig, FederationGatewayClient federationGatewayClient) {
    this.serviceConfig = serviceConfig;
    this.federationGatewayClient = federationGatewayClient;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!serviceConfig.isRegisterOnStartup()) {
      return;
    }

    String endpointUrl = serviceConfig.getEndpointUrl();
    String id = computeSha256Hash(endpointUrl);

    boolean callbackUrlIsRegistered = federationGatewayClient.getCallbackRegistrations().getBody().stream()
        .anyMatch(registrationResponse -> StringUtils.equals(id, registrationResponse.getId()));
    if (callbackUrlIsRegistered) {
      return;
    }

    federationGatewayClient.putCallbackRegistration(id, endpointUrl);
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
