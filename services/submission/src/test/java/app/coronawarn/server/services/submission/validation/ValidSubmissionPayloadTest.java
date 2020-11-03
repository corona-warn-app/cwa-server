package app.coronawarn.server.services.submission.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.controller.ApiExceptionHandler;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"disable-ssl-client-verification",
    "disable-ssl-client-verification-verify-hostname"})
/* This test must have the public modifier to be callable by the interpolation logic */
class ValidSubmissionPayloadTest {

  private static String interpolationSideEffect;

  @MockBean
  private ApiExceptionHandler apiExceptionHandler;

  @Captor
  private ArgumentCaptor<Exception> apiExceptionCaptor;


  @Autowired
  private RequestExecutor requestExecutor;

  @BeforeEach
  public void setup() {
    interpolationSideEffect = "";
    Mockito.doNothing().when(apiExceptionHandler).diagnosisKeyExceptions(apiExceptionCaptor.capture(), Mockito.any());
  }

  /**
   * Provide an EL expression in the protobuf message which modifies a member of this test class.
   * This string member can then be used to test whether interpolation was performed by the Java
   * Validation Framework during submission.
   */
  @Test
  void testOriginCountryConstraintViolationInterpolationIsTurnedOff() {
    SubmissionPayload payload = SubmissionPayloadMockData
        .buildPayloadWithOriginCountry("java.lang.Runtime.getRuntime().exec(\'%s\');//${"
            + "\'\'.getClass().forName(\'app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest\')"
            + ".newInstance().showInterpolationEffect()}");

    ResponseEntity<Void> response = requestExecutor.executePost(payload);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertEquals(apiExceptionCaptor.getValue().getMessage(),
        "submitDiagnosisKey.exposureKeys: Origin country java.lang.Runtime.getRuntime().exec('%s');//${''.getClass()."
            + "forName('app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest').newInstance().showInterpolationEffect()}"
            + " is not part of the supported countries list");
    assertNotEquals("INTERPOLATION_OCCURRED", interpolationSideEffect);
  }

  @Test
  void testVisitedCountryConstraintViolationInterpolationIsTurnedOff() {
    SubmissionPayload payload = SubmissionPayloadMockData
        .buildPayloadWithVisitedCountries(List.of("java.lang.Runtime.getRuntime().exec(\'%s\');//${"
            + "\'\'.getClass().forName(\'app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest\')"
            + ".newInstance().showInterpolationEffect()}"));

    ResponseEntity<Void> response = requestExecutor.executePost(payload);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertEquals(apiExceptionCaptor.getValue().getMessage(),
        "submitDiagnosisKey.exposureKeys: [java.lang.Runtime.getRuntime().exec('%s');//${''.getClass()."
            + "forName('app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest').newInstance().showInterpolationEffect()}]:"
            + " Visited country is not part of the supported countries list");
    assertNotEquals("INTERPOLATION_OCCURRED", interpolationSideEffect);
  }

  public static void showInterpolationEffect() {
    interpolationSideEffect = "INTERPOLATION_OCCURRED";
  }
}
