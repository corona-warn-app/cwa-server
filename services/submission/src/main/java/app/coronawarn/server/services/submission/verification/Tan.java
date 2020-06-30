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

package app.coronawarn.server.services.submission.verification;

import java.util.Objects;
import java.util.UUID;

/**
 * A representation of a tan.
 */
public class Tan {
  private final UUID tan;

  private Tan(UUID tan) {
    this.tan = tan;
  }

  /**
   * Creates a new {@link #Tan} instance for the given tan string.
   *
   * @param tanString A valid UUID string representation.
   * @return The Tan instance
   * @throws IllegalArgumentException when the given tan string is not a valid UUID.
   */
  public static Tan of(String tanString) {
    UUID tan = UUID.fromString(tanString.trim());
    return new Tan(tan);
  }

  /**
   * Returns the tan entity as UUID.
   * @return the tan.
   */
  public UUID getTan() {
    return tan;
  }

  /**
   * Returns the TAN in it's string representation.
   *
   * @return the tan UUID as a string.
   */
  @Override
  public String toString() {
    return tan.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Tan tan1 = (Tan) o;
    return tan.equals(tan1.tan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tan);
  }
}
