

package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import static java.util.function.Predicate.not;

import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class TraceTimeIntervalWarningsHourIndexingDecorator
    extends IndexingDecoratorOnDisk<Integer> {

  private final DistributionServiceConfig distributionServiceConfig;
  private final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;

  /**
   * Creates an instance.
   */
  public TraceTimeIntervalWarningsHourIndexingDecorator(
      TraceTimeWarningsHourDirectory hourDirectory,
      DistributionServiceConfig distributionServiceConfig,
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler) {
    super(hourDirectory, distributionServiceConfig.getOutputFileName());
    this.distributionServiceConfig = distributionServiceConfig;
    this.traceWarningsBundler = traceWarningsBundler;
  }

  @Override
  public Set<Integer> getIndex(ImmutableStack<Object> indices) {
    String currentDateIndex = (String) indices.peek();
    if (Boolean.FALSE.equals(distributionServiceConfig.getIncludeIncompleteHours())
        && TimeUtils.getUtcDate().equals(currentDateIndex)) {
      LocalDateTime currentHour = TimeUtils.getCurrentUtcHour();
      return super.getIndex(indices).stream().filter(not(currentHour::equals))
          .collect(Collectors.toSet());
    } else {
      return super.getIndex(indices);
    }
  }
}
