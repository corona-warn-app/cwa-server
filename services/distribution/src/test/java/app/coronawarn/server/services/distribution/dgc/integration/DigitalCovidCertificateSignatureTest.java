package app.coronawarn.server.services.distribution.dgc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import app.coronawarn.server.common.shared.util.SecurityUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignHttpClientProvider;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateSignatureException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import feign.RetryableException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDigitalCovidCertificateClient.class,
    CloudDccFeignClientConfiguration.class, CloudDccFeignHttpClientProvider.class, ApacheHttpTestConfiguration.class,
    DccSignatureValidator.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@ActiveProfiles("dcc-client-factory")
public class DigitalCovidCertificateSignatureTest {

  @Autowired
  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Test
  public void shouldThrowRuntimeExceptionWhenPublicKeyIsWrong() {
    try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
      utilities.when(() -> SecurityUtils.ecdsaSignatureVerification(any(), any(), any()))
          .thenThrow(NoSuchAlgorithmException.class)
          .thenThrow(SignatureException.class);

      Exception runtime = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(runtime.getCause()).isInstanceOf(DigitalCovidCertificateSignatureException.class);

      Exception secondException = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(secondException.getCause()).isInstanceOf(RetryableException.class);
      assertThat(secondException.getCause().getCause()).isInstanceOf(IOException.class);
      assertThat(secondException.getCause().getCause().getCause()).isInstanceOf(SignatureException.class);
    }
  }

  @Test
  public void shouldRetryAndThrowCheckedException() {
    try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
      utilities.when(() -> SecurityUtils.getPublicKeyFromString(any()))
          .thenThrow(NoSuchAlgorithmException.class)
          .thenThrow(InvalidKeySpecException.class);

      Exception exception = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(exception.getCause()).isInstanceOf(DigitalCovidCertificateSignatureException.class);

      Exception secondException = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(secondException.getCause()).isInstanceOf(DigitalCovidCertificateSignatureException.class);
    }
  }
}
