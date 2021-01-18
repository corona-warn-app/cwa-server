
package app.coronawarn.server.services.submission;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.federation.client.NoopHostnameVerifierProvider;
import app.coronawarn.server.services.submission.controller.SubmissionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = { NoopHostnameVerifierProvider.class })
@DirtiesContext
class ServerApplicationTests {

  @Autowired
  private SubmissionController controller;

  @MockBean
  private TestRestTemplate testRestTemplate;

  @Test
  void contextLoads() {
    assertThat(this.controller).isNotNull();
  }
}
