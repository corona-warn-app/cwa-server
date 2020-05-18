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

package app.coronawarn.server.services.submission.verification.tan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.submission.verification.AuthorizationType;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TanAuthorizationTest {

  @ParameterizedTest
  @MethodSource("createSuccessTestsTan")
  public void tanOk(TanTestCase testCase) throws TanAuthorizationException {
    var tanAuthorization = TanAuthorization.of(testCase.getAuthValue());

    assertEquals(testCase.getExpected(), tanAuthorization);
  }

  @ParameterizedTest
  @MethodSource("createSuccessTestsTeleTan")
  public void teleTanOk(TanTestCase testCase) throws TanAuthorizationException {
    var tanAuthorization = TanAuthorization.of(testCase.getAuthValue());

    assertEquals(testCase.getExpected(), tanAuthorization);
  }

  @ParameterizedTest
  @MethodSource("createFailedTests")
  public void fails(String authorizationValue) {
    assertThrows(TanAuthorizationException.class, () -> {
      TanAuthorization.of(authorizationValue);
    });
  }

  private static Stream<Arguments> createSuccessTestsTan() {
    return Stream.of(
        TanTestCase.with("TAN 123456").expect(AuthorizationType.TAN, "123456"),
        TanTestCase.with("TAN ABCD23X").expect(AuthorizationType.TAN, "ABCD23X"),
        TanTestCase.with(" TAN ZZZZZZZ").expect(AuthorizationType.TAN, "ZZZZZZZ"),
        TanTestCase.with(" TAN ZZZZZZZ ").expect(AuthorizationType.TAN, "ZZZZZZZ"),
        TanTestCase.with("    TAN ZZZZZZZ   ").expect(AuthorizationType.TAN, "ZZZZZZZ")
    ).map(Arguments::of);
  }

  private static Stream<Arguments> createSuccessTestsTeleTan() {
    return Stream.of(
        TanTestCase.with("TELETAN THE RED FOX").expect(AuthorizationType.TELETAN, "THE RED FOX"),
        TanTestCase.with("TELETAN JUMPS OVER FENCES").expect(AuthorizationType.TELETAN, "JUMPS OVER FENCES"),
        TanTestCase.with("TELETAN VERYLONGSINGLEWORD").expect(AuthorizationType.TELETAN, "VERYLONGSINGLEWORD")
    ).map(Arguments::of);
  }

  private static Stream<Arguments> createFailedTests() {
    return Stream.of(
        "",
        " ",
        null,
        "LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONG",
        "CWA-Authorization: TAN 1223141",
        "Bearer 3123fe",
        "TAN 231Œ321",
        "TAN 12345",
        "tan 123456",
        "TAN TDXX231_",
        "TAN TDXX231_",
        "TAN LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONG"

    ).map(Arguments::of);
  }
}
