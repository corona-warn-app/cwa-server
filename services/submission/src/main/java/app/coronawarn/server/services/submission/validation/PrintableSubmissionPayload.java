package app.coronawarn.server.services.submission.validation;

import static org.springframework.util.ObjectUtils.isEmpty;

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
   * 
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
    if (isEmpty(keys)) {
      stringBuilder.append(" keys: are empty!");
    } else {
      stringBuilder.append(" " + keys.size() + " keys: ");
    }
    for (TemporaryExposureKey key : keys) {
      stringBuilder
          .append("{")
          .append("data: HIDDEN")
          .append(",trl: ").append(key.getTransmissionRiskLevel())
          .append(",rsin: ").append(key.getRollingStartIntervalNumber())
          .append(",type: ").append(key.getReportType())
          .append(",dsoos: ").append(key.getDaysSinceOnsetOfSymptoms())
          .append("},");
    }
    return stringBuilder.toString();
  }

}
