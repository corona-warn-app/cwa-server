

package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.transformation.EnfParameterAdapter;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class, TransmissionRiskLevelEncoding.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CryptoProvider.class, DistributionServiceConfig.class,
        KeySharingPoliciesChecker.class, EnfParameterAdapter.class,},
    initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeysStructureProviderTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  KeySharingPoliciesChecker sharingPoliciesChecker;

  @Autowired
  EnfParameterAdapter enfParameterAdapter;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Mock
  DiagnosisKeyService diagnosisKeyService;
  List<DiagnosisKey> diagnosisKeys;

  @BeforeEach
  void setup() {
    diagnosisKeys = IntStream.range(0, 30)
        .mapToObj(currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    Mockito.when(diagnosisKeyService.getDiagnosisKeys()).thenReturn(diagnosisKeys);
  }

  @Test
  void testGetDiagnosisKeysReturnsCorrectDirectoryName() {
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig, sharingPoliciesChecker);
    DiagnosisKeysStructureProvider diagnosisKeysStructureProvider = new DiagnosisKeysStructureProvider(
        diagnosisKeyService, cryptoProvider, distributionServiceConfig, bundler, enfParameterAdapter);
    Directory<WritableOnDisk> diagnosisKeys = diagnosisKeysStructureProvider.getDiagnosisKeys();
    Assertions.assertEquals("diagnosis-keys", diagnosisKeys.getName());
  }

}
