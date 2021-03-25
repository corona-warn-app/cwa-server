package app.coronawarn.server.services.eventregistration.service;

import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.domain.errors.TraceLocationInsertionException;
import app.coronawarn.server.services.eventregistration.repository.TraceLocationRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationService {

  private final TraceLocationRepository traceLocationRepository;

  public TraceLocationService(final TraceLocationRepository traceLocationRepository) {
    this.traceLocationRepository = traceLocationRepository;
  }

  /**
   * Insert a TraceLocation {@link TraceLocation}.
   * <ul>
   * <li>generates a UUID and calculates a SHA-256 Hash</li>
   * </ul>
   *
   * @param traceLocation the trace location to create.
   */
  public void saveTraceLocation(final TraceLocation traceLocation, final String uuidHash) {
    try {
      traceLocationRepository.save(uuidHash, traceLocation.getVersion(),
          LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    } catch (final DuplicateKeyException e) {
      throw new TraceLocationInsertionException("Trying to save Trace Location object has failed.", e);
    }
  }
}
