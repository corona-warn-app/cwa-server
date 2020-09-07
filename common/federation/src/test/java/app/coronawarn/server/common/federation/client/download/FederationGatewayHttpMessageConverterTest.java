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

package app.coronawarn.server.common.federation.client.download;

import static app.coronawarn.server.common.federation.client.download.FederationGatewayHttpMessageConverter.PROTOBUF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

class FederationGatewayHttpMessageConverterTest {

  private static final String EXP_BATCH_TAG = "507f191e810c19729de860ea";
  private static final String EXP_NEXT_BATCH_TAG = "507f191e810c19729de860ea";
  private static final DiagnosisKeyBatch EXP_DIAGNOSIS_KEY_BATCH = DiagnosisKeyBatch.newBuilder()
      .addKeys(
          DiagnosisKey.newBuilder()
              .setKeyData(ByteString.copyFromUtf8("0123456789ABCDEF"))
              .addVisitedCountries("DE")
              .setRollingStartIntervalNumber(0)
              .setRollingPeriod(144)
              .setTransmissionRiskLevel(2)
              .build()).build();

  private final FederationGatewayHttpMessageConverter converter = new FederationGatewayHttpMessageConverter();

  @Test
  void supportedMediaTypesIsProtobuf() {
    assertThat(converter.getSupportedMediaTypes()).isEqualTo(list(PROTOBUF));
  }

  @Test
  void supportsReturnsTrueForBatchDownloadResponseClass() {
    assertThat(converter.supports(BatchDownloadResponse.class)).isTrue();
  }

  @Test
  void writeInternalThrowsUnsupportedOperationException() {
    BatchDownloadResponse message = mock(BatchDownloadResponse.class);
    HttpOutputMessage outputMessage = mock(HttpOutputMessage.class);
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> converter.writeInternal(message, outputMessage));
  }

  @Test
  void readInternalThrowsHttpMessageNotReadableExceptionIfPayloadInvalid() throws IOException {
    HttpInputMessage message = buildHttpInputMessage("somethingInvalid".getBytes(UTF_8), EXP_BATCH_TAG, "null");
    assertThatExceptionOfType(HttpMessageNotReadableException.class)
        .isThrownBy(() -> converter.readInternal(BatchDownloadResponse.class, message));
  }

  @Test
  void readInternalThrowsHttpMessageNotReadableExceptionIfBatchTagMissing() throws IOException {
    HttpInputMessage message = buildHttpInputMessageWithValidBody(null, "null");
    assertThatExceptionOfType(HttpMessageNotReadableException.class)
        .isThrownBy(() -> converter.readInternal(BatchDownloadResponse.class, message));
  }

  @Test
  void readInternalReturnsResponseWithoutNextBatchTag() throws IOException {
    HttpInputMessage message = buildHttpInputMessageWithValidBody(EXP_BATCH_TAG, "null");
    BatchDownloadResponse actResponse = converter.readInternal(BatchDownloadResponse.class, message);
    assertThat(actResponse)
        .isEqualTo(new BatchDownloadResponse(EXP_DIAGNOSIS_KEY_BATCH, EXP_BATCH_TAG, Optional.empty()));
  }

  @Test
  void readInternalReturnsResponseWithNextBatchTag() throws IOException {
    HttpInputMessage message = buildHttpInputMessageWithValidBody(EXP_BATCH_TAG, EXP_NEXT_BATCH_TAG);
    BatchDownloadResponse actResponse = converter.readInternal(BatchDownloadResponse.class, message);
    assertThat(actResponse)
        .isEqualTo(new BatchDownloadResponse(EXP_DIAGNOSIS_KEY_BATCH, EXP_BATCH_TAG, Optional.of(EXP_NEXT_BATCH_TAG)));
  }

  private static HttpInputMessage buildHttpInputMessageWithValidBody(String batchTag, String nextBatchTag)
      throws IOException {
    return buildHttpInputMessage(EXP_DIAGNOSIS_KEY_BATCH.toByteArray(), batchTag, nextBatchTag);
  }

  private static HttpInputMessage buildHttpInputMessage(byte[] body, String batchTag, String nextBatchTag)
      throws IOException {
    HttpInputMessage message = mock(HttpInputMessage.class);
    when(message.getBody()).thenReturn(new ByteArrayInputStream(body));

    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE, "application/protobuf; version=1.0");
    headers.add("batchTag", batchTag);
    headers.add("nextBatchTag", nextBatchTag);
    when(message.getHeaders()).thenReturn(headers);
    return message;
  }
}
