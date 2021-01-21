package app.coronawarn.server.services.distribution.statistics.validation;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ContextConfiguration(classes = {StatisticsJsonValidator.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class StatisticsJsonValidatorTest {

  @Autowired
  StatisticsJsonValidator validator;

  StatisticsJsonStringObject jsonWithDate(String date) {
    var obj = new StatisticsJsonStringObject();
    obj.setEffectiveDate(date);
    return obj;
  }

  StatisticsJsonStringObject jsonWithoutDate() {
    return new StatisticsJsonStringObject();
  }

  @Test
  void shouldSelectAllPropertiesWithEffectiveDate() {
    var result = validator.validate(asList(
        jsonWithDate("2021-01-01"),
        jsonWithDate("2021-01-02"),
        jsonWithDate("2021-01-03")
    ));
    assertThat(result).hasSize(3);
  }

  @Test
  void shouldFilterOutObjectsWithoutEffectiveDate() {
    var result = validator.validate(asList(
        jsonWithDate("2021-01-01"),
        jsonWithDate("2021-01-02"),
        jsonWithoutDate()
    ));
    assertThat(result).hasSize(2);
  }

  @Test
  void shouldFilterOutObjectWithWrongEffectiveDate() {
    var result = validator.validate(asList(
        jsonWithDate("2021-01-01"),
        jsonWithDate("back in the day"),
        jsonWithoutDate()
    ));
    assertThat(result).hasSize(1);
  }

}
