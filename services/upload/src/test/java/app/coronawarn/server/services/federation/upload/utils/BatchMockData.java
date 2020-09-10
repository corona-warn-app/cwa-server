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

package app.coronawarn.server.services.federation.upload.utils;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BatchMockData {

  public static DiagnosisKeyBatch makeSingleKeyBatch() {
    return makeBatch(List.of(makeDiagnosisKey()));
  }

  public static DiagnosisKeyBatch makeBatch(List<DiagnosisKey> keys) {
    return DiagnosisKeyBatch.newBuilder()
        .addAllKeys(keys).build();
  }

  public static DiagnosisKey makeDiagnosisKey() {
    byte[] bytes = new byte[16];
    ThreadLocalRandom.current().nextBytes(bytes);
    return DiagnosisKey.newBuilder().setKeyData(ByteString.copyFrom(bytes)).build();
  }

}
