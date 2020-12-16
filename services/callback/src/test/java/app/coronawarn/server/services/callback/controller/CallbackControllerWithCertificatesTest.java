
package app.coronawarn.server.services.callback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.callback.ServerApplication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, //
    classes = { ServerApplication.class, ClientCertificateTestConfig.class })
@DirtiesContext
class CallbackControllerWithCertificatesTest {

  @Autowired
  private RequestExecutor executor;

  @SpyBean
  FederationBatchInfoService spyFederationClient;

  @Test
  void shouldInsertBatchInfoWithCertificate() throws Exception {
    String batchTag = UUID.randomUUID().toString().substring(0, 11);
    LocalDate date = LocalDate.now();

    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, date.toString());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);

    assertThat(spyFederationClient.findByStatus(FederationBatchStatus.UNPROCESSED))
        .contains(new FederationBatchInfo(batchTag, date));
  }
}
