package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("testdata")
public class DiagnosisKeyGenerator implements DiagnosisKeyLoader {

  private final Random random = new Random();

  private DiagnosisKey generateKey(int ignoredValue) {
    byte[] randomKeyData = new byte[16];
    random.nextBytes(randomKeyData);
    return DiagnosisKey.builder()
        .withKeyData(randomKeyData)
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(1)
        .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
        .withConsentToFederation(true)
        .withCountryCode("DE")
        .withVisitedCountries(List.of("DE"))
        .build();
  }

  @Override
  public List<DiagnosisKey> loadDiagnosisKeys() {
    return IntStream.rangeClosed(0, 250)
        .mapToObj(this::generateKey)
        .collect(Collectors.toList());
  }
}
