/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

/**
 * Thrown when the application tried to parse the CWA-Authorization header, but the header value did not comply with the
 * specification.
 * <br>
 * A valid header format is: CWA-Authorization: (AuthorizationType) (Key)
 * <ul>
 * <li>AuthorizationType: A value of {@link AuthorizationType}</li>
 * <li>Key: Characters/numbers and spaces, min 6 and max 30</li>
 * </ul>
 */
public class IllegalTanAuthorizationFormatException extends TanAuthorizationException {

  /**
   * Creates a new IllegalTanAuthorizationFormatException with the given TAN.
   *
   * @param tan the TAN, which failed the syntax check
   */
  public IllegalTanAuthorizationFormatException(String tan) {
    super("Not a valid format: " + tan);
  }

}
