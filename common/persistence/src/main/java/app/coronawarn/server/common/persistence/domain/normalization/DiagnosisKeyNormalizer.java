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

package app.coronawarn.server.common.persistence.domain.normalization;

/**
 *  Responsible for sanitizing the information within a diagnosis key to fit the national specification.
 *  This can mean, for example, that either this service will initialize missing values that were not provided
 *  during submission and other injection channels, or it can mean restructuring the values that were
 *  provided, which might be valid, but not meeting national standards, as described by the system configuration
 *  (i.e. number scales, ranges etc).
 *
 *  <p>There is a slight overlap with validation topics from a class responsability point of view, but the
 *  the focus of this service is to directly modify information in given keys, rather than stoping a process
 *  when the key does not meet input validty requirements.
 *
 *  <p>There is no default normalization instance provided in the domain module. All other modules which
 *  depend on 'persistence' must provide their own implementation since this operation can be contextual.
 */
@FunctionalInterface
public interface DiagnosisKeyNormalizer {

  /**
   * Given a container of fields from the {@link DiagnosisKey} with their respective values,
   * return a new container with the normalized values.
   */
  NormalizableFields normalize(NormalizableFields fieldsAndValues);
}
