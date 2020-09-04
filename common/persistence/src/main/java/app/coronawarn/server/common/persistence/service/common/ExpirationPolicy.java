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

package app.coronawarn.server.common.persistence.service.common;

import java.time.temporal.ChronoUnit;

/**
 * Refers to the time that needs to pass after a key's rolling period has passed,
 * such that the key can be considered completely expired. This is a DPP policy enforced
 * upon processes which involve publishing/distributing/sharing keys with other 3rd party
 * systems.
 */
public final class ExpirationPolicy {

  private long expirationTime;
  private ChronoUnit timeUnit;

  private ExpirationPolicy() {
  }

  public long getExpirationTime() {
    return expirationTime;
  }

  public ChronoUnit getTimeUnit() {
    return timeUnit;
  }

  /**
   * Get an instance of an expiration policy.
   */
  public static ExpirationPolicy of(long timeValue, ChronoUnit timeUnit) {
    ExpirationPolicy policy = new ExpirationPolicy();
    policy.expirationTime = timeValue;
    policy.timeUnit = timeUnit;
    return policy;
  }
}
