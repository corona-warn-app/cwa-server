package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

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
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDateDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
class DateIndexingDecoratorTest {

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


  @Test
  void excludesCurrentDateFromIndex() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 0, 0), 5);
    diagnosisKeyBundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    DateIndexingDecorator decorator = makeDecoratedDateDirectory(diagnosisKeyBundler);
    decorator.prepare(new ImmutableStack<>().push("DE"));

    Set<LocalDate> index = decorator.getIndex(new ImmutableStack<>().push("DE"));

    assertThat(index).contains(LocalDate.of(1970, 1, 4))
        .doesNotContain(LocalDate.of(1970, 1, 5));
  }

  @Test
  void excludesDatesThatExceedTheMaximumNumberOfKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 6, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 10, 0), 1))
        .flatMap(List::stream)
        .collect(Collectors.toList());

    Api api = mock(Api.class);
    when(api.getOriginCountry()).thenReturn("DE");

    DistributionServiceConfig svcConfig = mock(DistributionServiceConfig.class);
    when(svcConfig.getExpiryPolicyMinutes()).thenReturn(120);
    when(svcConfig.getShiftingPolicyThreshold()).thenReturn(1);
    when(svcConfig.getMaximumNumberOfKeysPerBundle()).thenReturn(1);
    when(svcConfig.getApi()).thenReturn(api);
    when(svcConfig.getSupportedCountries()).thenReturn(new String[]{"DE"});

    DiagnosisKeyBundler diagnosisKeyBundler = new ProdDiagnosisKeyBundler(svcConfig, sharingPoliciesChecker);
    diagnosisKeyBundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 4, 0, 0));

    DateIndexingDecorator decorator = makeDecoratedDateDirectory(diagnosisKeyBundler);

    decorator.prepare(new ImmutableStack<>().push("DE"));

    Set<LocalDate> index = decorator.getIndex(new ImmutableStack<>().push("DE"));
    assertThat(index).isEmpty();
  }

  private DateIndexingDecorator makeDecoratedDateDirectory(DiagnosisKeyBundler diagnosisKeyBundler) {
    return new DateIndexingDecorator(
        new DiagnosisKeysDateDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig),
        distributionServiceConfig);
  }
}
