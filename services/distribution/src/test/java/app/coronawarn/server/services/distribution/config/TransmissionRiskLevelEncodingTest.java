package app.coronawarn.server.services.distribution.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;

class TransmissionRiskLevelEncodingTest {

  @Test
  void shouldDeriveDsosBasedOnMappingWhenProvided() {
    TransmissionRiskLevelEncoding encodings = TransmissionRiskLevelEncoding.from(
        Map.of(1,1,2,2,3,2,4,2,5,2,6,1,7,2,8,1),
        Map.of(1,1,2,2,3,3,4,4,5,1,6,2,7,3,8,4));
    assertEquals(1, encodings.getDaysSinceSymptomsForTransmissionRiskLevel(6));

    assertThrows(IllegalArgumentException.class, () -> {
      encodings.getDaysSinceSymptomsForTransmissionRiskLevel(9);
    });
  }

  @Test
  void shouldDeriveReportTypeBasedOnMappingWhenProvided() {
    TransmissionRiskLevelEncoding encodings = TransmissionRiskLevelEncoding.from(
        Map.of(1,1,2,2,3,2,4,2,5,2,6,1,7,2,8,1),
        Map.of(1,1,2,2,3,3,4,4,5,1,6,2,7,3,8,4));
    assertEquals(ReportType.CONFIRMED_TEST, encodings.getReportTypeForTransmissionRiskLevel(5));

    assertThrows(IllegalArgumentException.class, () -> {
      encodings.getReportTypeForTransmissionRiskLevel(9);
    });
  }

  @Test
  void shouldValidateTranmissionRiskLevelKeys() {
    /* Test both construction and individual method that is invoked by Spring validation */
    TransmissionRiskLevelEncoding validEncodings = TransmissionRiskLevelEncoding.from(
        Map.of(1,1,2,2,3,2,4,2,5,2,6,1,7,2,8,1),
        Map.of(1,1,2,2,3,3,4,4,5,1,6,2,7,3,8,4));
    BindingResult errorsMock = spy(BindingResult.class);
    validEncodings.validate(validEncodings, errorsMock);
    verify(errorsMock, times(0)).rejectValue(any(), any());

    /* Test construction time validation */
    assertThrows(IllegalStateException.class, () -> {
      TransmissionRiskLevelEncoding.from(Map.of(12,1,0,3), Map.of(13, 3));
    });

    /* Test method that is invoked by Spring validation */
    TransmissionRiskLevelEncoding invalidEncoding = new TransmissionRiskLevelEncoding();
    invalidEncoding.setTransmissionRiskToDaysSinceSymptoms(Map.of(12,1,0,3));
    invalidEncoding.setTransmissionRiskToReportType(Map.of(13, 3));

    validEncodings.validate(invalidEncoding, errorsMock);
    verify(errorsMock, times(2)).rejectValue(any(), any());
  }
}
