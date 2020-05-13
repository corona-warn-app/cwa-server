package app.coronawarn.server.services.distribution.exposureconfig.structure;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.file.decorator.SigningDecorator;
import java.util.List;

/**
 * Creates the directory structure {@code /parameters/country/:country} and writes a file called
 * {@code index} containing {@link RiskScoreParameters} wrapped in a {@link
 * app.coronawarn.server.common.protocols.internal.SignedPayload}.
 */
public class ExposureConfigurationDirectoryImpl extends DirectoryImpl {

  /**
   * Constructor.
   *
   * @param region         The region that the {@link RiskScoreParameters} apply to.
   * @param exposureConfig The {@link RiskScoreParameters} to sign and write.
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the {@link
   *                       app.coronawarn.server.common.protocols.internal.SignedPayload}.
   */
  public ExposureConfigurationDirectoryImpl(
      String region, RiskScoreParameters exposureConfig, CryptoProvider cryptoProvider) {
    super("parameters");

    IndexDirectoryImpl<String> country =
        new IndexDirectoryImpl<>("country", __ -> List.of(region), Object::toString);

    country.addFileToAll(__ ->
        new SigningDecorator(new FileImpl("index", exposureConfig.toByteArray()), cryptoProvider));

    this.addDirectory(new IndexingDecorator<>(country));
  }
}
