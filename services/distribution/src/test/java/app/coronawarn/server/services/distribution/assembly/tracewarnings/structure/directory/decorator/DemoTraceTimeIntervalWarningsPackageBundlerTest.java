package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import static app.coronawarn.server.services.distribution.common.Helpers.buildTraceTimeIntervalWarning;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DemoDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.DemoTraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, DemoTraceTimeIntervalWarningsPackageBundler.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("demo")
public class DemoTraceTimeIntervalWarningsPackageBundlerTest {

  @Autowired
  private TraceTimeIntervalWarningsPackageBundler bundler;

  @Test
  void testGetsTraceLocationWarningsForHour() {
    List<TraceTimeIntervalWarning> diagnosisKeys = Stream
        .of(buildTraceTimeIntervalWarning(6, 50, 5, 5),
            buildTraceTimeIntervalWarning(6, 50, 5, 5),
            buildTraceTimeIntervalWarning(6, 50, 5,5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setTraceTimeIntervalWarnings(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getTraceTimeWarningsForHour(5)).hasSize(15);
  }

  @Test
  void testGetHoursTraceLocationWarningsForCountry() {
    List<TraceTimeIntervalWarning> diagnosisKeys = Stream
        .of(buildTraceTimeIntervalWarning(6, 50, 5, 5),
            buildTraceTimeIntervalWarning(6, 50, 6, 5),
            buildTraceTimeIntervalWarning(6, 50, 7,5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setTraceTimeIntervalWarnings(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getHoursForDistributableWarnings("DE")).hasSize(3);
  }
}
