

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.ArchiveDecorator;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.Signature;

public abstract class AbstractSigningDecorator<W extends Writable<W>> extends ArchiveDecorator<W>
    implements SigningDecorator<W> {

  protected final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an AbstractSigningDecorator.
   *
   * @param archive needed to call the parent constructor of the archive
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   * @param distributionServiceConfig used to get origin country
   */
  protected AbstractSigningDecorator(Archive<W> archive, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(archive);
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    this.addWritable(this.getSignatureFile(distributionServiceConfig.getSignature().getFileName()));
  }

  protected TEKSignatureList createTemporaryExposureKeySignatureList() {
    return TEKSignatureList.newBuilder()
        .addSignatures(TEKSignature.newBuilder()
            .setSignatureInfo(distributionServiceConfig.getSignature().getSignatureInfo())
            .setBatchNum(getBatchNum())
            .setBatchSize(getBatchSize())
            .setSignature(ByteString.copyFrom(createSignature(cryptoProvider)))
            .build())
        .build();
  }

  private byte[] createSignature(CryptoProvider cryptoProvider) {
    try {
      Signature payloadSignature = Signature.getInstance(distributionServiceConfig.getSignature().getAlgorithmName(),
          distributionServiceConfig.getSignature().getSecurityProvider());
      payloadSignature.initSign(cryptoProvider.getPrivateKey());
      payloadSignature.update(this.getBytesToSign());
      return payloadSignature.sign();
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Failed to sign archive.", e);
    }
  }
}
