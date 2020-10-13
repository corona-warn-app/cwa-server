

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, DemoDiagnosisKeyBundler.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("demo")
class DemoDiagnosisKeyBundlerTest {

  @Autowired
  DiagnosisKeyBundler bundler;

  @Test
  void testGetsAllDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 5),
            buildDiagnosisKeys(6, 51L, 5),
            buildDiagnosisKeys(6, 52L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(15);
  }

  @Test
  void testIfOriginCountryKeyIsPartOfEuPackage(){
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 1))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(1);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(1);
  }
}
