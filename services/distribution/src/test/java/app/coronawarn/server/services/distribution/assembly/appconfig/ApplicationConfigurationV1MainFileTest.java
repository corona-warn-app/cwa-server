package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfigurationV2PublicationConfig.class,
    initializers = ConfigFileApplicationContextInitializer.class)
class ApplicationConfigurationV1MainFileTest {

  @Autowired
  @Qualifier("applicationConfigurationV1Android")
  private ApplicationConfigurationAndroid applicationConfigurationAndroid;

  @Autowired
  @Qualifier("applicationConfigurationV1Ios")
  private ApplicationConfigurationIOS applicationConfigurationIos;

  @Test
  void testMainFileSAreLoadedViaAutowiring() {
    assertThat(applicationConfigurationAndroid).isNotNull();
    assertThat(applicationConfigurationIos).isNotNull();
  }
}
