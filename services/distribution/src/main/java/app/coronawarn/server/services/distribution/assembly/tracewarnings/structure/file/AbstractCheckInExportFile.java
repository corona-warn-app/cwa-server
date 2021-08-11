package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;

public abstract class AbstractCheckInExportFile extends FileOnDiskWithChecksum {

  protected final String region;
  protected final int intervalNumber;

  protected abstract byte[] createTraceWarningExportBytes();

  /**
   * Constructor for abstract check in export file.
   *
   * @param region         the corresponding region that this file will be put in.
   * @param intervalNumber the interval number.
   */
  protected AbstractCheckInExportFile(String region, int intervalNumber,
      String fileName) {
    super(fileName, new byte[0]);
    this.region = region;
    this.intervalNumber = intervalNumber;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createTraceWarningExportBytes());
    super.prepare(indices);
  }
}
