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

package app.coronawarn.server.services.distribution.assembly.io;


import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

class IOTest {

  @Test
  void doesNotWriteIfMaximumFileSize() {
    File file = mock(File.class);
    assertThatExceptionOfType(UncheckedIOException.class)
        .isThrownBy(() -> IO.writeBytesToFile(new byte[IO.MAXIMUM_FILE_SIZE + 1], file));
    verify(file, never()).getPath();
  }
}
