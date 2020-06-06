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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import com.google.protobuf.ByteString;
import java.util.Comparator;

/**
 * Sorts the keys based on the TEK key data.
 */
public class TemporaryExposureKeyComparator implements Comparator<TemporaryExposureKey> {

  private static final Comparator<ByteString> byteStringComparator = ByteString.unsignedLexicographicalComparator();

  @Override
  public int compare(TemporaryExposureKey o1, TemporaryExposureKey o2) {
    return byteStringComparator.compare(o1.getKeyData(), o2.getKeyData());
  }
}
