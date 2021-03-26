package app.coronawarn.server.services.eventregistration.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static app.coronawarn.server.services.eventregistration.testdata.TestData.correctVersion;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EventRegistrationConfigurationTest {


  @Autowired
  private EventRegistrationConfiguration underTest;

  @Test
  public void configurationShouldBeCreated() {
    assertThat(underTest).isNotNull();
  }

  @Test
  public void versionShouldNotBeNull() {
    assertThat(underTest.getVersion()).isNotNull();
    assertThat(underTest.getVersion()).isEqualTo(correctVersion);
  }
}
