package app.coronawarn.server.common.persistence.domain.validation;

import app.coronawarn.server.common.persistence.domain.validation.util.Range;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public enum DaysSinceSymptomsRangeSpecification {

  /**
   * As of CWA version 1.5, the range below defines the range of DSOS values accepted by the
   * Exposure Notification framework.
   */
  ExposureNotificationAcceptedRange(-14, 21, (dsos) -> 0),


  /**
   * Describes a scenario where the date when symptoms appeared was known on submission. "Days since
   * onset of Symptoms"
   */
  SymptomaticWithPreciseDate(-14, 21, (dsos) -> 0),

  /**
   * Describes a scenario where an aproximate range of days when symptoms appeared was known on
   * submission. "Days since last day of interval"
   */
  SymptomaticWithDateRange(22, 1950, (dsos) -> {
    int intervalDuration = (int) Math.round((double)dsos / 100d);
    return intervalDuration * 100;
  }),

  /**
   * Describes a scenario where a user had symptoms but not sure about the dates/ranges. "Days since
   * submission of keys"
   */
  SymptomaticWithUnknownDate(1986, 2000, (dsos) -> 2000),

  /**
   * Describes a scenario where no symptoms were present. "Days since submission of keys"
   */
  Asymptomatic(2986, 3000, (dsos) -> 3000),

  /**
   * Describes a scenario where no symptomatic information is known.
   */
  NoSymptomaticInformation(3986, 4000, (dsos) -> 4000);


  private Function<Integer, Integer> offsetComputation;
  private Range<Integer> range;

  DaysSinceSymptomsRangeSpecification(int min, int max,
      Function<Integer, Integer> offsetComputation) {
    this.range = new Range<Integer>(min, max);
    if (Objects.isNull(offsetComputation)) {
      throw new IllegalArgumentException(
          "An offset computation function must be provided in order to define a"
              + " DSOS range specification.");
    }
    this.offsetComputation = offsetComputation;
  }

  /**
   * Returns the offset as defined by the EFGS specification for embedding additional information to
   * DSOS fields. The offset is sometimes computed from the embedded information like date range of
   * symptoms, thus it will be extracted from the actual key DSOS.
   */
  public int computeOffset(Integer daysSinceSymptomsValue) {
    return offsetComputation.apply(daysSinceSymptomsValue);
  }

  /**
   * Verifies that the given value conforms to the specification.
   */
  public boolean accept(Integer daysSinceSymptomsValue) {
    return range.contains(daysSinceSymptomsValue);
  }

  public static Optional<DaysSinceSymptomsRangeSpecification> findRangeSpecification(
      Integer originalDaysSinceSymptoms) {
    return Arrays.asList(values()).stream().filter(spec -> spec.accept(originalDaysSinceSymptoms))
        .findFirst();
  }
}
