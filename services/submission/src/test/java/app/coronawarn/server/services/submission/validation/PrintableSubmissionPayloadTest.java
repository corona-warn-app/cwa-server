package app.coronawarn.server.services.submission.validation;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PrintableSubmissionPayloadTest {


  @Test
  void keyDataIsHidden() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("DE");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString()).doesNotContain(
        submissionPayload.getKeysList().get(0).getKeyData().toStringUtf8());
  }

  @Test
  void containsVisitedCountries() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithVisitedCountries(List.of("DE, FR"));
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString()).contains(
        PrintableSubmissionPayload.VISITED_COUNTRIES_MESSAGE + "[DE, FR]");
  }

  @Test
  void containsOrigin() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("FR");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString()).contains(
        PrintableSubmissionPayload.ORIGIN_MESSAGE + "FR");
  }

  @Test
  void containsConsentToFederation() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("FR");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    assertThat(printableSubmissionPayload.toString()).contains(
        PrintableSubmissionPayload.CONSENT_MESSAGE + submissionPayload.getConsentToFederation());
  }

  @Test
  void containsTekData() {
    SubmissionPayload submissionPayload = SubmissionPayloadMockData.buildPayloadWithOriginCountry("FR");
    PrintableSubmissionPayload printableSubmissionPayload = new PrintableSubmissionPayload(submissionPayload);
    TemporaryExposureKey key = submissionPayload.getKeys(0);
    String payloadString = printableSubmissionPayload.toString();

    assertThat(payloadString).contains(" " + key.getTransmissionRiskLevel() + " ");
    assertThat(payloadString).contains(" " + key.getRollingStartIntervalNumber() + " ");
    assertThat(payloadString).contains(" " + key.getReportType() +  " ");
    assertThat(payloadString).contains(" " + key.getDaysSinceOnsetOfSymptoms() + " ");
  }
}
