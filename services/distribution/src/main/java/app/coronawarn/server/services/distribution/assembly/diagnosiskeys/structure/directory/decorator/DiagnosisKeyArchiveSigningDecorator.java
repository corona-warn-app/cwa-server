package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.file.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.decorator.ArchiveSigningDecoratorImpl;

public class DiagnosisKeyArchiveSigningDecorator extends ArchiveSigningDecoratorImpl {

  public DiagnosisKeyArchiveSigningDecorator(Archive archive, CryptoProvider cryptoProvider) {
    super(archive, cryptoProvider);
  }

  @Override
  public byte[] getBytesToSign() {
    // TODO Extract and reuse
    Writable archiveContent = this.getWritables().stream().findFirst().orElseThrow(
        () -> new RuntimeException("Archive must contain exactly one file for signing"));
    File fileToSign = (File) archiveContent;
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
