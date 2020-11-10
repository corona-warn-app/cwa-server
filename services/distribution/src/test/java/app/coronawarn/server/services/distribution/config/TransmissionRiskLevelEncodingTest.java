package app.coronawarn.server.services.distribution.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;

class TransmissionRiskLevelEncodingTest {

  private static final TransmissionRiskLevelEncoding VALID_ENCODINGS = TransmissionRiskLevelEncoding.from(
      Map.of(1,1,2,2,3,2,4,2,5,2,6,1,7,2,8,1),
      Map.of(1,1,2,2,3,3,4,4,5,1,6,2,7,3,8,4));

  @Test
  void shouldDeriveDsosBasedOnMappingWhenProvided() {
    assertEquals(1, VALID_ENCODINGS.getDaysSinceSymptomsForTransmissionRiskLevel(6));

    assertThrows(IllegalArgumentException.class, () -> {
      VALID_ENCODINGS.getDaysSinceSymptomsForTransmissionRiskLevel(9);
    });
  }

  @Test
  void shouldDeriveReportTypeBasedOnMappingWhenProvided() {
    assertEquals(ReportType.CONFIRMED_TEST, VALID_ENCODINGS.getReportTypeForTransmissionRiskLevel(5));

    assertThrows(IllegalArgumentException.class, () -> {
      VALID_ENCODINGS.getReportTypeForTransmissionRiskLevel(9);
    });
  }

  @Test
  void shouldValidateTransmissionRiskLevelKeys() {
    /* Test construction time validation passes for valid values */
    BindingResult errorsMock = spy(BindingResult.class);
    VALID_ENCODINGS.validate(VALID_ENCODINGS, errorsMock);
    verify(errorsMock, times(0)).rejectValue(any(), any());

    /* Test construction time validation with invalid TRLs */
    Map<Integer, Integer> m1 = Map.of(12, 1, 0, 3);
    Map<Integer, Integer> m2 = Map.of(13, 3);
    assertThrows(IllegalArgumentException.class, () -> TransmissionRiskLevelEncoding.from(m1, m2));

    /* Test method that is invoked by Spring validation */
    TransmissionRiskLevelEncoding invalidEncoding = new TransmissionRiskLevelEncoding();
    invalidEncoding.setTransmissionRiskToDaysSinceSymptoms(Map.of(12, 1, 0, 2));
    invalidEncoding.setTransmissionRiskToReportType(Map.of(13, 3));

    VALID_ENCODINGS.validate(invalidEncoding, errorsMock);
    verify(errorsMock, times(2)).rejectValue(any(), any(), any());
  }

  @Test
  void shouldValidateDaysSinceSymptomsValues() {

    Map<Integer, Integer> invalidDsosMap = Map.of(1, 3, 2, 4, 3, 2, 4, 2, 5, 2, 6, 1, 7, 2, 8, 1);

    /* Test construction time validation */
    Map<Integer, Integer> validTransmissionRiskToReportType = VALID_ENCODINGS.getTransmissionRiskToReportType();
    assertThrows(IllegalArgumentException.class, () -> TransmissionRiskLevelEncoding.from(
        invalidDsosMap,
        validTransmissionRiskToReportType));

    /* Test method that is invoked by Spring validation */
    TransmissionRiskLevelEncoding invalidEncoding = new TransmissionRiskLevelEncoding();
    invalidEncoding.setTransmissionRiskToDaysSinceSymptoms(invalidDsosMap);
    invalidEncoding.setTransmissionRiskToReportType(validTransmissionRiskToReportType);

    BindingResult errorsMock = spy(BindingResult.class);
    VALID_ENCODINGS.validate(invalidEncoding, errorsMock);
    verify(errorsMock, times(1)).rejectValue(any(), any(), any());
  }

  @Test
  void shouldValidateReportTypeValues() {

    Map<Integer, Integer> invalidReportTypeMap = Map.of(1, -2, 2, 6, 3, 2, 4, 2, 5, 2, 6, 1, 7, 2, 8, 1);

    /* Test construction time validation */
    Map<Integer, Integer> transmissionRiskToDaysSinceSymptoms = VALID_ENCODINGS
        .getTransmissionRiskToDaysSinceSymptoms();
    assertThrows(IllegalArgumentException.class, () -> {
      TransmissionRiskLevelEncoding.from(
          transmissionRiskToDaysSinceSymptoms,
          invalidReportTypeMap);
    });

    /* Test method that is invoked by Spring validation */
    TransmissionRiskLevelEncoding invalidEncoding = new TransmissionRiskLevelEncoding();
    invalidEncoding.setTransmissionRiskToDaysSinceSymptoms(transmissionRiskToDaysSinceSymptoms);
    invalidEncoding.setTransmissionRiskToReportType(invalidReportTypeMap);

    BindingResult errorsMock = spy(BindingResult.class);
    VALID_ENCODINGS.validate(invalidEncoding, errorsMock);
    verify(errorsMock, times(1)).rejectValue(any(), any(), any());
  }
}
