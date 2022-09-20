package app.coronawarn.server.services.distribution.statistics.directory;

import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.springframework.context.annotation.Profile;

@Profile("!revocation")
public class StatisticsDirectory {

  private final Statistics statistics;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an {@link StatisticsDirectory} for the mobile structure that contains a file which will be used by mobile
   * devices.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public StatisticsDirectory(
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      Statistics statistics) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.statistics = statistics;
  }

  /**
   * If the processing of the uploaded JSON file by TSI succeeds, it is written into a file, put into an archive with
   * the specified name and returned to be included in the CWA file structure.
   */
  public Writable<WritableOnDisk> getStatisticsArchive() {
    ArchiveOnDisk statisticsFile = new ArchiveOnDisk(distributionServiceConfig.getApi().getStatisticsFileName());
    statisticsFile
        .addWritable(new FileOnDisk("export.bin", statistics.toByteArray()));
    return new DistributionArchiveSigningDecorator(statisticsFile, cryptoProvider,
        distributionServiceConfig);
  }
}
