

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
