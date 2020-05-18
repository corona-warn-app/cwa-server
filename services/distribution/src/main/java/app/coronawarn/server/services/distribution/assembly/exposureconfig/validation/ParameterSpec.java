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

package app.coronawarn.server.services.distribution.assembly.exposureconfig.validation;

/**
 * Definition of the spec according to Apple/Google:
 * https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration
 */
public class ParameterSpec {

  private ParameterSpec() {
  }

  /**
   * The minimum weight value for mobile API.
   */
  public static final double WEIGHT_MIN = 0.001;

  /**
   * The maximum weight value for mobile API.
   */
  public static final int WEIGHT_MAX = 100;

  /**
   * Maximum number of allowed decimals.
   */
  public static final int WEIGHT_MAX_DECIMALS = 3;

}
