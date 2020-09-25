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
