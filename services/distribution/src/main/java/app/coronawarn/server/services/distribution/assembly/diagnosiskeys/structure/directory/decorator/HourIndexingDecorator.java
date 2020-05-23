package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysHourDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

public class HourIndexingDecorator extends IndexingDecoratorOnDisk<LocalDateTime> {

  private Boolean includeIncompleteHours;

  public HourIndexingDecorator(DiagnosisKeysHourDirectory directory, boolean includeIncompleteHours) {
    super(directory);
    this.includeIncompleteHours = includeIncompleteHours;
  }

  /**
   * Returns the index of the decorated {@link DiagnosisKeysHourDirectory}. If the decorated hour directory represents
   * the current date (today), then by default, the current hour will be excluded from the index. However, if the
   * profile `include-incomplete` is set, the current hour will be included.
   */
  @Override
  public Set<LocalDateTime> getIndex(ImmutableStack<Object> indices) {
    LocalDate currentDateIndex = (LocalDate) indices.peek();
    if (!includeIncompleteHours && LocalDate.now(ZoneOffset.UTC).equals(currentDateIndex)) {
      LocalDateTime currentHour = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
      return super.getIndex(indices).stream()
          .filter(hour -> !hour.equals(currentHour))
          .collect(Collectors.toSet());
    } else {
      return super.getIndex(indices);
    }
  }
}