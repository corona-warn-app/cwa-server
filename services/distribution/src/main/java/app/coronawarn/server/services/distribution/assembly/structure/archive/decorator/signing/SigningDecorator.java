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

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;

/**
 * A decorator that can sign an array of bytes and write the signature into a file.
 */
public interface SigningDecorator<W extends Writable<W>> extends Archive<W> {

  /**
   * Returns the file containing the signature.
   */
  File<W> getSignatureFile(String signatureFileName);

  /**
   * Returns the bytes that shall be signed.
   */
  byte[] getBytesToSign();

  /**
   * Returns the index number of the current batch.
   */
  int getBatchNum();

  /**
   * Returns the total size of the batch.
   */
  int getBatchSize();
}
