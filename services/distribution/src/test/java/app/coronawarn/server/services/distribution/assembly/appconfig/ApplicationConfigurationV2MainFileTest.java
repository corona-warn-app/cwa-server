package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.config.PreDistributionTrlValueMappingProvider;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.common.protocols.internal.v2.DGCBlocklistParameters;
import app.coronawarn.server.common.protocols.internal.v2.DGCTestCertificateParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationConfigurationV2PublicationConfig.class,
    PreDistributionTrlValueMappingProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class ApplicationConfigurationV2MainFileTest {

  @Autowired
  @Qualifier("applicationConfigurationV2Android")
  private ApplicationConfigurationAndroid applicationConfigurationAndroid;

  @Autowired
  @Qualifier("applicationConfigurationV2Ios")
  private ApplicationConfigurationIOS applicationConfigurationIos;

  @Test
  void testMainFilesAreLoadedViaAutowiring() {
    assertThat(applicationConfigurationAndroid).isNotNull();
    assertThat(applicationConfigurationIos).isNotNull();
  }

  @Nested
  public class DgcParametersTest {

    @Test
    public void testDgcParametersAreValid() {
      assertThat(applicationConfigurationAndroid).isNotNull();
      assertThat(applicationConfigurationAndroid.getDgcParameters()).isNotNull();
      assertThat(applicationConfigurationAndroid.getDgcParameters().getTestCertificateParameters()).isNotNull();
      assertThat(applicationConfigurationAndroid.getDgcParameters().getExpirationThresholdInDays()).isEqualTo(14);

      assertThat(applicationConfigurationIos).isNotNull();
      assertThat(applicationConfigurationIos.getDgcParameters()).isNotNull();
      assertThat(applicationConfigurationIos.getDgcParameters().getTestCertificateParameters()).isNotNull();
      assertThat(applicationConfigurationIos.getDgcParameters().getExpirationThresholdInDays()).isEqualTo(14);

      final DGCTestCertificateParameters testCertificateParameters = applicationConfigurationAndroid.getDgcParameters()
          .getTestCertificateParameters();
      assertThat(testCertificateParameters.getWaitAfterPublicKeyRegistrationInSeconds()).isEqualTo(10);
      assertThat(testCertificateParameters.getWaitForRetryInSeconds()).isEqualTo(10);
      assertThat(testCertificateParameters.getWaitForRetryInSeconds()).isEqualTo(10);

      final DGCTestCertificateParameters testCertificateParametersIos = applicationConfigurationIos.getDgcParameters()
          .getTestCertificateParameters();
      assertThat(testCertificateParametersIos.getWaitAfterPublicKeyRegistrationInSeconds()).isEqualTo(10);
      assertThat(testCertificateParametersIos.getWaitForRetryInSeconds()).isEqualTo(10);
      assertThat(testCertificateParametersIos.getWaitForRetryInSeconds()).isEqualTo(10);

      /* given: blocked-uvci-chunks: ${DGC_BLOCKED_UVCI_CHUNKS:[{"indices":[0],
       "hash":"9B09CAFEC0A6808411C348880C9C2D920646DFB980B5C959DC6EBF8A19B98120",
       "validFrom":1636040446},{"indices":[0,2],
       "hash":"7D5D5B336E903086D64D1207EC6E957A4B1301026699011026F84A5156317C2B","validFrom":1951642095}]},
       in test/application.yaml

       when: one entry is not valid

       then: should return only one BlockedUvciChunk
       */
      final DGCBlocklistParameters dgcBlocklistParameters =
          applicationConfigurationAndroid.getDgcParameters().getBlockListParameters();
      assertThat(dgcBlocklistParameters.getBlockedUvciChunksCount()).isEqualTo(1);

      final DGCBlocklistParameters dgcBlocklistParametersIos =
          applicationConfigurationIos.getDgcParameters().getBlockListParameters();
      assertThat(dgcBlocklistParametersIos.getBlockedUvciChunksCount()).isEqualTo(1);
    }
  }
}
