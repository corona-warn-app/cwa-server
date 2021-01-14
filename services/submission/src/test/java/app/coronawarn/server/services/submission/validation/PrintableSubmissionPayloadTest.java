package app.coronawarn.server.services.submission.validation;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData;
import org.junit.jupiter.api.Test;
import java.util.List;

public class PrintableSubmissionPayloadTest {


  @Test
  void keyDataIsHidden() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("DE");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString().contains(
        submissionPayload.getKeysList().get(0).getKeyData().toStringUtf8()))
        .isFalse();
  }

  @Test
  void containsVisitedCountries() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithVisitedCountries(List.of("DE, FR"));
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString().contains(
        printableSubmissionPayload.VISITED_COUNTRIES_MESSAGE + "[DE, FR]"))
        .isTrue();
  }

  @Test
  void containsOrigin() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("FR");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString().contains(
        printableSubmissionPayload.ORIGIN_MESSAGE + "FR"))
        .isTrue();
  }

  @Test
  void containsConsentToFederation() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("FR");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString().contains(
        printableSubmissionPayload.CONSENT_MESSAGE + submissionPayload.getConsentToFederation()))
        .isTrue();
  }

  @Test
  void containsTekData() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("FR");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    TemporaryExposureKey key = submissionPayload.getKeys(0);
    String payloadString = printableSubmissionPayload.toString();

    assertThat(payloadString.contains(" " + key.getTransmissionRiskLevel() + " ")).isTrue();
    assertThat(payloadString.contains(" " + key.getRollingStartIntervalNumber() + " ")).isTrue();
    assertThat(payloadString.contains(" " + key.getReportType() +  " ")).isTrue();
    assertThat(payloadString.contains(" " + key.getDaysSinceOnsetOfSymptoms() + " ")).isTrue();
  }
}
