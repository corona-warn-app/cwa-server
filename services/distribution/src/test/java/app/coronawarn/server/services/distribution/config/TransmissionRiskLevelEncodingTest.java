package app.coronawarn.server.services.distribution.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

class TransmissionRiskLevelEncodingTest {

  @Test
  void shouldDeriveDsosBasedOnMappingWhenProvided() {
    TransmissionRiskLevelEncoding encodings = TransmissionRiskLevelEncoding.from(Map.of(6, 2), Map.of());
    assertEquals(2, encodings.getDaysSinceSymptomsForTransmissionRiskLevel(6));

    assertThrows(IllegalArgumentException.class, () -> {
      encodings.getDaysSinceSymptomsForTransmissionRiskLevel(1);
    });
  }

  @Test
  void shouldDeriveReportTypeBasedOnMappingWhenProvided() {
    TransmissionRiskLevelEncoding encodings = TransmissionRiskLevelEncoding.from(Map.of(), Map.of(5, 3));
    assertEquals(3, encodings.getReportTypeForTransmissionRiskLevel(5));

    assertThrows(IllegalArgumentException.class, () -> {
      encodings.getReportTypeForTransmissionRiskLevel(1);
    });
  }

  @Test
  void shouldValidateTranmissionRiskLevelKeys() {
    TransmissionRiskLevelEncoding encodings = TransmissionRiskLevelEncoding.from(Map.of(1,1,2,3), Map.of(5, 3));
    BindingResult errorsMock = spy(BindingResult.class);
    encodings.validate(encodings, errorsMock);
    verify(errorsMock, times(0)).rejectValue(any(), any());

    encodings = TransmissionRiskLevelEncoding.from(Map.of(12,1,0,3), Map.of(13, 3));
    encodings.validate(encodings, errorsMock);
    verify(errorsMock, times(2)).rejectValue(any(), any());
  }
}
