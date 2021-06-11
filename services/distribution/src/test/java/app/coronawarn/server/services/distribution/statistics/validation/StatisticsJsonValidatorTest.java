package app.coronawarn.server.services.distribution.statistics.validation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ContextConfiguration(classes = {StatisticsJsonValidator.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class StatisticsJsonValidatorTest {

  static StatisticsJsonValidator<StatisticsJsonStringObject> validator;

  StatisticsJsonStringObject jsonWithDate(String date) {
    var obj = new StatisticsJsonStringObject();
    obj.setEffectiveDate(date);
    return obj;
  }

  StatisticsJsonStringObject jsonWithoutDate() {
    return new StatisticsJsonStringObject();
  }

  @BeforeAll
  static void setUpValidator() {
    validator = new StatisticsJsonValidator<>();
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
