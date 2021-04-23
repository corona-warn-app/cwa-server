

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import static app.coronawarn.server.common.shared.util.TimeUtils.getCurrentUtcHour;
import static app.coronawarn.server.common.shared.util.TimeUtils.getUtcDate;
import static java.util.function.Predicate.not;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysHourDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class HourIndexingDecorator extends IndexingDecoratorOnDisk<LocalDateTime> {

  private final DistributionServiceConfig distributionServiceConfig;

  public HourIndexingDecorator(DiagnosisKeysHourDirectory directory,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Returns the index of the decorated {@link DiagnosisKeysHourDirectory}. If the decorated hour directory represents
   * the current date (today), then by default, the current hour will be excluded from the index. However, if the
   * profile `demo` is set, the current hour will be included.
   */
  @Override
  public Set<LocalDateTime> getIndex(ImmutableStack<Object> indices) {
    LocalDate currentDateIndex = (LocalDate) indices.peek();
    if (Boolean.FALSE.equals(distributionServiceConfig.getIncludeIncompleteHours())
        && getUtcDate().equals(currentDateIndex)) {
      LocalDateTime currentHour = getCurrentUtcHour();
      return super.getIndex(indices).stream()
          .filter(not(currentHour::equals))
          .collect(Collectors.toSet());
    } else {
      return super.getIndex(indices);
    }
  }
}
