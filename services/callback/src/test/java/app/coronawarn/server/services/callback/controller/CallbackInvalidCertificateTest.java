package app.coronawarn.server.services.callback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import app.coronawarn.server.services.callback.ServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * This test verifies the certificate authentication of the callback service. For successful authentication, it is
 * required to use a client certificate that
 * <li>is properly signed</li>
 * <li>and has a CN that matches the env variable services.callback.efgs-cert-cn</li>
 * Using the spring profile <code>callback-change-certificate-cn</code>, this env variable is changed to one that does
 * not match the CN of the test certificates. Hence, the response code should be 403.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {ServerApplication.class, ClientCertificateTestConfig.class})
@DirtiesContext
@ActiveProfiles({"callback-change-certificate-cn"})
class CallbackInvalidCertificateTest {

  private final static String batchTag = "batchTag";
  private final static String dateString = "2020-05-05";
  @Autowired
  private RequestExecutor executor;

  @Test
  void forbidden() {
    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, dateString);
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
  }
}
