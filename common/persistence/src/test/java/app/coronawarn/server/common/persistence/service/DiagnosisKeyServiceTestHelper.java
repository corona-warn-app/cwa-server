/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.common.persistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.List;

public class DiagnosisKeyServiceTestHelper {

  public static void assertDiagnosisKeysEqual(List<DiagnosisKey> expKeys, List<DiagnosisKey> actKeys) {
    assertEquals(expKeys.size(), actKeys.size(), "Cardinality mismatch");

    for (int i = 0; i < expKeys.size(); i++) {
      var expKey = expKeys.get(i);
      var actKey = actKeys.get(i);

      assertEquals(expKey.getKeyData(), actKey.getKeyData(), "keyData mismatch");
      assertEquals(expKey.getRollingStartNumber(), actKey.getRollingStartNumber(),
          "rollingStartNumber mismatch");
      assertEquals(expKey.getRollingPeriod(), actKey.getRollingPeriod(),
          "rollingPeriod mismatch");
      assertEquals(expKey.getTransmissionRiskLevel(), actKey.getTransmissionRiskLevel(),
          "transmissionRiskLevel mismatch");
      assertEquals(expKey.getSubmissionTimestamp(), actKey.getSubmissionTimestamp(),
          "submissionTimestamp mismatch");
    }
  }
}
