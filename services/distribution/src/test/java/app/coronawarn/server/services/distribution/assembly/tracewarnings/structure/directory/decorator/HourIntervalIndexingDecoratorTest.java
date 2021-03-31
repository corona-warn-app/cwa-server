

package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import static app.coronawarn.server.services.distribution.common.Helpers.buildTraceTimeIntervalWarning;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.utils.CheckinsDateSpecification;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class HourIntervalIndexingDecoratorTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  TraceTimeIntervalWarningsPackageBundler traceTimeIntervalWarningsPackageBundler;


  @BeforeEach
  void setup() {
    traceTimeIntervalWarningsPackageBundler = new TraceTimeIntervalWarningsPackageBundler(distributionServiceConfig);
  }

  @AfterEach
  void tearDown() {
    TimeUtils.setNow(null);
  }

  @Test
  void testIndexContainsOnlyOneValue() {
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer submissionHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));
    List<TraceTimeIntervalWarning> traceWarnings =
        buildTraceTimeIntervalWarning(5, 10, submissionHour, 30);

    Api api = mock(Api.class);
    when(api.getOriginCountry()).thenReturn("DE");

    DistributionServiceConfig svcConfig = mock(DistributionServiceConfig.class);
    when(svcConfig.getExpiryPolicyMinutes()).thenReturn(120);
    when(svcConfig.getShiftingPolicyThreshold()).thenReturn(1);
    when(svcConfig.getApi()).thenReturn(api);
    when(svcConfig.getSupportedCountries()).thenReturn(new String[]{"DE"});

    traceTimeIntervalWarningsPackageBundler.setTraceTimeIntervalWarnings(traceWarnings, utcHour);

    HourIntervalIndexingDecorator decorator = makeDecoratedHourDirectory(traceTimeIntervalWarningsPackageBundler);

    decorator.prepare(new ImmutableStack<>().push("DE"));
    Set<Integer> index = decorator.getIndex(new ImmutableStack<>().push("DE"));
    assertThat(index.size()).isEqualTo(1);
    assertThat(index.contains(submissionHour));
  }

  @Test
  void testIndicesAreOldestAndLatestForMultipleSubmissions() {
    LocalDateTime utcHour = TimeUtils.getCurrentUtcHour();
    Integer submissionHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.toEpochSecond(ZoneOffset.UTC));
    Integer additionalSubmissionHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(10).toEpochSecond(ZoneOffset.UTC));
    Integer anotherSubmissionHour = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(utcHour.minusHours(6).toEpochSecond(ZoneOffset.UTC));
    List<TraceTimeIntervalWarning> traceWarnings =
        buildTraceTimeIntervalWarning(5, 10, submissionHour, 30);
    List<TraceTimeIntervalWarning> additionalTraceWarnings =
        buildTraceTimeIntervalWarning(5, 10, additionalSubmissionHour, 30);
    List<TraceTimeIntervalWarning> anotherTraceWarnings =
        buildTraceTimeIntervalWarning(5, 10, anotherSubmissionHour, 30);

    traceWarnings.addAll(additionalTraceWarnings);
    traceWarnings.addAll(anotherTraceWarnings);
    Api api = mock(Api.class);
    when(api.getOriginCountry()).thenReturn("DE");

    DistributionServiceConfig svcConfig = mock(DistributionServiceConfig.class);
    when(svcConfig.getExpiryPolicyMinutes()).thenReturn(120);
    when(svcConfig.getShiftingPolicyThreshold()).thenReturn(1);
    when(svcConfig.getApi()).thenReturn(api);
    when(svcConfig.getSupportedCountries()).thenReturn(new String[]{"DE"});

    traceTimeIntervalWarningsPackageBundler.setTraceTimeIntervalWarnings(traceWarnings, utcHour);

    HourIntervalIndexingDecorator decorator = makeDecoratedHourDirectory(traceTimeIntervalWarningsPackageBundler);

    decorator.prepare(new ImmutableStack<>().push("DE"));
    Set<Integer> index = decorator.getIndex(new ImmutableStack<>().push("DE"));
    assertThat(index.size()).isEqualTo(2);
    assertThat(index.contains(submissionHour));
    assertThat(index.contains(additionalSubmissionHour));
  }

  private HourIntervalIndexingDecorator makeDecoratedHourDirectory(
      TraceTimeIntervalWarningsPackageBundler traceTimeIntervalWarningsPackageBundler) {
    return new HourIntervalIndexingDecorator(
        new TraceTimeIntervalWarningsHourDirectory(traceTimeIntervalWarningsPackageBundler, cryptoProvider,
            distributionServiceConfig),
        traceTimeIntervalWarningsPackageBundler,
        distributionServiceConfig);
  }
}
