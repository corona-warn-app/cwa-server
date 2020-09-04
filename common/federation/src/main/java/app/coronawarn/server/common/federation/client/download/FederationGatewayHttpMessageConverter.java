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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Converter for converting federation gateway HTTP responses into {@link BatchDownloadResponse} objects.
 */
public class FederationGatewayHttpMessageConverter extends AbstractHttpMessageConverter<BatchDownloadResponse> {

  /**
   * The media-type for protobuf {@code application/protobuf}.
   */
  public static final MediaType PROTOBUF = new MediaType("application", "protobuf", StandardCharsets.UTF_8);
  public static final String HEADER_BATCH_TAG = "batchTag";
  public static final String HEADER_NEXT_BATCH_TAG = "nextBatchTag";
  public static final String EMPTY_HEADER = "null";

  public FederationGatewayHttpMessageConverter() {
    setSupportedMediaTypes(Collections.singletonList(PROTOBUF));
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return BatchDownloadResponse.class.isAssignableFrom(clazz);
  }

  @Override
  protected BatchDownloadResponse readInternal(Class<? extends BatchDownloadResponse> clazz, HttpInputMessage message)
      throws IOException, HttpMessageNotReadableException {
    String batchTag = getHeader(message, HEADER_BATCH_TAG).orElseThrow();
    Optional<String> nextBatchTag = getHeader(message, HEADER_NEXT_BATCH_TAG);

    try (InputStream body = message.getBody()) {
      DiagnosisKeyBatch diagnosisKeyBatch = DiagnosisKeyBatch.parseFrom(body);
      return new BatchDownloadResponse(diagnosisKeyBatch, batchTag, nextBatchTag);
    }
  }

  private Optional<String> getHeader(HttpInputMessage response, String header) {
    Collection<String> headerStrings = response.getHeaders().get(header);
    String headerString = headerStrings.iterator().next();
    return (!EMPTY_HEADER.equals(headerString))
        ? Optional.of(headerString)
        : Optional.empty();
  }

  @Override
  protected void writeInternal(BatchDownloadResponse message, HttpOutputMessage outputMessage)
      throws HttpMessageNotWritableException {
    throw new UnsupportedOperationException();
  }
}
