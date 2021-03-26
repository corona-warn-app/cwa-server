

package app.coronawarn.server.common.persistence;

import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.repository.StatisticsDownloadRepository;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;
import java.util.Map;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class TestApplication {

  @Bean
  ValidDiagnosisKeyFilter validKeysFilter() {
    return new ValidDiagnosisKeyFilter();
  }

  @Bean
  KeySharingPoliciesChecker keySharingPoliciesChecker() {
    return new KeySharingPoliciesChecker();
  }

  @Bean
  DiagnosisKeyService createDiagnosisKeyService(DiagnosisKeyRepository keyRepository) {
    return new DiagnosisKeyService(keyRepository, validKeysFilter());
  }

  @Bean
  FederationUploadKeyRepository createFederationUploadKeyRepository() {
    return Mockito.mock(FederationUploadKeyRepository.class);
  }

  @Bean
  FederationUploadKeyService createFederationUploadKeyService() {
    return new FederationUploadKeyService(createFederationUploadKeyRepository(), validKeysFilter(),
        keySharingPoliciesChecker());
  }

  @Bean
  FederationBatchInfoService createFederationBatchInfoService(FederationBatchInfoRepository federationBatchInfoRepository) {
    return new FederationBatchInfoService(federationBatchInfoRepository);
  }

  @Bean
  StatisticsDownloadService createStatisticsDownloadService(StatisticsDownloadRepository repository) {
    return new StatisticsDownloadService(repository);
  }

  @Bean
  TraceTimeIntervalWarningService traceTimeWarningService(TraceTimeIntervalWarningRepository timeIntervalWarningRepository) {
    return new TraceTimeIntervalWarningService(timeIntervalWarningRepository);
  }

  @Bean
  TekFieldDerivations tekFieldDerivations() {
    return TekFieldDerivations.from(Map.of(3997, 8), Map.of(8, 3997), 3);
  }

  @Bean
  YamlPropertySourceFactory propertySourceFactory() {
    return new YamlPropertySourceFactory();
  }
}
