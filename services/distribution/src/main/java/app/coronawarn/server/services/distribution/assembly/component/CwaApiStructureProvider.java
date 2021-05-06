

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Assembles the content underneath the {@code /version} path of the CWA API.
 */
@Component
public class CwaApiStructureProvider {

  private final AppConfigurationStructureProvider appConfigurationStructureProvider;
  private final AppConfigurationV2StructureProvider appConfigurationV2StructureProvider;
  private final StatisticsStructureProvider statisticsStructureProvider;
  private final DiagnosisKeysStructureProvider diagnosisKeysStructureProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final TraceTimeIntervalWarningsStructureProvider traceWarningsStructureProvider;
  private final QrCodePosterTemplateStructureProvider qrCodeTemplateStructureProvider;
  private final DigitalGreenCertificateStructureProvider dgcStructureProvider;

  /**
   * Creates a new CwaApiStructureProvider.
   */
  CwaApiStructureProvider(
      AppConfigurationStructureProvider appConfigurationStructureProvider,
      AppConfigurationV2StructureProvider appConfigurationV2StructureProvider,
      StatisticsStructureProvider statisticsStructureProvider,
      DiagnosisKeysStructureProvider diagnosisKeysStructureProvider,
      TraceTimeIntervalWarningsStructureProvider traceWarningsStructureProvider,
      QrCodePosterTemplateStructureProvider qrCodeTemplateStructureProvider,
      DigitalGreenCertificateStructureProvider dgcStructureProvider,
      DistributionServiceConfig distributionServiceConfig) {
    this.appConfigurationStructureProvider = appConfigurationStructureProvider;
    this.appConfigurationV2StructureProvider = appConfigurationV2StructureProvider;
    this.statisticsStructureProvider = statisticsStructureProvider;
    this.diagnosisKeysStructureProvider = diagnosisKeysStructureProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.traceWarningsStructureProvider = traceWarningsStructureProvider;
    this.qrCodeTemplateStructureProvider = qrCodeTemplateStructureProvider;
    this.dgcStructureProvider = dgcStructureProvider;
  }

  /**
   * Returns the base directory.
   *
   * @return new instance of IndexingDecoratorOnDisk base directory
   */
  public Directory<WritableOnDisk> getDirectory() {
    IndexDirectoryOnDisk<String> versionDirectory = new IndexDirectoryOnDisk<>(
        distributionServiceConfig.getApi().getVersionPath(),
        ignoredValue -> Set.of(distributionServiceConfig.getApi().getVersionV1()),
        Object::toString);

    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.of(appConfigurationStructureProvider.getAppConfiguration()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(appConfigurationStructureProvider.getAppConfigurationV1ForAndroid()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(appConfigurationStructureProvider.getAppConfigurationV1ForIos()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(qrCodeTemplateStructureProvider.getQrCodeTemplateForAndroid()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(qrCodeTemplateStructureProvider.getQrCodeTemplateForIos()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(dgcStructureProvider.getDigitalGreenCertificates()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.of(diagnosisKeysStructureProvider.getDiagnosisKeys()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.of(traceWarningsStructureProvider.getTraceWarningsDirectory()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(statisticsStructureProvider.getStatistics()));

    return new IndexingDecoratorOnDisk<>(versionDirectory, distributionServiceConfig.getOutputFileName());
  }

  /**
   * Returns the base directory.
   *
   * @return new instance of IndexingDecoratorOnDisk base directory
   */
  public Directory<WritableOnDisk> getDirectoryV2() {
    IndexDirectoryOnDisk<String> versionDirectory = new IndexDirectoryOnDisk<>(
        distributionServiceConfig.getApi().getVersionPath(),
        ignoredValue -> Set.of(distributionServiceConfig.getApi().getVersionV2()),
        Object::toString);

    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(appConfigurationV2StructureProvider.getAppConfigurationV2ForAndroid()));
    versionDirectory.addWritableToAll(
        ignoredValue -> Optional.ofNullable(appConfigurationV2StructureProvider.getAppConfigurationV2ForIos()));

    return new IndexingDecoratorOnDisk<>(versionDirectory, distributionServiceConfig.getOutputFileNameV2());
  }
}
