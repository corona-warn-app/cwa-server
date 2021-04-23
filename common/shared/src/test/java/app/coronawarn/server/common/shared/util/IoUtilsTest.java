package app.coronawarn.server.common.shared.util;

import app.coronawarn.server.common.shared.util.IoUtils;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class IoUtilsTest {

  @Test
  void doesNotWriteIfMaximumFileSize() {
    File file = mock(File.class);
    assertThatExceptionOfType(UncheckedIOException.class)
        .isThrownBy(() -> IoUtils.writeBytesToFile(new byte[IoUtils.MAXIMUM_FILE_SIZE + 1], file));
    verify(file, never()).getPath();
  }

}
