package app.coronawarn.server.services.distribution.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.component.StatisticsStructureProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@EnableConfigurationProperties(value = {DistributionServiceConfig.class, TransmissionRiskLevelEncoding.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class StatisticsStructureProviderTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;


  @Test
  void testGetDiagnosisKeysReturnsCorrectDirectoryName() {
    StatisticsStructureProvider statisticsStructureProvider = new StatisticsStructureProvider(
        cryptoProvider, distributionServiceConfig, Statistics.newBuilder().build());
    assertThat(statisticsStructureProvider).isNotNull();
  }
}
