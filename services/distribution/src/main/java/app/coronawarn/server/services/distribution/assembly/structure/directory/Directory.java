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

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import java.util.Set;

/**
 * A {@link Writable} that can contains other {@link Writable Writables}.
 *
 * @param <W> The specific type of {@link Writable} that this {@link Directory} can be a child of.
 */
public interface Directory<W extends Writable<W>> extends Writable<W> {

  /**
   * Adds a {@link Writable} to this {@link Directory}.
   */
  void addWritable(Writable<W> writable);

  /**
   * Returns all {@link Writable writables} contained in this {@link Directory}.
   */
  Set<Writable<W>> getWritables();
}
