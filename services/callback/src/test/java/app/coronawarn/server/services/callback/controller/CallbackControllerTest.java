package app.coronawarn.server.services.callback.controller;

import static app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem.EFGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.callback.ServerApplication;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, //
    classes = {ServerApplication.class, ClientCertificateTestConfig.class})
@DirtiesContext
class CallbackControllerTest {

  @Autowired
  private RequestExecutor executor;

  private final static String batchTag = "batchTag";
  private final static String validDateString = "2020-05-05";
  private final static String invalidDateString = "2020-20-20";

  @SpyBean
  FederationBatchInfoService spyFederationClient;

  @Test
  void ok() {
    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, validDateString);
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void shouldInsertBatchInfo() {
    String batchTag = UUID.randomUUID().toString().substring(0, 11);
    LocalDate date = LocalDate.now();

    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, date.toString());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);

    assertThat(spyFederationClient.findByStatus(FederationBatchStatus.UNPROCESSED, EFGS))
        .contains(new FederationBatchInfo(batchTag, date, EFGS));
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
