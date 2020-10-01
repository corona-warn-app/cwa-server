

package app.coronawarn.server.common.persistence.domain;

import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * Information about federation batches with their status.
 */
public class FederationBatchInfo {

  @Id
  private final String batchTag;
  private final LocalDate date;
  private FederationBatchStatus status;

  /**
   * Creates a FederationBatchInfo and sets its status to {@link FederationBatchStatus#UNPROCESSED}.
   */
  public FederationBatchInfo(String batchTag, LocalDate date) {
    this(batchTag, date, FederationBatchStatus.UNPROCESSED);
  }

  /**
   * Creates a FederationBatchInfo.
   */
  @PersistenceConstructor
  public FederationBatchInfo(String batchTag, LocalDate date, FederationBatchStatus status) {
    this.batchTag = batchTag;
    this.date = date;
    this.status = status;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public LocalDate getDate() {
    return date;
  }

  public FederationBatchStatus getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederationBatchInfo that = (FederationBatchInfo) o;
    return Objects.equals(batchTag, that.batchTag)
        && Objects.equals(date, that.date)
        && status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(batchTag, date, status);
  }
}
