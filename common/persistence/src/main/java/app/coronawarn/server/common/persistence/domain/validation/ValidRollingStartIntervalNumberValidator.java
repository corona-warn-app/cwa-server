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

package app.coronawarn.server.common.persistence.domain.validation;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidRollingStartIntervalNumberValidator
    implements ConstraintValidator<ValidRollingStartIntervalNumber, Integer> {

  @Override
  public boolean isValid(Integer rollingStartIntervalNumber, ConstraintValidatorContext constraintValidatorContext) {
    int currentInstant = Math.toIntExact(LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC) / 600L);
    return rollingStartIntervalNumber > 0 && rollingStartIntervalNumber < currentInstant;
  }
}
