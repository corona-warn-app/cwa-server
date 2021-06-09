package app.coronawarn.server.services.submission;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubmissionPayloadGenerator {

  private static final int NUMBER_OF_KEYS = 10;
  private static final int TRANSMISSION_RISK_LEVEL = 6;
  private static final int ROLLING_PERIOD = 144; // 24*60/10
  private static final ReportType REPORT_TYPE = ReportType.CONFIRMED_TEST;
  private static final ByteString REQUEST_PADDING = ByteString.copyFrom(new byte[100]);
  private static final List<String> VISITED_COUNTRIES = List.of("DE", "FR");
  private static final String ORIGIN_COUNTRY = "DE";
  private static final boolean CONSENT_TO_FEDERATION = true;
  private static final int DAYS_SINCE_ONSET_OF_SYMPTOMS = 0;
  private static final String MOBILE_CLIENT_PAYLOAD_PB_PATH = "services/submission/src/test/resources/payload/";
  private static final String MOBILE_CLIENT_PAYLOAD_PB_FILENAME = "mobile-client-payload.pb";

  public static void main(String[] args) throws IOException {

    LocalDateTime todayMidnight = LocalDateTime.now().toLocalDate().atStartOfDay();
    LocalDateTime todayMidnightMinusNumberOfKeys = todayMidnight.minusDays(NUMBER_OF_KEYS);

    List<TemporaryExposureKey> temporaryExposureKeys = buildTemporaryExposureKeys(NUMBER_OF_KEYS,
        todayMidnightMinusNumberOfKeys,
        TRANSMISSION_RISK_LEVEL, ROLLING_PERIOD, REPORT_TYPE, DAYS_SINCE_ONSET_OF_SYMPTOMS);
    SubmissionPayload submissionPayload = buildSubmissionPayload(temporaryExposureKeys, REQUEST_PADDING,
        VISITED_COUNTRIES, ORIGIN_COUNTRY, CONSENT_TO_FEDERATION);

    SubmissionPayloadGenerator submissionPayloadGenerator = new SubmissionPayloadGenerator();
    submissionPayloadGenerator.writeSubmissionPayloadProtobufFile(submissionPayload);
  }

  public void writeSubmissionPayloadProtobufFile(SubmissionPayload submissionPayload) throws IOException {
    Files.createDirectories(Paths.get(MOBILE_CLIENT_PAYLOAD_PB_PATH));
    File file = new File(MOBILE_CLIENT_PAYLOAD_PB_PATH + "/" + MOBILE_CLIENT_PAYLOAD_PB_FILENAME);
    file.createNewFile();
    submissionPayload
        .writeTo(new FileOutputStream(MOBILE_CLIENT_PAYLOAD_PB_PATH + "/" + MOBILE_CLIENT_PAYLOAD_PB_FILENAME));
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

  public static List<TemporaryExposureKey> buildTemporaryExposureKeyWithoutMultiplying(int numberOfKeys, LocalDateTime todayMidnight,
      int transmissionRiskLevel, int rollingPeriod, ReportType reportType, int daysSinceOnsetOfSymptoms) {
    List<TemporaryExposureKey> temporaryExposureKeys = new ArrayList<>();

    for (int i = 0; i < numberOfKeys; i++) {
      byte[] keyData = new byte[16];
      Random random = new Random();
      random.nextBytes(keyData);

      TemporaryExposureKey temporaryExposureKey = TemporaryExposureKey.newBuilder()
          .setKeyData(ByteString.copyFrom(keyData))
          .setTransmissionRiskLevel(transmissionRiskLevel)
          .setRollingStartIntervalNumber((int) todayMidnight.toEpochSecond(ZoneOffset.UTC) / 600)
          .setRollingPeriod(rollingPeriod)
          .setReportType(reportType)
          .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
          .build();
      temporaryExposureKeys.add(temporaryExposureKey);
    }
    return temporaryExposureKeys;
  }

}
