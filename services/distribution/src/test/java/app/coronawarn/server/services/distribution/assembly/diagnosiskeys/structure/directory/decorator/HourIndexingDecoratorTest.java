package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import static app.coronawarn.server.common.shared.util.TimeUtils.setNow;
import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import java.time.LocalDate;
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
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class,
    KeySharingPoliciesChecker.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class HourIndexingDecoratorTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  KeySharingPoliciesChecker sharingPoliciesChecker;

  @Autowired
  CryptoProvider cryptoProvider;

  private DiagnosisKeyBundler diagnosisKeyBundler;

  @BeforeEach
  void setup() {
    diagnosisKeyBundler = new ProdDiagnosisKeyBundler(distributionServiceConfig, sharingPoliciesChecker);
  }

  @AfterEach
  void tearDown() {
    setNow(null);
  }

  @Test
  void excludesHoursThatExceedTheMaximumNumberOfKeysPerBundle() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 4, 0), 2);

    setNow(LocalDateTime.of(1970, 1, 3, 0, 0).toInstant(ZoneOffset.UTC));

    Api api = mock(Api.class);
    when(api.getOriginCountry()).thenReturn("DE");

    DistributionServiceConfig svcConfig = mock(DistributionServiceConfig.class);
    when(svcConfig.getExpiryPolicyMinutes()).thenReturn(120);
    when(svcConfig.getShiftingPolicyThreshold()).thenReturn(1);
    when(svcConfig.getMaximumNumberOfKeysPerBundle()).thenReturn(1);
    when(svcConfig.getApi()).thenReturn(api);
    when(svcConfig.getSupportedCountries()).thenReturn(new String[]{"DE"});

    DiagnosisKeyBundler diagnosisKeyBundler = new ProdDiagnosisKeyBundler(svcConfig, sharingPoliciesChecker);
    diagnosisKeyBundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 5, 0));

    HourIndexingDecorator decorator = makeDecoratedHourDirectory(diagnosisKeyBundler);

    decorator.prepare(new ImmutableStack<>().push("DE").push(LocalDate.of(1970, 1, 3)));
    Set<LocalDateTime> index = decorator.getIndex(new ImmutableStack<>().push("DE").push(LocalDate.of(1970, 1, 3)));

    assertThat(index).isEmpty();
  }

  @Test
  void excludesEmptyHoursFromIndex() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 5, 0, 0), 5);

    setNow(LocalDateTime.of(1970, 1, 5, 1, 0).toInstant(ZoneOffset.UTC));

    diagnosisKeyBundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 1, 0));
    HourIndexingDecorator decorator = makeDecoratedHourDirectory(diagnosisKeyBundler);
    decorator.prepare(new ImmutableStack<>().push("DE").push(LocalDate.of(1970, 1, 5)));

    Set<LocalDateTime> index = decorator.getIndex(new ImmutableStack<>().push("DE").push(LocalDate.of(1970, 1, 5)));

    assertThat(index).contains(LocalDateTime.of(1970, 1, 5, 0, 0))
        .doesNotContain(LocalDateTime.of(1970, 1, 5, 1, 0));
  }

  private HourIndexingDecorator makeDecoratedHourDirectory(DiagnosisKeyBundler diagnosisKeyBundler) {
    return new HourIndexingDecorator(
        new DiagnosisKeysHourDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig),
        distributionServiceConfig);
  }
}
