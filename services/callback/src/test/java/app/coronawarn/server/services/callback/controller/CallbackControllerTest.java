package app.coronawarn.server.services.callback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})

public class CallbackControllerTest {

  @Autowired
  private RequestExecutor executor;

  @Test
  void checkResponseStatusForValidParameters() {
    ResponseEntity<Void> actResponse = executor.executeGet("batchTag","2020-01-01");
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkResponseStatusForInvalidDate() {
    ResponseEntity<Void> actResponse = executor.executeGet("batchTag","2020-20-20");
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkResponseStatusForEmptyDate() {
    ResponseEntity<Void> actResponse = executor.executeGet("batchTag","");
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkResponseStatusForEmptyBatchTag() {
    ResponseEntity<Void> actResponse = executor.executeGet("","2020-01-01");
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkResponseStatusForEmptyParameters() {
    ResponseEntity<Void> actResponse = executor.executeGet("","");
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkResponseStatusForMissingDate() {
    ResponseEntity<Void> actResponse = executor.executeGet("batchTag",null);
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkResponseStatusForMissingBatchTag() {
    ResponseEntity<Void> actResponse = executor.executeGet(null,"2020-01-01");
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  @Test
  void checkResponseStatusForMissingParameters() {
    ResponseEntity<Void> actResponse = executor.executeGet(null,null);
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }
}
