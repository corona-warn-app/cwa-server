package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.signing.SigningDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;

public class DiagnosisKeySigningDecorator extends SigningDecoratorOnDisk {

  public DiagnosisKeySigningDecorator(Directory<WritableOnDisk> directory, CryptoProvider cryptoProvider) {
    super(directory, cryptoProvider);
  }

  @Override
  public byte[] getBytesToSign() {
    // TODO Extract and reuse
    Writable<?> archiveContent = this.getWritables().stream().findFirst().orElseThrow(
        () -> new RuntimeException("Archive must contain exactly one file for signing"));
    File<?> fileToSign = (FileOnDisk) archiveContent;
    return fileToSign.getBytes();
  }

  @Override
  public int getBatchNum() {
    // TODO
    return 1;
  }

  @Override
  public int getBatchSize() {
    // TODO
    return 1;
  }
}
