package app.coronawarn.server.common.persistence.domain.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class TrlMappingDerivationTest {

  @Test
  void shouldDerivedTrlMappingsBasedOnApplicationConfig() {
    TrlDerivations trlDerivations = new TrlDerivations();
    trlDerivations.setTrlMapping(Map.of(1, 4, 2, 3, 3, 2, 4, 1));

    assertThat(trlDerivations.mapFromTrlSubmittedToTrlToStore(1)).isEqualTo(4);
    assertThat(trlDerivations.mapFromTrlSubmittedToTrlToStore(2)).isEqualTo(3);
    assertThat(trlDerivations.mapFromTrlSubmittedToTrlToStore(3)).isEqualTo(2);
    assertThat(trlDerivations.mapFromTrlSubmittedToTrlToStore(4)).isEqualTo(1);
  }
}
