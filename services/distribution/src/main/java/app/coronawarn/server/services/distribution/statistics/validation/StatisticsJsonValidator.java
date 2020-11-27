package app.coronawarn.server.services.distribution.statistics.validation;

import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsJsonValidator {

  private static final Logger logger = LoggerFactory.getLogger(StatisticsJsonValidator.class);

  /**
   * Validates mandatory fields on {@link StatisticsJsonStringObject}.
   * @param statisticsObjects the parsed JSON Object.
   * @return A list with only valid objects.
   */
  public List<StatisticsJsonStringObject> validate(List<StatisticsJsonStringObject> statisticsObjects) {
    List<StatisticsJsonStringObject> statisticsJsonStringObjects = new ArrayList<>();
    statisticsObjects.forEach(statisticsObject -> {
      if (isValidEffectiveDate(statisticsObject)) {
        statisticsJsonStringObjects.add(statisticsObject);
      }
    });
    return statisticsJsonStringObjects;
  }

  private boolean isValidEffectiveDate(StatisticsJsonStringObject statisticsJsonStringObject) {
    String effectiveDate = statisticsJsonStringObject.getEffectiveDate();

    if (Objects.isNull(effectiveDate)) {
      logger.warn("The effective_date attribute must not be null.");
      return false;
    }
    return checkValidEffectiveDateFormat(effectiveDate);
  }

  private boolean checkValidEffectiveDateFormat(String effectiveDate) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate.parse(effectiveDate, formatter);
    } catch (Exception e) {
      logger.warn("The value of the effective_date attribute is not correct.");
      return false;
    }
    return true;
  }
}
