package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.IoUtils.fileExistsInDirectory;
import static app.coronawarn.server.common.shared.util.IoUtils.makeNewFile;
import static app.coronawarn.server.common.shared.util.IoUtils.writeBytesToFile;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class IoUtilsTest {

  public static final String TESTFILE_TXT = "testfile.txt";

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @Test
  void doesNotWriteIfMaximumFileSize() {
    File file = mock(File.class);
    assertThatExceptionOfType(UncheckedIOException.class)
        .isThrownBy(() -> writeBytesToFile(new byte[IoUtils.MAXIMUM_FILE_SIZE + 1], file));
    verify(file, never()).getPath();
  }

  @Test
  void doesWriteWhenDoesNotExceedMaximumFileSize() throws IOException {
    outputFolder.create();
    File file = outputFolder.newFile();
    writeBytesToFile(new byte[IoUtils.MAXIMUM_FILE_SIZE - 1], file);

    assertTrue(file.getTotalSpace() != 0);
  }

  @Test
  void doesLog75PercentMaximumFileSize() throws IOException {
    outputFolder.create();
    File file = outputFolder.newFile();
    writeBytesToFile(new byte[(int)(IoUtils.MAXIMUM_FILE_SIZE * 0.8)], file);

    assertTrue(file.getTotalSpace() != 0);
  }

  @Test
  void doesCreateFile() throws IOException {
    outputFolder.create();
    File parentFile = outputFolder.newFolder();

    assertFalse(fileExistsInDirectory(parentFile, TESTFILE_TXT));

    makeNewFile(parentFile, TESTFILE_TXT);
    assertTrue(fileExistsInDirectory(parentFile, TESTFILE_TXT));
  }
}
