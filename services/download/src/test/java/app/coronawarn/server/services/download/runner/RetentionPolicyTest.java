package app.coronawarn.server.services.download.runner;

import static app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem.EFGS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DownloadServiceConfig.class, TekFieldDerivations.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RetentionPolicy.class}, initializers = ConfigDataApplicationContextInitializer.class)
@DirtiesContext
@ActiveProfiles("connect-efgs")
class RetentionPolicyTest {

  @MockBean
  FederationBatchInfoService federationBatchInfoService;

  @Autowired
  DownloadServiceConfig downloadServiceConfig;

  @Autowired
  RetentionPolicy retentionPolicy;

  @Test
  void shouldCallService() {
    retentionPolicy.run(null);

    verify(federationBatchInfoService, times(1))
        .applyRetentionPolicy(downloadServiceConfig.getRetentionDays(), EFGS);
  }
}
