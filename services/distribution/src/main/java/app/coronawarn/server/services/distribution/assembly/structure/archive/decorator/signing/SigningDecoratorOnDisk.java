

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public abstract class SigningDecoratorOnDisk extends AbstractSigningDecorator<WritableOnDisk> implements
    SigningDecorator<WritableOnDisk> {

  protected SigningDecoratorOnDisk(Archive<WritableOnDisk> archive, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(archive, cryptoProvider, distributionServiceConfig);
  }

  @Override
  public FileOnDisk getSignatureFile(String signatureFileName) {
    TEKSignatureList signatureList = this.createTemporaryExposureKeySignatureList();
    return new FileOnDisk(signatureFileName, signatureList.toByteArray());
  }
}
