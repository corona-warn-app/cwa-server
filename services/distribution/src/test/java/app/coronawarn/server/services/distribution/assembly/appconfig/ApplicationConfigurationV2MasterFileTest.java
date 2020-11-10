package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.v2.ApplicationConfigurationAndroidValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.v2.ApplicationConfigurationIosValidator;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfigurationV2PublicationConfig.class,
    initializers = ConfigFileApplicationContextInitializer.class)
class ApplicationConfigurationV2MasterFileTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Autowired
  private ApplicationConfigurationAndroid applicationConfigurationAndroid;

  @Autowired
  private ApplicationConfigurationIOS applicationConfigurationIos;

  @Test
  void testMasterFile() {
    assertMasterConfigIsValid(new ApplicationConfigurationAndroidValidator(applicationConfigurationAndroid));
    assertMasterConfigIsValid(new ApplicationConfigurationIosValidator(applicationConfigurationIos));
  }

  private void assertMasterConfigIsValid(ConfigurationValidator configValidator) {
    ValidationResult result = configValidator.validate();
    assertThat(result).isEqualTo(SUCCESS);
  }
}
