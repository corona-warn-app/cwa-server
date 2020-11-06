package app.coronawarn.server.common.persistence.domain.config;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TekFieldDerivationsTest {

  @Test
  void shouldDeriveTrlBasedOnMappingWhenProvided() {
    TekFieldDerivations derivations = TekFieldDerivations.from(Map.of(), Map.of(14, 5), 3);
    Assertions.assertEquals(5, derivations.deriveTransmissionRiskLevelFromDaysSinceSymptoms(14));
  }

  @Test
  void shouldDeriveTrlBasedOnDefaultWhenMappingNotAvailable() {
    TekFieldDerivations derivations = TekFieldDerivations.from(Map.of(), Map.of(14, 5), 3);
    Assertions.assertEquals(3, derivations.deriveTransmissionRiskLevelFromDaysSinceSymptoms(15));
  }

  @Test
  void shouldDeriveDsosBasedOnMappingWhenProvided() {
    TekFieldDerivations derivations = TekFieldDerivations.from(Map.of(5, 3996), Map.of(), 3);
    Assertions.assertEquals(3996, derivations.deriveDaysSinceSymptomsFromTransmissionRiskLevel(5));
  }
}
