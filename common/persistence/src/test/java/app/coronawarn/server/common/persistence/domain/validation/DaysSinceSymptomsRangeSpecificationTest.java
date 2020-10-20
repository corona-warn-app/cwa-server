package app.coronawarn.server.common.persistence.domain.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DaysSinceSymptomsRangeSpecificationTest {

  @ParameterizedTest
  @ValueSource(ints = {190, 1000, 2000, 3000, 4000})
  void testFindingRangeSpecificationByValue() {
    Optional<DaysSinceSymptomsRangeSpecification> spec = DaysSinceSymptomsRangeSpecification.findRangeSpecification(1000);
    assertTrue(spec.isPresent());
  }

  @ParameterizedTest
  @ValueSource(ints = {-14, 0, 1, 21})
  void testExposureNotificationFrameworkRangeValues(Integer value) {
    DaysSinceSymptomsRangeSpecification spec = DaysSinceSymptomsRangeSpecification.ExposureNotificationAcceptedRange;
    assertEquals(0, spec.computeOffset(value));
    assertTrue(spec.accept(value));
  }
}
