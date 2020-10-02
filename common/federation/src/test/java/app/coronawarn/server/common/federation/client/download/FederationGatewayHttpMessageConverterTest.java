

package app.coronawarn.server.common.federation.client.download;

import static app.coronawarn.server.common.federation.client.download.FederationGatewayHttpMessageConverter.PROTOBUF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

class FederationGatewayHttpMessageConverterTest {

  private static final String EXP_BATCH_TAG = "507f191e810c19729de860ea";

  private final FederationGatewayHttpMessageConverter converter = new FederationGatewayHttpMessageConverter();

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

  @Test
  void supportedMediaTypesIsProtobuf() {
    assertThat(converter.getSupportedMediaTypes()).isEqualTo(list(PROTOBUF));
  }

  @Test
  void supportsReturnsTrueForBatchDownloadResponseClass() {
    assertThat(converter.supports(DiagnosisKeyBatch.class)).isTrue();
  }

  @Test
  void writeInternalThrowsUnsupportedOperationException() {
    DiagnosisKeyBatch message = DiagnosisKeyBatch.newBuilder().build();
    HttpOutputMessage outputMessage = mock(HttpOutputMessage.class);
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> converter.writeInternal(message, outputMessage));
  }

  @Test
  void readInternalThrowsHttpMessageNotReadableExceptionIfPayloadInvalid() throws IOException {
    HttpInputMessage message = buildHttpInputMessage("somethingInvalid".getBytes(UTF_8), EXP_BATCH_TAG, "null");
    assertThatExceptionOfType(HttpMessageNotReadableException.class)
        .isThrownBy(() -> converter.readInternal(DiagnosisKeyBatch.class, message));
  }
}
