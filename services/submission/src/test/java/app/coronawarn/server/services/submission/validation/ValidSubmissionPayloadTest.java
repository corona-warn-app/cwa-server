package app.coronawarn.server.services.submission.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SecurityConfig;
import app.coronawarn.server.services.submission.controller.ApiExceptionHandler;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

/**
 * This test must have the public modifier to be callable by the interpolation logic.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ExtendWith(MockitoExtension.class)
public class ValidSubmissionPayloadTest {

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
   * Provide an EL expression in the protobuf message which modifies a member of this test class. This string member can
   * then be used to test whether interpolation was performed by the Java Validation Framework during submission.
   *
   * @see SecurityConfig#defaultValidator()
   * @see ValidSubmissionPayload#addViolation(ConstraintValidatorContext, String)
   */
  @Test
  void testOriginCountryConstraintViolationInterpolationIsTurnedOff() {
    SubmissionPayload payload = SubmissionPayloadMockData
        .buildPayloadWithOriginCountry("java.lang.Runtime.getRuntime().exec(\'%s\');//${\'\'.getClass().forName(\'"
            + ValidSubmissionPayloadTest.class.getName() + "\').newInstance().showInterpolationEffect()}");

    ResponseEntity<Void> response = requestExecutor.executePost(payload);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertEquals(apiExceptionCaptor.getValue().getMessage(),
        "submitDiagnosisKey.exposureKeys: Origin country java.lang.Runtime.getRuntime().exec('%s');//${''.getClass()."
            + "forName('app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest').newInstance().showInterpolationEffect()}"
            + " is not part of the supported countries list");
    assertNotEquals("INTERPOLATION_OCCURRED", interpolationSideEffect);
  }

  /**
   * <a href="https://securitylab.github.com/research/securing-the-fight-against-covid19-through-oss">Securing the fight
   * against COVID-19 through open source</a>.
   *
   * <a href="https://www.coronawarn.app/de/blog/2020-11-19-security-update>Sicherheitsupdate für
   * Corona-Warn-App-Server</a>
   *
   * @see SecurityConfig#defaultValidator()
   * @see ValidSubmissionPayload#addViolation(ConstraintValidatorContext, String)
   */
  @Test
  void testVisitedCountryConstraintViolationInterpolationIsTurnedOff() {
    SubmissionPayload payload = SubmissionPayloadMockData.buildPayloadWithVisitedCountries(
        List.of("java.lang.Runtime.getRuntime().exec(\'%s\');//${\'\'.getClass().forName(\'"
            + ValidSubmissionPayloadTest.class.getName() + "\').newInstance().showInterpolationEffect()}"));

    ResponseEntity<Void> response = requestExecutor.executePost(payload);
    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertEquals(apiExceptionCaptor.getValue().getMessage(),
        "submitDiagnosisKey.exposureKeys: [java.lang.Runtime.getRuntime().exec('%s');//${''.getClass()."
            + "forName('app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest').newInstance().showInterpolationEffect()}]:"
            + " Visited country is not part of the supported countries list");
    assertNotEquals("INTERPOLATION_OCCURRED", interpolationSideEffect);
  }

  /**
   * When this test fails, please verify that the two others do really want they should do: test if we can inject code
   * and execute via submission payload.
   *
   * @throws Exception - if method is not found
   * @see SecurityConfig#defaultValidator()
   * @see ValidSubmissionPayload#addViolation(ConstraintValidatorContext, String)
   */
  @Test
  void testThisClassDeclaration() throws Exception {
    Class<?> clazz = Class.forName(ValidSubmissionPayloadTest.class.getName());
    assertTrue(Modifier.isPublic(clazz.getModifiers()), ValidSubmissionPayloadTest.class.getName()
        + " is not public, but it has to be public for correct test execution.");
    Method showInterpolationEffect = clazz.getMethod("showInterpolationEffect");
    assertTrue(Modifier.isPublic(showInterpolationEffect.getModifiers()),
        "Method 'showInterpolationEffect' isn't public");
    assertTrue(Modifier.isStatic(showInterpolationEffect.getModifiers()),
        "Method 'showInterpolationEffect' isn't static");
  }

  public static void showInterpolationEffect() {
    interpolationSideEffect = "INTERPOLATION_OCCURRED";
  }
}
