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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Collections;
import java.util.Set;

public final class NilDirectory implements Directory<WritableOnDisk> {

  static final NilDirectory self = new NilDirectory();

  @Override
  public void addWritable(Writable<WritableOnDisk> writable) {
    //do nothing
  }

  @Override
  public Set<Writable<WritableOnDisk>> getWritables() {
    return Collections.emptySet();
  }

  @Override
  public void write() {
    //do nothing
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Directory<WritableOnDisk> getParent() {
    return self;
  }

  @Override
  public void setParent(Directory<WritableOnDisk> parent) {
    //do nothing
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    //do nothing
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isArchive() {
    return false;
  }
}
