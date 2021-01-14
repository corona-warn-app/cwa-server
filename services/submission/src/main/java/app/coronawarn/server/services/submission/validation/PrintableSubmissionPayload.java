package app.coronawarn.server.services.submission.validation;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import java.util.List;

public class PrintableSubmissionPayload {

  private final String origin;
  private final ProtocolStringList visitedCountries;
  private final boolean consentToFederation;
  private final ByteString padding;
  private final List<TemporaryExposureKey> keys;

  static final String ORIGIN_MESSAGE = " payload origin: ";
  static final String VISITED_COUNTRIES_MESSAGE = " visited_countries: ";
  static final String CONSENT_MESSAGE = " consent_to_federation: ";
  static final String PADDING_MESSAGE = " with padding_size: ";

  /**
   * Creates a printable Version of SubmissionPayload the logger can work with.
   * @param submissionPayload SubmissionPayload which shall be made printable
   */
  public PrintableSubmissionPayload(SubmissionPayload submissionPayload) {
    origin = submissionPayload.getOrigin();
    visitedCountries = submissionPayload.getVisitedCountriesList();
    consentToFederation = submissionPayload.getConsentToFederation();
    padding = submissionPayload.getRequestPadding();
    keys = submissionPayload.getKeysList();
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder
        .append(ORIGIN_MESSAGE).append(origin)
        .append(VISITED_COUNTRIES_MESSAGE).append(visitedCountries)
        .append(CONSENT_MESSAGE).append(consentToFederation)
        .append(PADDING_MESSAGE).append(padding.size());
    stringBuilder.append(" keys: ");
    for (TemporaryExposureKey key : keys) {
      stringBuilder
          .append(" {")
          .append(" key_data: HIDDEN")
          .append(" transmission_risk_level: ").append(key.getTransmissionRiskLevel())
          .append(" rolling_start_interval_number: ").append(key.getRollingStartIntervalNumber())
          .append(" report_type: ").append(key.getReportType())
          .append(" days_since_onset_of_symptoms: ").append(key.getDaysSinceOnsetOfSymptoms())
          .append(" }");
    }
    return stringBuilder.toString();
  }

}
