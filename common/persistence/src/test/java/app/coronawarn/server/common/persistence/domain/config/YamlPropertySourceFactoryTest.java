package app.coronawarn.server.common.persistence.domain.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class YamlPropertySourceFactoryTest {

  @Autowired
  private TekFieldDerivations tekDerivations;

  @Autowired
  private YamlPropertySourceFactory propertySourceFactory;


  @Test
  void test() {
    assertNotNull(tekDerivations);
    assertNotNull(propertySourceFactory);

    assertThat( tekDerivations.getDaysSinceSymptomsFromTransmissionRiskLevel()).isNotEmpty();
    assertThat( tekDerivations.getTransmissionRiskLevelFromDaysSinceSymptoms()).isNotEmpty();
  }
}
