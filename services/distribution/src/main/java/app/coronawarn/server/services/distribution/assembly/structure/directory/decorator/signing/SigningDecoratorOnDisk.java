package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;

public abstract class SigningDecoratorOnDisk extends AbstractSigningDecorator<WritableOnDisk>
    implements SigningDecorator<WritableOnDisk> {

  public SigningDecoratorOnDisk(Directory<WritableOnDisk> directory, CryptoProvider cryptoProvider) {
    super(directory, cryptoProvider);
  }

  @Override
  public FileOnDisk getSignatureFile(String signatureFileName) {
    TEKSignatureList signatureList = this.createTEKSignatureList(this.cryptoProvider);
    return new FileOnDisk(signatureFileName, signatureList.toByteArray());
  }
}
