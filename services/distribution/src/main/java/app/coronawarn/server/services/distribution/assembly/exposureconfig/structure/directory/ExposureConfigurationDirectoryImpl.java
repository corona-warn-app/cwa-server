package app.coronawarn.server.services.distribution.assembly.exposureconfig.structure.directory;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.file.decorator.SigningDecorator;
import java.util.Set;

/**
 * Creates the directory structure {@code /parameters/country/:country} and writes a file called
 * {@code index} containing {@link RiskScoreParameters} wrapped in a {@link
 * app.coronawarn.server.common.protocols.internal.SignedPayload}.
 */
public class ExposureConfigurationDirectoryImpl extends DirectoryImpl {

  private static final String PARAMETERS_DIRECTORY = "parameters";
  private static final String COUNTRY_DIRECTORY = "country";
  private static final String COUNTRY = "DE";
  private static final String INDEX_FILE_NAME = "index";

  /**
   * Constructor.
   *
   * @param exposureConfig The {@link RiskScoreParameters} to sign and write.
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the {@link
   *                       app.coronawarn.server.common.protocols.internal.SignedPayload}.
   */
  public ExposureConfigurationDirectoryImpl(RiskScoreParameters exposureConfig,
      CryptoProvider cryptoProvider) {
    super(PARAMETERS_DIRECTORY);

    IndexDirectoryImpl<String> country =
        new IndexDirectoryImpl<>(COUNTRY_DIRECTORY, __ -> Set.of(COUNTRY), Object::toString);

    country.addFileToAll(__ ->
        new SigningDecorator(new FileImpl(INDEX_FILE_NAME, exposureConfig.toByteArray()),
            cryptoProvider));

    this.addDirectory(new IndexingDecorator<>(country));
  }
}
