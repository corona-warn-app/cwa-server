/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */
package app.coronawarn.server.services.submission;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;

import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubmissionPayloadGenerator {

  public static void main(String[] args) throws IOException {
    int numberOfKeys = 10;
    int transmissionRiskLevel = 6;
    int rollingPeriod = 144; // 24*60/10
    ReportType reportType = ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
    ByteString requestPadding = ByteString.copyFrom(new byte[100]);
    final List<String> visitedCountries = List.of("DE", "FR");
    String originCountry = "DE";
    boolean consentToFederation = true;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayMidnight = LocalDateTime
        .of(now.getYear(), now.getMonth(), now.getDayOfMonth() - numberOfKeys, 0, 0);

    List<TemporaryExposureKey> temporaryExposureKeys = buildTemporaryExposureKeys(numberOfKeys, todayMidnight,
        transmissionRiskLevel, rollingPeriod,
        reportType);
    SubmissionPayload submissionPayload = buildSubmissionPayload(temporaryExposureKeys, requestPadding,
        visitedCountries, originCountry, consentToFederation);

    SubmissionPayloadGenerator submissionPayloadGenerator = new SubmissionPayloadGenerator();
    submissionPayloadGenerator.writeSubmissionPayloadProtobufFile(submissionPayload);
  }

  public void writeSubmissionPayloadProtobufFile(SubmissionPayload submissionPayload) throws IOException {
    submissionPayload
        .writeTo(new FileOutputStream("services/submission/src/test/resources/payload/mobile-client-payload.pb"));
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
      int transmissionRiskLevel, int rollingPeriod, ReportType reportType) {
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
          .build();
      temporaryExposureKeys.add(temporaryExposureKey);
    }
    return temporaryExposureKeys;
  }

}
