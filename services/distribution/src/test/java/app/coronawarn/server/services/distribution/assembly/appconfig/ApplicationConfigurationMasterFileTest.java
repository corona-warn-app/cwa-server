

package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ApplicationConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfigurationPublicationConfig.class,
    initializers = ConfigFileApplicationContextInitializer.class)
class ApplicationConfigurationMasterFileTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  @Test
  void testMasterFile() {
    var validator = new ApplicationConfigurationValidator(applicationConfiguration);
    ValidationResult result = validator.validate();

    assertThat(result).isEqualTo(SUCCESS);
  }
}
