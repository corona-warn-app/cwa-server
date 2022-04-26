package app.coronawarn.server.common.persistence;

import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.repository.CheckInProtectedReportsRepository;
import app.coronawarn.server.common.persistence.repository.DccRevocationEtagRepository;
import app.coronawarn.server.common.persistence.repository.DccRevocationListRepository;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.repository.StatisticsDownloadRepository;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;
import app.coronawarn.server.common.persistence.utils.YamlPropertySourceFactory;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("removal")
@SpringBootApplication
@Configuration
public class TestApplication {

  @Bean
  DccRevocationListService createDccRevocationListService(final DccRevocationListRepository repository,
      final DccRevocationEtagRepository etagRepository) {
    return new DccRevocationListService(repository, etagRepository);
  }

  @Bean
  DiagnosisKeyService createDiagnosisKeyService(final DiagnosisKeyRepository keyRepository) {
    return new DiagnosisKeyService(keyRepository, validDiagnosisKeyFilter());
  }

  @Bean
  FederationBatchInfoService createFederationBatchInfoService(
      final FederationBatchInfoRepository federationBatchInfoRepository) {
    return new FederationBatchInfoService(federationBatchInfoRepository);
  }

  @Bean
  FederationUploadKeyRepository createFederationUploadKeyRepository() {
    return Mockito.mock(FederationUploadKeyRepository.class);
  }

  @Bean
  FederationUploadKeyService createFederationUploadKeyService() {
    return new FederationUploadKeyService(createFederationUploadKeyRepository(), validDiagnosisKeyFilter(),
        keySharingPoliciesChecker());
  }

  @Bean
  StatisticsDownloadService createStatisticsDownloadService(final StatisticsDownloadRepository repository) {
    return new StatisticsDownloadService(repository);
  }

  @Bean
  KeySharingPoliciesChecker keySharingPoliciesChecker() {
    return new KeySharingPoliciesChecker();
  }

  @Bean
  YamlPropertySourceFactory propertySourceFactory() {
    return new YamlPropertySourceFactory();
  }

  @Bean
  TekFieldDerivations tekFieldDerivations() {
    return TekFieldDerivations.from(Map.of(3997, 8), Map.of(8, 3997), 3);
  }

  @Bean
  TraceTimeIntervalWarningService traceTimeIntervalWarningService(
      final TraceTimeIntervalWarningRepository timeIntervalWarningRepository,
      final CheckInProtectedReportsRepository checkInProtectedReportsRepository) throws NoSuchAlgorithmException {
    return new TraceTimeIntervalWarningService(timeIntervalWarningRepository, checkInProtectedReportsRepository);
  }

  @Bean
  ValidDiagnosisKeyFilter validDiagnosisKeyFilter() {
    return new ValidDiagnosisKeyFilter();
  }
}
