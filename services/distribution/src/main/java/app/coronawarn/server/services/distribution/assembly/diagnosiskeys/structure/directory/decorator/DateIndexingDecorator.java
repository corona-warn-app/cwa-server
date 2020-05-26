package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import static java.util.function.Predicate.not;

import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDateDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

public class DateIndexingDecorator extends IndexingDecoratorOnDisk<LocalDate> {

  private final DistributionServiceConfig distributionServiceConfig;

  public DateIndexingDecorator(DiagnosisKeysDateDirectory directory,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Returns the index of the decorated {@link DiagnosisKeysDateDirectory}. By default, the current date (today) will be
   * excluded from the index. However, if the profile `demo` is set, the current date will be included.
   */
  @Override
  public Set<LocalDate> getIndex(ImmutableStack<Object> indices) {
    if (Boolean.FALSE.equals(distributionServiceConfig.getIncludeIncompleteDays())) {
      LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
      return super.getIndex(indices).stream()
          .filter(not(currentDate::equals))
          .collect(Collectors.toSet());
    } else {
      return super.getIndex(indices);
    }
  }
}
