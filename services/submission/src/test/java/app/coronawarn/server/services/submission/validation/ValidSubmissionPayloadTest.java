package app.coronawarn.server.services.submission.validation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"disable-ssl-client-verification",
    "disable-ssl-client-verification-verify-hostname"})
/* This test must have the public modifier to be callable by the interpolation logic */
public class ValidSubmissionPayloadTest {

  private static String interpolationSideEffect;

  @Autowired
  private RequestExecutor requestExecutor;

  @BeforeEach
  public void setup() {
    interpolationSideEffect = "";
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

    requestExecutor.executePost(payload);
    assertNotEquals("INTERPOLATION_OCCURRED", interpolationSideEffect);
  }

  @Test
  void testVisitedCountryConstraintViolationInterpolationIsTurnedOff() {
    SubmissionPayload payload = SubmissionPayloadMockData
        .buildPayloadWithVisitedCountries(List.of("java.lang.Runtime.getRuntime().exec(\'%s\');//${"
            + "\'\'.getClass().forName(\'app.coronawarn.server.services.submission.validation.ValidSubmissionPayloadTest\')"
            + ".newInstance().showInterpolationEffect()}"));

    requestExecutor.executePost(payload);
    assertNotEquals("INTERPOLATION_OCCURRED", interpolationSideEffect);
  }

  public static void showInterpolationEffect() {
    interpolationSideEffect = "INTERPOLATION_OCCURRED";
  }
}
