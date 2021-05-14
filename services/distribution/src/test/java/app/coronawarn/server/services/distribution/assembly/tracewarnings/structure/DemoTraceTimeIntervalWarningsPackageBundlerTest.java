package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure;

import static app.coronawarn.server.services.distribution.common.Helpers.buildTraceTimeIntervalWarning;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.DemoTraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
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
class DemoTraceTimeIntervalWarningsPackageBundlerTest {

  @Autowired
  private TraceTimeIntervalWarningsPackageBundler bundler;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @BeforeEach
  void setup() {
    bundler = new DemoTraceTimeIntervalWarningsPackageBundler(distributionServiceConfig);
  }

  @Test
  void testGetsTraceLocationWarningsForHour() {
    List<TraceTimeIntervalWarning> warnings = Stream
        .of(buildTraceTimeIntervalWarning(6, 50, 5, 5),
            buildTraceTimeIntervalWarning(6, 50, 5, 5),
            buildTraceTimeIntervalWarning(6, 50, 5, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setTraceTimeIntervalWarnings(warnings, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getTraceTimeWarningsForHour(5)).hasSize(15);
  }

  @Test
  void testGetHoursTraceLocationWarningsForCountry() {
    List<TraceTimeIntervalWarning> warnings = Stream
        .of(buildTraceTimeIntervalWarning(6, 50, 5, 5),
            buildTraceTimeIntervalWarning(6, 50, 6, 5),
            buildTraceTimeIntervalWarning(6, 50, 7, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setTraceTimeIntervalWarnings(warnings, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getHoursForDistributableWarnings("DE")).hasSize(3);
  }

  @Test
  void should_include_current_hour() throws IOException {
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer oldestHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(10).toEpochSecond(ZoneOffset.UTC));
    Integer newestNotCurrentHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(1).toEpochSecond(ZoneOffset.UTC));
    Integer currentHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));

    List<TraceTimeIntervalWarning> traceWarnings = Stream
        .of(buildTraceTimeIntervalWarning(5, 10, oldestHour, 30),
            buildTraceTimeIntervalWarning(70, 100, newestNotCurrentHour, 30),
            buildTraceTimeIntervalWarning(90, 160, currentHour, 30))
        .flatMap(List::stream)
        .collect(Collectors.toList());

    bundler.setTraceTimeIntervalWarnings(traceWarnings, utcHour);

    assertThat(bundler.getLatestHourWithDistributableWarnings("DE")).contains(currentHour);
  }
}
