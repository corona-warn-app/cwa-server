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
    SubmissionPayloadGenerator submissionPayloadGenerator = new SubmissionPayloadGenerator();
    submissionPayloadGenerator.writeSubmissionPayloadProtobufFile();
  }

  public void writeSubmissionPayloadProtobufFile() throws IOException {
    createValidSubmissionPayload().writeTo(new FileOutputStream("mobile-client-payload.pb"));
  }

  public SubmissionPayload createValidSubmissionPayload() throws IOException {
    LocalDateTime now = LocalDateTime.now();
    now.minusDays(14);

    List<TemporaryExposureKey> temporaryExposureKeys = createTemporaryExposureKeys(10, now, 1,
        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS);

    SubmissionPayload.Builder submissionPayload = SubmissionPayload.newBuilder();
    submissionPayload.addAllKeys(temporaryExposureKeys);
    submissionPayload.setRequestPadding(ByteString.copyFrom(new byte[100]));
    submissionPayload.addAllVisitedCountries(List.of("DE", "FR"));
    submissionPayload.setOrigin("DE");
    submissionPayload.setConsentToFederation(true);

    return submissionPayload.build();
  }

  private List<TemporaryExposureKey> createTemporaryExposureKeys(int numberOfKeys, LocalDateTime startDate,
      int transmissionRiskLevel, ReportType reportType) {
    List<TemporaryExposureKey> temporaryExposureKeys = new ArrayList<>();
    for (int i = 0; i < numberOfKeys; i++) {
      byte[] keyData = new byte[16];
      Random random = new Random();
      random.nextBytes(keyData);

      TemporaryExposureKey.Builder key = TemporaryExposureKey.newBuilder();
      key.setKeyData(ByteString.copyFrom(keyData));
      key.setTransmissionRiskLevel(transmissionRiskLevel);
      key.setRollingStartIntervalNumber((int) startDate.toEpochSecond(ZoneOffset.UTC) + 144 * i);
      key.setRollingPeriod(144);
      key.setReportType(reportType);

      temporaryExposureKeys.add(key.build());
    }
    return temporaryExposureKeys;
  }

}
