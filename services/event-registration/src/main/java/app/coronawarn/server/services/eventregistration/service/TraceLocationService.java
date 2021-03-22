package app.coronawarn.server.services.eventregistration.service;

import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.repository.TraceLocationRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationService {

  private static final Logger logger = LoggerFactory.getLogger(TraceLocationService.class);
  private TraceLocationRepository traceLocationRepository;

  public TraceLocationService(TraceLocationRepository traceLocationRepository) {
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
  public void saveTraceLocation(TraceLocation traceLocation, final String uuidHash) {
    try {
      traceLocationRepository.save(uuidHash, traceLocation.getVersion(),
          LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    } catch (DuplicateKeyException e) {
      logger.error("Trying to save Trace Location object has failed. The limit has been exceeded!");
    }
  }
}
