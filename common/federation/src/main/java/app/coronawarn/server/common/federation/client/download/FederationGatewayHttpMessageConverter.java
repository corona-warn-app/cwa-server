

package app.coronawarn.server.common.federation.client.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Converter for converting federation gateway HTTP responses into {@link DiagnosisKeyBatch} objects.
 */
public class FederationGatewayHttpMessageConverter extends AbstractHttpMessageConverter<DiagnosisKeyBatch> {

  /**
   * The media-type for protobuf {@code application/protobuf}.
   */
  public static final MediaType PROTOBUF = new MediaType("application", "protobuf", StandardCharsets.UTF_8);

  public FederationGatewayHttpMessageConverter() {
    setSupportedMediaTypes(Collections.singletonList(PROTOBUF));
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return DiagnosisKeyBatch.class.isAssignableFrom(clazz);
  }

  @Override
  protected DiagnosisKeyBatch readInternal(Class<? extends DiagnosisKeyBatch> clazz, HttpInputMessage message)
      throws IOException {
    try (InputStream body = message.getBody()) {
      return DiagnosisKeyBatch.parseFrom(body);
    } catch (InvalidProtocolBufferException e) {
      throw new HttpMessageNotReadableException("Failed to parse protocol buffers message", e, message);
    }
  }

  @Override
  protected void writeInternal(DiagnosisKeyBatch message, HttpOutputMessage outputMessage) {
    throw new UnsupportedOperationException();
  }
}
