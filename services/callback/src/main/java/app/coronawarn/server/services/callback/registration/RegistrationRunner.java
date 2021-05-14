package app.coronawarn.server.services.callback.registration;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import org.apache.commons.lang3.StringUtils;
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
      logger.info("Callback registration on startup is disabled. See 'CALLBACK_REGISTER_ON_STARTUP'");
      return;
    }

    String endpointUrl = serviceConfig.getEndpointUrl();
    String registrationId = HashUtils.md5DigestAsHex(endpointUrl);
    logger.info("Starting callback registration for ID '{}' URL '{}'.", registrationId, endpointUrl);

    boolean callbackUrlIsAlreadyRegistered = federationGatewayClient.getCallbackRegistrations().getBody().stream()
        .anyMatch(registrationResponse -> StringUtils.equals(registrationId, registrationResponse.getId()));
    if (callbackUrlIsAlreadyRegistered) {
      logger.info("Callback with id '{}' was already registered.", registrationId);
      return;
    }

    federationGatewayClient.putCallbackRegistration(registrationId, endpointUrl);
    logger.info("Callback with id '{}' and URL '{}' registered successfully.", registrationId, endpointUrl);
  }
}
