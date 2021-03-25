package app.coronawarn.server.services.eventregistration.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    assertThat(underTest.getVersion()).isEqualTo(5);
  }
}
