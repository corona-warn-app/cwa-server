package app.coronawarn.server.services.callback.registration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.federation.client.callback.RegistrationResponse;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CallbackRegistrationRunnerTest {

  @Test
  void testCallbackRegistrationDisabled() {
    CallbackServiceConfig callbackServiceConfig = mock(CallbackServiceConfig.class);
    when(callbackServiceConfig.isRegisterOnStartup()).thenReturn(false);

    FederationGatewayClient federationGatewayClient = mock(FederationGatewayClient.class);

    RegistrationRunner registrationRunner = new RegistrationRunner(callbackServiceConfig, federationGatewayClient);
    registrationRunner.run(null);

    verify(callbackServiceConfig, times(1)).isRegisterOnStartup();
    verify(callbackServiceConfig, times(0)).getEndpointUrl();
    verify(federationGatewayClient, times(0)).getCallbackRegistrations();
    verify(federationGatewayClient, times(0)).putCallbackRegistration(any(), any());
  }

  @Test
  void testCallbackRegistrationAlreadyDone() {
    String endpointUrl = "url";

    CallbackServiceConfig callbackServiceConfig = mock(CallbackServiceConfig.class);
    when(callbackServiceConfig.isRegisterOnStartup()).thenReturn(true);
    when(callbackServiceConfig.getEndpointUrl()).thenReturn(endpointUrl);

    FederationGatewayClient federationGatewayClient = mock(FederationGatewayClient.class);
    when(federationGatewayClient.getCallbackRegistrations())
        .thenReturn(
            new ResponseEntity<>(List.of(new RegistrationResponse(HashUtils.md5DigestAsHex(endpointUrl), endpointUrl)),
                HttpStatus.OK));

    RegistrationRunner registrationRunner = new RegistrationRunner(callbackServiceConfig, federationGatewayClient);
    registrationRunner.run(null);

    verify(callbackServiceConfig, times(1)).isRegisterOnStartup();
    verify(callbackServiceConfig, times(1)).getEndpointUrl();
    verify(federationGatewayClient, times(1)).getCallbackRegistrations();
    verify(federationGatewayClient, times(0)).putCallbackRegistration(any(), any());
  }

  @Test
  void testCallbackRegistrationSuccessful() {
    String endpointUrl = "url";

    CallbackServiceConfig callbackServiceConfig = mock(CallbackServiceConfig.class);
    when(callbackServiceConfig.isRegisterOnStartup()).thenReturn(true);
    when(callbackServiceConfig.getEndpointUrl()).thenReturn(endpointUrl);

    FederationGatewayClient federationGatewayClient = mock(FederationGatewayClient.class);
    when(federationGatewayClient.getCallbackRegistrations())
        .thenReturn(
            new ResponseEntity<>(List.of(new RegistrationResponse(HashUtils.md5DigestAsHex("id"), "other-url")),
                HttpStatus.OK));

    RegistrationRunner registrationRunner = new RegistrationRunner(callbackServiceConfig, federationGatewayClient);
    registrationRunner.run(null);

    verify(callbackServiceConfig, times(1)).isRegisterOnStartup();
    verify(callbackServiceConfig, times(1)).getEndpointUrl();
    verify(federationGatewayClient, times(1)).getCallbackRegistrations();
    verify(federationGatewayClient, times(1))
        .putCallbackRegistration(HashUtils.md5DigestAsHex(endpointUrl), endpointUrl);
  }

}
