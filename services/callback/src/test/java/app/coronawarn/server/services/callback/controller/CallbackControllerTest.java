

package app.coronawarn.server.services.callback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
@DirtiesContext
class CallbackControllerTest {

  @Autowired
  private RequestExecutor executor;

  private final static String batchTag = "batchTag";
  private final static String validDateString = "2020-05-05";
  private final static String invalidDateString = "2020-20-20";

  @Test
  void ok() {
    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, validDateString);
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @ParameterizedTest
  @MethodSource("createTestString")
  void failsWithBadRequest(String batchTag, String dateString) {
    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, dateString);
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private static Stream<Arguments> createTestString() {
    return Stream.of(
        Arguments.of(batchTag, null),
        Arguments.of(batchTag, ""),
        Arguments.of("", ""),
        Arguments.of(null, null),
        Arguments.of(null, validDateString),
        Arguments.of("", validDateString),
        Arguments.of(batchTag, invalidDateString)
    );
  }
}
