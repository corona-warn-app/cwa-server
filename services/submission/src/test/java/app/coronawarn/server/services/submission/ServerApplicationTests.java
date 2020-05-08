package app.coronawarn.server.services.submission;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.coronawarn.server.services.submission.controller.SubmissionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServerApplicationTests {

  @Autowired
  private SubmissionController controller;

  @Test
  public void contextLoads() {
    assertNotNull(this.controller);
  }
}
