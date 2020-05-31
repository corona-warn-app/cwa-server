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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import static java.util.function.Predicate.not;

import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDateDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

public class DateIndexingDecorator extends IndexingDecoratorOnDisk<LocalDate> {

  private final DistributionServiceConfig distributionServiceConfig;

  public DateIndexingDecorator(DiagnosisKeysDateDirectory directory,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Returns the index of the decorated {@link DiagnosisKeysDateDirectory}. By default, the current date (today) will be
   * excluded from the index. However, if the profile `demo` is set, the current date will be included.
   */
  @Override
  public Set<LocalDate> getIndex(ImmutableStack<Object> indices) {
    if (Boolean.FALSE.equals(distributionServiceConfig.getIncludeIncompleteDays())) {
      LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
      return super.getIndex(indices).stream()
          .filter(not(currentDate::equals))
          .collect(Collectors.toSet());
    } else {
      return super.getIndex(indices);
    }
  }
}
