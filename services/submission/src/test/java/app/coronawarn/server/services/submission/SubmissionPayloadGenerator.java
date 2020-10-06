package app.coronawarn.server.services.submission;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;

import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubmissionPayloadGenerator {

  private static final int numberOfKeys = 10;
  private static final int transmissionRiskLevel = 6;
  private static final int rollingPeriod = 144; // 24*60/10
  private static final ReportType reportType = ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
  private static final ByteString requestPadding = ByteString.copyFrom(new byte[100]);
  private static final List<String> visitedCountries = List.of("DE", "FR");
  private static final String originCountry = "DE";
  private static final boolean consentToFederation = true;
  private static final int daysSinceOnsetOfSymptoms = 0;

  private static final String MOBILE_CLIENT_PAYLOAD_PB_PATH =
      "services/submission/src/test/resources/payload/mobile-client-payload.pb";

  public static void main(String[] args) throws IOException {

    LocalDateTime todayMidnight = LocalDateTime.now().toLocalDate().atStartOfDay();
    LocalDateTime todayMidnightMinusNumberOfKeys = todayMidnight.minusDays(numberOfKeys);

    List<TemporaryExposureKey> temporaryExposureKeys = buildTemporaryExposureKeys(numberOfKeys,
        todayMidnightMinusNumberOfKeys,
        transmissionRiskLevel, rollingPeriod, reportType, daysSinceOnsetOfSymptoms);
    SubmissionPayload submissionPayload = buildSubmissionPayload(temporaryExposureKeys, requestPadding,
        visitedCountries, originCountry, consentToFederation);

    SubmissionPayloadGenerator submissionPayloadGenerator = new SubmissionPayloadGenerator();
    submissionPayloadGenerator.writeSubmissionPayloadProtobufFile(submissionPayload);
  }

  public void writeSubmissionPayloadProtobufFile(SubmissionPayload submissionPayload) throws IOException {
    File file = new File(MOBILE_CLIENT_PAYLOAD_PB_PATH);
    file.createNewFile();
    submissionPayload
        .writeTo(new FileOutputStream(MOBILE_CLIENT_PAYLOAD_PB_PATH));
  }

  public static SubmissionPayload buildSubmissionPayload(List<TemporaryExposureKey> temporaryExposureKeys,
      ByteString requestPadding,
      List<String> visitedCountries, String originCountry, boolean consentToFederation) {

    return SubmissionPayload.newBuilder()
        .addAllKeys(temporaryExposureKeys)
        .setRequestPadding(requestPadding)
        .addAllVisitedCountries(visitedCountries)
        .setOrigin(originCountry)
        .setConsentToFederation(consentToFederation)
        .build();
  }

  public static List<TemporaryExposureKey> buildTemporaryExposureKeys(int numberOfKeys, LocalDateTime todayMidnight,
      int transmissionRiskLevel, int rollingPeriod, ReportType reportType, int daysSinceOnsetOfSymptoms) {
    List<TemporaryExposureKey> temporaryExposureKeys = new ArrayList<>();

    for (int i = 0; i < numberOfKeys; i++) {
      byte[] keyData = new byte[16];
      Random random = new Random();
      random.nextBytes(keyData);

      TemporaryExposureKey temporaryExposureKey = TemporaryExposureKey.newBuilder()
          .setKeyData(ByteString.copyFrom(keyData))
          .setTransmissionRiskLevel(transmissionRiskLevel)
          .setRollingStartIntervalNumber((int) todayMidnight.toEpochSecond(ZoneOffset.UTC) / 600 + rollingPeriod * i)
          .setRollingPeriod(rollingPeriod)
          .setReportType(reportType)
          .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
          .build();
      temporaryExposureKeys.add(temporaryExposureKey);
    }
    return temporaryExposureKeys;
  }

}
