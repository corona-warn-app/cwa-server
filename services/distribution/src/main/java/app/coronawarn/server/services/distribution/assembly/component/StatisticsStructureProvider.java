package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.directory.StatisticsDirectory;
import org.springframework.stereotype.Component;


/**
 * Reads configuration parameters from the respective files in the class path and build a {@link
 * StatisticsStructureProvider} with them.
 */
@Component
public class StatisticsStructureProvider {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final Statistics statistics;

  /**
   * Creates a new StatisticsStructureProvider.
   */
  public StatisticsStructureProvider(
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      Statistics statistics) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.statistics = statistics;
  }

  /**
   * Returns a list containing the archives with Statistics for
   * mobile clients using signature file.
   */
  public Writable<WritableOnDisk> getStatistics() {
    if (statistics.getKeyFigureCardsCount() == 0) {
      return null;
    } else {
      return new StatisticsDirectory(cryptoProvider, distributionServiceConfig, statistics).getStatisticsArchive();
    }
  }
}
