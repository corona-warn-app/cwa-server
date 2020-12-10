package app.coronawarn.server.services.callback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification-verify-hostname"})
@DirtiesContext
class CallbackControllerWithoutCertificatesTest {

  @Autowired
  private RequestExecutor executor;

  @SpyBean
  FederationBatchInfoService spyFederationClient;

  @Test
  void noCertificateShouldBeForbidden() {
    String batchTag = UUID.randomUUID().toString().substring(0, 11);
    LocalDate date = LocalDate.now();

    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, date.toString());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);

    assertThat(spyFederationClient.findByStatus(FederationBatchStatus.UNPROCESSED))
        .doesNotContain(new FederationBatchInfo(batchTag, date));
  }
}
