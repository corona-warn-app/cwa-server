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

import app.coronawarn.server.services.submission.verification.AuthorizationType;

public class TanTestCase {

  private String authValue;

  private TanAuthorization expected;

  private TanTestCase(String authValue) {
    this.authValue = authValue;
  }

  public static TanTestCase with(String authValue) {
    return new TanTestCase(authValue);
  }

  public TanTestCase expect(AuthorizationType type, String key) {
    this.expected = new TanAuthorization(type, key);
    return this;
  }

  public TanAuthorization getExpected() {
    return expected;
  }

  public String getAuthValue() {
    return authValue;
  }
}
