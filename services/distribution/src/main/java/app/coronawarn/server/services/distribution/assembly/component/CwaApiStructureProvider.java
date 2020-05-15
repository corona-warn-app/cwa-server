package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Assembles the content underneath the {@code /version} path of the CWA API.
 */
@Component
public class CwaApiStructureProvider {

  private final String VERSION_DIRECTORY = "version";
  private final String VERSION_V1 = "v1";

  @Autowired
  private ExposureConfigurationStructureProvider exposureConfigurationStructureProvider;

  @Autowired
  private DiagnosisKeysStructureProvider diagnosisKeysStructureProvider;

  public Directory getDirectory() {
    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>(VERSION_DIRECTORY, __ -> Set.of(VERSION_V1), Object::toString);

    versionDirectory.addDirectoryToAll(__ -> exposureConfigurationStructureProvider.getExposureConfiguration());
    versionDirectory.addDirectoryToAll(__ -> diagnosisKeysStructureProvider.getDiagnosisKeys());

    return new IndexingDecorator<>(versionDirectory);
  }
}
