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

package app.coronawarn.server.services.federation.upload.payload.helper;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Random;

public class PersistenceKeysGenerator {

  private static final Random RANDOM = new Random();

  public static DiagnosisKey makeDiagnosisKey() {
    byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);
    return DiagnosisKey.builder()
        .withKeyData(bytes)
        .withRollingStartIntervalNumber(144)
        .withTransmissionRiskLevel(0)
        .build();
  }

}
